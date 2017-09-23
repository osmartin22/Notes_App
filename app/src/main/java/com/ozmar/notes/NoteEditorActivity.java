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

    // Modify so that only changes in text cause a save
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

    }

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
//
//        else {
//            MainActivity.notes.add("");
//            noteID = MainActivity.notes.size() - 1;
//            MainActivity.myAdapter.notifyDataSetChanged();
//        }
//
//        editTextTitle.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
//
//            }
//
//            @Override
//            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
//                MainActivity.notes.set(noteID, String.valueOf(charSequence));
//                MainActivity.myAdapter.notifyDataSetChanged();
//            }
//
//            @Override
//            public void afterTextChanged(Editable editable) {
//
//            }
//        });
    }
}
