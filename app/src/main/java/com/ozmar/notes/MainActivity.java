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
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.ozmar.notes.utils.MainActivityHelper;
import com.ozmar.notes.utils.MenuItemHelper;

import java.util.ArrayList;
import java.util.List;

import static android.support.v7.widget.DividerItemDecoration.HORIZONTAL;
import static android.support.v7.widget.DividerItemDecoration.VERTICAL;

// TODO: Save status of SnackBar in case app is force closed and contextualItem action was not completed
// TODO: (CONT) complete action on startUp, or show message
// TODO: (CONT) Or let user do the action again (annoying to do for user)

// TODO: Add favorite button for multi select

// TODO: Allow multi select on orientation change
// TODO: (CONT) Check if any of the buffers are not empty, check multi select flag

// TODO: Make db read/write into AsyncTask

// TODO: Fix delay from user touch to when view changes background color

// TODO: Fix toast message in Trash editor

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private NotesAdapter notesAdapter;
    private RecyclerView rv;

    static DatabaseHandler db;
    private List<SingleNote> currentList = new ArrayList<>();

    private ActionMode actionMode;
    private boolean multiSelectFlag;
    private MenuItem cabItemSelected;
    private MultiSelectBuffer buffer = new MultiSelectBuffer();

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

    private int editorAction = -1;
    private boolean noteEditorAction = false;

    private MainActivityHelper mainActivityHelper;
    private MenuItemHelper itemHelper;

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

        // TODO: Temporary solution
        DividerItemDecoration decoration = new DividerItemDecoration(getApplicationContext(), VERTICAL);
        DividerItemDecoration decoration2 = new DividerItemDecoration(getApplicationContext(), HORIZONTAL);
        rv.addItemDecoration(decoration);
        rv.addItemDecoration(decoration2);
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

                    // Only allow multi select if a buffer is available to hold the data
                    if (buffer.isBufferAvailable()) {
                        buffer.swapBuffer();
                        ((AppCompatActivity) view.getContext()).startSupportActionMode(actionModeCallback);
                        multiSelect(currentList.get(position), position);
                    } else {
                        // TODO: Possibly Modify later
                        Toast.makeText(getApplicationContext(),
                                "Please wait while previous selections are completed.", Toast.LENGTH_SHORT).show();
                    }
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
        itemHelper = new MenuItemHelper(getApplicationContext(), db);

        drawer = (DrawerLayout) findViewById(R.id.drawerLayout);
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
            notesAdapter.removeSelectedViews(buffer.currentBufferPositions());
            showSnackBar(item, 0);
            mode.finish();
        }

        @Override
        public int hashCode() {
            return super.hashCode();
        }

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            actionMode = mode;
            multiSelectFlag = true;
            mode.getMenuInflater().inflate(R.menu.contextual_action_menu, menu);
            itemHelper.setCABMenuItems(menu, mainActivityHelper.getListUsed());
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
                    cabItemSelected = item;
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
                buffer.clearCurrentBuffer();
                notesAdapter.notifyDataSetChanged();
            }

            menuItemPressed = false;
        }
    }; // ActionMode.Callback() end

    private void deleteForever(MenuItem item) {
        String message = itemHelper.multiSelectMessage(item, buffer.currentBufferSize());
        new AlertDialog.Builder(MainActivity.this)
                .setMessage(message)
                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        db.deleteListFromRecycleBin(buffer.currentBufferNotes());
                        notesAdapter.removeSelectedViews(buffer.currentBufferPositions());
                        actionMode.finish();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showSnackBar(final MenuItem item, int num) {
        String message;
        if (noteEditorAction) {
            message = itemHelper.noteEditorMessage(num);
        } else {
            message = itemHelper.multiSelectMessage(item, buffer.currentBufferSize());
        }

        if (message != null) {
            snackBar = Snackbar.make(findViewById(R.id.coordinatorLayout), message, Snackbar.LENGTH_LONG);
        }

        if (snackBar != null) {
            snackBar.setAction(R.string.snackBarUndoAction, new UndoListener());
            snackBar.show();
            snackBar.addCallback(new Snackbar.Callback() {
                @Override
                public void onDismissed(Snackbar s, int event) {

                    if (!undoClickFlag) {           // Undo not pressed in SnackBar

                        if (snackBarForceClosed) {  //

                            if (noteEditorAction) {
                                itemHelper.doEditorAction(editorAction, buffer.otherBuffer().getNotes(),
                                        mainActivityHelper.getListUsed());
                            } else {
                                itemHelper.doCABAction(cabItemSelected, buffer.otherBuffer().getNotes(),
                                        mainActivityHelper.getListUsed());
                            }
                            snackBarForceClosed = false;
                            buffer.clearOtherBuffer();

                            // Any action that dismisses SnackBar that is not another multi select operation
                        } else {
                            if (noteEditorAction) {

                                itemHelper.doEditorAction(editorAction, buffer.currentBufferNotes(),
                                        mainActivityHelper.getListUsed());
                            } else {
                                itemHelper.doCABAction(cabItemSelected, buffer.currentBufferNotes(),
                                        mainActivityHelper.getListUsed());
                            }
                            buffer.clearCurrentBuffer();
                        }
                    }
                    undoClickFlag = false;
                    noteEditorAction = false;
                    snackBar = null;
                    notesAdapter.clearTempNotes();
                }
            });
        }
    } // showSnackBar() end

    private class UndoListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            undoClickFlag = true;
            snackBar = null;
            if (noteEditorAction) {
                notesAdapter.addSelectedViews(buffer.currentBufferPositions(),
                        buffer.currentBufferNotes());

            } else {
                notesAdapter.addSelectedViews(buffer.currentBufferPositions(), buffer.currentBufferNotes());
            }

            buffer.clearCurrentBuffer();
        }
    } // UndoListener() end

    private void multiSelect(SingleNote note, int position) {
        if (snackBar != null) {
            snackBarForceClosed = true;
            snackBar.dismiss();
            snackBar = null;
        }

        if (buffer.currentBufferPositions().contains(position)) {
            notesAdapter.removeSelectedId(position);
            buffer.removeDataFromPosition(position);

        } else {
            notesAdapter.addSelectedId(position);
            buffer.addDataToBuffer(note, position);
        }

        notesAdapter.notifyItemChanged(position);
        actionMode.setSubtitle(Integer.toString(buffer.currentBufferSize()));

        if (buffer.currentBufferSize() == 0) {
            actionMode.finish();
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
            Bundle bundle = data.getExtras();
            editorAction = bundle.getInt("menuAction", -1);
            int position = bundle.getInt("Note Position", -1);
            SingleNote note = bundle.getParcelable("Note");

            if (editorAction != -1) {
                noteEditorAction = true;
                buffer.addDataToBuffer(note, position);
                notesAdapter.removeSelectedViews(buffer.currentBufferPositions());
                showSnackBar(null, editorAction);

            } else {
                currentList = mainActivityHelper.updateAdapter(bundle, notesAdapter, currentList);
            }
        }

        getIntent().removeExtra("menuAction");
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