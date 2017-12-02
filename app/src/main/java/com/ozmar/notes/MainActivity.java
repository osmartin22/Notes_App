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

import com.ozmar.notes.async.AutoDeleteAsync;
import com.ozmar.notes.database.AppDatabase;
import com.ozmar.notes.database.NoteAndReminderPreview;
import com.ozmar.notes.databinding.ActivityMainBinding;
import com.ozmar.notes.noteEditor.NoteEditorActivity;

import static android.support.v7.widget.DividerItemDecoration.HORIZONTAL;
import static android.support.v7.widget.DividerItemDecoration.VERTICAL;

// TODO: When deleting an entire note, make sure the reminder is cancelled if it exists

// TODO: Rewrite parts of Undo Buffer to treat archive buttons in MainActivity and NoteEditorActivity as the same action

// TODO: Change FrequencyChoices to never be NULL

public class MainActivity extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener, MainActivityView {

    private static final int USER_NOTES = 0;
    private static final int FAVORITE_NOTES = 1;
    private static final int ARCHIVE_NOTES = 2;
    private static final int RECYCLE_BIN_NOTES = 3;

    private RecyclerView rv;
    private NotesAdapter notesAdapter;

    public static DatabaseHandler db;

    private int layoutChoice;
    private MenuItem layoutIcon;
    private Preferences preferences;


    private ActionMode mActionMode;

    private ActivityMainBinding mBinding;

    private MainActivityPresenter mainActivityPresenter;

    public void onFabClicked(View view) {
        mainActivityPresenter.onNoteClick(-1, -1, notesAdapter.getListUsed());
    }

    private void setUpRecyclerView() {
        notesAdapter = new NotesAdapter(MainActivity.this);

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
                mainActivityPresenter.onNoteClick(notesAdapter.getNoteIdAt(position), position, notesAdapter.getListUsed());
            }

            public void onLongClickItem(View view, final int position) {
                // TODO: add back later
            }
        }));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);

        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        setSupportActionBar(mBinding.myToolbar);

        db = new DatabaseHandler(MainActivity.this);
        preferences = new Preferences(MainActivity.this);
        AppDatabase.setUpAppDatabase(getApplicationContext());


