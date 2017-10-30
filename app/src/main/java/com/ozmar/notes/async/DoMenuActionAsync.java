package com.ozmar.notes.async;

import android.os.AsyncTask;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.Toolbar;

import com.ozmar.notes.DatabaseHandler;
import com.ozmar.notes.NotesAdapter;
import com.ozmar.notes.utils.MenuItemHelper;
import com.ozmar.notes.utils.MultiSelectFlagHelper;
import com.ozmar.notes.utils.UndoBuffer;

import java.lang.ref.WeakReference;

// TODO: Change so that a ProgressBar is used for the RecyclerView
// First empty RecyclerView -> NotesAdapter
// Will no longer need Toolbar or Fab inside AsyncTask

public class DoMenuActionAsync extends AsyncTask<Void, Void, Void> {

    private final WeakReference<Toolbar> weakToolbar;
    private final WeakReference<FloatingActionButton> weakFab;

    private DatabaseHandler db;
    private MultiSelectFlagHelper flagHelper;
    private MenuItemHelper itemHelper;
    private UndoBuffer buffer;
    private int listUsed;
    private NotesAdapter adapter;
    private int bufferInUse;

    public DoMenuActionAsync(DatabaseHandler db, MultiSelectFlagHelper flagHelper, MenuItemHelper itemHelper,
                             UndoBuffer buffer, NotesAdapter adapter, Toolbar toolbar, FloatingActionButton fab) {
        this.db = db;
        this.flagHelper = flagHelper;
        this.itemHelper = itemHelper;
        this.buffer = buffer;
        this.adapter = adapter;
        this.listUsed = adapter.getListUsed();
        this.bufferInUse = buffer.getBufferToProcess();

        this.weakToolbar = new WeakReference<>(toolbar);
        this.weakFab = new WeakReference<>(fab);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected Void doInBackground(Void... voids) {
        if (flagHelper.getEditorAction() != -1) {
            itemHelper.doEditorAction(flagHelper.getEditorAction(), buffer.bufferToProcess(bufferInUse).getNotes(), listUsed);
        } else if (flagHelper.getItem() != null) {
            itemHelper.doCABAction(flagHelper.getItem(), buffer.bufferToProcess(bufferInUse).getNotes(), listUsed);
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        flagHelper.setEditorAction(-1);
        flagHelper.setItem(null);

        buffer.clearBuffer(bufferInUse);

        if (flagHelper.isAnotherMultiSelect()) {
            flagHelper.setAnotherMultiSelect(false);
        }

        Toolbar toolbar = weakToolbar.get();
        FloatingActionButton fab = weakFab.get();

        if (toolbar != null && fab != null) {
            if (flagHelper.isInAsync()) {
                adapter.clearView();
                new NavMenuAsync(db, toolbar, fab, adapter, flagHelper.getCurrentNavMenu()).execute();
            }
        }

        flagHelper.setInAsync(false);
    }
}
