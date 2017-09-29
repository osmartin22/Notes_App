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
import android.util.Log;
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

// NOTE: Not using _id for notes, might take it off as unique ids do not seem necessary

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
    static List<SingleNote> currentList = new ArrayList<>();

    private SharedPreferences settings;

    // onClick method launch activity to create note
    public void launchNoteEditor(View view) {
        Intent intent = new Intent(this.getApplicationContext(), NoteEditorActivity.class);
        intent.putExtra("noteID", -1);
        startActivityForResult(intent, 1);
    } // launchNoteEditor() end

    // Get notes to initialize currentList
    private void getNotesList(int num) {
        switch (num) {
            case 0:
                currentList = db.getAllNotes();
                break;
            case 1:
                currentList = db.getAllFavoriteNotes();
                break;
        }
    } // getNotesList() end

    // Store spinner position from SharedPreferences
    private void storeSpinnerPosition() {
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt("Spinner Position", mainSpinner.getSelectedItemPosition()).apply();
    }

    // Restore spinner position from SharedPreferences
    private void restoreSpinnerPosition() {
        settings = getSharedPreferences("User Settings", Context.MODE_PRIVATE);
        int position = settings.getInt("Spinner Position", 0);

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

        Toast.makeText(getApplicationContext(), "onCreate()", Toast.LENGTH_SHORT).show();

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        db = new DatabaseHandler(MainActivity.this);

        // Note: currentList will be updated with desired list in onCreateOptionsMenu()
        notesAdapter = new NotesAdapter(this, R.layout.note_preview, currentList);
        rv = (RecyclerView) findViewById(R.id.listVIew);
        rv.setHasFixedSize(true);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(notesAdapter);

        rv.addOnItemTouchListener(new RecyclerItemListener(getApplicationContext(),
                rv, new RecyclerItemListener.RecyclerTouchListener() {
            public void onClickItem(View view, int position) {
                Intent intent = new Intent(getApplicationContext(), NoteEditorActivity.class);
                intent.putExtra("noteID", position);
                startActivityForResult(intent, 1);
            }

            public void onLongClickItem(View view, final int position) {
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

        // Restore spinner position and update currentList with the desired list
        restoreSpinnerPosition();

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
//        Intent intent = getIntent();
//        long save = intent.getIntExtra("Note Success", -1);
//        int position = intent.getIntExtra("Note Position", -1);
//
//        Log.d("Note Success", Long.toString(save));
//        Log.d("Note Position", Integer.toString(position));
//
//        if (save == 0) {
//            Toast.makeText(getApplicationContext(), "Discard Note", Toast.LENGTH_SHORT).show();
//        } else if (save == 1) {
//            Toast.makeText(getApplicationContext(), "Content Changed", Toast.LENGTH_SHORT).show();
//            notesAdapter.notifyItemChanged(position);
//        }  else if (save == 2) {
//            Toast.makeText(getApplicationContext(), "No change existing note", Toast.LENGTH_SHORT).show();
//        } else if (save == 3) {
//            Toast.makeText(getApplicationContext(), "New note", Toast.LENGTH_SHORT).show();
//            notesAdapter.addAt(position, currentList.get(position));
//        } else if (save == 4) {
//            Toast.makeText(getApplicationContext(), "Note deleted", Toast.LENGTH_SHORT).show();
//            currentList.remove(position);
//            notesAdapter.removeAt(position);
//        }
//
//        getIntent().removeExtra("Note Success");
//        getIntent().removeExtra("Note Position");
    } // onStart() end

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if (requestCode == 1 && data != null) {

            int save = data.getIntExtra("Note Success", -1);
            int position = data.getIntExtra("Note Position", -1);

            Log.d("Note Success", Integer.toString(save));
            Log.d("Note Position", Integer.toString(position));

            if (save == 0) {
                Toast.makeText(getApplicationContext(), "Discard Note", Toast.LENGTH_SHORT).show();
            } else if (save == 1) {
                Toast.makeText(getApplicationContext(), "Content Changed", Toast.LENGTH_SHORT).show();
                notesAdapter.updateAt(position, currentList.get(position));
            } else if (save == 2) {
                Toast.makeText(getApplicationContext(), "No change existing note", Toast.LENGTH_SHORT).show();
            } else if (save == 3) {

                // TODO: Possible optimize later
                if (currentList.equals(db.getAllNotes())) {
                    notesAdapter.addAt(position, currentList.get(position));
                } else if (currentList.equals(db.getAllFavoriteNotes()) && currentList.get(0).get_favorite() == 1) {
                    notesAdapter.addAt(position, currentList.get(position));
                } else {
                    currentList.remove(0);
                }

                Toast.makeText(getApplicationContext(), "New note", Toast.LENGTH_SHORT).show();
            } else if (save == 4) {
                Toast.makeText(getApplicationContext(), "Note deleted", Toast.LENGTH_SHORT).show();
                currentList.remove(position);
                notesAdapter.removeAt(position);
            }

            getIntent().removeExtra("Note Success");
            getIntent().removeExtra("Note Position");

        }

    }

    @Override
    protected void onStop() {
        super.onStop();
        storeSpinnerPosition();
    }
}
