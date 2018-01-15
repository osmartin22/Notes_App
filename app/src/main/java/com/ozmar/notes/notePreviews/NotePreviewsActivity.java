package com.ozmar.notes.notePreviews;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.ozmar.notes.NoteResult;
import com.ozmar.notes.NotesAdapter;
import com.ozmar.notes.R;
import com.ozmar.notes.RecyclerItemListener;
import com.ozmar.notes.SharedPreferencesHelper;
import com.ozmar.notes.database.NoteAndReminderPreview;
import com.ozmar.notes.databinding.ActivityNotePreviewsBinding;
import com.ozmar.notes.noteEditor.NoteEditorActivity;
import com.ozmar.notes.notifications.ReminderNotificationManager;

import java.util.List;

import javax.inject.Inject;

import dagger.android.AndroidInjection;

import static android.support.v7.widget.DividerItemDecoration.HORIZONTAL;
import static android.support.v7.widget.DividerItemDecoration.VERTICAL;

// TODO: When deleting an entire note, make sure the reminder is cancelled if it exists

// TODO: Sometimes when multi selecting a note, it gets removed from the list properly but is not added
// to the desired list. It is not shown in any of the lists

public class NotePreviewsActivity extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener, NotePreviewsView {

    private static final int USER_NOTES = 0;
    private static final int FAVORITE_NOTES = 1;
    private static final int ARCHIVE_NOTES = 2;
    private static final int RECYCLE_BIN_NOTES = 3;

    private static final int ARCHIVE = 0;
    private static final int UNARCHIVE = 1;
    private static final int DELETE = 2;
    private static final int RESTORE = 3;
    private static final int DELETE_FOREVER = 4;

    private RecyclerView rv;
    private NotesAdapter notesAdapter;

    private int layoutChoice;
    private MenuItem layoutIcon;

    private ActionMode mActionMode;
    private Snackbar mSnackBar;

    private ActivityNotePreviewsBinding mBinding;

    @Inject
    public NotePreviewsPresenter mActivityPresenter;

