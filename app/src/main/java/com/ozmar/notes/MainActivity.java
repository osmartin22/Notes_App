package com.ozmar.notes;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.List;

// TODO: 1) Use AsyncTask for db read/write instead of Main thread
// TODO: 2) Slide to delete
// TODO: 3) On long press, allow multiple deletes and show delete button
// TODO: 4) Add favorites button
// TODO: 5) If user presses back on new note and note has content, display warning that note will
// TODO: (CONT.) not be saved. Allow user to turn this off if they desire to

// TODO Possibly) Add GPS so that a notification appears/or vibrate phone when at location
// TODO Possibly) Let user choose theme (Maybe do in shared preferences)

public class MainActivity extends AppCompatActivity {
    private ListView listView;
    private NotesAdapter myAdapter;
    static DatabaseHandler db;

    static List<SingleNote> notesList;

    // onClick method launch activity to create note
    public void launchNoteEditor(View view) {
        Intent intent = new Intent(this.getApplicationContext(), NoteEditorActivity.class);
        intent.putExtra("noteID", -1);
        startActivity(intent);
    } // launchNoteEditor() end

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        db = new DatabaseHandler(MainActivity.this);
        notesList = db.getAllNotes();

        myAdapter = new NotesAdapter(this, R.layout.note_preview, notesList);
        listView = (ListView) findViewById(R.id.listVIew);
        listView.setAdapter(myAdapter);

        myAdapter.updateAdapter(notesList);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(getApplicationContext(), NoteEditorActivity.class);
                intent.putExtra("noteID", i);
                startActivity(intent);
            }
        });

        // Pop up  option to delete note
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {

                final int itemToDelete = i;

                new AlertDialog.Builder(MainActivity.this)
                        .setMessage("Do You Want To Delete This Note?")
                        .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                db.deleteNote(notesList.get(itemToDelete));
                                notesList = db.getAllNotes();
                                myAdapter.updateAdapter(notesList);
                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .show();

                return true;
            }
        }); // setOnItemLongClickListener() end


    } // onCreate() end

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.add_spinner_main, menu);
        MenuItem item = menu.findItem(R.id.mainSpinner);
        Spinner spinner = (Spinner) MenuItemCompat.getActionView(item);

        String[] spinnerItems = getResources().getStringArray(R.array.mainSpinnerArray);
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(MainActivity.this,
                android.R.layout.simple_spinner_dropdown_item, spinnerItems);

        spinner.setAdapter(spinnerAdapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                switch (adapterView.getItemAtPosition(i).toString()) {
                    case "All Notes":
                        listView.invalidateViews();
                        notesList = db.getAllNotes();
                        myAdapter.updateAdapter(notesList);
                        break;

                    case "Favorites":
                        listView.invalidateViews();
                        notesList = db.getAllFavoriteNotes();
                        myAdapter.updateAdapter(notesList);
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });


        return super.onCreateOptionsMenu(menu);
    }


    // Not doing what i wanted (Refresh listView)
    @Override
    public void onStart() {
        super.onStart();

        Toast toast1 = Toast.makeText(getApplicationContext(), "Number of notes: " + db.getNotesCount(), Toast.LENGTH_SHORT);
        toast1.show();

        Intent intent = getIntent();
        if (intent.getIntExtra("Note Success", -1) == 0) {
            Toast toast = Toast.makeText(getApplicationContext(), "No content to save. Note discarded", Toast.LENGTH_SHORT);
            toast.show();
        }

        getIntent().removeExtra("Note Success");

        // TODO: Instead of getting all the notes, add the new note to notesList at the front
        if (db.getNotesCount() != notesList.size()) {
            notesList = db.getAllNotes();
        }

        if (listView != null) {
            listView.invalidateViews();
        }

        myAdapter.updateAdapter(notesList);

        List<SingleNote> temp = db.getAllFavoriteNotes();
        int size = temp.size();
        Toast toast = Toast.makeText(getApplicationContext(), "# of favorite notes: " + size, Toast.LENGTH_SHORT);
        toast.show();

    } // onStart() end

}
