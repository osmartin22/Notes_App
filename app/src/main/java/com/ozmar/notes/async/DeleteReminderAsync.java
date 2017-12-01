package com.ozmar.notes.async;


import android.os.AsyncTask;

import com.ozmar.notes.database.AppDatabase;


public class DeleteReminderAsync extends AsyncTask<Void, Void, Void> {

    private final AppDatabase db;
    private final int reminderId;

    public DeleteReminderAsync(int reminderId) {
        this.db = AppDatabase.getAppDatabase();
        this.reminderId = reminderId;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        db.remindersDao().deleteReminder(reminderId);
        return null;
    }
}
