package com.ozmar.notes.database;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;

import com.ozmar.notes.Reminder;


@Database(entities = {MainNote.class, ArchiveNote.class, RecycleBinNote.class, Reminder.class},
        version = 1, exportSchema = false)
@TypeConverters({DaysChosenConverter.class, DateTimeConverter.class})
public abstract class TestDB extends RoomDatabase {

    public abstract DaoClass mDaoClass();

}
