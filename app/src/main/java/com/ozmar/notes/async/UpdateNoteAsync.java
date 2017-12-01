package com.ozmar.notes.async;


import android.os.AsyncTask;

import com.ozmar.notes.SingleNote;
import com.ozmar.notes.database.AppDatabase;
import com.ozmar.notes.database.NoteConversion;

import javax.annotation.Nonnull;

public class UpdateNoteAsync extends AsyncTask<Void, Void, Void> {

    private static final int USER_NOTES = 0;
    private static final int FAVORITE_NOTES = 1;
    private static final int ARCHIVE_NOTES = 2;
    private static final int RECYCLE_BIN_NOTES = 3;

    private final AppDatabase db;
    private final SingleNote mNote;
    private final int listUsed;

    public UpdateNoteAsync(@Nonnull SingleNote note, int listUsed) {
        this.db = AppDatabase.getAppDatabase();
        this.mNote = note;
        this.listUsed = listUsed;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        if (listUsed == USER_NOTES || listUsed == FAVORITE_NOTES) {
            db.notesDao().updateAUserNote(NoteConversion.getMainNoteFromSingleNote(mNote));
        } else if (listUsed == ARCHIVE_NOTES) {
            db.notesDao().updateAnArchiveNote(NoteConversion.getArchiveNoteFromSingleNote(mNote));
        }

        return null;
    }
}
