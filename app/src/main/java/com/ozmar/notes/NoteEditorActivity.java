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
import com.ozmar.notes.reminderDialog.ReminderDialogFragment;
import com.ozmar.notes.notifications.ReminderManager;
import com.ozmar.notes.utils.FormatUtils;
import com.ozmar.notes.utils.NoteChanges;

import org.joda.time.DateTime;

import static com.ozmar.notes.MainActivity.db;

// TODO: When deleting an entire note, make sure the reminder is cancelled if it exists

// TODO: Possibly pass a copy of FrequencyChoices to ReminderDialogFragment instead
// To do == comparison at the end to check whether the alarm needs updating
// Currently, FrequencyChoices will always change so the alarm always needs updating

public class NoteEditorActivity extends AppCompatActivity
        implements ReminderDialogFragment.OnReminderPickedListener {

    private EditText editTextTitle, editTextContent;
    private SingleNote currentNote;
    private FrequencyChoices frequencyChoices;

    private boolean favorite = false;
    private int notePosition;
    private int listUsed;

    private String[] noteResult;

    private TextView reminderText;
    private long reminderTime = 0;

    private void contextualActionResult(@NonNull MenuItem item, @Nullable SingleNote note,
                                        @Nullable FrequencyChoices choices, long reminderTime) {

        String title = editTextTitle.getText().toString();
        String content = editTextContent.getText().toString();

        if (!(title.isEmpty() && content.isEmpty() && note == null)) {
            Intent intent = new Intent(NoteEditorActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

            if (note != null) {
                note.set_favorite(favorite);

                upDateNote(note, null, title, content);
                saveReminder(note, choices, null, reminderTime);

            } else {

                // TODO: Modify so that empty notes are not processed
//                boolean titleEmpty = title.isEmpty();
//                boolean contentEmpty = content.isEmpty();
//                if (!(titleEmpty && contentEmpty)) {    // New note

                note = createNewNote(choices, title, content, favorite, reminderTime);
                intent.putExtra(getString(R.string.isNewNoteIntent), 1);
//                }
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

    // Check strings.xml for meaning of noteResult[]
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
            upDateNote(note, noteChanges, title, content);

            // Don't allow changes to favorite if in archive list
            if (listUsed != 2 && note.is_favorite() != favorite) {
                note.set_favorite(favorite);
                noteChanges.setFavoriteChanged(true);
            }

            saveReminder(note, choices, noteChanges, reminderTime);

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

                note = createNewNote(choices, title, content, favorite, reminderTime);
                new BasicDBAsync(db, null, note, listUsed, 0).execute();
                noteModifiedResult(note, noteResult[1]);
            }
        }

    } // saveNote() end

    public void upDateNote(@NonNull SingleNote note, @Nullable NoteChanges noteChanges,
                           @NonNull String title, @NonNull String content) {
        boolean titleTheSame = title.equals(note.get_title());
        boolean contentTheSame = content.equals(note.get_content());

        int changeNumber;
        if (!(titleTheSame && contentTheSame)) {
            if (titleTheSame) {
                changeNumber = 2;
                note.set_content(content);
            } else if (contentTheSame) {
                changeNumber = 1;
                note.set_title(title);
            } else {
                changeNumber = 3;
                note.set_title(title);
                note.set_content(content);
            }

            if (noteChanges != null) {
                noteChanges.setNoteTextChanges(changeNumber);
            }

            note.set_timeModified(System.currentTimeMillis());
        }
    }

    private SingleNote createNewNote(@Nullable FrequencyChoices choices, @NonNull String title,
                                     @NonNull String content, boolean favorite, long reminderTime) {
        SingleNote newNote;

        if (reminderTime != 0) {
            int reminderId = db.addReminder(choices, reminderTime);

            newNote = new SingleNote(title, content, favorite, System.currentTimeMillis(), reminderTime, reminderId);

            if (choices != null) {
                newNote.set_hasFrequencyChoices(true);
            }

            // TODO: Pass FrequencyChoices
            ReminderManager.start(getApplicationContext(), newNote);

        } else {
            newNote = new SingleNote(title, content, favorite, System.currentTimeMillis());
        }

        return newNote;
    }

    private void saveReminder(@NonNull SingleNote note, @Nullable FrequencyChoices choices,
                              @Nullable NoteChanges changes, long reminderTime) {

        boolean idChanged = false;

        // New reminder
        if (note.get_reminderId() == -1 && reminderTime != 0) {
            idChanged = true;
            int newId = db.addReminder(choices, reminderTime);
            note.set_reminderId(newId);
            ReminderManager.start(getApplicationContext(), note);

        } else if (note.get_reminderId() != -1) {

            // Delete reminder
            if (reminderTime == 0 && choices == null) {
                idChanged = true;
                ReminderManager.cancel(getApplicationContext(), note.get_id());
                db.deleteReminder(note.get_reminderId());
                note.set_reminderId(-1);

                // Updating reminder
            } else {
                if (reminderTime != note.get_nextReminderTime()) {
                    note.set_nextReminderTime(reminderTime);
                    ReminderManager.start(getApplicationContext(), note);
                }

                db.updateReminder(note.get_reminderId(), choices, reminderTime);
            }
        }

        if (changes != null) {
            changes.setReminderIdChanged(idChanged);
        }

        if (choices != null) {
            note.set_hasFrequencyChoices(true);
        } else {
            note.set_hasFrequencyChoices(false);
        }
    }

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

        if (currentNote != null) {
            setUpNoteView(currentNote);
            setupReminderDisplay(currentNote);

        } else {      // New note is being created, show keyboard at the start
            editTextContent.requestFocus();
        }

    } // onCreate() end

    private void setUpNoteView(@NonNull SingleNote note) {
        if (listUsed == 2 && note.is_favorite()) {
            note.set_favorite(false);
        } else {
            favorite = note.is_favorite();
        }

        setupReminderDisplay(note);

        TextView timeTextView = findViewById(R.id.lastModified);
        timeTextView.setText(FormatUtils.lastUpdated(NoteEditorActivity.this, note.get_timeModified()));

        editTextTitle.setText(note.get_title());
        editTextTitle.setFocusable(false);
        editTextContent.setText(note.get_content());
        editTextContent.setFocusable(false);

        // Keyboard does not pop up until the user clicks on the screen
        // allowing the user to see the entire note at the start
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

    } // setUpNoteView() end

    private void setupReminderDisplay(@NonNull SingleNote note) {
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

        if (choices == null) {
            reminderText.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_reminder_dark_gray_small,
                    0, 0, 0);
        } else {
            reminderText.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_repeat_dark_gray_small,
                    0, 0, 0);
        }

        reminderTime = dateTime.getMillis();
        reminderText.setText(FormatUtils.getReminderText(getApplication(), dateTime));
        reminderText.setVisibility(View.VISIBLE);
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
    protected void onPause() {
        super.onPause();
    }
} // NoteEditorActivity() end