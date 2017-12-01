package com.ozmar.notes.async;

import android.os.AsyncTask;
import android.support.annotation.IntRange;

import com.ozmar.notes.database.AppDatabase;
import com.ozmar.notes.database.NoteAndReminderPreview;

import java.util.List;


public class GetNotePreviewsList extends AsyncTask<Void, Void, List<NoteAndReminderPreview>> {

    public interface PreviewsListResult {
        void getPreviewListResult(List<NoteAndReminderPreview> list);
    }

    private static final int USER_NOTES = 0;
    private static final int FAVORITE_NOTES = 1;
    private static final int ARCHIVE_NOTES = 2;
    private static final int RECYCLE_BIN_NOTES = 3;

    private final PreviewsListResult mPreviewsListResult;
    private final int listUsed;
    private final AppDatabase db;

    public GetNotePreviewsList(PreviewsListResult previewsListResult,
                               @IntRange(from = 0, to = 3) int listUsed) {
        this.mPreviewsListResult = previewsListResult;
        this.listUsed = listUsed;
        this.db = AppDatabase.getAppDatabase();
    }

    @Override
    protected List<NoteAndReminderPreview> doInBackground(Void... voids) {
        return db.previewsDao().getListOfNotePreviews(listUsed);
    }

    @Override
    protected void onPostExecute(List<NoteAndReminderPreview> list) {
        mPreviewsListResult.getPreviewListResult(list);
    }
}
