package com.ozmar.notes;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.ozmar.notes.async.BasicDBAsync;
import com.ozmar.notes.databinding.ActivityNoteEditorBinding;
import com.ozmar.notes.notifications.ReminderManager;
import com.ozmar.notes.reminderDialog.ReminderDialogFragment;
import com.ozmar.notes.utils.FormatUtils;

import org.joda.time.DateTime;

import static com.ozmar.notes.MainActivity.db;


public class NoteEditorActivity extends AppCompatActivity
        implements ReminderDialogFragment.OnReminderPickedListener, NoteEditorView {

    private static final int USER_NOTES = 0;
    private static final int FAVORITE_NOTES = 1;
    private static final int ARCHIVE_NOTES = 2;
    private static final int RECYCLE_BIN_NOTES = 3;

    private int notePosition;
    private String[] noteResult;

    int menuActionClickedId = -1;
    private MenuItem favoriteIcon;

    private ActivityNoteEditorBinding mBinding;
    private NoteEditorPresenter noteEditorPresenter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);

        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_note_editor);
        setupToolbar();

        noteResult = getResources().getStringArray(R.array.noteResultArray);
        noteEditorPresenter = new NoteEditorPresenter(NoteEditorActivity.this);

        Intent intent = getIntent();
        int noteId = intent.getIntExtra("Note ID", -1);
        int listUsed = intent.getIntExtra(getString(R.string.listUsedIntent), USER_NOTES);
        notePosition = intent.getIntExtra(getString(R.string.notePositionIntent), -1);
        noteEditorPresenter.initialize(db, noteId, listUsed);
    }

    private void setupToolbar() {
        setSupportActionBar((Toolbar) mBinding.myToolbar);
        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.note_editor_menu, menu);
        favoriteIcon = menu.findItem(R.id.favorite_note);
        setupToolbarIcons(noteEditorPresenter.getNote(), menu, noteEditorPresenter.getListUsed());
        return super.onCreateOptionsMenu(menu);
    }

    private void setupToolbarIcons(@Nullable SingleNote note, @NonNull Menu menu, int listUsed) {
        if (listUsed != RECYCLE_BIN_NOTES) {
            if (listUsed == ARCHIVE_NOTES) {
                menu.findItem(R.id.archive_note).setVisible(false);
                menu.findItem(R.id.unarchive_note).setVisible(true);
            }

            if (note != null && note.is_favorite()) {
                menu.findItem(R.id.favorite_note).setIcon(R.drawable.ic_favorite_star_on);
            }

        } else {
            menu.findItem(R.id.restore_note).setVisible(true);
            menu.findItem(R.id.delete_note_forever).setVisible(true);
            menu.findItem(R.id.delete_note).setVisible(false);
            menu.findItem(R.id.archive_note).setVisible(false);
            menu.findItem(R.id.favorite_note).setVisible(false);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;

            case R.id.favorite_note:
                noteEditorPresenter.onFavoriteClicked();
                return true;

            case R.id.reminder:
                addReminder(null);
                return true;

            case R.id.delete_note_forever:
                deleteNoteForever(noteEditorPresenter.getNote(), noteEditorPresenter.getListUsed());
                return true;

            case R.id.delete_note:
            case R.id.archive_note:
            case R.id.unarchive_note:
            case R.id.restore_note:
                menuActionClickedId = item.getItemId();
                onSaveNote();
                return true;
        }

        return false;
    }

    @SuppressWarnings("WeakerAccess")
    public void addReminder(View view) {
        long time = noteEditorPresenter.getReminderTime();
        FrequencyChoices choices = noteEditorPresenter.getFrequencyChoices();

        ReminderDialogFragment dialogFragment = ReminderDialogFragment.newInstance(choices, time);
        dialogFragment.show(getSupportFragmentManager(), "reminder_dialog_layout");
    }

    @Override
    public void onBackPressed() {
        onSaveNote();
        super.onBackPressed();
    }

    private void onSaveNote() {
        String title = mBinding.editTextTitle.getText().toString();
        String content = mBinding.editTextContent.getText().toString();
        noteEditorPresenter.onSaveNote(title, content, db);
    }

    // TODO: Update to not use context to be able to call in presenter(FormatUtils)
    @Override
    public void onReminderPicked(@NonNull DateTime dateTime, @Nullable FrequencyChoices choices) {
        String newReminderText = FormatUtils.getReminderText(getApplication(), dateTime);
        noteEditorPresenter.onReminderPicked(choices, dateTime.getMillis(), newReminderText);
    }

    @Override
    public void onReminderDelete() {
        noteEditorPresenter.onReminderDeleted();
    }

    // TODO: Make on click call presenter to delete note with Model
    private void deleteNoteForever(@NonNull SingleNote note, int listUsed) {
        new AlertDialog.Builder(NoteEditorActivity.this)
                .setMessage(getString(R.string.messageDialog))
                .setPositiveButton(getString(R.string.deleteDialog), (dialogInterface, i) -> {
                    new BasicDBAsync(db, null, note, listUsed, 3).execute();
                    goBackToMainActivity(note, 2);
                    finish();
                })
                .setNegativeButton(getString(R.string.cancelDialog), null)
                .show();
    }

    @Override
    public void requestFocusOnContent() {
        mBinding.editTextContent.requestFocus();
    }

    @Override
    public void setupNoteEditTexts(@NonNull SingleNote note) {
        mBinding.lastModified.setText(FormatUtils.lastUpdated(NoteEditorActivity.this,
                note.get_timeModified()));

        mBinding.editTextTitle.setText(note.get_title());
        mBinding.editTextTitle.setFocusable(false);
        mBinding.editTextContent.setText(note.get_content());
        mBinding.editTextContent.setFocusable(false);

        // Keyboard does not pop up until the user clicks on the screen
        // allowing the user to see the entire note at the StartForNextRepeat
        View.OnTouchListener editTextListener = (view, motionEvent) -> {
            if (noteEditorPresenter.getListUsed() == RECYCLE_BIN_NOTES) {
                Toast.makeText(getApplicationContext(), "Can't edit in Trash", Toast.LENGTH_SHORT).show();
            } else {
                view.performClick();
                view.setFocusableInTouchMode(true);
                view.requestFocus();
            }
            return false;
        };

        mBinding.editTextTitle.setOnTouchListener(editTextListener);
        mBinding.editTextContent.setOnTouchListener(editTextListener);
    }

    @Override
    public void showReminder(@NonNull SingleNote note, long reminderTime) {
        if (note.hasFrequencyChoices()) {
            mBinding.reminderText.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_repeat_dark_gray_small,
                    0, 0, 0);
        } else {
            mBinding.reminderText.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_reminder_dark_gray_small,
                    0, 0, 0);
        }

        mBinding.reminderText.setVisibility(View.VISIBLE);
        mBinding.reminderText.setText(FormatUtils.getReminderText(NoteEditorActivity.this,
                new DateTime(reminderTime)));
    }

    @Override
    public void updateFavoriteIcon(boolean favorite) {
        if (favorite) {
            favoriteIcon.setIcon(R.drawable.ic_favorite_star_on);
        } else {
            favoriteIcon.setIcon(R.drawable.ic_favorite_star_off);
        }
    }

    @Override
    public void updateReminderDisplay(@NonNull String newReminderText,
                                      @Nullable FrequencyChoices choices) {
        if (choices == null) {
            mBinding.reminderText.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_reminder_dark_gray_small, 0, 0, 0);
        } else {
            mBinding.reminderText.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_repeat_dark_gray_small, 0, 0, 0);
        }

        mBinding.reminderText.setText(newReminderText);
        mBinding.reminderText.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideReminder() {
        mBinding.reminderText.setVisibility(View.INVISIBLE);
    }

    @Override
    public void goBackToMainActivity(@Nullable SingleNote note, int result) {
        if (result != -1 && note != null) {
            String value = noteResult[result];

            Intent intent = new Intent(NoteEditorActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

            if (value.equals(noteResult[1])) {
                if (noteEditorPresenter.getFavorite()) {
                    intent.putExtra(getString(R.string.isFavoriteIntent), true);
                }
            }

            intent.putExtra(getString(R.string.noteSuccessIntent), value);
            intent.putExtra(getString(R.string.notePositionIntent), notePosition);

            intent.putExtra("Note ID", note.get_id());

            checkIfMenuActionClicked(intent);
            setResult(RESULT_OK, intent);
        }

        finish();
    }

    private void checkIfMenuActionClicked(Intent intent) {
        if (menuActionClickedId != -1) {
            switch (menuActionClickedId) {
                case R.id.archive_note:
                    intent.putExtra(getString(R.string.menuActionIntent), 0);
                    break;
                case R.id.unarchive_note:
                    intent.putExtra(getString(R.string.menuActionIntent), 1);
                    break;
                case R.id.delete_note:
                    intent.putExtra(getString(R.string.menuActionIntent), 2);
                    break;
                case R.id.restore_note:
                    intent.putExtra(getString(R.string.menuActionIntent), 3);
                    break;
            }
        }
    }

    @Override
    protected void onDestroy() {
        noteEditorPresenter.onDestroy();
        super.onDestroy();
    }


    // TODO: Remove and add to Model
    @Override
    public void setupReminder(@NonNull SingleNote note) {
        ReminderManager.start(getApplicationContext(), note);
    }

    // TODO: Remove and add to Model
    @Override
    public void cancelReminder(int noteId) {
        ReminderManager.cancel(getApplicationContext(), noteId);
    }
}