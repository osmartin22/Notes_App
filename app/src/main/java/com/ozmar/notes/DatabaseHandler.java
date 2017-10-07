package com.ozmar.notes;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHandler extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "UserNotesDB";

    // TABLE_USER_NOTES
    private static final String TABLE_USER_NOTES = "userNotes";
    private static final String KEY_ID = "id";
    private static final String KEY_TITLE = "title";
    private static final String KEY_CONTENT = "content";
    private static final String KEY_FAVORITE = "favorite";

    // TABLE_ARCHIVE
    private static final String TABLE_ARCHIVE = "archive";
    private static final String KEY_ARCHIVE_ID = "id";
    private static final String KEY_ARCHIVE_TITLE = "title";
    private static final String KEY_ARCHIVE_CONTENT = "content";

    // TABLE_RECYCLE_BIN
    private static final String TABLE_RECYCLE_BIN = "recycleBin";
    private static final String KEY_RECYCLE_BIN_ID = "id";
    private static final String KEY_RECYCLE_BIN_TITLE = "title";
    private static final String KEY_RECYCLE_BIN_CONTENT = "content";

    private static final String CREATE_TABLE_USER_NOTES = "CREATE TABLE " + TABLE_USER_NOTES + "(" + KEY_ID
            + " INTEGER PRIMARY KEY, " + KEY_TITLE + " TEXT, " + KEY_CONTENT + " TEXT, "
            + KEY_FAVORITE + " INTEGER)";

    private static final String CREATE_TABLE_ARCHIVE = "CREATE TABLE " + TABLE_ARCHIVE + "(" + KEY_ARCHIVE_ID
            + " INTEGER PRIMARY KEY, " + KEY_ARCHIVE_TITLE + " TEXT, " + KEY_ARCHIVE_CONTENT + " TEXT)";

    private static final String CREATE_TABLE_RECYCLE_BIN = "CREATE TABLE " + TABLE_RECYCLE_BIN + "(" + KEY_RECYCLE_BIN_ID
            + " INTEGER PRIMARY KEY, " + KEY_RECYCLE_BIN_TITLE + " TEXT, " + KEY_RECYCLE_BIN_CONTENT + " TEXT)";

    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(CREATE_TABLE_USER_NOTES);
        sqLiteDatabase.execSQL(CREATE_TABLE_ARCHIVE);
        sqLiteDatabase.execSQL(CREATE_TABLE_RECYCLE_BIN);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        // Example for updating without losing data
        //sqLiteDatabase.execSQL(" ALTER TABLE " + TABLE_USER_NOTES + " ADD COLUMN " + KEY_FAVORITE + " INTEGER DEFAULT 0");

        // OR
        // Do separate code without break so that user gets al the new updates
//        switch (oldVersion) {
//            case 2:
//            case 3:
//        }
    }

    @Override
    public void onDowngrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
