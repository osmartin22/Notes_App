package com.ozmar.notes.noteEditor;


import android.support.annotation.IntRange;

import com.ozmar.notes.Reminder;
import com.ozmar.notes.SingleNote;
import com.ozmar.notes.database.AppDatabase;
import com.ozmar.notes.database.MainNote;
import com.ozmar.notes.database.NoteConversion;

import javax.annotation.Nonnull;

import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Single;

public class NoteEditorInteractor {

    private static final int USER_NOTES = 0;
    private static final int FAVORITE_NOTES = 1;
    private static final int ARCHIVE_NOTES = 2;
    private static final int RECYCLE_BIN_NOTES = 3;

    private AppDatabase db;

    public NoteEditorInteractor() {
        this.db = AppDatabase.getAppDatabase();
    }

    public long addNote(SingleNote note) {
        MainNote noteToInsert = NoteConversion.getMainNoteFromSingleNote(note);
        return db.notesDao().addToUserNotes(noteToInsert);
    }

    public Maybe<SingleNote> getNote(int noteId, @IntRange(from = 0, to = 3) int listUsed) {
        if (listUsed == USER_NOTES || listUsed == FAVORITE_NOTES) {
            return db.notesDao().getAUserNote(noteId)
                    .map(NoteConversion::getSingleNoteFromMainNote);

        } else if (listUsed == ARCHIVE_NOTES) {
            return db.notesDao().getAnArchiveNote(noteId)
                    .map(NoteConversion::getSingleNoteFromArchiveNote);

        } else {
            return db.notesDao().getARecycleBinNotes(noteId).
                    map(NoteConversion::getSingleNoteFromRecycleBinNote);
        }
    }

    public long addReminder(Reminder reminder) {
        return db.remindersDao().addReminder(reminder);
    }

    public Single<Reminder> getReminder(int reminderId) {
        return db.remindersDao().getReminder(reminderId);
    }

    // Notes in trash can not be updated
    public Completable updateNote(@Nonnull SingleNote note, @IntRange(from = 0, to = 2) int listUsed) {
        return Completable.fromAction(() -> {
            if (listUsed == USER_NOTES || listUsed == FAVORITE_NOTES) {
                db.notesDao().updateAUserNote(NoteConversion.getMainNoteFromSingleNote(note));
            } else {
                db.notesDao().updateAnArchiveNote(NoteConversion.getArchiveNoteFromSingleNote(note));
            }
        });
    }

//    public void updateReminder(Reminder reminder) {
//        new UpdateReminderAsync(reminder).execute();
//    }

    public Completable updateReminder(Reminder reminder) {
        return Completable.fromAction(() -> db.remindersDao().updateReminder(reminder));
    }

    public Completable deleteReminder(Reminder reminder) {
        return Completable.fromAction(() -> db.remindersDao().deleteReminder(reminder));
    }

    public Completable deleteReminder(int reminderId) {
        return Completable.fromAction(() -> db.remindersDao().deleteReminder(reminderId));
//        new DeleteReminderAsync(reminderId).execute();
    }
}
