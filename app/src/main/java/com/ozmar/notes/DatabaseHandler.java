package com.ozmar.notes;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;


public class DatabaseHandler extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 3;
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


    // TABLE_REMINDERS
    private static final String TABLE_REMINDERS = "frequencyChoices";
    private static final String KEY_NEXT_REMINDER_TIME = "reminder";
    private static final String KEY_REPEAT_TYPE = "repeatType";
    private static final String KEY_REPEAT_EVERY = "repeatTypeHowOften";
    private static final String KEY_REPEAT_FOREVER = "repeatForever";
    private static final String KEY_REPEAT_TO_DATE = "repeatToDate";
    private static final String KEY_REPEAT_EVENTS = "repeatEvents";
    private static final String KEY_MONTH_REPEAT_TYPE = "monthRepeatType";
    private static final String KEY_MONTH_WEEK_TO_REPEAT = "monthWeekToRepeat";
    private static final String KEY_MONTH_DAY_OF_WEEK_TO_REPEAT = "monthDayOfWeekToRepeat";
    private static final String KEY_DAYS_CHOSEN = "daysChosen";
    private static final String KEY_EVENTS_OCCURRED = "eventsOccurred";

    private static final String CREATE_TABLE_REMINDERS = "CREATE TABLE IF NOT EXISTS "
            + TABLE_REMINDERS + "("
            + KEY_ID + " INTEGER PRIMARY KEY, "
            + KEY_NEXT_REMINDER_TIME + " INTEGER, "
            + KEY_REPEAT_TYPE + " INTEGER DEFAULT -1, "
            + KEY_REPEAT_EVERY + " INTEGER, "
            + KEY_REPEAT_FOREVER + " INTEGER, "
            + KEY_REPEAT_TO_DATE + " INTEGER, "
            + KEY_REPEAT_EVENTS + " INTEGER, "
            + KEY_MONTH_REPEAT_TYPE + " INTEGER, "
            + KEY_MONTH_WEEK_TO_REPEAT + " INTEGER, "
            + KEY_MONTH_DAY_OF_WEEK_TO_REPEAT + " INTEGER, "
            + KEY_DAYS_CHOSEN + " TEXT , "
            + KEY_EVENTS_OCCURRED + " INTEGER);";

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
        sqLiteDatabase.execSQL("ALTER TABLE " + TABLE_REMINDERS + " ADD COLUMN " + KEY_EVENTS_OCCURRED + " INTEGER DEFAULT 0");
    }

    @Override
    public void onDowngrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_USER_NOTES);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_ARCHIVE);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_RECYCLE_BIN);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_REMINDERS);
        onCreate(sqLiteDatabase);
    }

    public class NextReminderTime {
        public final long nextReminderTime;
        public final boolean hasFrequencyChoices;

        public NextReminderTime(long nextReminderTime, boolean hasFrequencyChoices) {
            this.nextReminderTime = nextReminderTime;
            this.hasFrequencyChoices = hasFrequencyChoices;
        }
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
    }


    //--------------------------------------------------------------------------------------------//
    // User Notes Table Specific Methods
    //--------------------------------------------------------------------------------------------//
    @Nullable
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

                if (note.get_reminderId() != -1) {
                    NextReminderTime temp = getNextReminderTime(db, note.get_reminderId());
                    note.set_nextReminderTime(temp.nextReminderTime);
                    note.set_hasFrequencyChoices(temp.hasFrequencyChoices);
                }

                noteList.add(note);
            } while (cursor.moveToNext());
        }

        cursor.close();
        return noteList;
    }

    @Nullable
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

                if (note.get_reminderId() != -1) {
                    NextReminderTime temp = getNextReminderTime(db, note.get_reminderId());
                    note.set_nextReminderTime(temp.nextReminderTime);
                    note.set_hasFrequencyChoices(temp.hasFrequencyChoices);
                }

                noteList.add(note);
            } while (cursor.moveToNext());
        }

        cursor.close();
        return noteList;
    }

    @Nullable
    public SingleNote getAUserNote(int id) {
        String selectQuery = "SELECT * FROM " + TABLE_USER_NOTES + " WHERE ROWID = " + id;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        SingleNote note = null;

        if (cursor.moveToFirst()) {
            do {
                note = new SingleNote();
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

                if (note.get_reminderId() != -1) {
                    NextReminderTime temp = getNextReminderTime(db, note.get_reminderId());
                    note.set_nextReminderTime(temp.nextReminderTime);
                    note.set_hasFrequencyChoices(temp.hasFrequencyChoices);
                }
            } while (cursor.moveToNext());
        }

        cursor.close();

        return note;
    }

    public void updateNoteFromUserList(@NonNull SingleNote note, @NonNull ChangesInNote changes) {

        ContentValues values = new ContentValues();

        if (changes.isTitleChanged() || changes.isContentChanged()) {
            if (changes.isTitleChanged()) {
                values.put(KEY_TITLE, note.get_title());
            }
            if (changes.isContentChanged()) {
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

//        if (changes.isReminderIdChanged()) {
        values.put(KEY_REMINDER_ID, note.get_reminderId());
//        }

        if (values.size() != 0) {
            SQLiteDatabase db = this.getWritableDatabase();
            db.update(TABLE_USER_NOTES, values, KEY_ID + " = ?",
                    new String[]{String.valueOf(note.get_id())});
        }
    }

    public int addNoteToUserList(@NonNull SingleNote note) {
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
    }

    public void deleteNoteFromUserList(int id) {
        SQLiteDatabase db = this.getWritableDatabase();

        db.delete(TABLE_USER_NOTES, KEY_ID + " = ?",
                new String[]{String.valueOf(id)});
    }

    public void addListToUserList(@NonNull List<SingleNote> list) {
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

    public void deleteListFromUserList(@NonNull List<SingleNote> list) {
        SQLiteDatabase db = this.getWritableDatabase();

        for (SingleNote note : list) {
            db.delete(TABLE_USER_NOTES, KEY_ID + " = ?",
                    new String[]{String.valueOf(note.get_id())});
        }
    }


    //--------------------------------------------------------------------------------------------//
    // Archive Table Specific Methods
    //--------------------------------------------------------------------------------------------//
    @Nullable
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

                if (note.get_reminderId() != -1) {
                    NextReminderTime temp = getNextReminderTime(db, note.get_reminderId());
                    note.set_nextReminderTime(temp.nextReminderTime);
                    note.set_hasFrequencyChoices(temp.hasFrequencyChoices);
                }

                noteList.add(note);
            } while (cursor.moveToNext());
        }

        cursor.close();
        return noteList;
    }

    @Nullable
    public SingleNote getAnArchiveNote(int id) {
        String selectQuery = "SELECT * FROM " + TABLE_ARCHIVE + " WHERE ROWID = " + id;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        SingleNote note = null;

        if (cursor.moveToFirst()) {
            do {
                note = new SingleNote();
                note.set_id(cursor.getInt(0));
                note.set_title(cursor.getString(1));
                note.set_content(cursor.getString(2));
                note.set_timeCreated(cursor.getLong(3));
                note.set_timeModified(cursor.getLong(4));
                note.set_reminderId(cursor.getInt(5));

                if (note.get_reminderId() != -1) {
                    NextReminderTime temp = getNextReminderTime(db, note.get_reminderId());
                    note.set_nextReminderTime(temp.nextReminderTime);
                    note.set_hasFrequencyChoices(temp.hasFrequencyChoices);
                }
            } while (cursor.moveToNext());
        }

        cursor.close();

        return note;
    }

    public void updateNoteFromArchive(@NonNull SingleNote note, @NonNull ChangesInNote changes) {

        ContentValues values = new ContentValues();

        if (changes.isTitleChanged() || changes.isContentChanged()) {
            if (changes.isTitleChanged()) {
                values.put(KEY_TITLE, note.get_title());
            }
            if (changes.isContentChanged()) {
                values.put(KEY_CONTENT, note.get_content());
            }
            values.put(KEY_TIME_MODIFIED, note.get_timeModified());
        }

//        if (changes.isReminderIdChanged()) {
        values.put(KEY_REMINDER_ID, note.get_reminderId());
//        }

        if (values.size() != 0) {
            SQLiteDatabase db = this.getWritableDatabase();
            db.update(TABLE_ARCHIVE, values, KEY_ID + " = ?",
                    new String[]{String.valueOf(note.get_id())});
        }
    }

    public void addNoteToArchive(@NonNull SingleNote note) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_TITLE, note.get_title());
        values.put(KEY_CONTENT, note.get_content());
        values.put(KEY_TIME_CREATED, note.get_timeCreated());
        values.put(KEY_TIME_MODIFIED, note.get_timeModified());
        values.put(KEY_REMINDER_ID, note.get_reminderId());

        db.insert(TABLE_ARCHIVE, null, values);
    }

    public void deleteNoteFromArchive(int id) {
        SQLiteDatabase db = this.getWritableDatabase();

        db.delete(TABLE_ARCHIVE, KEY_ID + " = ?",
                new String[]{String.valueOf(id)});
    }

    public void addListToArchive(@NonNull List<SingleNote> list) {
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
    }

    public void deleteListFromArchive(@NonNull List<SingleNote> list) {
        SQLiteDatabase db = this.getWritableDatabase();

        for (SingleNote note : list) {
            db.delete(TABLE_ARCHIVE, KEY_ID + " = ?",
                    new String[]{String.valueOf(note.get_id())});
        }
    }


    //--------------------------------------------------------------------------------------------//
    // Recycle Bin Table Specific Methods
    //--------------------------------------------------------------------------------------------//
    @Nullable
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
    }

    @Nullable
    public SingleNote getARecycleBinNote(int id) {
        String selectQuery = "SELECT * FROM " + TABLE_ARCHIVE + " WHERE ROWID = " + id;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        SingleNote note = null;
        if (cursor.moveToFirst()) {
            do {
                note = new SingleNote();
                note.set_id(cursor.getInt(0));
                note.set_title(cursor.getString(1));
                note.set_content(cursor.getString(2));
                note.set_timeCreated(cursor.getLong(3));
                note.set_timeModified(cursor.getLong(4));

            } while (cursor.moveToNext());
        }

        cursor.close();
        return note;
    }

    public void addNoteToRecycleBin(@NonNull SingleNote note) {
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
    }

    public void deleteNoteFromRecycleBin(int id) {
        SQLiteDatabase db = this.getWritableDatabase();

        db.delete(TABLE_RECYCLE_BIN, KEY_ID + " = ?",
                new String[]{String.valueOf(id)});
    }

    public void addListToRecycleBin(@NonNull List<SingleNote> list) {
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
    }

    public void deleteListFromRecycleBin(@NonNull List<SingleNote> list) {
        SQLiteDatabase db = this.getWritableDatabase();

        for (SingleNote note : list) {
            db.delete(TABLE_RECYCLE_BIN, KEY_ID + " = ?",
                    new String[]{String.valueOf(note.get_id())});
        }
    }

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

    @NonNull
    private NextReminderTime getNextReminderTime(@NonNull SQLiteDatabase db, int id) {
        long nextReminderTime = 0;
        boolean isRepeating = false;

        String selectQuery = "SELECT " + KEY_NEXT_REMINDER_TIME + ", " + KEY_REPEAT_TYPE +
                " FROM " + TABLE_REMINDERS + " WHERE ROWID = " + id;
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                nextReminderTime = cursor.getLong(0);
                isRepeating = cursor.getInt(1) != -1;
            } while (cursor.moveToNext());
        }

        cursor.close();
        return new NextReminderTime(nextReminderTime, isRepeating);
    }

    @NonNull
    public NextReminderTime getNextReminderTime(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        return getNextReminderTime(db, id);
    }

    public void updateNextReminderTime(int id, long nextReminderTime) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_NEXT_REMINDER_TIME, nextReminderTime);

        db.update(TABLE_REMINDERS, values, KEY_ID + " = ?",
                new String[]{String.valueOf(id)});
    }

    @Nullable
    public FrequencyChoices getFrequencyChoice(int id) {
        String selectQuery = "SELECT * FROM " + TABLE_REMINDERS + " WHERE ROWID = " + id;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        FrequencyChoices choices = null;
        if (cursor.moveToFirst()) {
            do {
                int repeatType = cursor.getInt(2);
                int repeatEvery = cursor.getInt(3);
                int repeatForever = cursor.getInt(4);
                long repeatToDate = cursor.getLong(5);
                int repeatEvents = cursor.getInt(6);
                int monthRepeatType = cursor.getInt(7);
                int monthWeekToRepeat = cursor.getInt(8);
                int monthDayOfWeekToRepeat = cursor.getInt(9);

                List<Integer> list = null;

                if (cursor.getString(10) != null) {
                    Scanner scanner = new Scanner(cursor.getString(10));
                    list = new ArrayList<>();
                    while (scanner.hasNextInt()) {
                        list.add(scanner.nextInt());
                    }
                }

                choices = new FrequencyChoices(repeatType, repeatEvery, repeatForever, repeatToDate,
                        repeatEvents, monthRepeatType, monthWeekToRepeat, monthDayOfWeekToRepeat, list);

            } while (cursor.moveToNext());
        }

        cursor.close();
        return choices;
    }

    public int addReminder(@Nullable FrequencyChoices choices, long nextReminderTime) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_NEXT_REMINDER_TIME, nextReminderTime);

        if (choices != null) {
            putFrequencyChoiceInValues(values, choices);
        }

        return (int) db.insert(TABLE_REMINDERS, null, values);
    }

    public void updateReminder(int id, @Nullable FrequencyChoices choices, long nextReminderTime) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_NEXT_REMINDER_TIME, nextReminderTime);

        if (choices != null) {
            putFrequencyChoiceInValues(values, choices);

        } else {
            values.put(KEY_REPEAT_TYPE, -1);
        }

        db.update(TABLE_REMINDERS, values, KEY_ID + " = ?",
                new String[]{String.valueOf(id)});
    }

    private void putFrequencyChoiceInValues(@NonNull ContentValues values, @NonNull FrequencyChoices choices) {
        values.put(KEY_REPEAT_TYPE, choices.getRepeatType());
        values.put(KEY_REPEAT_EVERY, choices.getRepeatEvery());
        values.put(KEY_REPEAT_TO_DATE, choices.getRepeatToDate());
        values.put(KEY_REPEAT_EVENTS, choices.getRepeatEvents());
        values.put(KEY_MONTH_REPEAT_TYPE, choices.getMonthRepeatType());
        values.put(KEY_MONTH_WEEK_TO_REPEAT, choices.getMonthWeekToRepeat());
        values.put(KEY_MONTH_DAY_OF_WEEK_TO_REPEAT, choices.getMonthDayOfWeekToRepeat());

        List<Integer> chosenDays = choices.getDaysChosen();
        if (chosenDays != null) {
            StringBuilder daysString = new StringBuilder();
            for (Integer day : choices.getDaysChosen()) {
                daysString.append(day).append(" ");
            }
            values.put(KEY_DAYS_CHOSEN, daysString.toString());
        }

        values.put(KEY_EVENTS_OCCURRED, 0);
    }

    private void deleteReminder(@NonNull SQLiteDatabase db, int id) {
        db.delete(TABLE_REMINDERS, KEY_ID + " = ?",
                new String[]{String.valueOf(id)});
    }

    public void deleteReminder(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        deleteReminder(db, id);
    }

    public int getEventsOccurred(int id) {
        int eventsOccurred = 0;
        SQLiteDatabase db = this.getReadableDatabase();

        String selectQuery = "SELECT " + KEY_EVENTS_OCCURRED + " FROM " +
                TABLE_REMINDERS + " WHERE ROWID = " + id;

        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                eventsOccurred = cursor.getInt(0);
            } while (cursor.moveToNext());
        }

        cursor.close();
        return eventsOccurred;
    }

    public void updateEventsOccurred(int id, int eventsOccurred) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_EVENTS_OCCURRED, eventsOccurred);

        db.update(TABLE_REMINDERS, values, KEY_ID + " = ?",
                new String[]{String.valueOf(id)});
    }

} // DataBaseHandler() end
