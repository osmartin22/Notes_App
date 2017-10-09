package com.ozmar.notes.async;

import android.os.AsyncTask;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import com.ozmar.notes.DatabaseHandler;
import com.ozmar.notes.NotesAdapter;
import com.ozmar.notes.utils.MenuItemHelper;
import com.ozmar.notes.utils.MultiSelectFlagHelper;
import com.ozmar.notes.utils.UndoBuffer;


public class DoMenuActionAsync extends AsyncTask<Void, Void, Void> {

    private DatabaseHandler db;
    private MultiSelectFlagHelper flagHelper;
    private MenuItemHelper itemHelper;
    private UndoBuffer buffer;
    private int listUsed;
    private NotesAdapter adapter;
    private Toolbar toolbar;
    private FloatingActionButton fab;

    public DoMenuActionAsync(DatabaseHandler db, MultiSelectFlagHelper flagHelper, MenuItemHelper itemHelper,
                             UndoBuffer buffer, NotesAdapter adapter, Toolbar toolbar, FloatingActionButton fab) {
        this.db = db;
        this.flagHelper = flagHelper;
        this.itemHelper = itemHelper;
        this.buffer = buffer;
        this.adapter = adapter;
        this.listUsed = adapter.getListUsed();
        this.toolbar = toolbar;
        this.fab = fab;

    }

    private void noteEditorAction() {
        if (flagHelper.isAnotherMultiSelect()) {
            Log.d("Async", "Other: Editor");
            itemHelper.doEditorAction(flagHelper.getEditorAction(), buffer.otherBuffer().getNotes(), listUsed);
        } else {
            Log.d("Async", "Current: Editor");
            itemHelper.doEditorAction(flagHelper.getEditorAction(), buffer.currentBufferNotes(), listUsed);
        }
    }

    private void cabAction() {
        if (flagHelper.isAnotherMultiSelect()) {
            Log.d("Async", "Other: CAB");
            itemHelper.doCABAction(flagHelper.getItem(), buffer.otherBuffer().getNotes(), listUsed);
        } else {
            Log.d("Async", "Current: CAB");
            itemHelper.doCABAction(flagHelper.getItem(), buffer.currentBufferNotes(), listUsed);
        }
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected Void doInBackground(Void... voids) {
        if (flagHelper.isNoteEditorAction()) {
            noteEditorAction();
        } else if(flagHelper.getItem() != null){
            cabAction();
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        flagHelper.setUndoFlag(false);
        flagHelper.setNoteEditorAction(false);
        flagHelper.setEditorAction(-1);
        flagHelper.setItem(null);

        if (flagHelper.isAnotherMultiSelect()) {
            flagHelper.setAnotherMultiSelect(false);
            buffer.clearOtherBuffer();
        } else {
            buffer.clearCurrentBuffer();
        }

        if(flagHelper.isInAsync()) {
            adapter.clearView();
            new NavMenuAsync(db, toolbar, fab, adapter, flagHelper.getCurrentNavMenu()).execute();
        }

        flagHelper.setInAsync(false);
    }
}
