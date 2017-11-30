package com.ozmar.notes.noteEditor;


import com.ozmar.notes.Reminder;
import com.ozmar.notes.SingleNote;
import com.ozmar.notes.database.AppDatabase;
import com.ozmar.notes.database.MainNote;
import com.ozmar.notes.database.NoteConversion;

public class NoteEditorInteractor {

    private static final int USER_NOTES = 0;
    private static final int FAVORITE_NOTES = 1;
    private static final int ARCHIVE_NOTES = 2;
    private static final int RECYCLE_BIN_NOTES = 3;

    private AppDatabase db;

    public NoteEditorInteractor() {
        this.db = AppDatabase.getAppDatabase();
    }

    public int addNote(SingleNote note) {
        MainNote noteToInsert = NoteConversion.getMainNoteFromSingleNote(note);
        return (int) db.notesDao().addToUserNotes(noteToInsert);
    }

    // Notes in trash can not be updated
    public void updateNote(SingleNote note, int listUsed) {
        if (listUsed == USER_NOTES || listUsed == FAVORITE_NOTES) {
            db.notesDao().updateAUserNote(NoteConversion.getMainNoteFromSingleNote(note));
        } else if (listUsed == ARCHIVE_NOTES) {
            db.notesDao().updateAnArchiveNote(NoteConversion.getArchiveNoteFromSingleNote(note));
        }
    }


    public SingleNote getNote(int noteId, int listUsed) {
        if (noteId != -1) {
            Object object = null;
            if (listUsed == USER_NOTES || listUsed == FAVORITE_NOTES) {
                object = AppDatabase.getAppDatabase().notesDao().getAUserNote(noteId);
            } else if (listUsed == ARCHIVE_NOTES) {
                object = AppDatabase.getAppDatabase().notesDao().getAnArchiveNote(noteId);
            } else if (listUsed == RECYCLE_BIN_NOTES) {
                object = AppDatabase.getAppDatabase().notesDao().getARecycleBinNotes(noteId);
            }

            return NoteConversion.getToSingleNoteConversion(object, listUsed);
        }
        return null;
    }

    public int addReminder(Reminder reminder) {
        return (int) db.remindersDao().addReminder(reminder);
    }

    public void updateReminder(Reminder reminder) {
        db.remindersDao().updateReminder(reminder);
    }

    public void deleteReminder(int reminderId) {
        db.remindersDao().deleteReminder(reminderId);
    }

    public void deleteReminder(Reminder reminder) {
        db.remindersDao().deleteReminder(reminder);
    }

    public Reminder getReminder(int reminderId) {
        return db.remindersDao().getReminder(reminderId);
    }
}
