package com.ozmar.notes.async;


import android.os.AsyncTask;
import android.support.annotation.IntRange;

import com.ozmar.notes.SingleNote;
import com.ozmar.notes.database.AppDatabase;
import com.ozmar.notes.database.NoteConversion;

import javax.annotation.Nonnull;

public class GetNoteAsync extends AsyncTask<Void, Void, SingleNote> {

    public interface GetNoteResult {
        void getNoteResult(SingleNote note);
    }

    private final GetNoteResult mGetNoteResult;

    private static final int USER_NOTES = 0;
    private static final int FAVORITE_NOTES = 1;
    private static final int ARCHIVE_NOTES = 2;
    private static final int RECYCLE_BIN_NOTES = 3;

    private final int noteId;
    private final int listUsed;
    private final AppDatabase db;

    public GetNoteAsync(@Nonnull GetNoteResult getNoteResult, int noteId,
                        @IntRange(from = 0, to = 3) int listUsed) {
        this.noteId = noteId;
        this.listUsed = listUsed;
        this.mGetNoteResult = getNoteResult;
        this.db = AppDatabase.getAppDatabase();
    }

    @Override
    protected SingleNote doInBackground(Void... voids) {
        if (noteId != -1) {
            Object object = null;
            if (listUsed == USER_NOTES || listUsed == FAVORITE_NOTES) {
                object = db.notesDao().getAUserNote(noteId);
            } else if (listUsed == ARCHIVE_NOTES) {
                object = db.notesDao().getAnArchiveNote(noteId);
            } else if (listUsed == RECYCLE_BIN_NOTES) {
                object = db.notesDao().getARecycleBinNotes(noteId);
            }

            return NoteConversion.getToSingleNoteConversion(object, listUsed);
        }
        return null;
    }


    @Override
    protected void onPostExecute(SingleNote note) {
        mGetNoteResult.getNoteResult(note);
    }
}
