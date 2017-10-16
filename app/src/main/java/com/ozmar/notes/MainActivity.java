package com.ozmar.notes;

import android.content.DialogInterface;
import android.content.Intent;
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

import com.ozmar.notes.async.AutoDeleteAsync;
import com.ozmar.notes.async.BasicDBAsync;
import com.ozmar.notes.async.DoMenuActionAsync;
import com.ozmar.notes.async.NavMenuAsync;
import com.ozmar.notes.utils.MainActivityUtils;
import com.ozmar.notes.utils.MenuItemHelper;
import com.ozmar.notes.utils.MultiSelectFlagHelper;
import com.ozmar.notes.utils.UndoBuffer;

import java.util.ArrayList;
import java.util.List;

import static android.support.v7.widget.DividerItemDecoration.HORIZONTAL;
import static android.support.v7.widget.DividerItemDecoration.VERTICAL;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private NotesAdapter notesAdapter;
    private RecyclerView rv;

    static DatabaseHandler db;

    private ActionMode actionMode;
    private UndoBuffer buffer = new UndoBuffer();

    private int layoutChoice;
    private MenuItem layoutItem;
    private Preferences preferences;

    private Toolbar myToolbar;
    private DrawerLayout drawer;
    private FloatingActionButton fab;

    private Snackbar snackBar = null;

    private MultiSelectFlagHelper multiSelectHelper;
    private MenuItemHelper itemHelper;


    public void launchNoteEditor(View view) {
        if (actionMode != null) {
            actionMode.finish();
        }

        launchIntent(null, -1, notesAdapter.getListUsed());
    } // launchNoteEditor() end

    private void launchIntent(SingleNote note, int position, int listUsed) {
        if (snackBar != null) {
            snackBar.dismiss();
        }

        Intent intent = new Intent(this.getApplicationContext(), NoteEditorActivity.class);
        intent.putExtra("Note", note);
        intent.putExtra("noteID", position);
        intent.putExtra("listUsed", listUsed);
        startActivityForResult(intent, 1);
    } // launchIntent() end

    private void setUpRecyclerView() {
        notesAdapter = new NotesAdapter(getApplicationContext(), db);

        layoutChoice = preferences.getLayoutChoice();
        rv = (RecyclerView) findViewById(R.id.recyclerView);
        rv.setHasFixedSize(true);

        DividerItemDecoration decoration = new DividerItemDecoration(getApplicationContext(), VERTICAL);
        DividerItemDecoration decoration2 = new DividerItemDecoration(getApplicationContext(), HORIZONTAL);
        rv.addItemDecoration(decoration);
        rv.addItemDecoration(decoration2);
        rv.setAdapter(notesAdapter);

        rv.addOnItemTouchListener(new RecyclerItemListener(getApplicationContext(),
                rv, new RecyclerItemListener.RecyclerTouchListener() {
            public void onClickItem(View view, int position) {
                if (multiSelectHelper.isMultiSelectFlag()) {
                    multiSelect(notesAdapter.getNoteAt(position), position);

                } else {
                    launchIntent(notesAdapter.getNoteAt(position), position, notesAdapter.getListUsed());
                }
            }

            public void onLongClickItem(View view, final int position) {
                if (!multiSelectHelper.isMultiSelectFlag()) {

                    if (snackBar != null) {
                        snackBar.dismiss();
                    }

                    buffer.bufferToStartProcessing();

                    // Only allow multi select if a buffer is available to hold the data
                    if (buffer.isBufferAvailable()) {
                        buffer.swapBuffer();

                        multiSelectHelper.setAnotherMultiSelect(true);

                        ((AppCompatActivity) view.getContext()).startSupportActionMode(actionModeCallback);
                        multiSelect(notesAdapter.getNoteAt(position), position);
                    } else {
                        // TODO: Possibly Modify later
                        Toast.makeText(getApplicationContext(),
                                "Please wait while previous selections are completed.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    multiSelect(notesAdapter.getNoteAt(position), position);
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
        itemHelper = new MenuItemHelper(getApplicationContext(), db);
        preferences = new Preferences(getApplicationContext());
        multiSelectHelper = new MultiSelectFlagHelper();

        new AutoDeleteAsync(db, preferences.getDaysInTrash()).execute();

        fab = (FloatingActionButton) findViewById(R.id.floatingActionButton);

        drawer = (DrawerLayout) findViewById(R.id.drawerLayout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, myToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {

            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                super.onDrawerSlide(drawerView, slideOffset);
                if (multiSelectHelper.isMultiSelectFlag()) {    // Turn off multi select if drawer is opened
                    multiSelectHelper.setMultiSelectFlag(false);
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
            multiSelectHelper.setMultiSelectFlag(true);

            mode.getMenuInflater().inflate(R.menu.contextual_action_menu, menu);
            itemHelper.setCABMenuItems(menu, notesAdapter.getListUsed());
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
                    multiSelectHelper.setItem(item);
                    return true;
                case R.id.contextualDeleteForever:
                    actionMode = mode;
                    deleteForever(item);
                    menuItemPressed = true;
                    return true;
                default:
                    return false;
            }
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            multiSelectHelper.setMultiSelectFlag(false);
            actionMode = null;
            notesAdapter.clearSelectedIds();

            // Clear buffer if a CAB item was not pressed (i.e. adding note, open drawer, back pressed)
            if (!menuItemPressed) {
                buffer.clearBuffer();
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
                        List<SingleNote> temp = new ArrayList<>(buffer.currentBufferNotes());
                        new BasicDBAsync(db, temp, null, notesAdapter.getListUsed(), 3).execute();
                        notesAdapter.removeSelectedViews(buffer.currentBufferPositions());
                        buffer.clearBuffer();
                        actionMode.finish();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    } // deleteForever() end

    private void showSnackBar(final MenuItem item, int num) {
        String message;
        if (multiSelectHelper.getEditorAction() != -1) {
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
                    if (!multiSelectHelper.isUndoFlag()) {

                        if (!multiSelectHelper.isAnotherMultiSelect()) {
                            buffer.bufferToStartProcessing();
                        }

                        new DoMenuActionAsync(db, multiSelectHelper, itemHelper, buffer,
                                notesAdapter, myToolbar, fab).execute();
                    }

                    snackBar = null;
                    multiSelectHelper.setUndoFlag(false);
                }
            });
        }
    } // showSnackBar() end

    private class UndoListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            multiSelectHelper.setUndoFlag(true);

            if (multiSelectHelper.getEditorAction() != -1) {

                if (multiSelectHelper.getNewNoteAction() == 1) {
                    new BasicDBAsync(db, buffer.currentBufferNotes(), null, notesAdapter.getListUsed(), 0).execute();
                }

                notesAdapter.addSelectedViews(buffer.currentBufferPositions(),
                        buffer.currentBufferNotes());

            } else {
                notesAdapter.addSelectedViews(buffer.currentBufferPositions(), buffer.currentBufferNotes());
            }

            buffer.clearBuffer(buffer.getBufferToProcess());
        }
    } // UndoListener() end

    private void multiSelect(SingleNote note, int position) {
        if (buffer.currentBufferPositions().contains(position)) {
            notesAdapter.removeSelectedId(position);
            buffer.removeDataFromBuffer(position);

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
        MainActivityUtils.restoreLayout(getApplicationContext(), rv, layoutItem, layoutChoice);
        return super.onCreateOptionsMenu(menu);
    } // onCreateOptionsMenu() end

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        switch (item.getItemId()) {
            case R.id.layout:
                layoutChoice = MainActivityUtils.swapLayout(getApplicationContext(), rv, layoutItem, layoutChoice);
                return true;
        }

        return false;
    } // onOptionsItemSelected() end

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        if (multiSelectHelper.getCurrentNavMenu() != item) {
            multiSelectHelper.setCurrentNavMenu(item);

            if (snackBar != null) {
                snackBar.dismiss();
                multiSelectHelper.setInAsync(true);
            }

            // Only enters if not currently processing a multi select operation in a thread
            // If processing, that thread will set up the adapter list navigation selection
            if (!multiSelectHelper.isInAsync()) {
                notesAdapter.clearView();
                new NavMenuAsync(db, myToolbar, fab, notesAdapter, item).execute();
            }
        }

        drawer.closeDrawer(GravityCompat.START);
        return true;
    } // onNavigationItemSelected() end

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && data != null) {
            Bundle bundle = data.getExtras();

            multiSelectHelper.setEditorAction(bundle.getInt("menuAction", -1));
            if (multiSelectHelper.getEditorAction() != -1) {

                buffer.bufferToStartProcessing();
                if (buffer.isBufferAvailable()) {
                    buffer.swapBuffer();

                    multiSelectHelper.setNewNoteAction(bundle.getInt("New Note Action", -1));
                    buffer.addDataToBuffer((SingleNote) bundle.getParcelable("Note"), bundle.getInt("Note Position", -1));

                    if (multiSelectHelper.getNewNoteAction() != 1) {
                        notesAdapter.removeSelectedViews(buffer.currentBufferPositions());
                    }

                    showSnackBar(null, multiSelectHelper.getEditorAction());


                } else {        // Buffer full
                    multiSelectHelper.setEditorAction(-1);
                    multiSelectHelper.setNewNoteAction(-1);
                    Toast.makeText(getApplicationContext(),
                            "Please wait while previous selections are completed", Toast.LENGTH_SHORT).show();
                }

            } else {
                MainActivityUtils.updateAdapter(getApplicationContext(), bundle, notesAdapter);
            }
        }
    } // onActivityResult() end

    @Override
    protected void onStop() {
        super.onStop();
        preferences.saveLayoutChoice(layoutChoice);
    }
} // MainActivity() end