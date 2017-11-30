package com.ozmar.notes.async;

import android.os.AsyncTask;

import com.ozmar.notes.database.AppDatabase;


public class DeleteNoteAsync extends AsyncTask<Void, Void, Void> {

    private final int noteId;
    private final int reminderId;
    private final int listUsed;

    public DeleteNoteAsync(int noteId, int reminderId, int listUsed) {
        this.noteId = noteId;
        this.reminderId = reminderId;
        this.listUsed = listUsed;
    }

    @Override
    protected Void doInBackground(Void... voids) {

        if (listUsed == 0 || listUsed == 1) {
            AppDatabase.getAppDatabase().notesDao().deleteFromUserNotes(noteId);
        } else if (listUsed == 2) {
            AppDatabase.getAppDatabase().notesDao().deleteFromArchiveNotes(noteId);
        } else if (listUsed == 3) {
            AppDatabase.getAppDatabase().notesDao().deleteFromRecycleBinNotes(noteId);
        }

        if (reminderId != -1) {
            AppDatabase.getAppDatabase().remindersDao().deleteReminder(reminderId);
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
    }
}
