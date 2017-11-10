package com.ozmar.notes.async;

import android.os.AsyncTask;

import com.ozmar.notes.DatabaseHandler;
import com.ozmar.notes.SingleNote;
import com.ozmar.notes.utils.NoteChanges;

import java.util.List;


public class UpdateNoteAsync extends AsyncTask<Void, Void, Void> {

    private final DatabaseHandler db;
    private final List<SingleNote> list;
    private final SingleNote note;
    private final int listUsed;
    private final NoteChanges changes;

    public UpdateNoteAsync(DatabaseHandler db, List<SingleNote> list, SingleNote note,
                           int listUsed, NoteChanges changes) {
        this.db = db;
        this.list = list;
        this.note = note;
        this.listUsed = listUsed;
        this.changes = changes;
    }

    @Override
    protected Void doInBackground(Void... voids) {

        // TODO: Implement being able to pass a list
        if (listUsed == 0) {
            db.updateNoteFromUserList(note, changes);
        } else if (listUsed == 2) {
            db.updateNoteFromArchive(note, changes);
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        if (list != null) {
            list.clear();
        }
    }

}