//        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_USER_NOTES);
//        onCreate(sqLiteDatabase);
//        super.onDowngrade(db, oldVersion, newVersion);
    }


    //--------------------------------------------------------------------------------------------//
    // User Notes Table Specific Methods
    //--------------------------------------------------------------------------------------------//

    // Get count of the number of notes
    public int getNotesCount() {
        String countQuery = "SELECT * FROM " + TABLE_USER_NOTES;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);

        int count = cursor.getCount();
        cursor.close();

        return count;
    } // getNotesCount() end

    public SingleNote getNote(int id) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_USER_NOTES,
                new String[]{KEY_ID, KEY_TITLE, KEY_CONTENT}, KEY_ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            SingleNote note = new SingleNote(Integer.parseInt(cursor.getString(0)),
                    cursor.getString(1), cursor.getString(2), Integer.parseInt(cursor.getString(3)));
            cursor.close();
            return note;
        }

        return null;
    } // getNote() end

    public List<SingleNote> getUserNotes() {
        List<SingleNote> noteList = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_USER_NOTES + " ORDER BY ROWID DESC";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                SingleNote note = new SingleNote();
                note.set_id(Integer.parseInt(cursor.getString(0)));
                note.set_title(cursor.getString(1));
                note.set_content(cursor.getString(2));

                if (cursor.getString(3) == null) {
                    note.set_favorite(0);
                } else {
                    note.set_favorite(Integer.parseInt(cursor.getString(3)));
                }

                noteList.add(note);
            } while (cursor.moveToNext());
        }

        cursor.close();
        return noteList;
    } // getUserNotes() end

    public List<SingleNote> getFavoriteNotes() {
        List<SingleNote> noteList = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_USER_NOTES + " WHERE " + KEY_FAVORITE + " = 1" +
                " ORDER BY ROWID DESC";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                SingleNote note = new SingleNote();
                note.set_id(Integer.parseInt(cursor.getString(0)));
                note.set_title(cursor.getString(1));
                note.set_content(cursor.getString(2));
                note.set_favorite(Integer.parseInt(cursor.getString(3)));

                noteList.add(note);
            } while (cursor.moveToNext());
        }

        cursor.close();
        return noteList;
    } // getFavoriteNotes() end

    // Update a single note
    public int updateNoteFromUserList(SingleNote note) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_TITLE, note.get_title());
        values.put(KEY_CONTENT, note.get_content());
        values.put(KEY_FAVORITE, note.get_favorite());

        return db.update(TABLE_USER_NOTES, values, KEY_ID + " = ?",
                new String[]{String.valueOf(note.get_id())});
    } // updateNoteFromUserList() end

    public void addNoteToUserList(SingleNote note) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_TITLE, note.get_title());
        values.put(KEY_CONTENT, note.get_content());
        values.put(KEY_FAVORITE, note.get_favorite());

        db.insert(TABLE_USER_NOTES, null, values);
    } // addNoteToUserList() end

    public void deleteNoteFromUserList(SingleNote note) {
        SQLiteDatabase db = this.getWritableDatabase();

        db.delete(TABLE_USER_NOTES, KEY_ID + " = ?",
                new String[]{String.valueOf(note.get_id())});
    } // deleteNoteFromUserList() end

    public void addListToUserList(List<SingleNote> list) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        for (int i = 0; i < list.size(); i++) {
            values.put(KEY_TITLE, list.get(i).get_title());
            values.put(KEY_CONTENT, list.get(i).get_content());
            db.insert(TABLE_USER_NOTES, null, values);
            values.clear();
        }
    }

    public void deleteListFromUserList(List<SingleNote> list) {
        SQLiteDatabase db = this.getWritableDatabase();

        for (int i = 0; i < list.size(); i++) {
            db.delete(TABLE_USER_NOTES, KEY_ID + " = ?",
                    new String[]{String.valueOf(list.get(i).get_id())});
        }
    } // deleteListFromUserList() end


    //--------------------------------------------------------------------------------------------//
    // Archive Table Specific Methods
    //--------------------------------------------------------------------------------------------//
    public List<SingleNote> getArchiveNotes() {
        List<SingleNote> noteList = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_ARCHIVE + " ORDER BY ROWID DESC";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                SingleNote note = new SingleNote();
                note.set_id(Integer.parseInt(cursor.getString(0)));
                note.set_title(cursor.getString(1));
                note.set_content(cursor.getString(2));

                noteList.add(note);
            } while (cursor.moveToNext());
        }

        cursor.close();
        return noteList;
    } // getArchiveNotes() end

    public int updateNoteFromArchive(SingleNote note) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_ARCHIVE_TITLE, note.get_title());
        values.put(KEY_ARCHIVE_CONTENT, note.get_content());

        return db.update(TABLE_ARCHIVE, values, KEY_ARCHIVE_ID + " = ?",
                new String[]{String.valueOf(note.get_id())});
    } // updateNoteFromArchive() end

    // Add note to the archive list
    public void addNoteToArchive(SingleNote note) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_ARCHIVE_TITLE, note.get_title());
        values.put(KEY_ARCHIVE_CONTENT, note.get_content());

        db.insert(TABLE_ARCHIVE, null, values);
    } // addNoteToArchive() end

    public void deleteNoteFromArchive(SingleNote note) {
        SQLiteDatabase db = this.getWritableDatabase();

        db.delete(TABLE_ARCHIVE, KEY_ARCHIVE_ID + " = ?",
                new String[]{String.valueOf(note.get_id())});
    } // deleteNoteFromArchive() end

    public void addListToArchive(List<SingleNote> list) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        for (int i = 0; i < list.size(); i++) {
            values.put(KEY_ARCHIVE_TITLE, list.get(i).get_title());
            values.put(KEY_ARCHIVE_CONTENT, list.get(i).get_content());
            db.insert(TABLE_ARCHIVE, null, values);
            values.clear();
        }
    } // addListToArchive() end

    public void deleteListFromArchive(List<SingleNote> list) {
        SQLiteDatabase db = this.getWritableDatabase();

        for (int i = 0; i < list.size(); i++) {
            db.delete(TABLE_ARCHIVE, KEY_ARCHIVE_ID + " = ?",
                    new String[]{String.valueOf(list.get(i).get_id())});
        }
    } // deleteListFromArchive() end


    //--------------------------------------------------------------------------------------------//
    // Recycle Bin Table Specific Methods
    //--------------------------------------------------------------------------------------------//

    // Get all notes in RecycleBin
    public List<SingleNote> getRecycleBinNotes() {
        List<SingleNote> noteList = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_RECYCLE_BIN + " ORDER BY ROWID DESC";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                SingleNote note = new SingleNote();
                note.set_id(Integer.parseInt(cursor.getString(0)));
                note.set_title(cursor.getString(1));
                note.set_content(cursor.getString(2));

                noteList.add(note);
            } while (cursor.moveToNext());
        }

        cursor.close();
        return noteList;
    } // getRecycleBinNotes() end

    // Add note to RecycleBin
    public void addNoteToRecycleBin(SingleNote note) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_RECYCLE_BIN_TITLE, note.get_title());
        values.put(KEY_RECYCLE_BIN_CONTENT, note.get_content());

        db.insert(TABLE_RECYCLE_BIN, null, values);
    } // addNoteToRecycleBin() end


    public void deleteNoteFromRecycleBin(SingleNote note) {
        SQLiteDatabase db = this.getWritableDatabase();

        db.delete(TABLE_RECYCLE_BIN, KEY_RECYCLE_BIN_ID + " = ?",
                new String[]{String.valueOf(note.get_id())});
    } // deleteNoteFromRecycleBin() end

    public void addListToRecycleBin(List<SingleNote> list) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        for (int i = 0; i < list.size(); i++) {
            values.put(KEY_RECYCLE_BIN_TITLE, list.get(i).get_title());
            values.put(KEY_RECYCLE_BIN_CONTENT, list.get(i).get_content());
            db.insert(TABLE_RECYCLE_BIN, null, values);
            values.clear();
        }
    } // addListToRecycleBin() end

    public void deleteListFromRecycleBin(List<SingleNote> list) {
        SQLiteDatabase db = this.getWritableDatabase();

        for (int i = 0; i < list.size(); i++) {
            db.delete(TABLE_RECYCLE_BIN, KEY_RECYCLE_BIN_ID + " = ?",
                    new String[]{String.valueOf(list.get(i).get_id())});
        }
    } // deleteListFromRecycleBin() end

} // DataBaseHandler() end