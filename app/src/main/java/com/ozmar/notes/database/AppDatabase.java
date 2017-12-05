package com.ozmar.notes.database;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;
import android.content.Context;

import com.ozmar.notes.Reminder;


@Database(entities = {MainNote.class, ArchiveNote.class, RecycleBinNote.class, Reminder.class},
        version = 2, exportSchema = false)
@TypeConverters({DaysChosenConverter.class, DateTimeConverter.class})
public abstract class AppDatabase extends RoomDatabase {

    private static AppDatabase INSTANCE;

    public abstract NotesDao notesDao();

    public abstract RemindersDao remindersDao();

    public abstract NotePreviewsDao previewsDao();

    public static void setUpAppDatabase(Context context) {
        if (INSTANCE == null) {
            INSTANCE =
                    Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "notes-database")
                            .fallbackToDestructiveMigration()
                            .build();
        }
    }


    public static AppDatabase getAppDatabase() {
        return INSTANCE;
    }

    public static void destroyInstance() {
        INSTANCE = null;
    }

}
