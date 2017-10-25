package com.ozmar.notes;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.ozmar.notes.async.BasicDBAsync;
import com.ozmar.notes.async.UpdateNoteAsync;
import com.ozmar.notes.reminderDialog.ReminderDialogFragment;
import com.ozmar.notes.reminderDialog.ReminderManager;
import com.ozmar.notes.utils.FormatUtils;
import com.ozmar.notes.utils.NoteChanges;

import org.joda.time.DateTime;

import static com.ozmar.notes.MainActivity.db;

// TODO: Add Clock Symbol and repeat symbol to reminder display

// TODO: Possibly pass a copy of FrequencyChoices to ReminderDialogFragment instead
// To do == comparison at the end to check whether the alarm needs updating
// Currently, FrequencyChoices will always change so the alarm always needs updating

public class NoteEditorActivity extends AppCompatActivity
        implements ReminderDialogFragment.OnReminderPickedListener {

    private EditText editTextTitle, editTextContent;
    private SingleNote currentNote;
    private FrequencyChoices choices;
    private NoteChanges noteChanges;

    private boolean favorite = false;
    private int notePosition;
    private int listUsed;

    private String[] noteResult;

    private Button reminderButton;
    private long reminderTime = 0;

    private void contextualActionResult(MenuItem item) {
        String title = editTextTitle.getText().toString();
        String content = editTextContent.getText().toString();

        if (!(title.isEmpty() && content.isEmpty() && currentNote == null)) {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

            if (currentNote != null) {
                currentNote.set_favorite(favorite);

                upDateNote(title, content);
                saveReminder();

            } else {

                // TODO: Modify so that empty notes are not processed
//                boolean titleEmpty = title.isEmpty();
//                boolean contentEmpty = content.isEmpty();
//                if (!(titleEmpty && contentEmpty)) {    // New note

                createNewNote(title, content);
                intent.putExtra("New Note Action", 1);
//                }
            }

            switch (item.getItemId()) {
                case R.id.archive_note:
                    intent.putExtra("menuAction", 0);
                    break;
                case R.id.unarchive_note:
                    intent.putExtra("menuAction", 1);
                    break;
                case R.id.delete_note:
                    intent.putExtra("menuAction", 2);
                    break;
                case R.id.restore_note:
                    intent.putExtra("menuAction", 3);
                    break;
            }

            intent.putExtra("Note", currentNote);
            intent.putExtra("Note Position", notePosition);
            setResult(RESULT_OK, intent);

        }

        finish();   // Only allow undo for notes that are not new
    }

    // Check strings.xml for meaning of noteResult[]
    private void noteModifiedResult(String value) {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        if (value.equals(noteResult[1])) {
            if (favorite) {
                intent.putExtra("Note Favorite", true);
            }
        }

        intent.putExtra("Note Success", value);
        intent.putExtra("Note Position", notePosition);
        intent.putExtra("Note", currentNote);
        setResult(RESULT_OK, intent);
    } // noteModifiedResult() end

    // Save note into db and go back to MainActivity
    private void saveNote() {
        String title = editTextTitle.getText().toString();
        String content = editTextContent.getText().toString();

        if (currentNote != null) {

            noteChanges = new NoteChanges();
            upDateNote(title, content);

            // Don't allow changes to favorite if in archive list
            if (listUsed != 2 && currentNote.is_favorite() != favorite) {
                currentNote.set_favorite(favorite);
                noteChanges.setFavoriteChanged(true);
            }

            saveReminder();

            new UpdateNoteAsync(db, null, currentNote, listUsed, noteChanges).execute();
            if (listUsed == 1 && noteChanges.isFavoriteChanged()) {       // Note not a favorite anymore
                noteModifiedResult(noteResult[3]);
            } else {
                noteModifiedResult(noteResult[0]);
            }

        } else {
            boolean titleEmpty = title.isEmpty();
            boolean contentEmpty = content.isEmpty();
            if (!(titleEmpty && contentEmpty)) {    // New note

                createNewNote(title, content);
                new BasicDBAsync(db, null, currentNote, listUsed, 0).execute();
                noteModifiedResult(noteResult[1]);
            }
        }

        finish();       // Note unmodified or empty new note
    } // saveNote() end

    public void upDateNote(String title, String content) {
        boolean titleTheSame = title.equals(currentNote.get_title());
        boolean contentTheSame = content.equals(currentNote.get_content());

        int changeNumber;
        if (!(titleTheSame && contentTheSame)) {
            if (titleTheSame) {
                changeNumber = 2;
                currentNote.set_content(content);
            } else if (contentTheSame) {
                changeNumber = 1;
                currentNote.set_title(title);
            } else {
                changeNumber = 3;
                currentNote.set_title(title);
                currentNote.set_content(content);
            }

            if (noteChanges != null) {
                noteChanges.setNoteTextChanges(changeNumber);
            }

            currentNote.set_timeModified(System.currentTimeMillis());
        }
    }

    private void createNewNote(String title, String content) {
        if (reminderTime != 0) {
            int reminderId = db.addReminder(choices, reminderTime);
            currentNote = new SingleNote(title, content, favorite, System.currentTimeMillis(), reminderTime, reminderId);

            if (choices != null) {
                currentNote.set_hasFrequencyChoices(true);
            }

            // TODO: Pass FrequencyChoices
            ReminderManager.start(getApplicationContext(), currentNote);

        } else {
            currentNote = new SingleNote(title, content, favorite, System.currentTimeMillis());
        }
    }

    private void saveReminder() {
        boolean idChanged = false;
        if (currentNote.get_reminderId() == -1 && reminderTime != 0) {
            int newId = db.addReminder(choices, reminderTime);
            currentNote.set_reminderId(newId);
            idChanged = true;
            ReminderManager.start(getApplicationContext(), currentNote);
            Log.d("Reminder", "Add Reminder To Existing Note -> " + newId);

        } else if (currentNote.get_reminderId() != -1) {
            if (reminderTime == 0 && choices == null) {
                Log.d("Reminder", "Delete Reminder From Existing Note -> " + currentNote.get_reminderId());
                ReminderManager.cancel(getApplicationContext(), currentNote.get_id());
                db.deleteReminder(currentNote.get_reminderId());
                currentNote.set_reminderId(-1);
                idChanged = true;

            } else {
                if (reminderTime != currentNote.get_nextReminderTime()) {
                    currentNote.set_nextReminderTime(reminderTime);
                    ReminderManager.start(getApplicationContext(), currentNote);
                }

                db.updateReminder(currentNote.get_reminderId(), choices, reminderTime);
                Log.d("Reminder", "Updating Reminder Of Existing Note -> " + currentNote.get_reminderId());
            }
        }

        if (noteChanges != null) {
            noteChanges.setReminderIdChanged(idChanged);
        }

        if (choices != null) {
            currentNote.set_hasFrequencyChoices(true);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_editor);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        noteResult = getResources().getStringArray(R.array.noteResultArray);

        reminderButton = (Button) findViewById(R.id.reminderText);
        editTextTitle = (EditText) findViewById(R.id.editTextTitle);
        editTextContent = (EditText) findViewById(R.id.editTextContent);

        Intent intent = getIntent();
        notePosition = intent.getIntExtra("noteID", 0);
        listUsed = intent.getIntExtra("listUsed", 0);
        currentNote = intent.getParcelableExtra("Note");

        setUpNoteView();
    } // onCreate() end

    private void setUpNoteView() {
        if (currentNote != null) {

            // TODO: Check why I need this if() statement
            if (listUsed == 2 && currentNote.is_favorite()) {
                currentNote.set_favorite(false);
            } else {
                favorite = currentNote.is_favorite();
            }

            Log.d("Reminder", "ReminderID -> " + currentNote.get_reminderId());
            Log.d("Reminder", "HasFrequency -> " + currentNote.hasFrequencyChoices());
            if (currentNote.get_reminderId() != -1) {
                if (currentNote.hasFrequencyChoices()) {
                    // TODO: Possibly use AsyncTask for this
                    choices = db.getFrequencyChoice(currentNote.get_reminderId());
                }
                reminderTime = currentNote.get_nextReminderTime();
                reminderButton.setText(FormatUtils.getReminderText(getApplication(), new DateTime(reminderTime)));
                reminderButton.setVisibility(View.VISIBLE);
            }

            TextView timeTextView = (TextView) findViewById(R.id.lastModified);
            timeTextView.setText(FormatUtils.lastUpdated(getApplicationContext(), currentNote.get_timeModified()));

            editTextTitle.setText(currentNote.get_title());
            editTextTitle.setFocusable(false);
            editTextContent.setText(currentNote.get_content());
            editTextContent.setFocusable(false);

            // Keyboard does not pop up until the user clicks on the screen
            // allowing the user to see the entire note at the start
            View.OnTouchListener editTextListener = new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    if (listUsed == 3) {
                        // TODO: Possibly change to SnackBar
                        Toast.makeText(getApplicationContext(), "Can't edit in Trash", Toast.LENGTH_SHORT).show();
                    } else {
                        view.setFocusableInTouchMode(true);
                        view.requestFocus();
                    }
                    return false;
                }
            };

            editTextTitle.setOnTouchListener(editTextListener);
            editTextContent.setOnTouchListener(editTextListener);

        } else {      // New note is being created, show keyboard at the start
            editTextContent.requestFocus();
        }
    } // setUpNoteView() end

    @Override
    public void onBackPressed() {
        saveNote();
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
                deleteNoteForever();
                return true;

            case R.id.delete_note:
            case R.id.archive_note:
            case R.id.unarchive_note:
            case R.id.restore_note:
                contextualActionResult(item);
                return true;
        }

        return false;
    } // onOptionsItemSelected() end

    public void addReminder(View view) {
        ReminderDialogFragment dialogFragment = ReminderDialogFragment.newInstance(choices, reminderTime);
        dialogFragment.show(getSupportFragmentManager(), "reminder_dialog_layout");
    }

    @Override
    public void onReminderPicked(DateTime dateTime, int frequencyPicked, FrequencyChoices choices) {
        // TODO: Do something with frequencyPicked

        if (this.choices != choices) {
            this.choices = choices;
        }

        reminderTime = dateTime.getMillis();
        reminderButton.setText(FormatUtils.getReminderText(getApplication(), dateTime));
        reminderButton.setVisibility(View.VISIBLE);
    }

    @Override
    public void onReminderDelete() {
        choices = null;
        reminderTime = 0;
        reminderButton.setVisibility(View.INVISIBLE);
    }

    // TODO: Add deletion of Reminder if Exists
    private void deleteNoteForever() {
        new AlertDialog.Builder(NoteEditorActivity.this)
                .setMessage("Do you want to delete this note?")
                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        new BasicDBAsync(db, null, currentNote, listUsed, 3).execute();
                        noteModifiedResult(noteResult[2]);
                        finish();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    } // deleteNoteMenu() end

    @Override
    protected void onPause() {
        super.onPause();
    }
} // NoteEditorActivity() end