package com.ozmar.notes.async;

import android.os.AsyncTask;

import com.ozmar.notes.DatabaseHandler;
import com.ozmar.notes.NotesAdapter;
import com.ozmar.notes.SingleNote;

import java.util.List;

public class FetchDBListAsync extends AsyncTask<Void, Void, List<SingleNote>> {

    private DatabaseHandler db;
    private int listChoice;
    private NotesAdapter adapter;

    public FetchDBListAsync(DatabaseHandler db, NotesAdapter adapter, Integer listChoice) {
        this.db = db;
        this.adapter = adapter;
        this.listChoice = listChoice;
    }

    private List<SingleNote> decideAction() {
        switch (listChoice) {
            case 0:
            default:
                return db.getUserNotes();
            case 1:
                return db.getFavoriteNotes();
            case 2:
                return db.getArchiveNotes();
            case 3:
                return db.getRecycleBinNotes();
        }
    }

    @Override
    protected void onPreExecute() {

    }

    @Override
    protected List<SingleNote> doInBackground(Void... voids) {
        return decideAction();
    }

    @Override
    protected void onPostExecute(List<SingleNote> list) {
        adapter.getList(list);
        adapter.notifyDataSetChanged();
    }
}
