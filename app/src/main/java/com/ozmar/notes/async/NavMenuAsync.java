package com.ozmar.notes.async;

import android.os.AsyncTask;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.ozmar.notes.DatabaseHandler;
import com.ozmar.notes.NotesAdapter;
import com.ozmar.notes.R;
import com.ozmar.notes.SingleNote;

import java.lang.ref.WeakReference;
import java.util.List;

// TODO: Change so that a ProgressBar is used for the RecyclerView
    // First empty RecyclerView -> NotesAdapter
    // Will no longer need Toolbar or Fab inside AsyncTask

public class NavMenuAsync extends AsyncTask<Void, Void, List<SingleNote>> {

    private final WeakReference<Toolbar> weakToolbar;
    private final WeakReference<FloatingActionButton> weakFab;

    private final DatabaseHandler db;
    private final NotesAdapter adapter;
    private final MenuItem item;

    public NavMenuAsync(DatabaseHandler db, Toolbar toolbar, FloatingActionButton fab, NotesAdapter adapter, MenuItem item) {
        this.db = db;
        this.adapter = adapter;
        this.item = item;
        this.weakToolbar = new WeakReference<>(toolbar);
        this.weakFab = new WeakReference<>(fab);

    }

    @Override
    protected List<SingleNote> doInBackground(Void... voids) {
        switch (item.getItemId()) {
            case R.id.all_notes_drawer:
            default:
                adapter.setListUsed(0);
                return db.getUserNotes();

            case R.id.favorite_notes_drawer:
                adapter.setListUsed(1);
                return db.getFavoriteNotes();

            case R.id.archive_drawer:
                adapter.setListUsed(2);
                return db.getArchiveNotes();

            case R.id.recycle_bin_drawer:
                adapter.setListUsed(3);
                return db.getRecycleBinNotes();
        }
    }

    @Override
    protected void onPostExecute(List<SingleNote> list) {

//        adapter.getList(list);

        Toolbar toolbar = weakToolbar.get();
        FloatingActionButton fab = weakFab.get();

        if (toolbar != null && fab != null) {
            switch (item.getItemId()) {
                case R.id.all_notes_drawer:
                default:
                    toolbar.setTitle("Notes");
                    fab.show();
                    break;

                case R.id.favorite_notes_drawer:
                    toolbar.setTitle("Favorite Notes");
                    fab.show();
                    break;

                case R.id.archive_drawer:
                    toolbar.setTitle("Archive");
                    fab.hide();
                    break;

                case R.id.recycle_bin_drawer:
                    toolbar.setTitle("Trash");
                    fab.hide();
                    break;
            }
        }
    }
}
