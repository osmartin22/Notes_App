package com.ozmar.notes.noteEditor;


import android.support.annotation.IntRange;

import com.ozmar.notes.Reminder;
import com.ozmar.notes.database.AppDatabase;
import com.ozmar.notes.database.ArchiveNote;
import com.ozmar.notes.database.MainNote;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Single;

public class NoteEditorInteractor {

    private static final int USER_NOTES = 0;
    private static final int FAVORITE_NOTES = 1;
    private static final int ARCHIVE_NOTES = 2;
    private static final int RECYCLE_BIN_NOTES = 3;

    private AppDatabase db;

    @Inject
    public NoteEditorInteractor(AppDatabase appDatabase) {
        this.db = appDatabase;
    }

    public Single<Long> addNote(@Nonnull MainNote note) {
        return Single.fromCallable(() -> db.notesDao().addToUserNotes(note));
    }

    public Maybe<MainNote> getNote(int noteId, @IntRange(from = 0, to = 3) int listUsed) {
        if (listUsed == USER_NOTES || listUsed == FAVORITE_NOTES) {
            return db.notesDao().getAUserNote(noteId);

        } else if (listUsed == ARCHIVE_NOTES) {
            return db.notesDao().getAnArchiveNote(noteId).map(MainNote::new);

        } else {
            return db.notesDao().getARecycleBinNotes(noteId).map(MainNote::new);
        }
    }

    public Completable updateNote(@Nonnull MainNote note, @IntRange(from = 0, to = 2) int listUsed) {
        return Completable.fromAction(() -> {
            if (listUsed == USER_NOTES || listUsed == FAVORITE_NOTES) {
                db.notesDao().updateAUserNote(note);
            } else {
                db.notesDao().updateAnArchiveNote(new ArchiveNote(note));
            }
        });
    }

//    public Completable deleteNote(){
//
//    }


    public Single<Long> addReminder(@Nonnull Reminder reminder) {
        return Single.fromCallable(() -> db.remindersDao().addReminder(reminder));
    }

    public Single<Reminder> getReminder(int reminderId) {
        return Single.fromCallable(() -> db.remindersDao().getReminder(reminderId));
    }

    public Completable updateReminder(@Nonnull Reminder reminder) {
        return Completable.fromAction(() -> db.remindersDao().updateReminder(reminder));
    }

    public Completable deleteReminder(@Nonnull Reminder reminder) {
        return Completable.fromAction(() -> db.remindersDao().deleteReminder(reminder));
    }

    public Completable deleteReminder(int reminderId) {
        return Completable.fromAction(() -> db.remindersDao().deleteReminder(reminderId));
    }
}
