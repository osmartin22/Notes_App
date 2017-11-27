package com.ozmar.notes.database;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import com.ozmar.notes.Reminder;


@Dao
public interface RemindersDao {

    @Query("SELECT reminderTime, repeatType FROM remindersTable WHERE reminderId = :reminderId")
    ReminderPreview getReminderPreview(int reminderId);

    @Insert
    long addReminder(Reminder reminder);

    @Query("SELECT * FROM remindersTable WHERE reminderId = :reminderId")
    Reminder getReminder(int reminderId);
}
