package com.ozmar.notes.async;

import android.os.AsyncTask;
import android.util.Log;

import com.ozmar.notes.DatabaseHandler;
import com.ozmar.notes.SingleNote;

import java.util.List;

/**
 * Created by ozmar on 10/7/2017.
 */

public class BasicDBAsync extends AsyncTask<Void, Void, Void> {

    private DatabaseHandler db;
    private List<SingleNote> list;
    private SingleNote note;
    private int listUsed;
    private int action;

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
                    db.addNoteToUserList(note);
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

    private void update() {
        switch (listUsed) {
            case 0:
            case 1:
                if (list != null) {
                    // TODO
                } else {
                    db.updateNoteFromUserList(note);
                }
                break;
            case 2:
                if (list != null) {
                    // TODO
                } else {
                    db.updateNoteFromArchive(note);
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
                    db.deleteNoteFromUserList(note);
                }
                break;
            case 2:
                if (list != null) {
                    db.deleteListFromArchive(list);
                } else {
                    db.deleteNoteFromArchive(note);
                }
                break;
        }
    }

    private void deleteForever() {
        Log.d("IN", "HERE MAN");
        if (list != null) {
            db.deleteListFromRecycleBin(list);
        } else {
            db.deleteNoteFromRecycleBin(note);
        }
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected Void doInBackground(Void... voids) {
        switch (action) {
            case 0:
                add();
                break;
            case 1:
                update();
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
