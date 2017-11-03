package com.ozmar.notes;

import android.content.Intent;
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
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.ozmar.notes.async.BasicDBAsync;
import com.ozmar.notes.async.UpdateNoteAsync;
import com.ozmar.notes.notifications.ReminderManager;
import com.ozmar.notes.reminderDialog.ReminderDialogFragment;
import com.ozmar.notes.utils.FormatUtils;
import com.ozmar.notes.utils.NoteChanges;

import org.joda.time.DateTime;

import static com.ozmar.notes.MainActivity.db;

// TODO: When deleting an entire note, make sure the reminder is cancelled if it exists


public class NoteEditorActivity extends AppCompatActivity
        implements ReminderDialogFragment.OnReminderPickedListener, NoteEditorView {

    private SingleNote currentNote;
    private FrequencyChoices frequencyChoices;
    private EditText editTextTitle, editTextContent;

    private int listUsed;
    private int notePosition;
    private boolean favorite = false;

    private String[] noteResult;

    private long reminderTime = 0;
    private TextView reminderText;

    private NoteEditorPresenter noteEditorPresenter;

    private void contextualActionResult(@NonNull MenuItem item, @Nullable SingleNote note,
                                        @Nullable FrequencyChoices choices, long reminderTime) {

        String title = editTextTitle.getText().toString();
        String content = editTextContent.getText().toString();

        if (!(title.isEmpty() && content.isEmpty() && note == null)) {
            Intent intent = new Intent(NoteEditorActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

            if (note != null) {
                note.set_favorite(favorite);

                noteEditorPresenter.upDateNote(note, null, title, content);
                noteEditorPresenter.saveReminder(db, note, choices, null, reminderTime);

            } else {
                note = noteEditorPresenter.createNewNote(db, choices, title, content, favorite, reminderTime);
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

        // TODO: Change this to allow new/old notes
        finish();   // Only allow undo for notes that are not new
    }

    private void noteModifiedResult(@Nullable SingleNote note, @NonNull String value) {
        Intent intent = new Intent(NoteEditorActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        if (value.equals(noteResult[1])) {
            if (favorite) {
                intent.putExtra(getString(R.string.isFavoriteIntent), true);
            }
        }

        intent.putExtra(getString(R.string.noteSuccessIntent), value);
        intent.putExtra(getString(R.string.notePositionIntent), notePosition);
        intent.putExtra(getString(R.string.noteIntent), note);
        setResult(RESULT_OK, intent);
    } // noteModifiedResult() end


    // Save note into db and go back to MainActivity
    private void saveNote(@Nullable SingleNote note, @Nullable FrequencyChoices choices, int listUsed,
                          long reminderTime) {

        String title = editTextTitle.getText().toString();
        String content = editTextContent.getText().toString();

        if (note != null) {

            NoteChanges noteChanges = new NoteChanges();
            noteEditorPresenter.upDateNote(note, noteChanges, title, content);

            // Don't allow changes to favorite if in archive list
            if (listUsed != 2 && note.is_favorite() != favorite) {
                note.set_favorite(favorite);
                noteChanges.setFavoriteChanged(true);
            }

            noteEditorPresenter.saveReminder(db, note, choices, noteChanges, reminderTime);

            new UpdateNoteAsync(db, null, note, listUsed, noteChanges).execute();
            if (listUsed == 1 && noteChanges.isFavoriteChanged()) {       // Note not a favorite anymore
                noteModifiedResult(note, noteResult[3]);
            } else {
                noteModifiedResult(note, noteResult[0]);
            }

        } else {
            boolean titleEmpty = title.isEmpty();
            boolean contentEmpty = content.isEmpty();
            if (!(titleEmpty && contentEmpty)) {    // New note
                note = noteEditorPresenter.createNewNote(db, choices, title, content, favorite, reminderTime);
                noteModifiedResult(note, noteResult[1]);
            }
        }

    } // saveNote() end


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_editor);

        Toolbar myToolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        noteResult = getResources().getStringArray(R.array.noteResultArray);

        editTextTitle = findViewById(R.id.editTextTitle);
        editTextContent = findViewById(R.id.editTextContent);
        reminderText = findViewById(R.id.reminderText);

        Intent intent = getIntent();
        notePosition = intent.getIntExtra(getString(R.string.notePositionIntent), 0);
        listUsed = intent.getIntExtra(getString(R.string.listUsedIntent), 0);
        currentNote = intent.getParcelableExtra(getString(R.string.noteIntent));

        noteEditorPresenter = new NoteEditorPresenter(NoteEditorActivity.this);

        if (currentNote != null) {

            if (listUsed == 2 && currentNote.is_favorite()) {
                currentNote.set_favorite(false);
            } else {
                favorite = currentNote.is_favorite();
            }

            noteEditorPresenter.setUpNoteView(currentNote);
            noteEditorPresenter.setUpReminderDisplay(currentNote);

        } else {      // New note is being created, show keyboard at the StartForNextRepeat
            editTextContent.requestFocus();
        }

    } // onCreate() end

    @Override
    public void onBackPressed() {
        saveNote(currentNote, frequencyChoices, listUsed, reminderTime);
        super.onBackPressed();
    } // onBackPressed() end

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.note_editor_menu, menu);

        if (listUsed != 3) {
            if (listUsed == 2) {
                menu.findItem(R.id.archive_note).setVisible(false);
                menu.findItem(R.id.unarchive_note).setVisible(true);
            }

            if (currentNote != null && currentNote.is_favorite()) {
                menu.findItem(R.id.favorite_note).setIcon(R.drawable.ic_favorite_star_on);
            }

        } else {
            menu.findItem(R.id.restore_note).setVisible(true);
            menu.findItem(R.id.delete_note_forever).setVisible(true);
            menu.findItem(R.id.delete_note).setVisible(false);
            menu.findItem(R.id.archive_note).setVisible(false);
            menu.findItem(R.id.favorite_note).setVisible(false);
        }

        return super.onCreateOptionsMenu(menu);
    } // onCreateOptionsMenu() end

    // To use for menu button on action bar
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;

            case R.id.favorite_note:

                favorite = !favorite;
                if (favorite) {
                    item.setIcon(R.drawable.ic_favorite_star_on);
                } else {
                    item.setIcon(R.drawable.ic_favorite_star_off);
                }

                return true;

            case R.id.reminder:
                addReminder(null);
                return true;

            case R.id.delete_note_forever:
                deleteNoteForever(currentNote, listUsed);
                return true;

            case R.id.delete_note:
            case R.id.archive_note:
            case R.id.unarchive_note:
            case R.id.restore_note:
                contextualActionResult(item, currentNote, frequencyChoices, reminderTime);
                return true;
        }

        return false;
    } // onOptionsItemSelected() end

    public void addReminder(View view) {
        ReminderDialogFragment dialogFragment = ReminderDialogFragment.newInstance(frequencyChoices, reminderTime);
        dialogFragment.show(getSupportFragmentManager(), "reminder_dialog_layout");
    }

    @Override
    public void onReminderPicked(DateTime dateTime, FrequencyChoices choices) {
        if (this.frequencyChoices != choices) {
            this.frequencyChoices = choices;
        }

        reminderTime = dateTime.getMillis();
        String newReminderText = FormatUtils.getReminderText(getApplication(), dateTime);
        noteEditorPresenter.updateReminderDisplay(reminderTime, newReminderText, choices);
    }

    @Override
    public void onReminderDelete() {
        frequencyChoices = null;
        reminderTime = 0;
        reminderText.setVisibility(View.INVISIBLE);
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
    } // deleteNoteMenu() end


    @Override
    public void setUpNoteEditTexts(@NonNull SingleNote note) {
        TextView timeTextView = findViewById(R.id.lastModified);
        timeTextView.setText(FormatUtils.lastUpdated(NoteEditorActivity.this, note.get_timeModified()));

        editTextTitle.setText(note.get_title());
        editTextTitle.setFocusable(false);
        editTextContent.setText(note.get_content());
        editTextContent.setFocusable(false);

        // Keyboard does not pop up until the user clicks on the screen
        // allowing the user to see the entire note at the StartForNextRepeat
        View.OnTouchListener editTextListener = (view, motionEvent) -> {
            if (listUsed == 3) {
                // TODO: Possibly change to SnackBar
                Toast.makeText(getApplicationContext(), "Can't edit in Trash", Toast.LENGTH_SHORT).show();
            } else {
                view.performClick();
                view.setFocusableInTouchMode(true);
                view.requestFocus();
            }
            return false;
        };

        editTextTitle.setOnTouchListener(editTextListener);
        editTextContent.setOnTouchListener(editTextListener);
    }

    @Override
    public void displayReminder(@NonNull SingleNote note) {
        if (note.get_reminderId() != -1) {

            if (note.hasFrequencyChoices()) {
                reminderText.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_repeat_dark_gray_small,
                        0, 0, 0);
                frequencyChoices = db.getFrequencyChoice(note.get_reminderId());

            } else {
                reminderText.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_reminder_dark_gray_small,
                        0, 0, 0);
            }

            reminderTime = note.get_nextReminderTime();
            reminderText.setText(FormatUtils.getReminderText(NoteEditorActivity.this,
                    new DateTime(reminderTime)));
            reminderText.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void updateDisplayReminder(Long newReminderTime, @NonNull String newReminderText, @Nullable FrequencyChoices choices) {
        if (choices == null) {
            reminderText.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_reminder_dark_gray_small,
                    0, 0, 0);
        } else {
            reminderText.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_repeat_dark_gray_small,
                    0, 0, 0);
        }

        reminderText.setText(newReminderText);
        reminderText.setVisibility(View.VISIBLE);
    }

    @Override
    public void setupReminder(@NonNull SingleNote note) {
        ReminderManager.start(getApplicationContext(), note);
    }

    @Override
    public void cancelReminder(int noteId) {
        ReminderManager.cancel(getApplicationContext(), noteId);
    }


    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        noteEditorPresenter.onDestroy();
        super.onDestroy();
    }
} // NoteEditorActivity() end