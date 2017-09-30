package com.ozmar.notes;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private NotesAdapter notesAdapter;
    private Spinner mainSpinner;

    static DatabaseHandler db;
    static List<SingleNote> currentList = new ArrayList<>();
    private int listUsed;

    private boolean multiSelect;
    private ActionMode actionMode;
    private RecyclerViewHelper selectedNotes = new RecyclerViewHelper();

    private SharedPreferences settings;

    // onClick method launch activity to create note
    public void launchNoteEditor(View view) {
        Intent intent = new Intent(this.getApplicationContext(), NoteEditorActivity.class);
        intent.putExtra("noteID", -1);
        intent.putExtra("listUsed", listUsed);
        startActivityForResult(intent, 1);
    } // launchNoteEditor() end

    // Get notes to initialize currentList
    private void getNotesList(int num) {
        switch (num) {
            case 0:
                currentList = db.getAllNotes();
                listUsed = 0;
                break;
            case 1:
                currentList = db.getAllFavoriteNotes();
                listUsed = 1;
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
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        Toast.makeText(getApplicationContext(), "onCreate()", Toast.LENGTH_SHORT).show();

        db = new DatabaseHandler(MainActivity.this);

        // Note: currentList will be updated with desired list in onCreateOptionsMenu()
        notesAdapter = new NotesAdapter(this, R.layout.note_preview, currentList);
        RecyclerView rv = (RecyclerView) findViewById(R.id.listVIew);
        rv.setHasFixedSize(true);

        //rv.setLayoutManager(new GridLayoutManager(this, 2));
        rv.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
//        rv.setLayoutManager(new LinearLayoutManager(this));

        rv.setAdapter(notesAdapter);

        rv.addOnItemTouchListener(new RecyclerItemListener(getApplicationContext(),
                rv, new RecyclerItemListener.RecyclerTouchListener() {
            public void onClickItem(View view, int position) {
                if (multiSelect) {
                    Toast.makeText(getApplicationContext(), "Selected: " + position, Toast.LENGTH_SHORT).show();
                    selectItem(view, currentList.get(position), position);

                } else {
                    Intent intent = new Intent(getApplicationContext(), NoteEditorActivity.class);
                    intent.putExtra("noteID", position);
                    intent.putExtra("listUsed", listUsed);
                    startActivityForResult(intent, 1);
                }
            }

            public void onLongClickItem(View view, final int position) {
                if (!multiSelect) {
                    ((AppCompatActivity) view.getContext()).startSupportActionMode(actionModeCallback);
                    selectItem(view, currentList.get(position), position);
                } else {
                    selectItem(view, currentList.get(position), position);
                }
            }
        }));

    } // onCreate() end

    private ActionMode.Callback actionModeCallback = new ActionMode.Callback() {
        @Override
        public int hashCode() {
            return super.hashCode();
        }

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            multiSelect = true;
            actionMode = mode;
            mode.getMenuInflater().inflate(R.menu.contextual_action_menu, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.contextualDelete:

                    // TODO : More testing
//                    db.deleteNoteList(selectedNotes.getNotes());
                    getNotesList(listUsed);
                    notesAdapter.removeSelectedViews(selectedNotes.getViews(), selectedNotes.getPositions());
                    mode.finish();
                    return true;
                default:
                    return false;
            }
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            multiSelect = false;
            actionMode = null;
            Toast.makeText(getApplicationContext(), "Size: " + selectedNotes.getNotes().size(), Toast.LENGTH_SHORT).show();
            selectedNotes.clearLists();

        }
    };

    private void selectItem(View view, SingleNote note, int position) {
        if (multiSelect) {
            if (selectedNotes.getNotes().contains(note)) {
                selectedNotes.removeFromLists(view, note);
                view.setBackgroundColor(Color.WHITE);

                if (selectedNotes.getNotes().isEmpty()) {      // Exit CAB if no notes are selected
                    actionMode.finish();
                }
            } else {
                view.setBackgroundColor(Color.GRAY);
                selectedNotes.addToLists(view, note, position);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.add_spinner_main, menu);
        MenuItem item = menu.findItem(R.id.mainSpinner);

        mainSpinner = (Spinner) MenuItemCompat.getActionView(item);
        String[] spinnerItems = getResources().getStringArray(R.array.mainSpinnerArray);
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(MainActivity.this,
                android.R.layout.simple_spinner_dropdown_item, spinnerItems);
        mainSpinner.setAdapter(spinnerAdapter);

        restoreSpinnerPosition();   // Restore spinner position and update currentList with the desired list

        mainSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                currentList.clear();
                notesAdapter.clearView();
                switch (adapterView.getItemAtPosition(i).toString()) {
                    case "All Notes":
                        currentList.addAll(db.getAllNotes());
                        notesAdapter.getList(currentList);
                        listUsed = 0;
                        break;

                    case "Favorites":
                        currentList.addAll(db.getAllFavoriteNotes());
                        notesAdapter.getList(currentList);
                        listUsed = 1;
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && data != null) {
            int save = data.getIntExtra("Note Success", -1);
            int position = data.getIntExtra("Note Position", -1);

            if (save == 0) {
                Toast.makeText(getApplicationContext(), "No content to save. Note discarded.", Toast.LENGTH_SHORT).show();

            } else if (save == 1) {
                notesAdapter.updateAt(position, currentList.get(position));

            } else if (save == 3) {
                if (listUsed == 0) {
                    notesAdapter.addAt(position, currentList.get(position));
                } else if (listUsed == 1 && currentList.get(0).get_favorite() == 1) {
                    notesAdapter.addAt(position, currentList.get(position));
                } else {
                    currentList.remove(0);  // No need to update RecyclerView as new note does not belong to the list
                }

            } else if (save == 4) {
                currentList.remove(position);
                notesAdapter.removeAt(position);

            } else if (save == 5) {
                if (listUsed == 1) {
                    currentList.remove(position);
                    notesAdapter.removeAt(position);
                }
            }

            getIntent().removeExtra("Note Success");
            getIntent().removeExtra("Note Position");
        }
    } // onActivityResult() end

    @Override
    protected void onStop() {
        super.onStop();
        storeSpinnerPosition();
    }
} // MainActivity() end
