package com.ozmar.notes;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import static com.ozmar.notes.MainActivity.db;
import static com.ozmar.notes.MainActivity.notesList;

public class NoteEditorActivity extends AppCompatActivity {

    EditText editTextTitle, editTextContent;

    int noteID;

    // TODO: Modify so that only changes in text cause a save
    // TODO: Possibly change how saveNote is done. Right now it uses notesList to access notes.
        // TODO: (Cont.) The key member is not used for getting notes. db is only used to add a note
        // TODO: (Cont.) and notesList is then updated with the new note
    // Update note if it exists else create a new one
    public void saveNote(View view){
        String title = editTextTitle.getText().toString();
        String content = editTextContent.getText().toString();

        if(noteID != -1) {
            SingleNote tempNote = notesList.get(noteID);
            tempNote.set_title(title);
            tempNote.set_content(content);
            db.updateNote(tempNote);

//            Log.d("Note", "Updated");
        }

        else {
            SingleNote temp = new SingleNote(title, content);
            db.addNote(temp);
            notesList = db.getAllNotes();
            noteID = notesList.size()-1;    // Set noteID to last note in the List since the newly added note
                                                // will be in that position

//            Log.d("Note", "Added");
        }

        //MainActivity.myAdapter.notifyDataSetChanged();

    } // saveNote() end

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_editor);

        editTextTitle = (EditText)findViewById(R.id.editTextTitle);
        editTextContent = (EditText)findViewById(R.id.editTextContent);

        Intent intent = getIntent();
        noteID = intent.getIntExtra("noteID", -1);

         if(noteID != -1) {
            editTextTitle.setText(notesList.get(noteID).get_title());
             editTextContent.setText(notesList.get(noteID).get_content());
        }
    } // onCreate() end

} // NoteEditorActiity() end
