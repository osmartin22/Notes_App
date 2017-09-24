package com.ozmar.notes;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import static com.ozmar.notes.MainActivity.db;
import static com.ozmar.notes.MainActivity.notesList;

public class NoteEditorActivity extends AppCompatActivity {

    EditText editTextTitle, editTextContent;

    int noteID;

    // TODO: Possibly change how saveNote is done. Right now it uses notesList to access notes.
        // TODO: (Cont.) The key member is not used for getting notes. db is only used to add a note
        // TODO: (Cont.) and notesList is then updated with the new note
    // Update note if it exists else create a new one
    public void saveNote(){
        String title = editTextTitle.getText().toString();
        String content = editTextContent.getText().toString();

        // Intent will go pack to MainActivity and clear the stack until MainActivity is reached
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        // Check if the note has anything to save
        if(!title.isEmpty() || !content.isEmpty()) {
            if (noteID != -1) {
                SingleNote tempNote = notesList.get(noteID);

                // Check if user is saving note without changes
                // If statement is true if no changes were made
                if( !(title.equals(tempNote.get_title()) && content.equals(tempNote.get_content())) ) {

                    // Only save content if title not changed
                    if(title.equals(tempNote.get_title())) {
                        tempNote.set_content(content);
                    }

                    // Only save title if content not changed
                    else if(content.equals(tempNote.get_content())) {
                        tempNote.set_title(title);
                    }

                    // Title and content were changed
                    else {
                        tempNote.set_title(title);
                        tempNote.set_content(content);
                    }

                    db.updateNote(tempNote);
                }
            }

            // New note is being added
            else {
                SingleNote temp = new SingleNote(title, content);
                db.addNote(temp);
                notesList = db.getAllNotes();
            }

            intent.putExtra("Note Success", 1);
        }

        // Note is empty
        else {
            intent.putExtra("Note Success", 0);
        }

        startActivity(intent);

    } // saveNote() end

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_editor);

        editTextTitle = (EditText)findViewById(R.id.editTextTitle);
        editTextContent = (EditText)findViewById(R.id.editTextContent);
        editTextContent.requestFocus();

        Intent intent = getIntent();
        noteID = intent.getIntExtra("noteID", -1);

         if(noteID != -1) {
            editTextTitle.setText(notesList.get(noteID).get_title());
             editTextContent.setText(notesList.get(noteID).get_content());
        }
    } // onCreate() end

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

        if(item.getItemId() == R.id.save_note) {
            saveNote();

            return true;
        }

        return false;
    }

} // NoteEditorActivity() end
