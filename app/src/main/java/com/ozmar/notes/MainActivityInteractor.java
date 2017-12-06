package com.ozmar.notes;


import com.ozmar.notes.database.AppDatabase;
import com.ozmar.notes.database.NoteAndReminderPreview;

import java.util.List;

import io.reactivex.Maybe;


public class MainActivityInteractor {

    private AppDatabase db;

    public MainActivityInteractor() {
        this.db = AppDatabase.getAppDatabase();
    }

    public Maybe<List<NoteAndReminderPreview>> getListOfPreviewsToShow(int listUsed) {
        return Maybe.fromCallable(() -> db.previewsDao().getListOfNotePreviews(listUsed));
    }

    public Maybe<NoteAndReminderPreview> getNotePreview(int noteId, int listUsed){
        return Maybe.fromCallable(() -> AppDatabase.getAppDatabase().previewsDao()
                .getANotePreview(noteId, listUsed));
    }
}
