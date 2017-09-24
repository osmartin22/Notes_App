package com.ozmar.notes;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHandler extends SQLiteOpenHelper{

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "UserNotesDB";
    private static final String TABLE_USER_NOTES = "userNotes";

    // Not sure about KEY_ID
    private static final String KEY_ID = "id";
    private static final String KEY_TITLE = "title";
    private static final String KEY_CONTENT = "content";

    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String CREATE_USER_NOTES_TABLE = "CREATE TABLE " + TABLE_USER_NOTES + "(" + KEY_ID
                + " INTEGER PRIMARY KEY, "  + KEY_TITLE + " TEXT, " + KEY_CONTENT + " TEXT)";
        sqLiteDatabase.execSQL(CREATE_USER_NOTES_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS" + TABLE_USER_NOTES);
        onCreate(sqLiteDatabase);
    }

    // Add a new note
    public void addNote(SingleNote note) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_TITLE, note.get_title());
        values.put(KEY_CONTENT, note.get_content());

        db.insert(TABLE_USER_NOTES, null, values);
    } // launchNoteEditor() end

    // NOTE: Not in use since my notes are not using unique keys
    // Get a single note
//    public SingleNote getNote(int id) {
//        SQLiteDatabase db = this.getReadableDatabase();
//
//        Cursor cursor = db.query(TABLE_USER_NOTES,
//                new String[] { KEY_ID, KEY_TITLE, KEY_CONTENT }, KEY_ID + "=?",
//                new String[] { String.valueOf(id) }, null, null, null, null);
//
//        if( cursor != null && cursor.moveToFirst()) {
//            SingleNote note = new SingleNote(Integer.parseInt(cursor.getString(0)),
//                    cursor.getString(1), cursor.getString(2));
//            cursor.close();
//            return note;
//        }
//
//        return null;            // WANT TO TEST THIS
//    } // getNote() end

    // Get all the notes
    public List<SingleNote> getAllNotes() {
        List<SingleNote> noteList = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_USER_NOTES + " ORDER BY ROWID DESC";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor =  db.rawQuery(selectQuery, null);

        if(cursor.moveToFirst()) {
            do{
                SingleNote note = new SingleNote();
                note.set_id(Integer.parseInt(cursor.getString(0)));
                note.set_title(cursor.getString(1));
                note.set_content(cursor.getString(2));

                noteList.add(note);
            } while(cursor.moveToNext());
        }

        cursor.close();
        return noteList;
    } // getAllNotes() end

    // Get count of the number of notes
    public int getNotesCount() {
        String countQuery = "SELECT * FROM " + TABLE_USER_NOTES;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);

        int count = cursor.getCount();
        cursor.close();

        return count;
    } // getNotesCount() end

    // Update a single note
    public int updateNote(SingleNote note) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_TITLE, note.get_title());
        values.put(KEY_CONTENT, note.get_content());

        return db.update(TABLE_USER_NOTES, values, KEY_ID + " = ?",
                new String[] {String.valueOf(note.get_id())});
    } // updateNode() end

    // Delete a single note
    public void deleteNote(SingleNote note) {
        SQLiteDatabase db = this.getWritableDatabase();

        db.delete(TABLE_USER_NOTES, KEY_ID + " = ?",
                new String[] {String.valueOf(note.get_id())});
    } // deleteNote() end
}
