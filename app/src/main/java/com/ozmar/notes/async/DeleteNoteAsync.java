package com.ozmar.notes.async;

import android.os.AsyncTask;
import android.support.annotation.IntRange;

import com.ozmar.notes.database.AppDatabase;


public class DeleteNoteAsync extends AsyncTask<Void, Void, Void> {

    private final int noteId;
    private final int reminderId;
    private final int listUsed;
    private final AppDatabase db;

    public DeleteNoteAsync(int noteId, int reminderId, @IntRange(from = 0, to = 3) int listUsed) {
        this.noteId = noteId;
        this.reminderId = reminderId;
        this.listUsed = listUsed;
        this.db = AppDatabase.getAppDatabase();
    }

    @Override
    protected Void doInBackground(Void... voids) {

        if (listUsed == 0 || listUsed == 1) {
            db.notesDao().deleteFromUserNotes(noteId);
        } else if (listUsed == 2) {
            db.notesDao().deleteFromArchiveNotes(noteId);
        } else if (listUsed == 3) {
            db.notesDao().deleteFromRecycleBinNotes(noteId);
        }

        if (reminderId != -1) {
            db.remindersDao().deleteReminder(reminderId);
        }

        return null;
    }
}
