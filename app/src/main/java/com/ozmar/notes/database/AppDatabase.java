package com.ozmar.notes.database;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;

import com.ozmar.notes.Reminder;


@Database(entities = {MainNote.class, ArchiveNote.class, RecycleBinNote.class, Reminder.class},
        version = 2, exportSchema = false)
@TypeConverters({DaysChosenConverter.class, DateTimeConverter.class})
public abstract class AppDatabase extends RoomDatabase {

    public abstract NotesDao notesDao();

    public abstract RemindersDao remindersDao();

    public abstract NotePreviewsDao previewsDao();

    public abstract MultiSelectDao multiSelectDao();
}