//        MainNote test1 = new MainNote();
//        test1.setTitle("TestTitle1");
//        test1.setContent("TestContent1");
//        test1.setTimeCreated(System.currentTimeMillis());
//        test1.setTimeModified(System.currentTimeMillis());
//        test1.setFavorite(0);
//        test1.setReminderId(-1);
//        AppDatabase.getAppDatabase().notesDao().addToUserNotes(test1);
//
//        MainNote test2 = new MainNote();
//        test2.setTitle("TestTitle2");
//        test2.setContent("TestContent2");
//        test2.setTimeCreated(System.currentTimeMillis());
//        test2.setTimeModified(System.currentTimeMillis());
//        test2.setFavorite(1);
//        test2.setReminderId(-1);
//        AppDatabase.getAppDatabase().notesDao().addToUserNotes(test2);
//
//        MainNote test3 = new MainNote();
//        test3.setTitle("TestTitle3");
//        test3.setContent("TestContent3");
//        test3.setTimeCreated(System.currentTimeMillis());
//        test3.setTimeModified(System.currentTimeMillis());
//        test3.setFavorite(1);
//        test3.setReminderId(-1);
//
//        Reminder reminder = new Reminder(DateTime.now(), new FrequencyChoices(2, null));
//        long reminderId = AppDatabase.getAppDatabase().remindersDao().addReminder(reminder);
//        test3.setReminderId((int) reminderId);
//        AppDatabase.getAppDatabase().notesDao().addToUserNotes(test3);


        new AutoDeleteAsync(db, preferences.getDaysInTrash()).execute();

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mBinding.drawerLayout, mBinding.myToolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close) {

            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                super.onDrawerSlide(drawerView, slideOffset);
            }
        };

        mBinding.drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        mBinding.navView.setNavigationItemSelectedListener(this);
        mBinding.navView.getMenu().getItem(0).setChecked(true);


        mainActivityPresenter = new MainActivityPresenter(MainActivity.this);

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


    private final ActionMode.Callback mCallback = new ActionMode.Callback() {

        boolean menuActionPressed = false;

        private void removeViews(ActionMode mode, MenuItem item) {
            menuActionPressed = true;
//            notesAdapter.removeSelectedViews(buffer.currentBufferPositions());
//            showSnackBar(item, 0);
//            mode.finish();
        }

        @Override
        public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
            mActionMode = actionMode;
            mActionMode.getMenuInflater().inflate(R.menu.contextual_action_menu, menu);
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
                case R.id.contextualUnarchive:
                case R.id.contextualDelete:
                case R.id.contextualRestore:
//                    removeViews(mode, item);
//                    multiSelectHelper.setItem(item);
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
            mActionMode = null;
//            notesAdapter.clearSelectedIds();
//
//            // Clear buffer if a CAB item was not pressed (i.e. adding note, open drawer, back pressed)
//            if (!menuItemPressed) {
//                buffer.clearBuffer();
//                notesAdapter.notifyDataSetChanged();
//            }
//
//            menuItemPressed = false;
        }
    };

    private void deleteForever(MenuItem item) {
//        String message = itemHelper.multiSelectMessage(item, buffer.currentBufferSize());
        new AlertDialog.Builder(MainActivity.this)
                .setMessage("")
                .setPositiveButton(getString(R.string.deleteDialog), (dialogInterface, i) -> {
//                    List<SingleNote> temp = new ArrayList<>(buffer.currentBufferNotes());
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
                mainActivityPresenter.onLayoutIconClicked(layoutChoice);
                return true;
        }

        return false;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        notesAdapter.clearView();

        switch (item.getItemId()) {
            case R.id.all_notes_drawer:
            default:
                notesAdapter.setListUsed(0);
                mBinding.myToolbar.setTitle("Notes");
                mBinding.fab.show();
                break;

            case R.id.favorite_notes_drawer:
                notesAdapter.setListUsed(1);
                mBinding.myToolbar.setTitle("Favorite Notes");
                mBinding.fab.show();
                break;

            case R.id.archive_drawer:
                notesAdapter.setListUsed(2);
                mBinding.myToolbar.setTitle("Archive");
                mBinding.fab.hide();
                break;

            case R.id.recycle_bin_drawer:
                notesAdapter.setListUsed(3);
                mBinding.myToolbar.setTitle("Trash");
                mBinding.fab.hide();
                break;
        }

        notesAdapter.updateAdapterList();
        mBinding.drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

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

                NoteAndReminderPreview preview = null;
                if (noteId != -1 && (noteEditorAction != -1 || noteResult != -1)) {
                    preview = AppDatabase.getAppDatabase().previewsDao()
                            .getANotePreview(noteId, listUsed);
                }


                if (preview != null) {
                    if (noteEditorAction != -1) {

//                        if (noteIsFavorite && listUsed == ARCHIVE_NOTES) {
//                            note.setFavorite(true);
//                        }

                    } else {
                        noteModifiedInNoteEditor(preview, notePosition, listUsed, noteResult, noteIsFavorite);
                    }
                }
            }
        }
    }

    private void noteModifiedInNoteEditor(@NonNull NoteAndReminderPreview preview, int notePosition, int listUsed,
                                          int noteModifiedResult, boolean noteIsFavorite) {


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
        if (mainActivityPresenter != null) {
            mainActivityPresenter.onAttach(MainActivity.this);
        }
        super.onStart();
    }

    @Override
    protected void onPause() {
        mainActivityPresenter.onDestroy();
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        preferences.saveLayoutChoice(layoutChoice);
    }

    @Override
    protected void onDestroy() {
        db.close();
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
        Intent intent = new Intent(MainActivity.this, NoteEditorActivity.class);

        intent.putExtra(getString(R.string.noteIdIntent), noteId);
        intent.putExtra(getString(R.string.notePositionIntent), notePosition);
        intent.putExtra(getString(R.string.listUsedIntent), listUsed);

        startActivityForResult(intent, 1);
    }
}