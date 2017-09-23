package com.ozmar.notes;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.List;

// TODO: 1) Use AsyncTask for db read/write


public class MainActivity extends AppCompatActivity {
    ListView listView;
    NotesAdapter myAdapter;
    static DatabaseHandler db;

    static List<SingleNote> notesList;

    // onClick method for add button
    public void addNote(View view) {
        Intent intent = new Intent(this.getApplicationContext(), NoteEditorActivity.class);
        intent.putExtra("noteID", -1);
        startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = new DatabaseHandler(MainActivity.this);

        notesList = db.getAllNotes();

        listView = (ListView)findViewById(R.id.listVIew);

        myAdapter = new NotesAdapter(this, R.layout.note_preview, notesList);
        listView.setAdapter(myAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(getApplicationContext(), NoteEditorActivity.class);
                intent.putExtra("noteID", i);
                startActivity(intent);
            }
        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {

                final int itemToDelete = i;

                new AlertDialog.Builder(MainActivity.this)
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .setTitle("Are You Sure?")
                                .setMessage("Do You Want To Delete This Item?")
                                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        db.deleteNote(notesList.get(itemToDelete));
                                        notesList = db.getAllNotes();
                                        myAdapter.notifyDataSetChanged();
                                    }
                                })
                                .setNegativeButton("No", null)
                                .show();

                return true;
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        notesList = db.getAllNotes();

        if(listView.getAdapter() == null) {
            listView.setAdapter(myAdapter);
        }

        else {
            listView.setAdapter(myAdapter);
            myAdapter.notifyDataSetChanged();
            listView.invalidateViews();
            listView.refreshDrawableState();
        }
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        MenuInflater menuInflater = getMenuInflater();
//        menuInflater.inflate(R.menu.add_note_menu, menu);
//
//        return super.onCreateOptionsMenu(menu);
//    }
//
//    // To use for menu button on action bar
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        super.onOptionsItemSelected(item);
//
//        if(item.getItemId() == R.id.add_note) {
//            Intent intent = new Intent(getApplicationContext(), NoteEditorActivity.class);
//            startActivity(intent);
//
//            return true;
//        }
//
//        return false;
//    }
}
