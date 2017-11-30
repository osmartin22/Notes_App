package com.ozmar.notes.async;


import android.os.AsyncTask;
import android.support.annotation.IntRange;

import com.ozmar.notes.SingleNote;
import com.ozmar.notes.database.AppDatabase;
import com.ozmar.notes.database.ArchiveNote;
import com.ozmar.notes.database.MainNote;
import com.ozmar.notes.database.NoteConversion;
import com.ozmar.notes.database.RecycleBinNote;

import javax.annotation.Nonnull;

public class AddNoteAsync extends AsyncTask<Void, Void, Integer> {

    private static final int USER_NOTES = 0;
    private static final int FAVORITE_NOTES = 1;
    private static final int ARCHIVE_NOTES = 2;
    private static final int RECYCLE_BIN_NOTES = 3;

    public interface NewNoteResult {
        void getNewId(int id);
    }

    private final int listUsed;
    private final SingleNote mNote;
    private final AppDatabase db;
    private final NewNoteResult mNewNoteResult;

    public AddNoteAsync(@Nonnull NewNoteResult newNoteResult, @Nonnull SingleNote note,
                        @IntRange(from = 0, to = 3) int listUsed) {
        this.listUsed = listUsed;
        this.mNote = note;
        this.db = AppDatabase.getAppDatabase();
        this.mNewNoteResult = newNoteResult;
    }

    @Override
    protected Integer doInBackground(Void... voids) {
        int newNoteId;
        if (listUsed == USER_NOTES || listUsed == FAVORITE_NOTES) {
            MainNote tempNote = NoteConversion.getMainNoteFromSingleNote(mNote);
            newNoteId = (int) db.notesDao().addToUserNotes(tempNote);
        } else if (listUsed == ARCHIVE_NOTES) {
            ArchiveNote tempNote = NoteConversion.getArchiveNoteFromSingleNote(mNote);
            newNoteId = (int) db.notesDao().addToArchiveNotes(tempNote);
        } else {
            RecycleBinNote tempNote = NoteConversion.getRecycleBinNoteFromSingleNote(mNote);
            newNoteId = (int) db.notesDao().addToRecycleBinNotes(tempNote);
        }

        return newNoteId;
    }

    @Override
    protected void onPostExecute(Integer i) {
        mNewNoteResult.getNewId(i);
    }
}
