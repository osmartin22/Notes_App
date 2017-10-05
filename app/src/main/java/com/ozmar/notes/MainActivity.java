package com.ozmar.notes;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
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
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

// TODO: swap selectedItems for currentBuffer().size()

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private NotesAdapter notesAdapter;
    private RecyclerView rv;
    private RecyclerView.LayoutManager staggeredGridLayout;
    private RecyclerView.LayoutManager linearLayout;

    static DatabaseHandler db;
    static List<SingleNote> currentList = new ArrayList<>();    // TODO: pass/return note instead and make currentList private
    private int listUsed;

    private boolean multiSelectFlag;
    private int selectedItems = 0;
    private ActionMode actionMode;
    private MultiSelectBuffer selectedNotesBuffer = new MultiSelectBuffer();

    private SharedPreferences settings;
    private int layoutChoice;
    private MenuItem layoutItem;

    private MenuItem currentNavigationMenuItem;

    private DrawerLayout drawer;
    private FloatingActionButton fab;
    private Toolbar myToolbar;

    Snackbar snackbar = null;
    private boolean snackBarFlag = false;

    public void launchNoteEditor(View view) {
        Intent intent = new Intent(this.getApplicationContext(), NoteEditorActivity.class);
        intent.putExtra("noteID", -1);
        intent.putExtra("listUsed", listUsed);
        startActivityForResult(intent, 1);
    } // launchNoteEditor() end

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

            case 2:
                currentList = db.getArchiveNotes();
                listUsed = 2;
                break;

            case 3:
                currentList = db.getRecycleBinNotes();
                listUsed = 3;
        }
    } // getNotesList() end

    private void setUpRecyclerView() {
        // Note: currentList will be updated with desired list in onCreateOptionsMenu()
        getNotesList(0);
        notesAdapter = new NotesAdapter(currentList);
        restoreLayoutChoice();
        staggeredGridLayout = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        linearLayout = new LinearLayoutManager(this);
        rv = (RecyclerView) findViewById(R.id.recyclerView);
        rv.setHasFixedSize(true);
        rv.setAdapter(notesAdapter);

        rv.addOnItemTouchListener(new RecyclerItemListener(getApplicationContext(),
                rv, new RecyclerItemListener.RecyclerTouchListener() {
            public void onClickItem(View view, int position) {
                if (multiSelectFlag) {
                    multiSelect(view, currentList.get(position), position);

                } else {
                    Intent intent = new Intent(getApplicationContext(), NoteEditorActivity.class);
                    intent.putExtra("noteID", position);
                    intent.putExtra("listUsed", listUsed);
                    startActivityForResult(intent, 1);
                }
            }

            public void onLongClickItem(View view, final int position) {
                if (!multiSelectFlag) {
                    Log.d("Buffer", "Long Click Not Multi Select Flag");
                    selectedNotesBuffer.swapBuffer();
                    selectedItems = 0;
                    ((AppCompatActivity) view.getContext()).startSupportActionMode(actionModeCallback);
                    multiSelect(view, currentList.get(position), position);
                } else {
                    Log.d("Buffer", "Long Click Multi Select");
                    multiSelect(view, currentList.get(position), position);
                }
            }
        }));
    } // setUpRecyclerView() end

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        db = new DatabaseHandler(MainActivity.this);

        fab = (FloatingActionButton) findViewById(R.id.floatingActionButton);

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, myToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.getMenu().getItem(0).setChecked(true);

        setUpRecyclerView();

    } // onCreate() end

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    private ActionMode.Callback actionModeCallback = new ActionMode.Callback() {

        boolean menuItemPressed = false;

        @Override
        public int hashCode() {
            return super.hashCode();
        }

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            multiSelectFlag = true;
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
                    menuItemPressed = true;
                    notesAdapter.removeSelectedViews(selectedNotesBuffer.currentBufferViews(), selectedNotesBuffer.currentBufferPositions());
                    showSnackBar(item);
                    mode.finish();
                    return true;

                case R.id.contextualArchive:
                    menuItemPressed = true;
                    notesAdapter.removeSelectedViews(selectedNotesBuffer.currentBufferViews(), selectedNotesBuffer.currentBufferPositions());
                    showSnackBar(item);
                    mode.finish();

                default:
                    return false;
            }
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            Toast.makeText(getApplicationContext(), "Cleared", Toast.LENGTH_SHORT).show();
            multiSelectFlag = false;
            actionMode = null;
            notesAdapter.setToWhite(selectedNotesBuffer.currentBufferViews());

            // Only clear lists if a menu item in CAB was not pressed
            if(!menuItemPressed) {
                selectedNotesBuffer.clearCurrentBuffer();
            }

            selectedItems = 0;
            menuItemPressed = false;
        }
    };

    private void showSnackBar(MenuItem item) {
        int size = selectedNotesBuffer.currentBufferSize();
        Toast.makeText(getApplicationContext(), "Buffer Size: " + selectedNotesBuffer.currentBufferSize(), Toast.LENGTH_SHORT).show();
        String message;

        switch (item.getItemId()) {
            case R.id.contextualDelete:
                message = (size == 1) ? getString(R.string.snackBarDeleteSingle) : getString(R.string.snackBarDeleteMultiple);
                snackbar = Snackbar.make(findViewById(R.id.drawer_layout), message, Snackbar.LENGTH_LONG);
                break;
            case R.id.contextualArchive:
                message = (size == 1) ? getString(R.string.snackBarArchiveSingle) : getString(R.string.snackBarArchiveMultiple);
                snackbar = Snackbar.make(findViewById(R.id.drawer_layout), message, Snackbar.LENGTH_LONG);
                break;
        }

        if (snackbar != null) {
            snackbar.setAction(R.string.snackBarUndoAction, new UndoListener());
            snackbar.show();
            snackbar.addCallback(new Snackbar.Callback() {
                @Override
                public void onDismissed(Snackbar snackbar, int event) {
                    if(!snackBarFlag) {
                        Log.d("Buffer", "Long Click onDismissed");
                        // TODO: Do menu button press here
                        selectedNotesBuffer.clearOtherBuffer();
                        Toast.makeText(getApplicationContext(), "Removed", Toast.LENGTH_SHORT).show();
                    }

                    selectedNotesBuffer.clearCurrentBuffer();
                    snackBarFlag = false;
                }
            });
        }
    }

    public class UndoListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            Log.d("Buffer", "Undo");
            Toast.makeText(getApplicationContext(), "Undone", Toast.LENGTH_SHORT).show();
            notesAdapter.addSelectedViews(selectedNotesBuffer.currentBufferPositions(), selectedNotesBuffer.currentBufferNotes());
            snackBarFlag = true;
        }
    }

    // TODO: Set selectedItems to currentBufferSize()
    private void multiSelect(View view, SingleNote note, int position) {
        if(snackbar != null) {
            snackbar.dismiss();
        }
        if (multiSelectFlag) {

            if(selectedNotesBuffer.currentBufferNotes().contains(note)) {
                selectedNotesBuffer.removeDataFromBuffer(view,note);
                view.setBackgroundColor(Color.WHITE);
                selectedItems--;

            } else {
                view.setBackgroundColor(Color.GRAY);
                selectedNotesBuffer.addDataToBuffer(view, note, position);
                selectedItems++;
            }

            actionMode.setSubtitle(Integer.toString(selectedItems));

            if(selectedItems == 0) {
                actionMode.finish();
            }

        }
    }

    // Swap RecyclerView layout
    private void swapLayout() {
        switch (layoutChoice) {
            case 0:
                layoutChoice = 1;
                layoutItem.setIcon(R.drawable.ic_staggered_grid_layout);
                rv.setLayoutManager(linearLayout);
                break;
            case 1:
                layoutChoice = 0;
                layoutItem.setIcon(R.drawable.ic_linear_layout);
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

        restoreLayout();     // Restore previous layout choice

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

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        if (currentNavigationMenuItem != item) {

            int id = item.getItemId();

            currentList.clear();
            notesAdapter.clearView();
            switch (id) {
                case R.id.all_notes_drawer:
                    myToolbar.setTitle("Notes");
                    getNotesList(0);
                    fab.show();
                    break;

                case R.id.favorite_notes_drawer:
                    myToolbar.setTitle("Favorite Notes");
                    getNotesList(1);
                    fab.show();
                    break;

                case R.id.archive_drawer:
                    myToolbar.setTitle("Archive");
                    getNotesList(2);
                    fab.hide();
                    break;

                case R.id.recycle_bin_drawer:
                    myToolbar.setTitle("Trash");
                    getNotesList(3);
                    fab.hide();
                    break;

                default:
                    myToolbar.setTitle("Notes");
                    getNotesList(0);
                    fab.show();
            }

            notesAdapter.getList(currentList);
            notesAdapter.notifyDataSetChanged();
        }

        currentNavigationMenuItem = item;
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    // Get result from NoteEditorActivity, decide how to update RecyclerView
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && data != null) {

            // Array used to check NoteEditor outcome
            String[] noteResult = getResources().getStringArray(R.array.noteResultArray);

            Bundle bundle = data.getExtras();
            String save = bundle.getString("Note Success", "");
            int position = bundle.getInt("Note Position", -1);
            boolean favorite = bundle.getBoolean("Note Favorite", false);

            if (save.equals(noteResult[0])) {
                Toast.makeText(getApplicationContext(), "No content to save. Note discarded.", Toast.LENGTH_SHORT).show();

            } else if (save.equals(noteResult[1])) {    // Update rv with changes to the note
                notesAdapter.updateAt(position, currentList.get(position));

            } else if (save.equals(noteResult[3])) {    // Update rv with new note
                getNotesList(listUsed);

                if (listUsed == 0) {
                    notesAdapter.addAt(position, currentList.get(position));
                } else if (listUsed == 1 && favorite) {
                    notesAdapter.addAt(position, currentList.get(position));
                }

            } else if (save.equals(noteResult[4])) {    // Remove note from rv
                currentList.remove(position);
                notesAdapter.removeAt(position);

            } else if (save.equals(noteResult[5])) {    // Note title/content not modified but note is no longer a favorite
                if (listUsed == 1) {
                    currentList.remove(position);
                    notesAdapter.removeAt(position);
                }
            }
        }

        getIntent().removeExtra("Note Favorite");
        getIntent().removeExtra("Note Success");
        getIntent().removeExtra("Note Position");

    } // onActivityResult() end

    @Override
    protected void onStop() {
        super.onStop();
        storeSharedPreferences();
    }

    private void storeSharedPreferences() {
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt("Layout Choice", layoutChoice);
        editor.apply();
    }

    private void restoreLayoutChoice() {
        settings = getSharedPreferences("User Settings", Context.MODE_PRIVATE);
        layoutChoice = settings.getInt("Layout Choice", 0);
    }


} // MainActivity() end
