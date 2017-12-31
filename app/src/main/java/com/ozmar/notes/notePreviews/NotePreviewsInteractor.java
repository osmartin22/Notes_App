package com.ozmar.notes.notePreviews;


import android.support.annotation.IntRange;
import android.support.annotation.NonNull;

import com.ozmar.notes.database.AppDatabase;
import com.ozmar.notes.database.ArchiveNote;
import com.ozmar.notes.database.MainNote;
import com.ozmar.notes.database.NoteAndReminderPreview;
import com.ozmar.notes.database.RecycleBinNote;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Single;


public class NotePreviewsInteractor {

    private static final int USER_NOTES = 0;
    private static final int FAVORITE_NOTES = 1;
    private static final int ARCHIVE_NOTES = 2;
    private static final int RECYCLE_BIN_NOTES = 3;

    private AppDatabase db;

    public NotePreviewsInteractor(AppDatabase appDatabase) {
        this.db = appDatabase;
    }

    @NonNull
    public Maybe<List<NoteAndReminderPreview>> getListOfPreviewsToShow(
            @IntRange(from = 0, to = 3) int listUsed) {
        return Maybe.fromCallable(() -> db.previewsDao().getListOfNotePreviews(listUsed));
    }

    @NonNull
    public Maybe<NoteAndReminderPreview> getNotePreview(int noteId,
                                                        @IntRange(from = 0, to = 3) int listUsed) {
        return Maybe.fromCallable(() -> db.previewsDao().getANotePreview(noteId, listUsed));
    }

    public Completable deleteNoteFromRecycleBin(int noteId) {
        return Completable.fromAction(() -> db.notesDao().deleteFromRecycleBinNotes(noteId));
    }

    //---------------------------------------------------------------------------------------//
    // Main specific
    //---------------------------------------------------------------------------------------//
    @NonNull
    public Single<List<MainNote>> getListOfMainNotes(@NonNull List<Integer> noteIds) {
        return db.multiSelectDao().getMainNotes(noteIds);
    }

    @NonNull
    public Completable deleteRemindersFromMain(@NonNull List<MainNote> list) {
        List<Integer> reminderIds = new ArrayList<>();
        for (MainNote note : list) {
            if (note.getReminderId() != -1) {
                reminderIds.add(note.getReminderId());
            }
        }
        return Completable.fromAction(() -> db.multiSelectDao().deleteReminders(reminderIds));
    }

    // Notes from Main can only be added to the Archive or RecycleBin list
    @NonNull
    public Completable addMainListTo(@NonNull List<MainNote> list, int listToAddTo) {
        Completable completable = null;
        if (listToAddTo == ARCHIVE_NOTES) {
            completable = Completable.fromAction(() ->
                    db.multiSelectDao().addMainListToArchive(list));

        } else if (listToAddTo == RECYCLE_BIN_NOTES) {
            completable = Completable.fromAction(() ->
                    db.multiSelectDao().addMainListToRecycleBin(list));
        }

        if (completable == null) {
            throw new IllegalArgumentException("Wrong listToAddTo value was passed");
        }

        return completable;
    }


    //---------------------------------------------------------------------------------------//
    // Archive specific
    //---------------------------------------------------------------------------------------//
    @NonNull
    public Single<List<ArchiveNote>> getListOfArchiveNotes(@NonNull List<Integer> noteIds) {
        return db.multiSelectDao().getArchiveNotes(noteIds);
    }

    @NonNull
    public Completable deleteRemindersFromArchive(@NonNull List<ArchiveNote> list) {
        List<Integer> reminderIds = new ArrayList<>();
        for (ArchiveNote note : list) {
            if (note.getReminderId() != -1) {
                reminderIds.add(note.getReminderId());
            }
        }
        return Completable.fromAction(() -> db.multiSelectDao().deleteReminders(reminderIds));
    }

    // Notes from the Archive can only be added to the Main or RecycleBin list
    @NonNull
    public Completable addArchiveListTo(@NonNull List<ArchiveNote> list, int listToAddTo) {
        Completable completable = null;

        if (listToAddTo == USER_NOTES) {
            completable = Completable.fromAction(() ->
                    db.multiSelectDao().addArchiveListToMainNote(list));

        } else if (listToAddTo == RECYCLE_BIN_NOTES) {
            completable = Completable.fromAction(() ->
                    db.multiSelectDao().addArchiveListToRecycleBin(list));
        }

        if (completable == null) {
            throw new IllegalArgumentException("Wrong listToAddTo value was passed");
        }

        return completable;
    }


    //---------------------------------------------------------------------------------------//
    // RecycleBin specific
    //---------------------------------------------------------------------------------------//
    @NonNull
    public Single<List<RecycleBinNote>> getListOfRecycleBinNotes(@NonNull List<Integer> noteIds) {
        return db.multiSelectDao().getRecycleBinNotes(noteIds);
    }

    @NonNull
    public Completable deleteListOfNotes(@NonNull List<Integer> noteIds,
                                         @IntRange(from = 0, to = 3) int listUsed) {
        if (listUsed == USER_NOTES || listUsed == FAVORITE_NOTES) {
            return Completable.fromAction(() -> db.multiSelectDao().deleteMainNotes(noteIds));
        } else if (listUsed == ARCHIVE_NOTES) {
            return Completable.fromAction(() -> db.multiSelectDao().deleteArchiveNotes(noteIds));
        } else {
            return Completable.fromAction(() -> db.multiSelectDao().deleteRecycleBinNotes(noteIds));
        }
    }

    // Notes from the RecycleBin can only be added to the Main list
    @NonNull
    public Completable addRecycleBinListToMain(@NonNull List<RecycleBinNote> list) {
        return Completable.fromAction(() -> db.multiSelectDao().addRecycleBinListToMainNote(list));
    }
}
