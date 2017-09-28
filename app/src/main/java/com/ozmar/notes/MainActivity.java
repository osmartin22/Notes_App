package com.ozmar.notes;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
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
    private RecyclerView rv;
    private NotesAdapter notesAdapter;
    private Spinner mainSpinner;

    static DatabaseHandler db;
    static List<SingleNote> currentList;

    SharedPreferences settings;

    // onClick method launch activity to create note
    public void launchNoteEditor(View view) {
        Intent intent = new Intent(this.getApplicationContext(), NoteEditorActivity.class);
        intent.putExtra("noteID", -1);
        startActivity(intent);
    } // launchNoteEditor() end

    // Get notes to display on screen
    // TODO: Optimize, right now currentList might get the same list from db
    private void getNotesList(int num) {
        switch (num) {
            case 0:
                currentList = db.getAllNotes();
                break;
            case 1:
                currentList = db.getAllFavoriteNotes();
                break;
        }
    }

    // Store spinner position from SharedPreferences
    private void storeSpinnerPosition() {
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt("Spinner Position", mainSpinner.getSelectedItemPosition()).apply();
    }

    // Get spinner position from SharedPreferences
    private int getSpinnerPosition() {
        settings = getSharedPreferences("User Settings", Context.MODE_PRIVATE);
        return settings.getInt("Spinner Position", 0);
    }

    // Set spinner position from SharedPreferences
    private void setSpinnerPosition(int position) {
        if (position != -1) {
            mainSpinner.setSelection(position);
            getNotesList(position);
            notesAdapter.notifyDataSetChanged();
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

        notesAdapter = new NotesAdapter(this, R.layout.note_preview, currentList);
        rv = (RecyclerView) findViewById(R.id.listVIew);
        rv.setHasFixedSize(true);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(notesAdapter);

        rv.addOnItemTouchListener(new RecyclerItemListener(getApplicationContext(),
                rv, new RecyclerItemListener.RecyclerTouchListener() {
            public void onClickItem(View view, int position) {
                Toast.makeText(getApplicationContext(), "Short Press", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getApplicationContext(), NoteEditorActivity.class);
                intent.putExtra("noteID", position);
                startActivity(intent);
            }

            public void onLongClickItem(View view, final int position) {
                Toast.makeText(getApplicationContext(), "Long Press", Toast.LENGTH_SHORT).show();
                new AlertDialog.Builder(MainActivity.this)
                        .setMessage("Do You Want To Delete This Note?")
                        .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                db.deleteNote(currentList.get(position));
                                currentList.remove(position);
                                rv.removeViewAt(position);

                                notesAdapter.removeAt(position);
                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            }
        }));

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

        setSpinnerPosition(getSpinnerPosition());   // Restore spinner position

        mainSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                currentList.clear();
                notesAdapter.notifyItemRangeRemoved(0, currentList.size());
                switch (adapterView.getItemAtPosition(i).toString()) {
                    case "All Notes":
                        notesAdapter.getList(db.getAllNotes());
                        currentList.addAll(db.getAllNotes());
                        break;

                    case "Favorites":
                        notesAdapter.getList(db.getAllFavoriteNotes());
                        currentList.addAll(db.getAllFavoriteNotes());
                        break;
                }

//

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
