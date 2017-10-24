package com.ozmar.notes;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.ozmar.notes.utils.NoteChanges;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class DatabaseHandler extends SQLiteOpenHelper {

    // TODO: Need to Update all get methods to look at TABLE_REMINDER if KEY_REMINDER_ID != -1

    // TODO: Change unnecessary passing of SingleNote when only KEY_ID is needed (i.e deleting)

    private static final int DATABASE_VERSION = 5;
    private static final String DATABASE_NAME = "UserNotesDB";

    // TABLES
    private static final String TABLE_USER_NOTES = "userNotes";
    private static final String TABLE_ARCHIVE = "archive";
    private static final String TABLE_RECYCLE_BIN = "recycleBin";

    // KEYS
    private static final String KEY_ID = "id";
    private static final String KEY_TITLE = "title";
    private static final String KEY_CONTENT = "content";
    private static final String KEY_FAVORITE = "favorite";
    private static final String KEY_TIME_CREATED = "timeCreated";
    private static final String KEY_TIME_MODIFIED = "timeModified";
    private static final String KEY_REMINDER_ID = "reminderId";

    private static final String CREATE_TABLE_USER_NOTES = "CREATE TABLE IF NOT EXISTS "
            + TABLE_USER_NOTES + "("
            + KEY_ID + " INTEGER PRIMARY KEY, "
            + KEY_TITLE + " TEXT, "
            + KEY_CONTENT + " TEXT, "
            + KEY_FAVORITE + " INTEGER, "
            + KEY_TIME_CREATED + " INTEGER, "
            + KEY_TIME_MODIFIED + " INTEGER, "
            + KEY_REMINDER_ID + " INTEGER DEFAULT -1);";

    private static final String CREATE_TABLE_ARCHIVE = "CREATE TABLE IF NOT EXISTS "
            + TABLE_ARCHIVE + "("
            + KEY_ID + " INTEGER PRIMARY KEY, "
            + KEY_TITLE + " TEXT, "
            + KEY_CONTENT + " TEXT, "
            + KEY_TIME_CREATED + " INTEGER, "
            + KEY_TIME_MODIFIED + " INTEGER, "
            + KEY_REMINDER_ID + " INTEGER DEFAULT -1);";

    private static final String CREATE_TABLE_RECYCLE_BIN = "CREATE TABLE IF NOT EXISTS "
            + TABLE_RECYCLE_BIN + "("
            + KEY_ID + " INTEGER PRIMARY KEY, "
            + KEY_TITLE + " TEXT, "
            + KEY_CONTENT + " TEXT, "
            + KEY_TIME_CREATED + " INTEGER, "
            + KEY_TIME_MODIFIED + " INTEGER);";

    private static final String TABLE_REMINDERS = "frequencyChoices";
    private static final String KEY_NEXT_REMINDER_TIME = "reminder";
    private static final String KEY_REPEAT_TYPE = "repeatType";
    private static final String KEY_REPEAT_TYPE_HOW_OFTEN = "repeatTypeHowOften";
    private static final String KEY_REPEAT_TO_DATE = "repeatToDate";
    private static final String KEY_REPEAT_EVENTS = "repeatEvents";
    private static final String KEY_MONTH_REPEAT_TYPE = "monthRepeatType";
    private static final String KEY_DAYS_CHOSEN = "daysChosen";

    private static final String CREATE_TABLE_REMINDERS = "CREATE TABLE IF NOT EXIST "
            + TABLE_REMINDERS + "("
            + KEY_ID + " INTEGER PRIMARY KEY, "
            + KEY_NEXT_REMINDER_TIME + " INTEGER DEFAULT -1, "
            + KEY_REPEAT_TYPE + " INTEGER DEFAULT -1, "
            + KEY_REPEAT_TYPE_HOW_OFTEN + " INTEGER DEFAULT 0, "
            + KEY_REPEAT_TO_DATE + " INTEGER DEFAULT 0, "
            + KEY_REPEAT_EVENTS + " INTEGER DEFAULT 0, "
            + KEY_MONTH_REPEAT_TYPE + " INTEGER DEFAULT 0, "
            + KEY_DAYS_CHOSEN + " TEXT);";

    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(CREATE_TABLE_USER_NOTES);
        sqLiteDatabase.execSQL(CREATE_TABLE_ARCHIVE);
        sqLiteDatabase.execSQL(CREATE_TABLE_RECYCLE_BIN);
        sqLiteDatabase.execSQL(CREATE_TABLE_REMINDERS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
//        Example for updating without losing data
//        sqLiteDatabase.execSQL(" ALTER TABLE " + TABLE_USER_NOTES + " ADD COLUMN " + KEY_REMINDER_ID + " INTEGER DEFAULT 0 ");
//        sqLiteDatabase.execSQL("ALTER TABLE " + TABLE_ARCHIVE + " ADD COLUMN " + KEY_REMINDER_ID + " INTEGER DEFAULT 0 ");
//        sqLiteDatabase.execSQL("ALTER TABLE " + TABLE_RECYCLE_BIN + " ADD COLUMN " + KEY_RECYCLE_BIN_TIME_MODIFIED + " INTEGER DEFAULT 0");

        // OR
        // Do separate code without break so that user gets all the new updates
//        switch (oldVersion) {
//            case 2:
//            case 3:
//        }
    }

    @Override
    public void onDowngrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_USER_NOTES);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_ARCHIVE);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_RECYCLE_BIN);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_REMINDERS);
        onCreate(sqLiteDatabase);
    }

    public int getNotesCount(int table) {
        String countQuery;
        if (table == 0) {
            countQuery = "SELECT * FROM " + TABLE_USER_NOTES;
        } else if (table == 1) {
            countQuery = "SELECT * FROM " + TABLE_ARCHIVE;
        } else {
            countQuery = "SELECT * FROM " + TABLE_RECYCLE_BIN;
        }

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);

        int count = cursor.getCount();
        cursor.close();

        return count;
    } // getNotesCount() end


    //--------------------------------------------------------------------------------------------//
    // User Notes Table Specific Methods
    //--------------------------------------------------------------------------------------------//
    public List<SingleNote> getUserNotes() {
        List<SingleNote> noteList = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_USER_NOTES + " ORDER BY ROWID DESC";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                SingleNote note = new SingleNote();
                note.set_id(cursor.getInt(0));
                note.set_title(cursor.getString(1));
                note.set_content(cursor.getString(2));

                if (cursor.getInt(3) == 0) {
                    note.set_favorite(false);
                } else {
                    note.set_favorite(true);
                }

                note.set_timeCreated(cursor.getLong(4));
                note.set_timeModified(cursor.getLong(5));
                note.set_reminderId(cursor.getInt(6));

                // TODO: Test This
                if (note.get_reminderId() != -1) {
                    NextReminderTime temp = getNextReminderTime(db, note.get_reminderId());
                    note.set_nextReminderTime(temp.nextReminderTime);
                }

                noteList.add(note);
            } while (cursor.moveToNext());
        }

        cursor.close();
        return noteList;
    } // getUserNotes() end

    public List<SingleNote> getFavoriteNotes() {
        List<SingleNote> noteList = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_USER_NOTES
                + " WHERE " + KEY_FAVORITE + " = 1 ORDER BY ROWID DESC";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                SingleNote note = new SingleNote();
                note.set_id(cursor.getInt(0));
                note.set_title(cursor.getString(1));
                note.set_content(cursor.getString(2));

                if (cursor.getInt(3) == 0) {
                    note.set_favorite(false);
                } else {
                    note.set_favorite(true);
                }

                note.set_timeCreated(cursor.getLong(4));
                note.set_timeModified(cursor.getLong(5));
                note.set_reminderId(cursor.getInt(6));

                // TODO: Test This
                if (note.get_reminderId() != -1) {
                    NextReminderTime temp = getNextReminderTime(db, note.get_reminderId());
                    note.set_nextReminderTime(temp.nextReminderTime);
                }

                noteList.add(note);
            } while (cursor.moveToNext());
        }

        cursor.close();
        return noteList;
    } // getFavoriteNotes() end

    public void updateNoteFromUserList(SingleNote note, NoteChanges changes) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        if (changes.getNoteTextChanges() != 0) {
            if (changes.getNoteTextChanges() == 1) {
                values.put(KEY_TITLE, note.get_title());
            } else if (changes.getNoteTextChanges() == 2) {
                values.put(KEY_CONTENT, note.get_content());
            } else if (changes.getNoteTextChanges() == 3) {
                values.put(KEY_TITLE, note.get_title());
                values.put(KEY_CONTENT, note.get_content());
            }
            values.put(KEY_TIME_MODIFIED, note.get_timeModified());
        }

        if (changes.isFavoriteChanged()) {
            if (note.is_favorite()) {
                values.put(KEY_FAVORITE, 1);
            } else {
                values.put(KEY_FAVORITE, 0);
            }
        }

        // TODO: Rewrite
        // Check for changes to nextReminderTime or FrequencyChoices
        // Update the changed value


        if (changes.isReminderTimeChanged()) {
            values.put(KEY_REMINDER_ID, note.get_reminderId());
        }

        db.update(TABLE_USER_NOTES, values, KEY_ID + " = ?",
                new String[]{String.valueOf(note.get_id())});
    }

    public int addNoteToUserList(SingleNote note) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_TITLE, note.get_title());
        values.put(KEY_CONTENT, note.get_content());

        if (note.is_favorite()) {
            values.put(KEY_FAVORITE, 1);
        } else {
            values.put(KEY_FAVORITE, 0);
        }

        values.put(KEY_TIME_CREATED, note.get_timeCreated());
        values.put(KEY_TIME_MODIFIED, note.get_timeModified());
        values.put(KEY_REMINDER_ID, note.get_reminderId());

        return (int) db.insert(TABLE_USER_NOTES, null, values);
    } // addNoteToUserList() end

    public void deleteNoteFromUserList(SingleNote note) {
        SQLiteDatabase db = this.getWritableDatabase();

        db.delete(TABLE_USER_NOTES, KEY_ID + " = ?",
                new String[]{String.valueOf(note.get_id())});
    } // deleteNoteFromUserList() end

    public void addListToUserList(List<SingleNote> list) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        for (SingleNote note : list) {
            values.put(KEY_TITLE, note.get_title());
            values.put(KEY_CONTENT, note.get_content());

            if (note.is_favorite()) {
                values.put(KEY_FAVORITE, 1);
            } else {
                values.put(KEY_FAVORITE, 0);
            }

            values.put(KEY_TIME_CREATED, note.get_timeCreated());
            values.put(KEY_TIME_MODIFIED, note.get_timeModified());
            values.put(KEY_REMINDER_ID, note.get_reminderId());

            db.insert(TABLE_USER_NOTES, null, values);
            values.clear();
        }
    }

    public void deleteListFromUserList(List<SingleNote> list) {
        SQLiteDatabase db = this.getWritableDatabase();

        for (SingleNote note : list) {
            db.delete(TABLE_USER_NOTES, KEY_ID + " = ?",
                    new String[]{String.valueOf(note.get_id())});
        }
    } // deleteListFromUserList() end


    //--------------------------------------------------------------------------------------------//
    // Archive Table Specific Methods
    //--------------------------------------------------------------------------------------------//
    public List<SingleNote> getArchiveNotes() {
        List<SingleNote> noteList = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_ARCHIVE + " ORDER BY ROWID DESC";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                SingleNote note = new SingleNote();
                note.set_id(cursor.getInt(0));
                note.set_title(cursor.getString(1));
                note.set_content(cursor.getString(2));
                note.set_timeCreated(cursor.getLong(3));
                note.set_timeModified(cursor.getLong(4));
                note.set_reminderId(cursor.getInt(5));

                // TODO: Test This
                if (note.get_reminderId() != -1) {
                    NextReminderTime temp = getNextReminderTime(db, note.get_reminderId());
                    note.set_nextReminderTime(temp.nextReminderTime);
                }

                noteList.add(note);
            } while (cursor.moveToNext());
        }

        cursor.close();
        return noteList;
    } // getArchiveNotes() end

    public void updateNoteFromArchive(SingleNote note, NoteChanges changes) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        if (changes.getNoteTextChanges() != 0) {
            if (changes.getNoteTextChanges() == 1) {
                values.put(KEY_TITLE, note.get_title());
            } else if (changes.getNoteTextChanges() == 2) {
                values.put(KEY_CONTENT, note.get_content());
            } else if (changes.getNoteTextChanges() == 3) {
                values.put(KEY_TITLE, note.get_title());
                values.put(KEY_CONTENT, note.get_content());
            }
            values.put(KEY_TIME_MODIFIED, note.get_timeModified());
        }

        // TODO: Rewrite
        if (changes.isReminderTimeChanged()) {
            values.put(KEY_REMINDER_ID, note.get_reminderId());
        }

        db.update(TABLE_ARCHIVE, values, KEY_ID + " = ?",
                new String[]{String.valueOf(note.get_id())});
    }

    public void addNoteToArchive(SingleNote note) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_TITLE, note.get_title());
        values.put(KEY_CONTENT, note.get_content());
        values.put(KEY_TIME_CREATED, note.get_timeCreated());
        values.put(KEY_TIME_MODIFIED, note.get_timeModified());
        values.put(KEY_REMINDER_ID, note.get_reminderId());

        db.insert(TABLE_ARCHIVE, null, values);
    } // addNoteToArchive() end

    public void deleteNoteFromArchive(SingleNote note) {
        SQLiteDatabase db = this.getWritableDatabase();

        db.delete(TABLE_ARCHIVE, KEY_ID + " = ?",
                new String[]{String.valueOf(note.get_id())});
    } // deleteNoteFromArchive() end

    public void addListToArchive(List<SingleNote> list) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        for (SingleNote note : list) {
            values.put(KEY_TITLE, note.get_title());
            values.put(KEY_CONTENT, note.get_content());
            values.put(KEY_TIME_CREATED, note.get_timeCreated());
            values.put(KEY_TIME_MODIFIED, note.get_timeModified());
            values.put(KEY_REMINDER_ID, note.get_reminderId());

            db.insert(TABLE_ARCHIVE, null, values);
            values.clear();
        }
    } // addListToArchive() end

    public void deleteListFromArchive(List<SingleNote> list) {
        SQLiteDatabase db = this.getWritableDatabase();

        for (SingleNote note : list) {
            db.delete(TABLE_ARCHIVE, KEY_ID + " = ?",
                    new String[]{String.valueOf(note.get_id())});
        }
    } // deleteListFromArchive() end


    //--------------------------------------------------------------------------------------------//
    // Recycle Bin Table Specific Methods
    //--------------------------------------------------------------------------------------------//

    // Get all notes in RecycleBin
    public List<SingleNote> getRecycleBinNotes() {
        List<SingleNote> noteList = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_RECYCLE_BIN + " ORDER BY ROWID DESC";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                SingleNote note = new SingleNote();
                note.set_id(cursor.getInt(0));
                note.set_title(cursor.getString(1));
                note.set_content(cursor.getString(2));
                note.set_timeCreated(cursor.getLong(3));
                note.set_timeModified(cursor.getLong(4));

                noteList.add(note);
            } while (cursor.moveToNext());
        }

        cursor.close();
        return noteList;
    } // getRecycleBinNotes() end

    public void addNoteToRecycleBin(SingleNote note) {
        SQLiteDatabase db = this.getWritableDatabase();

        if (note.get_reminderId() != -1) {
            deleteReminder(db, note.get_reminderId());
        }

        ContentValues values = new ContentValues();
        values.put(KEY_TITLE, note.get_title());
        values.put(KEY_CONTENT, note.get_content());
        values.put(KEY_TIME_CREATED, note.get_timeCreated());
        values.put(KEY_TIME_MODIFIED, note.get_timeModified());

        db.insert(TABLE_RECYCLE_BIN, null, values);
    } // addNoteToRecycleBin() end

    public void deleteNoteFromRecycleBin(SingleNote note) {
        SQLiteDatabase db = this.getWritableDatabase();

        db.delete(TABLE_RECYCLE_BIN, KEY_ID + " = ?",
                new String[]{String.valueOf(note.get_id())});
    } // deleteNoteFromRecycleBin() end

    public void addListToRecycleBin(List<SingleNote> list) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        for (SingleNote note : list) {
            if (note.get_reminderId() != -1) {
                deleteReminder(db, note.get_reminderId());
            }

            values.put(KEY_TITLE, note.get_title());
            values.put(KEY_CONTENT, note.get_content());
            values.put(KEY_TIME_CREATED, note.get_timeCreated());
            values.put(KEY_TIME_MODIFIED, System.currentTimeMillis());

            db.insert(TABLE_RECYCLE_BIN, null, values);
            values.clear();
        }
    } // addListToRecycleBin() end

    public void deleteListFromRecycleBin(List<SingleNote> list) {
        SQLiteDatabase db = this.getWritableDatabase();

        for (SingleNote note : list) {
            db.delete(TABLE_RECYCLE_BIN, KEY_ID + " = ?",
                    new String[]{String.valueOf(note.get_id())});
        }
    } // deleteListFromRecycleBin() end

    // TODO: Set daysForDeletion from SharedPreferences
    public void deleteNotesPastDeleteDay(int days) {
        long time = TimeUnit.DAYS.toMillis(days);
        long currentTime = System.currentTimeMillis();

        List<Integer> notesToDelete = new ArrayList<>();
        String selectQuery = "SELECT " + KEY_ID + ", " + KEY_TIME_MODIFIED + " FROM " + TABLE_RECYCLE_BIN;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                if (cursor.getLong(1) + time < currentTime) {
                    notesToDelete.add(cursor.getInt(0));
                }
            } while (cursor.moveToNext());
        }

        for (Integer id : notesToDelete) {
            db.delete(TABLE_RECYCLE_BIN, KEY_ID + " = ?",
                    new String[]{String.valueOf(id)});
        }

        cursor.close();
    }


    //--------------------------------------------------------------------------------------------//
    // Reminders Table Specific Methods
    //--------------------------------------------------------------------------------------------//

    private NextReminderTime getNextReminderTime(SQLiteDatabase db, int id) {
        int nextReminderTime = 0;
        boolean isRepeating = false;

        String selectQuery = "SELECT " + KEY_NEXT_REMINDER_TIME + ", " + KEY_REPEAT_TYPE +
                " FROM " + TABLE_REMINDERS + " WHERE ROWID = " + id;
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                nextReminderTime = cursor.getInt(0);
                isRepeating = cursor.getInt(1) != -1;
            } while (cursor.moveToNext());
        }

        cursor.close();
        return new NextReminderTime(nextReminderTime, isRepeating);
    }

    public NextReminderTime getNextReminderTime(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        return getNextReminderTime(db, id);
    }

    public FrequencyChoices getFrequencyChoices(int id) {
        String selectQuery = "SELECT * FROM " + TABLE_REMINDERS + " WHERE ROWID = " + id;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        FrequencyChoices choices = null;
        if (cursor.moveToFirst()) {
            do {
                choices = new FrequencyChoices();
                choices.setRepeatType(cursor.getInt(1));
                choices.setRepeatTypeHowOften(cursor.getInt(2));
                choices.setRepeatToSpecificDate(cursor.getLong(3));
                choices.setHowManyRepeatEvents(cursor.getInt(4));
                choices.setMonthRepeatType(cursor.getInt(5));

                String days = cursor.getString(6);

                Scanner scanner = new Scanner(days);
                List<Integer> list = new ArrayList<>();
                while (scanner.hasNextInt()) {
                    list.add(scanner.nextInt());
                }
                choices.setDaysChosen(list);

            } while (cursor.moveToNext());
        }

        cursor.close();
        return choices;
    }

    public int addReminder(FrequencyChoices choices, long nextReminderTime) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put(KEY_NEXT_REMINDER_TIME, nextReminderTime);
        if (choices != null) {
            values.put(KEY_REPEAT_TYPE, choices.getRepeatType());
            values.put(KEY_REPEAT_TYPE_HOW_OFTEN, choices.getRepeatTypeHowOften());
            values.put(KEY_REPEAT_TO_DATE, choices.getRepeatToSpecificDate());
            values.put(KEY_REPEAT_EVENTS, choices.getHowManyRepeatEvents());
            values.put(KEY_MONTH_REPEAT_TYPE, choices.getMonthRepeatType());

            String days = "";
            for (Integer day : choices.getDaysChosen()) {
                days += day + " ";
            }
            values.put(KEY_DAYS_CHOSEN, days);
        }

        return (int) db.insert(TABLE_RECYCLE_BIN, null, values);
    }

    public void updateReminder() {
        // TODO: Possibly call in noteUpdate instead
        // Should update UpdateNoteAsync if taking this route
    }

    private void deleteReminder(SQLiteDatabase db, int id) {
        db.delete(TABLE_RECYCLE_BIN, KEY_ID + " = ?",
                new String[]{String.valueOf(id)});
    }

    public void deleteReminder(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        deleteReminder(db, id);
    }

    // Setting KEY_REPEAT_TYPE to -1 denotes that FrequencyChoice is null and will appear as null
    // to the program until a new FrequencyChoice is inserted
    public void partialFrequencyChoiceDelete(int id) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_REPEAT_TYPE, -1);

        db.update(TABLE_ARCHIVE, values, KEY_ID + " = ?",
                new String[]{String.valueOf(id)});
    }


} // DataBaseHandler() end
