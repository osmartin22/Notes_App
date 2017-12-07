package com.ozmar.notes.database;


import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Transaction;
import android.support.annotation.IntRange;

import java.util.ArrayList;
import java.util.List;

@Dao
public abstract class NotePreviewsDao {

    @Query("SELECT id, title, content, reminderId FROM userNotes WHERE id = :noteId")
    public abstract NotePreviewWithReminderId getAMainPreview(int noteId);

    @Query("SELECT id, title, content, reminderId FROM userNotes ORDER BY id DESC")
    public abstract List<NotePreviewWithReminderId> getMainPreviewList();

    @Query("SELECT id, title, content, reminderId FROM userNotes WHERE favorite = 1 ORDER BY id DESC")
    public abstract List<NotePreviewWithReminderId> getFavoritePreviewList();


    @Query("SELECT id, title, content, reminderId FROM archiveNotes WHERE id = :noteId")
    public abstract NotePreviewWithReminderId getAnArchivePreview(int noteId);

    @Query("SELECT id, title, content, reminderId FROM archiveNotes ORDER BY id DESC")
    public abstract List<NotePreviewWithReminderId> getArchivePreviewList();


    @Query("SELECT id, title, content FROM recycleBinNotes WHERE id =:noteId")
    public abstract NotePreview getARecycleBinPreview(int noteId);

    @Query("SELECT id, title, content FROM recycleBinNotes ORDER BY id DESC")
    public abstract List<NotePreview> getRecycleBinPreviewList();


    @Query("SELECT reminderTime, repeatType FROM remindersTable WHERE reminderId = :reminderId")
    public abstract ReminderPreview getReminderPreview(int reminderId);


    @Transaction
    public NoteAndReminderPreview getANotePreview(int noteId,
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

    @Transaction
    public List<NoteAndReminderPreview> getListOfNotePreviews(@IntRange(from = 0, to = 3) int listUsed) {

        List<NoteAndReminderPreview> list = new ArrayList<>();
        if (listUsed != 3) {
            List<NotePreviewWithReminderId> notePreviewList;
            if (listUsed == 0) {
                notePreviewList = getMainPreviewList();
            } else if (listUsed == 1) {
                notePreviewList = getFavoritePreviewList();
            } else {
                notePreviewList = getArchivePreviewList();
            }

            for (NotePreviewWithReminderId note : notePreviewList) {
                ReminderPreview reminderPreview = null;
                if (note.getReminderId() != -1) {
                    reminderPreview = getReminderPreview(note.getReminderId());
                }
                list.add(new NoteAndReminderPreview(note, reminderPreview));
            }

        } else {
            List<NotePreview> recycleBinList = getRecycleBinPreviewList();
            for (NotePreview notePreview : recycleBinList) {
                NotePreviewWithReminderId newPreview = new NotePreviewWithReminderId(notePreview);
                list.add(new NoteAndReminderPreview(newPreview, null));
            }
        }

        return list;
    }
}
