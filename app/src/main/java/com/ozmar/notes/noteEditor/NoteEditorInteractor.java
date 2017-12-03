package com.ozmar.notes.noteEditor;


import android.support.annotation.IntRange;

import com.ozmar.notes.Reminder;
import com.ozmar.notes.SingleNote;
import com.ozmar.notes.async.DeleteReminderAsync;
import com.ozmar.notes.async.UpdateNoteAsync;
import com.ozmar.notes.async.UpdateReminderAsync;
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

    public int addNote(SingleNote note) {
        MainNote noteToInsert = NoteConversion.getMainNoteFromSingleNote(note);
        return (int) db.notesDao().addToUserNotes(noteToInsert);
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

    public Completable addReminder(Reminder reminder) {
        return Completable.fromCallable(() -> db.remindersDao().addReminder(reminder));

//        return Single.fromCallable(()->{
//            db.remindersDao().addReminder(reminder);
//        });


//        return (int) db.remindersDao().addReminder(reminder);
    }


    public Single<Reminder> getReminder(int reminderId) {
        return db.remindersDao().getReminder(reminderId);
    }

    // Notes in trash can not be updated
    public void updateNote(@Nonnull SingleNote note, int listUsed) {
        new UpdateNoteAsync(note, listUsed).execute();
    }

    public void updateReminder(Reminder reminder) {
        new UpdateReminderAsync(reminder).execute();
    }

    public void deleteReminder(Reminder reminder) {
        new DeleteReminderAsync(reminder.getId()).execute();
    }

    public void deleteReminder(int reminderId) {
        new DeleteReminderAsync(reminderId).execute();
        new DeleteReminderAsync(reminderId).execute();
    }
}
