package com.ozmar.notes;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import static com.ozmar.notes.MainActivity.db;
import static com.ozmar.notes.MainActivity.notesList;

// TODO: Possibly change how saveNote is done. Right now it uses notesList to access notes.
// TODO: (Cont.) The key member is not used for getting notes. db is only used to add a note
// TODO: (Cont.) and notesList is then updated with the new note

// TODO: Save position current action and cursor position when rotating between landscape and portrait


public class NoteEditorActivity extends AppCompatActivity {

    private EditText editTextTitle, editTextContent;
    private SingleNote currentNote = null;
    private boolean favorite = false;

    // Hide the soft keyboard
    private void hideSoftKeyboard() {
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
    }

    // Show the soft keyboard
    private void showSoftKeyboard() {
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
    }

    private void hideMenuItem(View view) {
        // Might not be necessary
        view.setVisibility(View.INVISIBLE);
    }

    private void favoriteNoteMenu() {
        favorite = !favorite;
        Toast toast = Toast.makeText(getApplicationContext(), "Note is favorite: " + favorite, Toast.LENGTH_SHORT);
        toast.show();

        int temp = 0;
        if (favorite) {     // Show boolean as int to store in db
            temp = 1;
        }

        if (currentNote != null) {  // Save favorite even if user does explicitly press save menu item
            currentNote.set_favorite(temp);
            db.updateNote(currentNote);
        }
    }

    private void deleteNoteMenu() {
        // Pop up warning dialog
        // Remove note from db
        // Update notesList
        // Go back to MainActivity
    }

    // Update note if it exists else create a new one
    private void saveNoteMenu() {
        String title = editTextTitle.getText().toString();
        String content = editTextContent.getText().toString();

        // Intent will go pack to MainActivity and clear the stack
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        // Check if the note has anything to save
        if (!title.isEmpty() || !content.isEmpty()) {
            if (currentNote != null) {
                boolean titleTheSame = title.equals(currentNote.get_title());
                boolean contentTheSame = content.equals(currentNote.get_content());

                // Check if user is saving note without changes
                // Only save changes that are required
                if (!(titleTheSame && contentTheSame)) {

                    if (titleTheSame) {
                        currentNote.set_content(content);
                    } else if (contentTheSame) {
                        currentNote.set_title(title);
                    } else {
                        currentNote.set_title(title);
                        currentNote.set_content(content);
                    }

                    db.updateNote(currentNote);
                }
            }

            // New note is being added
            else {
                SingleNote temp;
                if (favorite) {
                    temp = new SingleNote(title, content, 1);
                } else {
                    temp = new SingleNote(title, content);
                }

                db.addNote(temp);
                notesList = db.getAllNotes();
            }

            intent.putExtra("Note Success", 1);
        }

        // Note is empty, do not save
        else {
            intent.putExtra("Note Success", 0);
        }

        startActivity(intent);

    } // saveNote() end

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_editor);

        editTextTitle = (EditText) findViewById(R.id.editTextTitle);
        editTextContent = (EditText) findViewById(R.id.editTextContent);

        Intent intent = getIntent();
        int noteID = intent.getIntExtra("noteID", -1);

        if (noteID != -1) {     // Note exists
            currentNote = notesList.get(noteID);
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
                    view.setFocusableInTouchMode(true);
                    view.requestFocus();
                    showSoftKeyboard();
                    return false;
                }
            };
            editTextTitle.setOnTouchListener(editTextListener);
            editTextContent.setOnTouchListener(editTextListener);
        }

        // New note is being created, show keyboard at the start
        else {
            editTextContent.requestFocus();
            showSoftKeyboard();
        }
    } // onCreate() end

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.add_note_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    // To use for menu button on action bar
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        if (item.getItemId() == R.id.save_note) {
            saveNoteMenu();
            return true;
        } else if (item.getItemId() == R.id.delete_note) {
            deleteNoteMenu();
            return true;
        } else if (item.getItemId() == R.id.favorite_note) {
            favoriteNoteMenu();
            return true;
        }

        return false;
    }

} // NoteEditorActivity() end
