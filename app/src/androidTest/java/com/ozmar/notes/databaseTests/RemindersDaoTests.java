package com.ozmar.notes.databaseTests;


import android.arch.core.executor.testing.InstantTaskExecutorRule;
import android.arch.persistence.room.Room;
import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.ozmar.notes.FrequencyChoices;
import com.ozmar.notes.Reminder;
import com.ozmar.notes.database.AppDatabase;

import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;


@RunWith(AndroidJUnit4.class)
public class RemindersDaoTests {
    private AppDatabase mDb;

    private Reminder mReminder;

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Before
    public void createDb() {
        Context context = InstrumentationRegistry.getTargetContext();
        mDb = Room.inMemoryDatabaseBuilder(context, AppDatabase.class)
                .allowMainThreadQueries()
                .build();

        mReminder = new Reminder(1, DateTime.now(), null);
    }

    @After
    public void closeDb() throws IOException {
        mDb.close();
    }

    private void updateReminder(Reminder reminder) {
        reminder.setDateTime(new DateTime(2018, 1, 11, 1, 1));
        reminder.setFrequencyChoices(new FrequencyChoices(0, null));
    }

    @Test
    public void getNonExistentReminder() throws Exception {
        Assert.assertNull(mDb.remindersDao().getReminder(2));
    }

    @Test
    public void writeToAndReadReminder() throws Exception {
        mDb.remindersDao().addReminder(mReminder);

        Assert.assertEquals(mReminder, mDb.remindersDao().getReminder(mReminder.getId()));
    }

    @Test
    public void updateAndReadReminder() throws Exception {
        mDb.remindersDao().addReminder(mReminder);
        updateReminder(mReminder);
        mDb.remindersDao().updateReminder(mReminder);

        Assert.assertEquals(mReminder, mDb.remindersDao().getReminder(mReminder.getId()));
    }

    @Test
    public void deleteReminder() throws Exception {
        mDb.remindersDao().addReminder(mReminder);
        mDb.remindersDao().deleteReminder(mReminder);
        Assert.assertNull(mDb.remindersDao().getReminder(mReminder.getId()));

        mDb.remindersDao().addReminder(mReminder);
        mDb.remindersDao().deleteReminder(mReminder.getId());
        Assert.assertNull(mDb.remindersDao().getReminder(mReminder.getId()));
    }

    @Test
    public void updateReminderTimeAndRead() throws Exception {
        mDb.remindersDao().addReminder(mReminder);

        DateTime newReminderTime = new DateTime(2018, 1, 1, 1, 1);
        mReminder.setDateTime(newReminderTime);
        mDb.remindersDao().updateReminderTime(mReminder.getId(), newReminderTime.getMillis());

        Assert.assertEquals(mReminder, mDb.remindersDao().getReminder(mReminder.getId()));
    }

    @Test
    public void updateEventsOccurred() throws Exception {
        updateReminder(mReminder);
        mDb.remindersDao().addReminder(mReminder);

        if (mReminder.getFrequencyChoices() != null) {
            int eventsOccurred = mReminder.getFrequencyChoices().getRepeatEventsOccurred() + 1;
            mDb.remindersDao().updateEventsOccurred(mReminder.getId(), eventsOccurred);

            int databaseEventsOccurred = mDb.remindersDao().getReminder(mReminder.getId()).getFrequencyChoices().getRepeatEventsOccurred();
            Assert.assertEquals(eventsOccurred, databaseEventsOccurred);

        } else {
            throw new IllegalArgumentException("Did not pass a reminder with a non null FrequencyChoice");
        }
    }
}
