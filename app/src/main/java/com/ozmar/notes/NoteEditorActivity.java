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
    // Update note if it exists else create a new one
    public void saveNote(View view){
        String title = editTextTitle.getText().toString();
        String content = editTextContent.getText().toString();

        if(noteID != -1) {
            SingleNote tempNote = notesList.get(noteID);
            tempNote.set_title(title);
            tempNote.set_content(content);
            db.updateNote(tempNote);

            Log.d("Note", "Updated");
            Log.d("Current Key", Integer.toString(noteID));
        }

        else {
            db.addNote(new SingleNote(title, content));
            notesList = db.getAllNotes();
            noteID = notesList.size()-1;    // set key to access note in db to the last position in the list

            Log.d("Note", "Added");
            Log.d("New Key", Integer.toString(noteID));
            Log.d("Size", Integer.toString(notesList.size()));
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
