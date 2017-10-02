package com.ozmar.notes;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
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
    private RecyclerView rv;
    private RecyclerView.LayoutManager staggeredGridLayout;
    private RecyclerView.LayoutManager linearLayout;

    static DatabaseHandler db;
    static List<SingleNote> currentList = new ArrayList<>();
    private int listUsed;

    private boolean multiSelect;
    private ActionMode actionMode;
    private RecyclerViewHelper selectedNotes = new RecyclerViewHelper();

    private SharedPreferences settings;
    private int layoutChoice;
    private MenuItem layoutItem;

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
                currentList = db.getAllNotesFromUserList();
                listUsed = 0;
                break;
            case 1:
                currentList = db.getAllFavoriteNotes();
                listUsed = 1;
                break;
        }
    } // getNotesList() end

    private void setUpRecyclerView() {
        // Note: currentList will be updated with desired list in onCreateOptionsMenu()
        notesAdapter = new NotesAdapter(this, R.layout.note_preview, currentList);

        restoreLayoutChoice();
        staggeredGridLayout = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        linearLayout = new LinearLayoutManager(this);
        rv = (RecyclerView) findViewById(R.id.listVIew);
        rv.setHasFixedSize(true);
//        rv.setLayoutManager(staggeredGridLayout);
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

        setUpRecyclerView();

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
                    db.deleteNoteList(selectedNotes.getNotes());
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
            notesAdapter.setToWhite(selectedNotes.getViews());
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

    // Swap RecyclerView layout
    private void swapLayout() {
        switch (layoutChoice) {
            case 0:
                layoutChoice = 1;
                layoutItem.setIcon(R.drawable.ic_linear_layout);
                rv.setLayoutManager(linearLayout);
                break;
            case 1:
                layoutChoice = 0;
                layoutItem.setIcon(R.drawable.ic_staggered_grid_layout);
                rv.setLayoutManager(staggeredGridLayout);
                break;
        }
    } // swapLayout() end

    private void restoreLayout() {
        if (layoutChoice == 0) {
            rv.setLayoutManager(staggeredGridLayout);
        } else if (layoutChoice == 1) {
            rv.setLayoutManager(linearLayout);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_activity_menu, menu);
        layoutItem = menu.findItem(R.id.layout);
        MenuItem item = menu.findItem(R.id.mainSpinner);

        mainSpinner = (Spinner) MenuItemCompat.getActionView(item);
        String[] spinnerItems = getResources().getStringArray(R.array.mainSpinnerArray);
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(MainActivity.this,
                android.R.layout.simple_spinner_dropdown_item, spinnerItems);
        mainSpinner.setAdapter(spinnerAdapter);

        restoreSpinnerPosition();   // Restore spinner position and update currentList with the desired list
        restoreLayout();     // Restore previous layout choice

        mainSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                currentList.clear();
                notesAdapter.clearView();
                switch (adapterView.getItemAtPosition(i).toString()) {
                    case "All Notes":
                        currentList.addAll(db.getAllNotesFromUserList());
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
    } // onCreateOptionsMenu() end

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        switch (item.getItemId()) {
            case R.id.layout:
                swapLayout();
                return true;
        }

        return false;
    } // onOptionsItemSelected() end

    // Get result from NoteEditorActivity, decide how to update RecyclerView
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && data != null) {
            Bundle bundle = data.getExtras();

            // Array used to check NoteEditor outcome
            String[] noteResult = getResources().getStringArray(R.array.noteResultArray);

            String save = bundle.getString("Note Success", "");
            int position = bundle.getInt("Note Position", -1);
            boolean favorite = bundle.getBoolean("Note Favorite", false);

            if (save.equals(noteResult[0])) {
                Toast.makeText(getApplicationContext(), "No content to save. Note discarded.", Toast.LENGTH_SHORT).show();

            } else if (save.equals(noteResult[1])) {
                notesAdapter.updateAt(position, currentList.get(position));

            } else if (save.equals(noteResult[3])) {
                getNotesList(listUsed);

                if (listUsed == 0) {
                    notesAdapter.addAt(position, currentList.get(position));
                } else if (listUsed == 1 && favorite) {
                    notesAdapter.addAt(position, currentList.get(position));    // Display new note if it was set to favorite
                }

            } else if (save.equals(noteResult[4])) {
                currentList.remove(position);
                notesAdapter.removeAt(position);

            } else if (save.equals(noteResult[5])) {
                if (listUsed == 1) {
                    currentList.remove(position);
                    notesAdapter.removeAt(position);
                }
            }

            getIntent().removeExtra("Note Favorite");
            getIntent().removeExtra("Note Success");
            getIntent().removeExtra("Note Position");
        }
    } // onActivityResult() end

    @Override
    protected void onStop() {
        super.onStop();
        storeSharedPreferences();
    }

    private void storeSharedPreferences() {
        Log.d("SP", layoutChoice + "");

        SharedPreferences.Editor editor = settings.edit();
        editor.putInt("Spinner Position", mainSpinner.getSelectedItemPosition());
        editor.putInt("Layout Choice", layoutChoice);
        editor.apply();
    }

    private void restoreLayoutChoice() {
        settings = getSharedPreferences("User Settings", Context.MODE_PRIVATE);
        layoutChoice = settings.getInt("Layout Choice", 0);
        Log.d("SPR", layoutChoice + "");
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

} // MainActivity() end
