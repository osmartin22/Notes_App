package com.ozmar.notes;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
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
import com.ozmar.notes.utils.NoteEditorUtils;

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
    private MenuItem menuItem;

    private boolean favorite = false;
    private int notePosition;
    private int listUsed;

    private String[] noteResult;

    private Button reminderButton;
    private long reminderTime = 0;

    private Preferences preferences;

    private FrequencyChoices choices;
    private boolean choicesChanged = false;

    private void contextualActionResult(MenuItem item) {
        String title = editTextTitle.getText().toString();
        String content = editTextContent.getText().toString();

        if (!(title.isEmpty() && content.isEmpty() && currentNote == null)) {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

            if (currentNote != null) {
                boolean titleChanged = !currentNote.get_title().equals(title);
                boolean contentChanged = !currentNote.get_content().equals(content);
                currentNote.set_favorite(favorite);
                NoteEditorUtils.updateNoteObject(currentNote, title, content, titleChanged, contentChanged);

                NoteChanges noteChanges = new NoteChanges();
                boolean reminderChanged = NoteEditorUtils.reminderChanged(reminderTime, currentNote, noteChanges);
                boolean noteTextChanged = NoteEditorUtils.noteChanges(title, content, currentNote, noteChanges);
                NoteEditorUtils.modifyReminderIntent(getApplicationContext(), preferences, currentNote, reminderChanged, noteTextChanged);

            } else {

                if (reminderTime != 0) {
                    int reminderId = db.addReminder(choices, reminderTime);
                    currentNote = new SingleNote(title, content, favorite, System.currentTimeMillis(), reminderTime, reminderId);
                    // TODO: Rewrite ReminderManager
                    ReminderManager.start(getApplicationContext(), currentNote);

                } else {
                    currentNote = new SingleNote(title, content, favorite, System.currentTimeMillis());
                }

                intent.putExtra("New Note Action", 1);
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
            NoteChanges noteChanges = new NoteChanges();
            boolean favoriteChanged = false;
            boolean reminderChanged = NoteEditorUtils.reminderChanged(reminderTime, currentNote, noteChanges);
            boolean noteTextChanged = NoteEditorUtils.noteChanges(title, content, currentNote, noteChanges);

            NoteEditorUtils.modifyReminderIntent(getApplicationContext(), preferences, currentNote,
                    reminderChanged, noteTextChanged);

            if (listUsed != 2) {     // Don't allow changes to favorite if in archive list
                favoriteChanged = NoteEditorUtils.favoriteChanged(favorite, currentNote, noteChanges);
            }

            if (noteTextChanged || favoriteChanged || reminderChanged) {
                new UpdateNoteAsync(db, null, currentNote, listUsed, noteChanges).execute();

                if (listUsed == 1 && favoriteChanged) {       // Note not a favorite anymore
                    noteModifiedResult(noteResult[3]);
                } else {
                    noteModifiedResult(noteResult[0]);
                }
            }

        } else {
            boolean titleEmpty = title.isEmpty();
            boolean contentEmpty = content.isEmpty();
            if (!(titleEmpty && contentEmpty)) {    // New note

                if (reminderTime != 0) {
                    int reminderId = db.addReminder(choices, reminderTime);
                    currentNote = new SingleNote(title, content, favorite, System.currentTimeMillis(), reminderTime, reminderId);
                    ReminderManager.start(getApplicationContext(), currentNote);

                } else {
                    currentNote = new SingleNote(title, content, favorite, System.currentTimeMillis());
                }

                new BasicDBAsync(db, null, currentNote, listUsed, 0).execute();
                noteModifiedResult(noteResult[1]);
            }
        }

        finish();       // Note unmodified or empty new note
    } // saveNote() end

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

        preferences = new Preferences(getApplicationContext());

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

            if (listUsed == 2 && currentNote.is_favorite()) {
                currentNote.set_favorite(false);
            } else {
                favorite = currentNote.is_favorite();
            }

            if (currentNote.get_nextReminderTime() != 0) {
                reminderTime = currentNote.get_nextReminderTime();
                String reminderText = FormatUtils.getReminderText(getApplication(),
                        new DateTime(currentNote.get_nextReminderTime()));
                if (currentNote.get_reminderId() != -1) {
                    choices = db.getFrequencyChoices(currentNote.get_reminderId());
                    reminderText += " " + FormatUtils.formatFrequencyText(getApplicationContext(), choices);
                }
                reminderButton.setText(reminderText);
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
        menuItem = menu.findItem(R.id.favorite_note);
        NoteEditorUtils.setUpMenu(menu, currentNote, listUsed);
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
                favorite = NoteEditorUtils.favoriteNote(favorite, menuItem);
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
        if (this.choices != choices) {
            this.choices = choices;
            choicesChanged = true;
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