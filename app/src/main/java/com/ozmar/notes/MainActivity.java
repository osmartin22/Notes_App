package com.ozmar.notes;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.ozmar.notes.database.AppDatabase;
import com.ozmar.notes.database.NoteAndReminderPreview;
import com.ozmar.notes.databinding.ActivityMainBinding;
import com.ozmar.notes.noteEditor.NoteEditorActivity;

import java.util.List;

import static android.support.v7.widget.DividerItemDecoration.HORIZONTAL;
import static android.support.v7.widget.DividerItemDecoration.VERTICAL;

// TODO: When deleting an entire note, make sure the reminder is cancelled if it exists

public class MainActivity extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener, MainActivityView {

    private static final int USER_NOTES = 0;
    private static final int FAVORITE_NOTES = 1;
    private static final int ARCHIVE_NOTES = 2;
    private static final int RECYCLE_BIN_NOTES = 3;

    private RecyclerView rv;
    private NotesAdapter notesAdapter;

    private int layoutChoice;
    private MenuItem layoutIcon;
    private Preferences preferences;
    private ActionMode mActionMode;

    private boolean isMultiSelect = false;

    private ActivityMainBinding mBinding;
    private MainActivityPresenter mActivityPresenter;

    public void onFabClicked(View view) {
        mActivityPresenter.onNoteClick(-1, -1);
    }

