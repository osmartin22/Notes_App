package com.ozmar.notes.async;

import android.os.AsyncTask;
import android.util.Log;

import com.ozmar.notes.utils.MenuItemHelper;
import com.ozmar.notes.utils.MultiSelectFlagHelper;
import com.ozmar.notes.utils.UndoBuffer;

/**
 * Created by ozmar on 10/7/2017.
 */

public class DoMenuActionAsync extends AsyncTask<Void, Void, Void> {

    private MultiSelectFlagHelper flagHelper;
    private MenuItemHelper itemHelper;
    private UndoBuffer buffer;
    private int listUsed;

    private int bufferSelected; // 0 = currentBuffer    1 = otherBuffer (

    public DoMenuActionAsync(MultiSelectFlagHelper flagHelper, MenuItemHelper itemHelper,
                             UndoBuffer buffer, int listUsed) {
        this.flagHelper = flagHelper;
        this.itemHelper = itemHelper;
        this.buffer = buffer;
        this.listUsed = listUsed;
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
        Log.d("Async", "start");
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
        flagHelper.setInAsync(false);
        Log.d("Async", "Finished");

        if (flagHelper.isAnotherMultiSelect()) {
            flagHelper.setAnotherMultiSelect(false);
            buffer.clearOtherBuffer();
        } else {
            buffer.clearCurrentBuffer();
        }
    }
}
