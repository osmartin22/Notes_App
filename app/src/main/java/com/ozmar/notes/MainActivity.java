package com.ozmar.notes;

import android.content.Context;
import android.content.DialogInterface;
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
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.ozmar.notes.utils.MainActivityHelper;

import java.util.ArrayList;
import java.util.List;

// TODO: Save status of SnackBar in case app is force closed and contextualItem action was not completed
// TODO: (CONT) complete action on startUp, or show message
// TODO: (CONT) Or let user do the action again (annoying to do for user)

// TODO: Add favorite button for multi select

// TODO: Move FAB along with SnackBar

// TODO: Allow multi select on orientation change
// TODO: (CONT) Check if any of the buffers are not empty, check multi select flag

// TODO: Make db read/write into AsyncTask

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private NotesAdapter notesAdapter;
    private RecyclerView rv;

    static DatabaseHandler db;
    private List<SingleNote> currentList = new ArrayList<>();

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

    private Snackbar snackBar = null;
    private boolean undoClickFlag = false;
    private boolean snackBarForceClosed = false;

    private MainActivityHelper mainActivityHelper;

    public void launchNoteEditor(View view) {
        if (actionMode != null) {
            actionMode.finish();
        }
        if (snackBar != null) {
            snackBar.dismiss();
        }

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
                    if (snackBar != null) {
                        snackBar.dismiss();
                    }

                    Intent intent = new Intent(getApplicationContext(), NoteEditorActivity.class);
                    intent.putExtra("noteID", position);
                    intent.putExtra("listUsed", mainActivityHelper.getListUsed());
                    intent.putExtra("Note", currentList.get(position));

                    startActivityForResult(intent, 1);
                }
            }

            public void onLongClickItem(View view, final int position) {
                if (!multiSelectFlag) {
                    selectedNotesBuffer.swapBuffer();
                    ((AppCompatActivity) view.getContext()).startSupportActionMode(actionModeCallback);
                    multiSelect(currentList.get(position), position);
                } else {
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

                if (snackBar != null) {
                    snackBar.dismiss();
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

            mainActivityHelper.setCABMenuItems(menu);

            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.contextualArchive:
                case R.id.contextualUnarchive:
                case R.id.contextualDelete:
                case R.id.contextualRestore:
                    removeViews(mode, item);
                    contextualItemSelected = item;
                    return true;
                case R.id.contextualDeleteForever:
                    actionMode = mode;
                    deleteForever(item);
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

            // Clear buffer if a CAB item was not pressed (i.e. adding note, open drawer, back pressed)
            if (!menuItemPressed) {
                selectedNotesBuffer.clearCurrentBuffer();
                notesAdapter.notifyDataSetChanged();
            }

            menuItemPressed = false;
        }
    }; // ActionMode.Callback() end

    private void deleteForever(MenuItem item) {
        String message = mainActivityHelper.messageToDisplay(item, selectedNotesBuffer.currentBufferSize());
        new AlertDialog.Builder(MainActivity.this)
                .setMessage(message)
                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        db.deleteListFromRecycleBin(selectedNotesBuffer.currentBufferNotes());
                        notesAdapter.removeSelectedViews(selectedNotesBuffer.currentBufferPositions());
                        actionMode.finish();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showSnackBar(MenuItem item) {
        String message = mainActivityHelper.messageToDisplay(item, selectedNotesBuffer.currentBufferSize());

        if (message != null) {
            snackBar = Snackbar.make(findViewById(R.id.drawer_layout), message, Snackbar.LENGTH_LONG);
        }

        if (snackBar != null) {
            snackBar.setAction(R.string.snackBarUndoAction, new UndoListener());
            snackBar.show();
            snackBar.addCallback(new Snackbar.Callback() {
                @Override
                public void onDismissed(Snackbar s, int event) {

                    if (!undoClickFlag) {           // Undo not pressed in SnackBar

                        // User started another multi select operation before previous one is finished
                        if (snackBarForceClosed) {
                            mainActivityHelper.doCABAction(contextualItemSelected, selectedNotesBuffer.otherBuffer().getNotes());
                            snackBarForceClosed = false;
                            selectedNotesBuffer.clearOtherBuffer();

                            // Any action that dismisses SnackBar that is not another multi select operation
                        } else {
                            mainActivityHelper.doCABAction(contextualItemSelected, selectedNotesBuffer.currentBufferNotes());
                            selectedNotesBuffer.clearCurrentBuffer();
                        }

                    } else {
                        undoClickFlag = false;
                    }

                    snackBar = null;
                }
            });
        }
    } // showSnackBar() end

    private class UndoListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            notesAdapter.addSelectedViews(selectedNotesBuffer.currentBufferPositions(), selectedNotesBuffer.currentBufferNotes());
            selectedNotesBuffer.clearCurrentBuffer();
            undoClickFlag = true;
            snackBar = null;
        }
    } // UndoListener() end

    private void multiSelect(SingleNote note, int position) {
        if (snackBar != null) {
            snackBarForceClosed = true;
            snackBar.dismiss();
            snackBar = null;
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
            currentList = mainActivityHelper.updateAdapter(data, notesAdapter, currentList);
        }

        getIntent().removeExtra("Note Favorite");
        getIntent().removeExtra("Note Success");
        getIntent().removeExtra("Note Position");
        getIntent().removeExtra("Note");

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