package com.ozmar.notes.async;

import android.os.AsyncTask;

import com.ozmar.notes.DatabaseHandler;
import com.ozmar.notes.SingleNote;

import java.util.List;


public class BasicDBAsync extends AsyncTask<Void, Void, Void> {

    private final DatabaseHandler db;
    private final List<SingleNote> list;
    private final SingleNote note;
    private final int listUsed;
    private final int action;

    public BasicDBAsync(DatabaseHandler db, List<SingleNote> list, SingleNote note, int listUsed, int action) {
        this.db = db;
        this.list = list;
        this.note = note;
        this.listUsed = listUsed;
        this.action = action;
    }

    private void add() {
        switch (listUsed) {
            case 0:
            case 1:
                if (list != null) {
                    db.addListToUserList(list);
                } else {
                    note.setId(db.addNoteToUserList(note));
                }
                break;
            case 2:
                if (list != null) {
                    db.addListToArchive(list);
                } else {
                    db.addNoteToArchive(note);
                }
                break;
            case 3:
                if (list != null) {
                    db.addListToRecycleBin(list);
                } else {
                    db.addNoteToRecycleBin(note);
                }
                break;
        }
    }

    private void delete() {
        switch (listUsed) {
            case 0:
            case 1:
                if (list != null) {
                    db.deleteListFromUserList(list);
                } else {
                    db.deleteNoteFromUserList(note.getId());
                }
                break;
            case 2:
                if (list != null) {
                    db.deleteListFromArchive(list);
                } else {
                    db.deleteNoteFromArchive(note.getId());
                }
                break;
        }
    }

    private void deleteForever() {
        if (list != null) {
            db.deleteListFromRecycleBin(list);
        } else {
            db.deleteNoteFromRecycleBin(note.getId());
        }
    }

    @Override
    protected Void doInBackground(Void... voids) {
        switch (action) {
            case 0:
                add();
                break;
            case 1:
//                update();     // Done in another AsyncTask
                break;
            case 2:
                delete();
                break;
            case 3:
                deleteForever();
                break;
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
