package com.ozmar.notes;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.ozmar.notes.utils.MainActivityHelper;

import java.util.ArrayList;
import java.util.List;

// TODO: Save status of SnackBar in case app is force closed and contextualItem action was not completed
// TODO: (CONT) complete action on startUp, or show message

// TODO: Close SnackBar when user adds a note
// TODO: Move FAB along with SnackBar

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private NotesAdapter notesAdapter;
    private RecyclerView rv;

    static DatabaseHandler db;
    static List<SingleNote> currentList = new ArrayList<>();    // TODO: pass/return note instead and make currentList private

    private ActionMode actionMode;
    private boolean multiSelectFlag;
    private MenuItem contextualItemSelected;
    private MultiSelectBuffer selectedNotesBuffer = new MultiSelectBuffer();

    private int layoutChoice;
    private MenuItem layoutItem;
    private SharedPreferences settings;

    private MenuItem currentNavigationMenuItem;

    private Toolbar myToolbar;
    private DrawerLayout drawer;
    private FloatingActionButton fab;

    private Snackbar snackbar = null;
    private boolean undoClickFlag = false;
    private boolean snackBarForceClosed = false;

    private MainActivityHelper mainActivityHelper;

    public void launchNoteEditor(View view) {
        Intent intent = new Intent(this.getApplicationContext(), NoteEditorActivity.class);
        intent.putExtra("noteID", -1);
        intent.putExtra("listUsed", mainActivityHelper.getListUsed());
        startActivityForResult(intent, 1);
    } // launchNoteEditor() end

    private void setUpRecyclerView() {
        currentList = mainActivityHelper.getNotesList(0);
        notesAdapter = new NotesAdapter(currentList);

        restoreLayoutChoice();
        rv = (RecyclerView) findViewById(R.id.recyclerView);
        rv.setHasFixedSize(true);
        rv.setAdapter(notesAdapter);

        rv.addOnItemTouchListener(new RecyclerItemListener(getApplicationContext(),
                rv, new RecyclerItemListener.RecyclerTouchListener() {
            public void onClickItem(View view, int position) {
                if (multiSelectFlag) {
                    multiSelect(currentList.get(position), position);

                } else {
                    Intent intent = new Intent(getApplicationContext(), NoteEditorActivity.class);
                    intent.putExtra("noteID", position);
                    intent.putExtra("listUsed", mainActivityHelper.getListUsed());
                    startActivityForResult(intent, 1);
                }
            }

            public void onLongClickItem(View view, final int position) {
                if (!multiSelectFlag) {
                    Log.d("Buffer", "Start multi select");
                    selectedNotesBuffer.swapBuffer();
                    ((AppCompatActivity) view.getContext()).startSupportActionMode(actionModeCallback);
                    multiSelect(currentList.get(position), position);
                } else {
                    Log.d("Buffer", "Still multi select (long press)");
                    multiSelect(currentList.get(position), position);
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
        mainActivityHelper = new MainActivityHelper(getApplicationContext(), db);

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, myToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {

            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                super.onDrawerSlide(drawerView, slideOffset);
                if (multiSelectFlag) {
                    multiSelectFlag = false;
                    actionMode.finish();
                }
            }
        };

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
    } // onBackPressed() end

    private ActionMode.Callback actionModeCallback = new ActionMode.Callback() {
        boolean menuItemPressed = false;

        private void removeViews(ActionMode mode, MenuItem item) {
            menuItemPressed = true;
            notesAdapter.removeSelectedViews(selectedNotesBuffer.currentBufferPositions());
            showSnackBar(item);
            mode.finish();
        }

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
                    removeViews(mode, item);
                    contextualItemSelected = item;
                    return true;

                case R.id.contextualArchive:
                    removeViews(mode, item);
                    contextualItemSelected = item;
                    return true;

                default:
                    return false;
            }
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            multiSelectFlag = false;
            actionMode = null;
            notesAdapter.clearSelectedIds();

            if (!menuItemPressed) {         // Only clear lists if a menu item in CAB was not pressed
                selectedNotesBuffer.clearCurrentBuffer();
                notesAdapter.notifyDataSetChanged();
            }

            menuItemPressed = false;
        }
    }; // ActionMode.Callback() end

    private void showSnackBar(MenuItem item) {
        int size = selectedNotesBuffer.currentBufferSize();
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
                public void onDismissed(Snackbar s, int event) {

                    if (!undoClickFlag) {                        // Undo not pressed in SnackBar
                        // Do action in db then clear respective list
                        // use contextualItemSelected to decide action

                        if (snackBarForceClosed) {
                            Log.d("Buffer", "SnackBar force closed");
                            snackBarForceClosed = false;
                            selectedNotesBuffer.clearOtherBuffer();
                        } else {
                            Log.d("Buffer", "SnackBar timeout");
                            selectedNotesBuffer.clearCurrentBuffer();
                        }

                    } else {
                        Log.d("Buffer", "onDismiss Undo");
                        undoClickFlag = false;
                    }

                    snackbar = null;
                }
            });
        }
    } // showSnackBar() end

    private class UndoListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            Log.d("Buffer", "Undo Pressed");
            notesAdapter.addSelectedViews(selectedNotesBuffer.currentBufferPositions(), selectedNotesBuffer.currentBufferNotes());
            selectedNotesBuffer.clearCurrentBuffer();
            undoClickFlag = true;
            snackbar = null;
        }
    } // UndoListener() end

    private void multiSelect(SingleNote note, int position) {
        if (snackbar != null) {
            snackBarForceClosed = true;
            snackbar.dismiss();
            snackbar = null;
        }

        if (multiSelectFlag) {
            if (selectedNotesBuffer.currentBufferPositions().contains(position)) {
                selectedNotesBuffer.removeDataFromPosition(position);
                notesAdapter.removeSelectedId(position);

            } else {
                selectedNotesBuffer.addDataToBuffer(note, position);
                notesAdapter.addSelectedId(position);
            }

            notesAdapter.notifyItemChanged(position);
            actionMode.setSubtitle(Integer.toString(selectedNotesBuffer.currentBufferSize()));
            if (selectedNotesBuffer.currentBufferSize() == 0) {
                actionMode.finish();
            }
        }
    } // multiSelect() end

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_activity_menu, menu);
        layoutItem = menu.findItem(R.id.layout);
        mainActivityHelper.restoreLayout(rv, layoutItem, layoutChoice);
        return super.onCreateOptionsMenu(menu);
    } // onCreateOptionsMenu() end

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        switch (item.getItemId()) {
            case R.id.layout:
                layoutChoice = mainActivityHelper.swapLayout(rv, layoutItem, layoutChoice);
                return true;
        }

        return false;
    } // onOptionsItemSelected() end

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        if (currentNavigationMenuItem != item) {
            int id = item.getItemId();
            notesAdapter.clearView();

            switch (id) {
                case R.id.all_notes_drawer:
                default:
                    myToolbar.setTitle("Notes");
                    currentList = mainActivityHelper.getNotesList(0);
                    fab.show();
                    break;

                case R.id.favorite_notes_drawer:
                    myToolbar.setTitle("Favorite Notes");
                    currentList = mainActivityHelper.getNotesList(1);
                    fab.show();
                    break;

                case R.id.archive_drawer:
                    myToolbar.setTitle("Archive");
                    currentList = mainActivityHelper.getNotesList(2);
                    fab.hide();
                    break;

                case R.id.recycle_bin_drawer:
                    myToolbar.setTitle("Trash");
                    currentList = mainActivityHelper.getNotesList(3);
                    fab.hide();
                    break;
            }

            notesAdapter.getList(currentList);
            notesAdapter.notifyDataSetChanged();
        }

        currentNavigationMenuItem = item;
        drawer.closeDrawer(GravityCompat.START);
        return true;
    } // onNavigationItemSelected() end

    // Get result from NoteEditorActivity, decide how to update RecyclerView
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && data != null) {
            mainActivityHelper.updateAdapter(data, notesAdapter, currentList);
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