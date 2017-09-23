package com.ozmar.notes;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import static com.ozmar.notes.MainActivity.db;
import static com.ozmar.notes.MainActivity.notesList;

public class NoteEditorActivity extends AppCompatActivity {

    EditText editTextTitle, editTextContent;

    int noteID;

    // TODO: Modify so that only changes in text cause a save
    // Update not if it exists else create a new one
    public void saveNote(View view){
        // Save note to permanent storage
        String title = editTextTitle.getText().toString();
        String content = editTextContent.getText().toString();

        if(noteID != -1) {
            SingleNote tempNote = notesList.get(noteID);
            tempNote.set_title(title);
            tempNote.set_content(content);
//            int key = tempNote.get_id();
            db.updateNote(tempNote);
//            Log.d("Note", "Updated");
        }

        else {
            db.addNote(new SingleNote(title, content));
//            Log.d("Note", "Added");
            notesList = db.getAllNotes();
//            Log.d("Size", Integer.toString(notesList.size()));
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
