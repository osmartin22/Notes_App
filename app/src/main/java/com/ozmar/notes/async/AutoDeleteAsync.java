package com.ozmar.notes.async;

import android.os.AsyncTask;

import com.ozmar.notes.DatabaseHandler;

/**
 * Created by ozmar on 10/10/2017.
 */

public class AutoDeleteAsync extends AsyncTask<Void, Void, Void> {
    DatabaseHandler db;

    public AutoDeleteAsync(DatabaseHandler db) {
        this.db = db;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected Void doInBackground(Void... voids) {
        db.deleteNotesPastDeleteDay();
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
    }
}
