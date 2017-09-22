package com.ozmar.notes;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    ListView listView;


    static ArrayList<String> notes = new ArrayList<>();
    static ArrayAdapter arrayAdapter;

    public void addNote(View view) {
        Intent intent = new Intent(this.getApplicationContext(), NoteEditorActivity.class);
        startActivity(intent);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView = (ListView)findViewById(R.id.listVIew);
        notes.add("Example Notes");
        arrayAdapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_list_item_1, notes );
        listView.setAdapter(arrayAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(getApplicationContext(), NoteEditorActivity.class);
                intent.putExtra("noteID", i);
                startActivity(intent);
            }
        });


        DatabaseHandler db = new DatabaseHandler(this);
        Log.d("Insert: ", "Inserting ..");
        db.addNote(new SingleNote("Test 1 Title", "Test 1 Content"));
        db.addNote(new SingleNote("Test 2 Title", "Test 2 Content"));
        db.addNote(new SingleNote("Test 3 Title", "Test 3 Content"));
        db.addNote(new SingleNote("Test 4 Title", "Test 4 Content"));

        Log.d("Reading: ", "Reading all contacts..");
        List<SingleNote> notesList = db.getAllNotes();

        for(SingleNote note : notesList) {
            String log = "ID: " + note.get_id() + ", Title: " + note.get_title() + ", Content: " + note.get_content();
            Log.d("User Note: ", log);
        }




//        try {
//            SQLiteDatabase userNotesDatabase = MainActivity.this.openOrCreateDatabase("User Notes", MODE_PRIVATE, null);
//            userNotesDatabase.execSQL("CREATE TABLE IF NOT EXISTS userNotes (title VARCHAR, content VARCHAR");
////            userNotesDatabase.execSQL("INSERT INTO userNotes (title, content) VALUES ('Test Title', 'Test Content')");
//
//            Cursor c = userNotesDatabase.rawQuery("SELECT * FROM useNotesDatabase", null);
//
//            int titleIndex = c.getColumnIndex("title");
//            int contentIndex = c.getColumnIndex("content");
//
//            c.moveToFirst();
//            while(c != null) {
//                Log.i("Title", c.getString(titleIndex));
//                Log.i("Content", c.getString(contentIndex));
//
//                c.moveToNext();
//            }
//
//            c.close();
//
//        }
//        catch(Exception e) {
//
//        }


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
                                        notes.remove(itemToDelete);
                                        arrayAdapter.notifyDataSetChanged();
                                    }
                                })
                                .setNegativeButton("No", null)
                                .show();

                return true;
            }
        });
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