    private void setUpRecyclerView() {
        notesAdapter = new NotesAdapter(MainActivity.this);
        mActivityPresenter.onGetPreviewList(0);

        layoutChoice = preferences.getLayoutChoice();
        rv = mBinding.recyclerView;
        rv.setHasFixedSize(true);

        DividerItemDecoration decoration = new DividerItemDecoration(MainActivity.this, VERTICAL);
        DividerItemDecoration decoration2 = new DividerItemDecoration(MainActivity.this, HORIZONTAL);
        rv.addItemDecoration(decoration);
        rv.addItemDecoration(decoration2);
        rv.setAdapter(notesAdapter);

        rv.addOnItemTouchListener(new RecyclerItemListener(MainActivity.this,
                rv, new RecyclerItemListener.RecyclerTouchListener() {
            public void onClickItem(View view, int position) {
                if (isMultiSelect) {
                    multiSelect(position);
                } else {
                    mActivityPresenter.onNoteClick(notesAdapter.getNoteIdAt(position), position);
                }
            }

            public void onLongClickItem(View view, final int position) {
                ((AppCompatActivity) view.getContext()).startSupportActionMode(mActionModeCallback);
                isMultiSelect = true;
                multiSelect(position);
            }
        }));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);

        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        setSupportActionBar(mBinding.myToolbar);
        preferences = new Preferences(MainActivity.this);
        AppDatabase.setUpAppDatabase(getApplicationContext());

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mBinding.drawerLayout, mBinding.myToolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close) {

            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                super.onDrawerSlide(drawerView, slideOffset);
                if (mActionMode != null) {
                    mActionMode.finish();
                }
            }
        };

        mBinding.drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        mBinding.navView.setNavigationItemSelectedListener(this);
        mBinding.navView.getMenu().getItem(0).setChecked(true);


        mActivityPresenter = new MainActivityPresenter(MainActivity.this);

        setUpRecyclerView();
    }

    @Override
    public void onBackPressed() {
        if (mBinding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            mBinding.drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    private void restoreLayout(int layoutChoice) {
        switch (layoutChoice) {
            case 0:
            default:
                layoutIcon.setIcon(R.drawable.ic_linear_layout);
                rv.setLayoutManager(new StaggeredGridLayoutManager(2,
                        StaggeredGridLayoutManager.VERTICAL));
                break;
            case 1:
                layoutIcon.setIcon(R.drawable.ic_staggered_grid_layout);
                rv.setLayoutManager(new LinearLayoutManager(MainActivity.this));
        }
    }


    private final ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {

        boolean menuActionPressed = false;

        private void removePreviews(ActionMode mode, MenuItem item) {
            menuActionPressed = true;
            notesAdapter.removeSelectedViews();
//            showSnackBar(item, 0);
            mode.finish();
        }

        private void d(ActionMode mode, MenuItem item, int listToAddTo) {
            mActivityPresenter.processChosenNotes(notesAdapter.getSelectedPreviewIds(), listToAddTo);
            menuActionPressed = true;
            notesAdapter.removeSelectedViews();
//            showSnackBar(item, 0);
            mode.finish();
        }

        private void setCABMenuItems(Menu menu, int listUsed) {
            switch (listUsed) {
                case 0:
                case 1:
                default:
                    break;
                case 2:
                    menu.findItem(R.id.contextualArchive).setVisible(false);
                    menu.findItem(R.id.contextualUnarchive).setVisible(true);
                    break;
                case 3:
                    menu.findItem(R.id.contextualArchive).setVisible(false);
                    menu.findItem(R.id.contextualDelete).setVisible(false);
                    menu.findItem(R.id.contextualRestore).setVisible(true);
                    menu.findItem(R.id.contextualDeleteForever).setVisible(true);
            }
        }

        @Override
        public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
            mActionMode = actionMode;
            actionMode.getMenuInflater().inflate(R.menu.contextual_action_menu, menu);
            setCABMenuItems(menu, mActivityPresenter.getListUsed());
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
            switch (menuItem.getItemId()) {
                case R.id.contextualArchive:
                    d(actionMode, menuItem, ARCHIVE_NOTES);
                    return true;
                case R.id.contextualUnarchive:
                    d(actionMode, menuItem, USER_NOTES);
                    return true;
                case R.id.contextualDelete:
                    d(actionMode, menuItem, RECYCLE_BIN_NOTES);
                    return true;
                case R.id.contextualRestore:
                    d(actionMode, menuItem, USER_NOTES);
                    return true;
                case R.id.contextualDeleteForever:
//                    deleteForever(item);
                    menuActionPressed = true;
                    return true;
                default:
                    return false;
            }
        }

        @Override
        public void onDestroyActionMode(ActionMode actionMode) {
            notesAdapter.clearSelectedPositions();
            notesAdapter.notifyDataSetChanged();
            mActionMode = null;
        }
    };

    private void deleteForever(MenuItem item) {
//        String message = itemHelper.multiSelectMessage(item, buffer.currentBufferSize());
        new AlertDialog.Builder(MainActivity.this)
                .setMessage("")
                .setPositiveButton(getString(R.string.deleteDialog), (dialogInterface, i) -> {
//                    List<MainNote> temp = new ArrayList<>(buffer.currentBufferNotes());
//                    new BasicDBAsync(db, temp, null, notesAdapter.getListUsed(), 3).execute();
//                    notesAdapter.removeSelectedViews(buffer.currentBufferPositions());
//                    buffer.clearBuffer();
//                    actionMode.finish();
                })
                .setNegativeButton(getString(R.string.cancelDialog), null)
                .show();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_activity_menu, menu);
        layoutIcon = menu.findItem(R.id.layout);
        restoreLayout(layoutChoice);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        switch (item.getItemId()) {
            case R.id.layout:
                mActivityPresenter.onLayoutIconClicked(layoutChoice);
                return true;
        }

        return false;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int listToUse;
        switch (item.getItemId()) {
            case R.id.all_notes_drawer:
            default:
                listToUse = 0;
                mBinding.myToolbar.setTitle("Notes");
                mBinding.fab.show();
                break;

            case R.id.favorite_notes_drawer:
                listToUse = 1;
                mBinding.myToolbar.setTitle("Favorite Notes");
                mBinding.fab.show();
                break;

            case R.id.archive_drawer:
                listToUse = 2;
                mBinding.myToolbar.setTitle("Archive");
                mBinding.fab.hide();
                break;

            case R.id.recycle_bin_drawer:
                listToUse = 3;
                mBinding.myToolbar.setTitle("Trash");
                mBinding.fab.hide();
                break;
        }

        mActivityPresenter.onGetPreviewList(listToUse);
        mBinding.drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void updateAdapterList(List<NoteAndReminderPreview> list) {
        notesAdapter.updateAdapterList(list);
    }

    private void multiSelect(int notePosition) {
        if (notesAdapter.getSelectedPositions().contains(notePosition)) {
            notesAdapter.removeSelectedPosition(notePosition);

        } else {
            notesAdapter.addSelectedPosition(notePosition);
        }

        notesAdapter.notifyItemChanged(notePosition);
        mActionMode.setSubtitle(Integer.toString(notesAdapter.getSelectedPositions().size()));

        if (notesAdapter.getSelectedPositions().isEmpty()) {
            isMultiSelect = false;
            mActionMode.finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && data != null) {
            Bundle bundle = data.getExtras();

            if (bundle != null) {

                int noteId = bundle.getInt(getString(R.string.noteIdIntent), -1);
                int notePosition = bundle.getInt(getString(R.string.notePositionIntent), -1);
                int listUsed = bundle.getInt(getString(R.string.listUsedIntent), mActivityPresenter.getListUsed());
                int noteResult = bundle.getInt(getString(R.string.noteSuccessIntent), -1);
                int noteEditorAction = bundle.getInt(getString(R.string.menuActionIntent), -1);

                boolean noteIsFavorite = bundle.getBoolean(getString(R.string.noteIsFavoriteIntent), false);

                boolean isNewNote = notePosition == -1;
                if (isNewNote) {
                    notePosition = 0;
                }

                if (noteId != -1 && (noteEditorAction != -1 || noteResult != -1)) {
                    mActivityPresenter.onGetANotePreview(noteId, listUsed, notePosition,
                            noteResult, noteIsFavorite);
                }
            }
        }
    }

    @Override
    public void noteModifiedInNoteEditor(@NonNull NoteAndReminderPreview preview, int notePosition, int listUsed,
                                         int noteModifiedResult, boolean noteIsFavorite) {

        final int FAVORITE_NOTES = 1;

        if (noteModifiedResult == 0) {    // Update rv with noteChanges to the note
            notesAdapter.updateAt(notePosition, preview);

        } else if (noteModifiedResult == 1) {    // Update rv with new note
            if (listUsed == 0) {
                notesAdapter.addAt(notePosition, preview);
            } else if (listUsed == FAVORITE_NOTES && noteIsFavorite) {
                notesAdapter.addAt(notePosition, preview);
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
    protected void onStart() {
        if (mActivityPresenter != null) {
            mActivityPresenter.onAttach(MainActivity.this);
        }
        super.onStart();
    }

    @Override
    protected void onPause() {
        mActivityPresenter.onDestroy();
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        preferences.saveLayoutChoice(layoutChoice);
    }

    @Override
    protected void onDestroy() {
        AppDatabase.destroyInstance();
        super.onDestroy();
    }


    @Override
    public void swapLayout(int layout) {
        switch (layout) {
            case 0:
            default:
                layoutIcon.setIcon(R.drawable.ic_staggered_grid_layout);
                rv.setLayoutManager(new LinearLayoutManager(MainActivity.this));
                layoutChoice = 1;
                break;
            case 1:
                layoutIcon.setIcon(R.drawable.ic_linear_layout);
                rv.setLayoutManager(new StaggeredGridLayoutManager(2,
                        StaggeredGridLayoutManager.VERTICAL));
                layoutChoice = 0;
                break;
        }
    }

    @Override
    public void openNoteEditorActivity(int noteId, int notePosition, int listUsed) {
        if (mActionMode != null) {
            mActionMode.finish();
        }

        Intent intent = new Intent(MainActivity.this, NoteEditorActivity.class);

        intent.putExtra(getString(R.string.noteIdIntent), noteId);
        intent.putExtra(getString(R.string.notePositionIntent), notePosition);
        intent.putExtra(getString(R.string.listUsedIntent), listUsed);

        startActivityForResult(intent, 1);
    }
}