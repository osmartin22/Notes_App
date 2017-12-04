package com.ozmar.notes.async;


import android.os.AsyncTask;

import com.ozmar.notes.database.AppDatabase;
import com.ozmar.notes.database.NoteAndReminderPreview;

import javax.annotation.Nonnull;

public class GetANotePreview extends AsyncTask<Void, Void, NoteAndReminderPreview> {

    public interface PreviewResult {
        void getPreviewResult(NoteAndReminderPreview noteAndReminderPreview);
    }

    private static final int USER_NOTES = 0;
    private static final int FAVORITE_NOTES = 1;
    private static final int ARCHIVE_NOTES = 2;
    private static final int RECYCLE_BIN_NOTES = 3;

    private final int noteId;
    private final int listUsed;
    private final AppDatabase db;
    private final PreviewResult mPreviewResult;

    public GetANotePreview(@Nonnull PreviewResult previewResult, int noteId, int listUsed) {
        this.noteId = noteId;
        this.listUsed = listUsed;
        this.mPreviewResult = previewResult;
        this.db = AppDatabase.getAppDatabase();
    }

    @Override
    protected NoteAndReminderPreview doInBackground(Void... voids) {
//        return db.previewsDao().getANotePreview(noteId, listUsed);
        return null;
    }

    @Override
    protected void onPostExecute(NoteAndReminderPreview noteAndReminderPreview) {
        mPreviewResult.getPreviewResult(noteAndReminderPreview);
    }
}
