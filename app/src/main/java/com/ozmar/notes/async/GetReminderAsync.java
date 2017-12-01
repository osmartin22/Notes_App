package com.ozmar.notes.async;

import android.os.AsyncTask;

import com.ozmar.notes.Reminder;
import com.ozmar.notes.database.AppDatabase;

import javax.annotation.Nonnull;


public class GetReminderAsync extends AsyncTask<Void, Void, Reminder> {

    public interface ReminderResult {
        void getReminderResult(Reminder reminder);
    }

    private final ReminderResult mReminderResult;

    private final AppDatabase db;
    private final int reminderId;

    public GetReminderAsync(@Nonnull ReminderResult reminderResult, int reminderId) {
        this.mReminderResult = reminderResult;
        this.reminderId = reminderId;
        this.db = AppDatabase.getAppDatabase();
    }

    @Override
    protected Reminder doInBackground(Void... voids) {
        return db.remindersDao().getReminder(reminderId);
    }

    @Override
    protected void onPostExecute(Reminder reminder) {
        mReminderResult.getReminderResult(reminder);
    }
}
