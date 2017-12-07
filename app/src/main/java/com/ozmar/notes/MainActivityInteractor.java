package com.ozmar.notes;


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


public class MainActivityInteractor {

    private static final int USER_NOTES = 0;
    private static final int FAVORITE_NOTES = 1;
    private static final int ARCHIVE_NOTES = 2;
    private static final int RECYCLE_BIN_NOTES = 3;

    private AppDatabase db;

    public MainActivityInteractor() {
        this.db = AppDatabase.getAppDatabase();
    }

    public Maybe<List<NoteAndReminderPreview>> getListOfPreviewsToShow(int listUsed) {
        return Maybe.fromCallable(() -> db.previewsDao().getListOfNotePreviews(listUsed));
    }

    public Maybe<NoteAndReminderPreview> getNotePreview(int noteId, int listUsed) {
        return Maybe.fromCallable(() -> db.previewsDao().getANotePreview(noteId, listUsed));
    }


    public Completable deleteRemindersFromMain(List<MainNote> list) {
        List<Integer> reminderIds = new ArrayList<>();
        for (MainNote note : list) {
            if (note.getReminderId() != -1) {
                reminderIds.add(note.getReminderId());
            }
        }
        return Completable.fromAction(() -> db.multiSelectDao().deleteReminders(reminderIds));
    }

    public Completable deleteRemindersFromArchive(List<ArchiveNote> list) {
        List<Integer> reminderIds = new ArrayList<>();
        for (ArchiveNote note : list) {
            if (note.getReminderId() != -1) {
                reminderIds.add(note.getReminderId());
            }
        }
        return Completable.fromAction(() -> db.multiSelectDao().deleteReminders(reminderIds));
    }

    public <T> Single<List<T>> getListOfNotes(List<Integer> noteIds, int listUsed) {
        if (listUsed == USER_NOTES || listUsed == FAVORITE_NOTES) {
            return db.multiSelectDao().getMainNotes(noteIds);
        } else if (listUsed == ARCHIVE_NOTES) {
            return db.multiSelectDao().getArchiveNotes(noteIds);
        } else {
            return db.multiSelectDao().getRecyclBinNotes(noteIds);
        }
    }

    public Completable deleteListOfNotes(List<Integer> noteIds, int listUsed) {
        if (listUsed == USER_NOTES || listUsed == FAVORITE_NOTES) {
            return Completable.fromAction(() -> db.multiSelectDao().deleteMainNotes(noteIds));
        } else if (listUsed == ARCHIVE_NOTES) {
            return Completable.fromAction(() -> db.multiSelectDao().deleteArchiveNotes(noteIds));
        } else {
            return Completable.fromAction(() -> db.multiSelectDao().deleteRecycleBinNotes(noteIds));
        }
    }

    public Completable addArchiveList(List<ArchiveNote> list, int listToAddTo) {
        Completable completable = null;
        if (listToAddTo == USER_NOTES) {
            completable = Completable.fromAction(() -> db.multiSelectDao().addArchiveListToMainNote(list));
        } else if (listToAddTo == RECYCLE_BIN_NOTES) {
            completable = Completable.fromAction(() -> db.multiSelectDao().addArchiveListToRecycleBin(list));
        }

        return completable;
    }

    public Completable addMainList(List<MainNote> list, int listToAddTo) {
        Completable completable = null;
        if (listToAddTo == ARCHIVE_NOTES) {
            completable = Completable.fromAction(() -> db.multiSelectDao().addMainListToArchive(list));
        } else if (listToAddTo == RECYCLE_BIN_NOTES) {
            completable = Completable.fromAction(() -> db.multiSelectDao().addMainListToRecycleBin(list));
        }

        return completable;
    }

    public Completable addRecycleBinList(List<RecycleBinNote> list) {
        return Completable.fromAction(() -> db.multiSelectDao().addRecycleBinListToMainNote(list));
    }


    public Completable addArchiveListToMain(List<ArchiveNote> list) {
        return Completable.fromAction(() -> db.multiSelectDao().addArchiveListToMainNote(list));
    }

    public Completable addArchiveListToRecycleBin(List<ArchiveNote> list) {
        return Completable.fromAction(() -> db.multiSelectDao().addArchiveListToRecycleBin(list));
    }

    public Completable addMainListToArchive(List<MainNote> list) {
        return Completable.fromAction(() -> db.multiSelectDao().addMainListToArchive(list));
    }
}
