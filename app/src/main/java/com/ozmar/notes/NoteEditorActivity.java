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
import android.widget.EditText;
import android.widget.Toast;

import com.ozmar.notes.async.BasicDBAsync;
import com.ozmar.notes.utils.NoteEditorUtils;

import static com.ozmar.notes.MainActivity.db;


// 1) Removed sav button, notes save automatically now

public class NoteEditorActivity extends AppCompatActivity {

    private EditText editTextTitle, editTextContent;
    private SingleNote currentNote = null;
    private MenuItem menuItem;

    private boolean favorite = false;
    private int notePosition;
    private int listUsed;

    private String[] noteResult;

    private void contextualActionResult(MenuItem item) {

        if (currentNote != null) {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

            boolean title = !currentNote.get_title().equals(editTextTitle.toString());
            boolean content = !currentNote.get_content().equals(editTextContent.toString());
            updateNote(title, content);

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

        // Set notePosition to 0 from -1 if the note being added is a new note
        if (value.equals(noteResult[3])) {
            if (favorite) {
                intent.putExtra("Note Favorite", true);
            }
            notePosition = 0;
        }

        intent.putExtra("Note Success", value);
        intent.putExtra("Note Position", notePosition);
        intent.putExtra("Note", currentNote);
        setResult(RESULT_OK, intent);
        finish();
    } // noteModifiedResult() end

    private void updateNote(boolean titleChanged, boolean contentChanged) {
        String title = editTextTitle.getText().toString();
        String content = editTextContent.getText().toString();

        if (titleChanged) {
            currentNote.set_title(title);
        }

        if (contentChanged) {
            currentNote.set_content(content);
        }
    } // updateNote() end

    // Save note into db and go back to MainActivity
    private void saveNote() {
        String title = editTextTitle.getText().toString();
        String content = editTextContent.getText().toString();

        String difference = NoteEditorUtils.differenceFromOriginal(getApplicationContext(), title, content, currentNote);
        String[] noteChanges = getResources().getStringArray(R.array.noteChanges);

        if (difference.equals(noteChanges[0])) {
            updateNote(false, true);
            new BasicDBAsync(db, null, currentNote, listUsed, 1).execute();
            noteModifiedResult(noteResult[1]);

        } else if (difference.equals(noteChanges[1])) {
            updateNote(true, false);
            new BasicDBAsync(db, null, currentNote, listUsed, 1).execute();
            noteModifiedResult(noteResult[1]);

        } else if (difference.equals(noteChanges[2])) {
            updateNote(true, true);
            new BasicDBAsync(db, null, currentNote, listUsed, 1).execute();
            noteModifiedResult(noteResult[1]);

        } else if (difference.equals(noteChanges[3])) {
            if (listUsed == 1 && !favorite) {
                noteModifiedResult(noteResult[5]);
            } else {
                noteModifiedResult(noteResult[2]);    // No updates to RecyclerView
            }

        } else if (difference.equals(noteChanges[4])) {
            SingleNote temp;
            temp = favorite ? new SingleNote(title, content, 1) : new SingleNote(title, content, 0);
            new BasicDBAsync(db, null, temp, listUsed, 0).execute();
            noteModifiedResult(noteResult[3]);

        } else {
            noteModifiedResult(noteResult[0]);
        }

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

        noteResult = getResources().getStringArray(R.array.noteResultArray);

        editTextTitle = (EditText) findViewById(R.id.editTextTitle);
        editTextContent = (EditText) findViewById(R.id.editTextContent);

        Intent intent = getIntent();
        notePosition = intent.getIntExtra("noteID", -1);
        listUsed = intent.getIntExtra("listUsed", 0);
        currentNote = intent.getParcelableExtra("Note");

        setUpNoteView();
    } // onCreate() end

    private void setUpNoteView() {
        if (currentNote != null) {

            if (currentNote.get_favorite() == 1) {
                favorite = true;
            }

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
        }

        else {      // New note is being created, show keyboard at the start
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

        if (listUsed != 3) {

            if (listUsed == 2) {
                menu.findItem(R.id.archive_note).setVisible(false);
                menu.findItem(R.id.unarchive_note).setVisible(true);
            }

            if (currentNote != null && currentNote.get_favorite() == 1) {
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
                favorite = NoteEditorUtils.favoriteNote(favorite, menuItem);
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

    private void deleteNoteForever() {
        new AlertDialog.Builder(NoteEditorActivity.this)
                .setMessage("Do you want to delete this note?")
                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        new BasicDBAsync(db, null, currentNote, listUsed, 3).execute();
                        noteModifiedResult(noteResult[4]);
                        finish();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    } // deleteNoteMenu() end

    @Override
    protected void onPause() {
        super.onPause();
        int temp = favorite ? 1 : 0;

        if (currentNote != null && listUsed != 2) {  // Save favorite even if user does not explicitly press save menu item
            currentNote.set_favorite(temp);
            new BasicDBAsync(db, null, currentNote, listUsed, 1).execute();
        }
    }
} // NoteEditorActivity() end