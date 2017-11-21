package com.ozmar.notes;

import android.arch.persistence.room.Room;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.ozmar.notes.async.AutoDeleteAsync;
import com.ozmar.notes.async.BasicDBAsync;
import com.ozmar.notes.async.DoMenuActionAsync;
import com.ozmar.notes.async.NavMenuAsync;
import com.ozmar.notes.database.RecycleBinNote;
import com.ozmar.notes.database.TestDB;
import com.ozmar.notes.databinding.ActivityMainBinding;
import com.ozmar.notes.noteEditor.NoteEditorActivity;
import com.ozmar.notes.utils.MenuItemHelper;
import com.ozmar.notes.utils.MultiSelectFlagHelper;
import com.ozmar.notes.utils.UndoBuffer;

import java.util.ArrayList;
import java.util.List;

import static android.support.v7.widget.DividerItemDecoration.HORIZONTAL;
import static android.support.v7.widget.DividerItemDecoration.VERTICAL;

// TODO: When deleting an entire note, make sure the reminder is cancelled if it exists

// TODO: Rewrite parts of Undo Buffer to treat archive buttons in MainActivity and NoteEditorActivity as the same action


public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final int USER_NOTES = 0;
    private static final int FAVORITE_NOTES = 1;
    private static final int ARCHIVE_NOTES = 2;
    private static final int RECYCLE_BIN_NOTES = 3;

    private RecyclerView rv;
    private NotesAdapter notesAdapter;

    public static DatabaseHandler db;

    private ActionMode actionMode;
    private final UndoBuffer buffer = new UndoBuffer();

    private int layoutChoice;
    private MenuItem layoutItem;
    private Preferences preferences;

    private Snackbar snackBar = null;

    private ActivityMainBinding mBinding;
    private MenuItemHelper itemHelper;
    private MultiSelectFlagHelper multiSelectHelper;


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

        Intent intent = new Intent(MainActivity.this, NoteEditorActivity.class);

        int id = note != null ? note.getId() : -1;
        intent.putExtra(getString(R.string.noteIdIntent), id);
        intent.putExtra(getString(R.string.notePositionIntent), position);
        intent.putExtra(getString(R.string.listUsedIntent), listUsed);

        startActivityForResult(intent, 1);
    } // launchIntent() end

    private void restoreLayout() {
        switch (layoutChoice) {
            case 0:
            default:
                layoutItem.setIcon(R.drawable.ic_linear_layout);
                rv.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
                break;
            case 1:
                layoutItem.setIcon(R.drawable.ic_staggered_grid_layout);
                rv.setLayoutManager(new LinearLayoutManager(MainActivity.this));
        }
    }

    private int swapLayout() {
        switch (layoutChoice) {
            case 0:
            default:
                layoutItem.setIcon(R.drawable.ic_staggered_grid_layout);
                rv.setLayoutManager(new LinearLayoutManager(MainActivity.this));
                return 1;
            case 1:
                layoutItem.setIcon(R.drawable.ic_linear_layout);
                rv.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
                return 0;
        }
    }

    private void setUpRecyclerView() {
        notesAdapter = new NotesAdapter(MainActivity.this, db);

        layoutChoice = preferences.getLayoutChoice();
        rv = findViewById(R.id.recyclerView);
        rv.setHasFixedSize(true);

        DividerItemDecoration decoration = new DividerItemDecoration(MainActivity.this, VERTICAL);
        DividerItemDecoration decoration2 = new DividerItemDecoration(MainActivity.this, HORIZONTAL);
        rv.addItemDecoration(decoration);
        rv.addItemDecoration(decoration2);
        rv.setAdapter(notesAdapter);

        rv.addOnItemTouchListener(new RecyclerItemListener(MainActivity.this,
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
                                getString(R.string.bufferFull), Toast.LENGTH_SHORT).show();
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

        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);


        setSupportActionBar(mBinding.myToolbar);


        TestDB testDB = Room.databaseBuilder(getApplicationContext(), TestDB.class, "test-db")
                .allowMainThreadQueries()
                .build();

        RecycleBinNote note = new RecycleBinNote();
        note.setTitle("Testing A Title");
        note.setContent("Testing The Content");
        note.setTimeCreated(System.currentTimeMillis());
        note.setTimeModified(System.currentTimeMillis());
        int id = (int)testDB.mDaoClass().addToRecycleBinNotes(note);

        note = testDB.mDaoClass().getARecycleBinNotes(id);
        if(note!=null){
            Log.d("Test", note.getTitle());
            Log.d("Test", note.getContent());
        } else {
            Log.d("Test", "NULL");
        }


        db = new DatabaseHandler(MainActivity.this);
        itemHelper = new MenuItemHelper(MainActivity.this, db);
        preferences = new Preferences(MainActivity.this);
        multiSelectHelper = new MultiSelectFlagHelper();

        new AutoDeleteAsync(db, preferences.getDaysInTrash()).execute();

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mBinding.drawerLayout, mBinding.myToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {

            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                super.onDrawerSlide(drawerView, slideOffset);
                if (multiSelectHelper.isMultiSelectFlag()) {    // Turn off multi select if drawer is opened
                    multiSelectHelper.setMultiSelectFlag(false);
                    actionMode.finish();
                }
            }
        };

        mBinding.drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        mBinding.navView.setNavigationItemSelectedListener(this);
        mBinding.navView.getMenu().getItem(0).setChecked(true);

        setUpRecyclerView();
    } // onCreate() end

    @Override
    public void onBackPressed() {
        if (mBinding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            mBinding.drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    } // onBackPressed() end

    private final ActionMode.Callback actionModeCallback = new ActionMode.Callback() {
        boolean menuItemPressed = false;

        private void removeViews(ActionMode mode, MenuItem item) {
            menuItemPressed = true;
            notesAdapter.removeSelectedViews(buffer.currentBufferPositions());
            showSnackBar(item, 0);
            mode.finish();
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
                .setPositiveButton(getString(R.string.deleteDialog), (dialogInterface, i) -> {
                    List<SingleNote> temp = new ArrayList<>(buffer.currentBufferNotes());
                    new BasicDBAsync(db, temp, null, notesAdapter.getListUsed(), 3).execute();
                    notesAdapter.removeSelectedViews(buffer.currentBufferPositions());
                    buffer.clearBuffer();
                    actionMode.finish();
                })
                .setNegativeButton(getString(R.string.cancelDialog), null)
                .show();
    } // deleteForever() end

    private void showSnackBar(MenuItem item, int num) {
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
                                notesAdapter, mBinding.myToolbar, mBinding.fab).execute();
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
        restoreLayout();
        return super.onCreateOptionsMenu(menu);
    } // onCreateOptionsMenu() end

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        switch (item.getItemId()) {
            case R.id.layout:
                layoutChoice = swapLayout();
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
                new NavMenuAsync(db, mBinding.myToolbar, mBinding.fab, notesAdapter, item).execute();
            }
        }

        mBinding.drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    } // onNavigationItemSelected() end

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && data != null) {
            Bundle bundle = data.getExtras();

            if (bundle != null) {

                int noteId = bundle.getInt(getString(R.string.noteIdIntent), -1);
                int notePosition = bundle.getInt(getString(R.string.notePositionIntent), -1);
                int listUsed = bundle.getInt(getString(R.string.listUsedIntent), notesAdapter.getListUsed());
                int noteResult = bundle.getInt(getString(R.string.noteSuccessIntent), -1);
                int noteEditorAction = bundle.getInt(getString(R.string.menuActionIntent), -1);

                boolean noteIsFavorite = bundle.getBoolean(getString(R.string.noteIsFavoriteIntent), false);

                boolean isNewNote = notePosition == -1;
                if (isNewNote) {
                    notePosition = 0;
                }

                SingleNote note = null;
                if (noteId != -1 && (noteEditorAction != -1 || noteResult != -1)) {
                    if (listUsed == USER_NOTES || listUsed == FAVORITE_NOTES) {
                        note = db.getAUserNote(noteId);
                    } else if (listUsed == ARCHIVE_NOTES) {
                        note = db.getAnArchiveNote(noteId);
                    } else if (listUsed == RECYCLE_BIN_NOTES) {
                        note = db.getARecycleBinNote(noteId);
                    }
                }

                if (note != null) {
                    if (noteEditorAction != -1) {

                        if (noteIsFavorite && listUsed == ARCHIVE_NOTES) {
                            note.setFavorite(true);
                        }

                        multiSelectHelper.setEditorAction(noteEditorAction);
                        menuItemClickedInNoteEditor(note, notePosition, isNewNote);
                    } else {
                        noteModifiedInNoteEditor(note, notePosition, listUsed, noteResult, noteIsFavorite);
                    }
                }
            }
        }
    } // onActivityResult() end


    private void menuItemClickedInNoteEditor(@NonNull SingleNote note, int notePosition, boolean isNewNote) {
        buffer.bufferToStartProcessing();
        if (buffer.isBufferAvailable()) {
            buffer.swapBuffer();

            multiSelectHelper.setNewNoteAction(isNewNote ? 1 : -1);
            buffer.addDataToBuffer(note, notePosition);

            if (multiSelectHelper.getNewNoteAction() != 1) {
                notesAdapter.removeSelectedViews(buffer.currentBufferPositions());
            }

            showSnackBar(null, multiSelectHelper.getEditorAction());


        } else {        // Buffer full
            multiSelectHelper.setEditorAction(-1);
            multiSelectHelper.setNewNoteAction(-1);
            Toast.makeText(getApplicationContext(),
                    getString(R.string.bufferFull), Toast.LENGTH_SHORT).show();
        }
    }

    private void noteModifiedInNoteEditor(@NonNull SingleNote note, int notePosition, int listUsed,
                                          int noteModifiedResult, boolean noteIsFavorite) {


        if (noteModifiedResult == 0) {    // Update rv with noteChanges to the note
            notesAdapter.updateAt(notePosition, note);

        } else if (noteModifiedResult == 1) {    // Update rv with new note
            if (listUsed == 0) {
                notesAdapter.addAt(notePosition, note);
            } else if (listUsed == FAVORITE_NOTES && noteIsFavorite) {
                notesAdapter.addAt(notePosition, note);
            }

        } else if (noteModifiedResult == 2) {    // Remove note from rv (Delete Forever)
            notesAdapter.removeAt(notePosition);

        } else if (noteModifiedResult == 3) {    // Title/Content not modified but note is no longer a favorite
            if (listUsed == FAVORITE_NOTES) {
                notesAdapter.removeAt(notePosition);
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        preferences.saveLayoutChoice(layoutChoice);
    }

    @Override
    protected void onDestroy() {
        db.close();
        super.onDestroy();
    }
} // MainActivity() end