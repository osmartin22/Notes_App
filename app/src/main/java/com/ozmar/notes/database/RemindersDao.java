package com.ozmar.notes.database;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.ozmar.notes.Reminder;

import io.reactivex.Single;


@Dao
public interface RemindersDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long addReminder(Reminder reminder);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void updateReminder(Reminder reminder);

    @Query("SELECT * FROM remindersTable WHERE reminderId = :reminderId")
    Single<Reminder> getReminder(int reminderId);

    @Query("DELETE FROM remindersTable WHERE reminderId = :reminderId")
    void deleteReminder(int reminderId);

    @Delete
    void deleteReminder(Reminder reminder);

    @Query("UPDATE remindersTable SET reminderTime = :reminderTime WHERE reminderId = :reminderId")
    void updateReminderTime(int reminderId, long reminderTime);

    @Query("UPDATE remindersTable SET repeatEventsOccurred = :eventsOccurred WHERE reminderId = :reminderId")
    void updateEventsOccurred(int reminderId, int eventsOccurred);
}
