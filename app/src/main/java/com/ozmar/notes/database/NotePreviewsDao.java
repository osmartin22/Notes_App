package com.ozmar.notes.database;


import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Transaction;
import android.support.annotation.IntRange;

@Dao
public abstract class NotePreviewsDao {

    @Query("SELECT id, title, content, reminderId FROM userNotes WHERE id = :noteId")
    public abstract NotePreviewWithReminderId getAMainPreview(int noteId);

    @Query("SELECT id, title, content, reminderId FROM archiveNotes WHERE id = :noteId")
    public abstract NotePreviewWithReminderId getAnArchivePreview(int noteId);

    @Query("SELECT id, title, content FROM recycleBinNotes WHERE id =:noteId")
    public abstract NotePreview getARecycleBinPreview(int noteId);


    @Query("SELECT reminderTime, repeatType FROM remindersTable WHERE reminderId = :reminderId")
    public abstract ReminderPreview getReminderPreview(int reminderId);


    @Transaction
    public NoteAndReminderPreview getNoteAndReminderPreview(int noteId,
                                                            @IntRange(from = 0, to = 3) int lisUsed) {
        NotePreviewWithReminderId previewWithReminderId;
        if (lisUsed == 0 || lisUsed == 1) {
            previewWithReminderId = getAMainPreview(noteId);

        } else if (lisUsed == 2) {
            previewWithReminderId = getAnArchivePreview(noteId);

        } else {
            previewWithReminderId = new NotePreviewWithReminderId(getARecycleBinPreview(noteId));
        }


        ReminderPreview reminderPreview = null;
        if (previewWithReminderId.getReminderId() != -1) {
            reminderPreview = getReminderPreview(previewWithReminderId.getReminderId());

        }

        return new NoteAndReminderPreview(previewWithReminderId, reminderPreview);
    }
}
