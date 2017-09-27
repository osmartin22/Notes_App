package com.ozmar.notes;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

// TODO: 1) Use AsyncTask for db read/write instead of Main thread
// TODO: 2) Slide to delete
// TODO: 3) On long press, allow multiple deletes and show delete button
// TODO: 4) Optimize currentList towards the end

// TODO Possibly) Add GPS so that a notification appears/or vibrate phone when at location
// TODO Possibly) Let user choose theme (Maybe do in shared preferences)

public class MainActivity extends AppCompatActivity {
    private ListView listView;
    private NotesAdapter myAdapter;
    private Spinner mainSpinner;
//    private int spinnerPositionOnRestore = -1;

    static DatabaseHandler db;
    static List<SingleNote> currentList;

    SharedPreferences settings;

    // onClick method launch activity to create note
    public void launchNoteEditor(View view) {
        Intent intent = new Intent(this.getApplicationContext(), NoteEditorActivity.class);
        intent.putExtra("noteID", -1);
        startActivity(intent);
    } // launchNoteEditor() end

    private void getNotesList(int num) {
        switch (num) {
            case 0:
                if(currentList.size() != db.getNotesCount()) {
                    currentList = db.getAllNotes();
                }
                break;
            case 1:
                currentList = db.getAllFavoriteNotes();
                break;
        }
    }

    // Get spinner position from SharedPreferences
    private int getSpinnerPosition() {
        settings = getSharedPreferences("User Settings", Context.MODE_PRIVATE);
        return settings.getInt("Spinner Position", 0);
    }

    // Store spinner position from SharedPreferences
    private void storeSpinnerPosition() {
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt("Spinner Position", mainSpinner.getSelectedItemPosition()).apply();
    }

    // Set spinner position from SharedPreferences
    private void setSpinnerPosition() {
        int position = getSpinnerPosition();
        if (position != -1) {
            mainSpinner.setSelection(position);
            getNotesList(position);
            myAdapter.updateAdapter(currentList);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);


        db = new DatabaseHandler(MainActivity.this);
        currentList = new ArrayList<>();
        getNotesList(getSpinnerPosition());

        myAdapter = new NotesAdapter(this, R.layout.note_preview, currentList);
        listView = (ListView) findViewById(R.id.listVIew);
        listView.setAdapter(myAdapter);

        myAdapter.updateAdapter(currentList);
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
                                db.deleteNote(currentList.get(itemToDelete));
                                currentList.remove(itemToDelete);
                                myAdapter.updateAdapter(currentList);
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
        getMenuInflater().inflate(R.menu.add_spinner_main, menu);
        MenuItem item = menu.findItem(R.id.mainSpinner);

        mainSpinner = (Spinner) MenuItemCompat.getActionView(item);
        String[] spinnerItems = getResources().getStringArray(R.array.mainSpinnerArray);
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(MainActivity.this,
                android.R.layout.simple_spinner_dropdown_item, spinnerItems);
        mainSpinner.setAdapter(spinnerAdapter);

        getSpinnerPosition();
        setSpinnerPosition();

        mainSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                switch (adapterView.getItemAtPosition(i).toString()) {
                    case "All Notes":
                        currentList = db.getAllNotes();
                        break;

                    case "Favorites":
                        currentList = db.getAllFavoriteNotes();
                        break;
                }

                listView.invalidateViews();
                myAdapter.updateAdapter(currentList);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onStart() {
        super.onStart();

        Intent intent = getIntent();
        if (intent.getIntExtra("Note Success", -1) == 0) {
            Toast.makeText(getApplicationContext(), "No content to save. Note discarded", Toast.LENGTH_SHORT).show();
        }

        getIntent().removeExtra("Note Success");
    } // onStart() end


    @Override
    protected void onStop() {
        super.onStop();
        storeSpinnerPosition();
    }
}
