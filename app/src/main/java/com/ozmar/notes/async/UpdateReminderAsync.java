package com.ozmar.notes.async;

import android.os.AsyncTask;

import com.ozmar.notes.Reminder;
import com.ozmar.notes.database.AppDatabase;

import javax.annotation.Nonnull;


public class UpdateReminderAsync extends AsyncTask<Void, Void, Void> {

    private final AppDatabase db;
    private final Reminder mReminder;

    public UpdateReminderAsync(@Nonnull Reminder reminder) {
        this.db = AppDatabase.getAppDatabase();
        this.mReminder = reminder;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        db.remindersDao().updateReminder(mReminder);
        return null;
    }
}