    @Inject
    public SharedPreferencesHelper preferences;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        AndroidInjection.inject(this);
        super.onCreate(savedInstanceState);

        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_note_previews);
        setSupportActionBar(mBinding.myToolbar);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mBinding.drawerLayout, mBinding.myToolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close) {

            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                super.onDrawerSlide(drawerView, slideOffset);
                mActivityPresenter.onDrawerSlide();
            }
        };

        mBinding.drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        mBinding.navView.setNavigationItemSelectedListener(this);
        mBinding.navView.getMenu().getItem(0).setChecked(true);

        setUpRecyclerView();
    }

    private void setUpRecyclerView() {
        notesAdapter = new NotesAdapter(NotePreviewsActivity.this);
//        mActivityPresenter.onGetPreviewList(0);

        layoutChoice = preferences.getLayoutChoice();
        rv = mBinding.recyclerView;
        rv.setHasFixedSize(true);

        DividerItemDecoration decoration = new DividerItemDecoration(NotePreviewsActivity.this, VERTICAL);
        DividerItemDecoration decoration2 = new DividerItemDecoration(NotePreviewsActivity.this, HORIZONTAL);
        rv.addItemDecoration(decoration);
        rv.addItemDecoration(decoration2);
        rv.setAdapter(notesAdapter);

        rv.addOnItemTouchListener(new RecyclerItemListener(NotePreviewsActivity.this,
                rv, new RecyclerItemListener.RecyclerTouchListener() {
            public void onClickItem(View view, int position) {
                mActivityPresenter.onNoteClick(position);
            }

            public void onLongClickItem(View view, final int position) {
                mActivityPresenter.onNoteLongClick(position);
            }
        }));
    }


    @Override
    public void onBackPressed() {
        if (mBinding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            mBinding.drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
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
                mBinding.myToolbar.setTitle(getString(R.string.toolbarMainNotes));
                mBinding.fab.setVisibility(View.VISIBLE);
                break;

            case R.id.favorite_notes_drawer:
                listToUse = 1;
                mBinding.myToolbar.setTitle(getString(R.string.toolbarFavoriteNotes));
                mBinding.fab.setVisibility(View.VISIBLE);
                break;

            case R.id.archive_drawer:
                listToUse = 2;
                mBinding.myToolbar.setTitle(getString(R.string.toolbarArchiveNotes));
                mBinding.fab.setVisibility(View.INVISIBLE);
                break;

            case R.id.recycle_bin_drawer:
                listToUse = 3;
                mBinding.myToolbar.setTitle(getString(R.string.toolbarRecycleBinNotes));
                mBinding.fab.setVisibility(View.INVISIBLE);
                break;
        }

        mActivityPresenter.onGetPreviewList(listToUse);
        mBinding.drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void swapLayout(int layout) {
        switch (layout) {
            case 0:
            default:
                layoutIcon.setIcon(R.drawable.ic_staggered_grid_layout);
                rv.setLayoutManager(new LinearLayoutManager(NotePreviewsActivity.this));
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
                rv.setLayoutManager(new LinearLayoutManager(NotePreviewsActivity.this));
        }
    }

    public void onFabClicked(View view) {
        mActivityPresenter.onNoteClick(-1);
    }

    @Override
    public void openNoteEditorActivity(int notePosition, int listUsed) {
        Intent intent = new Intent(NotePreviewsActivity.this, NoteEditorActivity.class);
        intent.putExtra(getString(R.string.noteIdIntent), notePosition != -1 ? notesAdapter.getNoteIdAt(notePosition) : -1);
        intent.putExtra(getString(R.string.notePositionIntent), notePosition);
        intent.putExtra(getString(R.string.listUsedIntent), listUsed);

        startActivityForResult(intent, 1);
    }

    private final ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {

        private void setCABMenuItems(Menu menu) {
            switch (mActivityPresenter.getListUsed()) {
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
            setCABMenuItems(menu);
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
                    mActivityPresenter.onMenuActionIconClicked(notesAdapter.getSelectedPositions(),
                            notesAdapter.getSelectedPreviews(), ARCHIVE, ARCHIVE_NOTES);
                    return true;
                case R.id.contextualUnarchive:
                    mActivityPresenter.onMenuActionIconClicked(notesAdapter.getSelectedPositions(),
                            notesAdapter.getSelectedPreviews(), UNARCHIVE, USER_NOTES);
                    return true;
                case R.id.contextualDelete:
                    mActivityPresenter.onMenuActionIconClicked(notesAdapter.getSelectedPositions(),
                            notesAdapter.getSelectedPreviews(), DELETE, RECYCLE_BIN_NOTES);
                    return true;
                case R.id.contextualRestore:
                    mActivityPresenter.onMenuActionIconClicked(notesAdapter.getSelectedPositions(),
                            notesAdapter.getSelectedPreviews(), RESTORE, USER_NOTES);
                    return true;
                case R.id.contextualDeleteForever:
                    deleteForever(notesAdapter.getSelectedPreviews());
                    return true;
                default:
                    return false;
            }
        }

        @Override
        public void onDestroyActionMode(ActionMode actionMode) {
            mActivityPresenter.onMultiSelectDestroy();
            mActionMode = null;
        }
    };

    @Override
    public void finishMultiSelectCAB() {
        if (mActionMode != null) {
            mActionMode.finish();
        }
    }

    @Override
    public void notifyEntireAdapter() {
        notesAdapter.notifyDataSetChanged();
    }

    @Override
    public void clearSelectedPositions() {
        notesAdapter.clearSelectedPositions();
    }

    @Override
    public void removeSelectedPreviews() {
        notesAdapter.removeSelectedPreviews();
    }

    @Override
    public void addBackSelectedPreviews(@NonNull List<Integer> selectedPositions,
                                        @NonNull List<NoteAndReminderPreview> selectedPreviews) {
        notesAdapter.addSelectedPreviews(selectedPositions, selectedPreviews);
    }

    @Override
    public void startMultiSelect(int position) {
        this.startSupportActionMode(mActionModeCallback);
        multiSelect(position);
    }

    @Override
    public void multiSelect(int notePosition) {
        if (notesAdapter.getSelectedPositions().contains(notePosition)) {
            notesAdapter.removeSelectedPosition(notePosition);

        } else {
            notesAdapter.addSelectedPosition(notePosition);
        }

        notesAdapter.notifyItemChanged(notePosition);
        mActionMode.setSubtitle(Integer.toString(notesAdapter.getSelectedPositions().size()));

        if (notesAdapter.getSelectedPositions().isEmpty()) {
            mActivityPresenter.onEndMultiSelect();
        }
    }


    @Override
    public void showSnackBar(int cabAction, int numOfNotesSelected) {
        String message = getSnackBarMessage(cabAction, numOfNotesSelected);

        if (message != null) {
            mSnackBar = Snackbar.make(findViewById(R.id.coordinatorLayout), message, Snackbar.LENGTH_LONG);
            mSnackBar.setAction(R.string.snackBarUndoAction, new UndoListener());
            mSnackBar.show();
            mSnackBar.addCallback(new Snackbar.Callback() {
                @Override
                public void onDismissed(Snackbar transientBottomBar, int event) {
                    mActivityPresenter.processChosenNotes();
                    mSnackBar = null;
                }
            });
        }
    }

    @Nullable
    private String getSnackBarMessage(int num, int size) {
        switch (num) {
            case ARCHIVE:
                return this.getResources().getQuantityString(R.plurals.snackBarArchive, size);
            case UNARCHIVE:
                return this.getResources().getQuantityString(R.plurals.snackBarUnarchive, size);
            case DELETE:
                return this.getResources().getQuantityString(R.plurals.snackBarDelete, size);
            case RESTORE:
                return this.getResources().getQuantityString(R.plurals.snackBarRestore, size);
            case DELETE_FOREVER:
                return this.getResources().getQuantityString(R.plurals.deleteForever, size);
            default:
                return null;
        }
    }

    @Override
    public void dismissSnackBar() {
        if (mSnackBar != null) {
            mSnackBar.dismiss();
        }
    }

    private class UndoListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            mActivityPresenter.onUndoClicked();
        }
    }

    @Override
    public void cancelReminderNotifications(@NonNull List<Integer> reminderIds) {
        ReminderNotificationManager.cancelListOfAlarms(getApplicationContext(), reminderIds);
    }


    private void deleteForever(@NonNull List<NoteAndReminderPreview> selectedPreviews) {
        String message = getSnackBarMessage(DELETE_FOREVER, notesAdapter.getSelectedPositions().size());
        new AlertDialog.Builder(NotePreviewsActivity.this)
                .setMessage(message)
                .setPositiveButton(getString(R.string.deleteDialog), (dialogInterface, i)
                        -> mActivityPresenter.onDeleteIconClicked(selectedPreviews))
                .setNegativeButton(getString(R.string.cancelDialog), null)
                .show();
    }

    @Override
    public void updateAdapterList(@NonNull List<NoteAndReminderPreview> list) {
        notesAdapter.updateAdapterList(list);
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
                int noteModification = bundle.getInt(getString(R.string.noteSuccessIntent), -1);
                int noteEditorAction = bundle.getInt(getString(R.string.menuActionIntent), -1);
                boolean isFavoriteNote = bundle.getBoolean(getString(R.string.noteIsFavoriteIntent), false);

                boolean isNewNote = notePosition == -1;
                if (isNewNote) {
                    notePosition = 0;
                }

                if (noteId != -1 && (noteEditorAction != -1 || noteModification != -1)) {
                    mActivityPresenter.onNoteEditorActivityResult(
                            new NoteResult(noteId, notePosition, listUsed, noteModification,
                                    noteEditorAction, isFavoriteNote, isNewNote));
                }
            }
        }
    }


    @Override
    public void removeAPreview(int position) {
        notesAdapter.removeAt(position);
    }

    @Override
    public void addAPreview(@NonNull NoteAndReminderPreview preview, int position) {
        notesAdapter.addAt(preview, position);
    }

    @Override
    public void updateAPreview(@NonNull NoteAndReminderPreview preview, int position) {
        notesAdapter.updateAt(preview, position);
    }


    @Override
    protected void onStart() {

        if (mActivityPresenter != null) {
            mActivityPresenter.onAttach(NotePreviewsActivity.this);

            // TODO: Test this
            if (notesAdapter.isAdapterEmpty()) {
                mActivityPresenter.onGetPreviewList(0);
            }
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
        super.onDestroy();
    }
}