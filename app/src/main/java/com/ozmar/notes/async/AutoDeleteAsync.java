package com.ozmar.notes.async;

import android.os.AsyncTask;

import com.ozmar.notes.DatabaseHandler;


public class AutoDeleteAsync extends AsyncTask<Void, Void, Void> {
    private final DatabaseHandler db;
    private final int days;

    public AutoDeleteAsync(DatabaseHandler db, int days) {
        this.db = db;
        this.days = days;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        db.deleteNotesPastDeleteDay(days);
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
    }
}
