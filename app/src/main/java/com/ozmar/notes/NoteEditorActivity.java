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

// TODO: Fix bug where setting a favorite note to not favorite is not saved correctly

// TODO: Make menu action buttons to represent the same action
// Currently in MainActivity and NoteEditorActivity, they do the same action but are
// represented differently

// TODO: Change anything to do with AlarmManager to be handled in the Model

// TODO: Fix data being lost on orientation change

// TODO: Decide if currentNote should stay in view

public class NoteEditorActivity extends AppCompatActivity
        implements ReminderDialogFragment.OnReminderPickedListener, NoteEditorView {

    private static final int All_NOTES = 0;
    private static final int FAVORITE_NOTES = 1;
    private static final int ARCHIVE_NOTES = 2;
    private static final int RECYCLE_BIN_NOTES = 3;

    private int listUsed;
    private int notePosition;

    private String[] noteResult;

    private ActivityNoteEditorBinding mBinding;
    private NoteEditorPresenter noteEditorPresenter;

    private void contextualActionResult(@NonNull MenuItem item, @Nullable SingleNote note, long reminderTime) {

        String title = mBinding.editTextTitle.getText().toString();
        String content = mBinding.editTextContent.getText().toString();

        if (!(title.isEmpty() && content.isEmpty() && note == null)) {
            Intent intent = new Intent(NoteEditorActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

            if (note != null) {
                note.set_favorite(noteEditorPresenter.getFavorite());

                noteEditorPresenter.updateNote(note, null, title, content);
                noteEditorPresenter.saveReminder(db, note, null, reminderTime);

            } else {
                note = noteEditorPresenter.createNewNote(db, title, content);
                intent.putExtra(getString(R.string.isNewNoteIntent), 1);
            }

            switch (item.getItemId()) {
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

            intent.putExtra(getString(R.string.notePositionIntent), notePosition);
            intent.putExtra(getString(R.string.noteIntent), note);
            setResult(RESULT_OK, intent);
        }

        finish();   // Only allow undo for notes that are not new
    }

    private void noteModifiedResult(@Nullable SingleNote note, @NonNull String value) {
        Intent intent = new Intent(NoteEditorActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        if (value.equals(noteResult[1])) {
            if (noteEditorPresenter.getFavorite()) {
                intent.putExtra(getString(R.string.isFavoriteIntent), true);
            }
        }

        intent.putExtra(getString(R.string.noteSuccessIntent), value);
        intent.putExtra(getString(R.string.notePositionIntent), notePosition);
        intent.putExtra(getString(R.string.noteIntent), note);
        setResult(RESULT_OK, intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);

        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_note_editor);
        setupToolbar();

        Intent intent = getIntent();
        notePosition = intent.getIntExtra(getString(R.string.notePositionIntent), 0);
        listUsed = intent.getIntExtra(getString(R.string.listUsedIntent), All_NOTES);
        SingleNote note = intent.getParcelableExtra(getString(R.string.noteIntent));

        noteResult = getResources().getStringArray(R.array.noteResultArray);
        noteEditorPresenter = new NoteEditorPresenter(NoteEditorActivity.this);
        noteEditorPresenter.updateNote(note);
        setupNoteView(noteEditorPresenter.getNote(), listUsed);
    }

    private void setupToolbar() {
        setSupportActionBar((Toolbar) mBinding.myToolbar);
        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    private void setupNoteView(@Nullable SingleNote note, int listUsed) {
        if (note != null) {
            noteEditorPresenter.setNoteFavoriteAtStart(note, listUsed);

            setupNoteEditTexts(note);
            if (note.get_reminderId() != -1) {
                displayReminder(note);
            }

        } else {      // New note is being created, show keyboard at the StartForNextRepeat
            noteEditorPresenter.updateFavorite(false);
            mBinding.editTextContent.requestFocus();
        }
    }


    private void save() {
        String title = mBinding.editTextTitle.getText().toString();
        String content = mBinding.editTextContent.getText().toString();

        if (listUsed == ARCHIVE_NOTES) {
            noteEditorPresenter.updateFavorite(false);
        }

        int result = noteEditorPresenter.overall(noteEditorPresenter.getNote(), title, content, listUsed, db);
        noteModifiedResult(noteEditorPresenter.getNote(), noteResult[result]);
    }

    @Override
    public void onBackPressed() {

        save();

        super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.note_editor_menu, menu);
        setupToolbarIcons(noteEditorPresenter.getNote(), menu, listUsed);
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
                setFavoriteIcon(item, noteEditorPresenter.getFavorite());
                return true;

            case R.id.reminder:
                addReminder(null);
                return true;

            case R.id.delete_note_forever:
                deleteNoteForever(noteEditorPresenter.getNote(), listUsed);
                return true;

            case R.id.delete_note:
            case R.id.archive_note:
            case R.id.unarchive_note:
            case R.id.restore_note:
                contextualActionResult(item, noteEditorPresenter.getNote(),
                        noteEditorPresenter.getReminderTime());
                return true;
        }

        return false;
    }

    private void setFavoriteIcon(@NonNull MenuItem item, boolean favorite) {
        noteEditorPresenter.updateFavorite(favorite);

        if (noteEditorPresenter.getFavorite()) {
            item.setIcon(R.drawable.ic_favorite_star_on);
        } else {
            item.setIcon(R.drawable.ic_favorite_star_off);
        }
    }


    @SuppressWarnings("WeakerAccess")
    public void addReminder(View view) {
        long time = noteEditorPresenter.getReminderTime();
        FrequencyChoices choices = noteEditorPresenter.getFrequencyChoices();

        ReminderDialogFragment dialogFragment = ReminderDialogFragment.newInstance(choices, time);
        dialogFragment.show(getSupportFragmentManager(), "reminder_dialog_layout");
    }

    @Override
    public void onReminderPicked(@NonNull DateTime dateTime, @Nullable FrequencyChoices choices) {
        noteEditorPresenter.updateFrequencyChoices(choices);
        noteEditorPresenter.updateReminderTime(dateTime.getMillis());
        String newReminderText = FormatUtils.getReminderText(getApplication(), dateTime);
        updateDisplayReminder(newReminderText, noteEditorPresenter.getFrequencyChoices());
    }

    @Override
    public void onReminderDelete() {
        noteEditorPresenter.updateFrequencyChoices(null);
        noteEditorPresenter.updateReminderTime(0);
        mBinding.reminderText.setVisibility(View.INVISIBLE);
    }

    private void deleteNoteForever(@NonNull SingleNote note, int listUsed) {
        new AlertDialog.Builder(NoteEditorActivity.this)
                .setMessage(getString(R.string.messageDialog))
                .setPositiveButton(getString(R.string.deleteDialog), (dialogInterface, i) -> {
                    new BasicDBAsync(db, null, note, listUsed, 3).execute();
                    noteModifiedResult(note, noteResult[2]);
                    finish();
                })
                .setNegativeButton(getString(R.string.cancelDialog), null)
                .show();
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
            if (listUsed == RECYCLE_BIN_NOTES) {
                // TODO: Possibly change to SnackBar as it is hard to see right now
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


    // TODO: Update FChoices and reminderTime(don't use db)
    @Override
    public void displayReminder(@NonNull SingleNote note) {
        if (note.hasFrequencyChoices()) {

            noteEditorPresenter.updateFrequencyChoices(db.getFrequencyChoice(note.get_reminderId()));

            mBinding.reminderText.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_repeat_dark_gray_small,
                    0, 0, 0);

        } else {
            mBinding.reminderText.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_reminder_dark_gray_small,
                    0, 0, 0);
        }

        long time = note.get_nextReminderTime();
        noteEditorPresenter.updateReminderTime(time);
        mBinding.reminderText.setText(FormatUtils.getReminderText(NoteEditorActivity.this,
                new DateTime(time)));
        mBinding.reminderText.setVisibility(View.VISIBLE);
    }

    @Override
    public void updateDisplayReminder(@NonNull String newReminderText,
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

    @Override
    public void noteResult(@Nullable SingleNote note, int result) {
        if (result != 0 && note != null) {
            noteModifiedResult(note, noteResult[result]);
        }
    }


    @Override
    protected void onDestroy() {
        noteEditorPresenter.onDestroy();
        super.onDestroy();
    }
}