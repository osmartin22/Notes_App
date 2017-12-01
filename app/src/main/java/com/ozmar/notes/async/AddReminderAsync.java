package com.ozmar.notes.async;


import android.os.AsyncTask;

import com.ozmar.notes.Reminder;
import com.ozmar.notes.database.AppDatabase;

import javax.annotation.Nonnull;

public class AddReminderAsync extends AsyncTask<Void, Void, Integer> {

    public interface NewReminderResult {
        void getNewReminderId(int reminderId);
    }

    private final AppDatabase db;
    private final Reminder mReminder;
    private final NewReminderResult mNewReminderResult;

    public AddReminderAsync(@Nonnull NewReminderResult newReminderResult, @Nonnull Reminder reminder) {
        this.mNewReminderResult = newReminderResult;
        this.mReminder = reminder;
        this.db = AppDatabase.getAppDatabase();
    }

    @Override
    protected Integer doInBackground(Void... voids) {
        return (int) db.remindersDao().addReminder(mReminder);
    }

    @Override
    protected void onPostExecute(Integer integer) {
        mNewReminderResult.getNewReminderId(integer);
    }
}
