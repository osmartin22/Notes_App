package com.ozmar.notes.database;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Ignore;

import javax.annotation.Nonnull;


public class NotePreviewWithReminderId extends NotePreview {

    @ColumnInfo(name = "reminderId")
    private int reminderId;

    public NotePreviewWithReminderId(int id, @Nonnull String title, @Nonnull String content,
                                     int reminderId) {
        super(id, title, content);
        this.reminderId = reminderId;
    }

    @Ignore
    public NotePreviewWithReminderId(@Nonnull NotePreview notePreview) {
        super(notePreview.getId(), notePreview.getTitle(), notePreview.getContent());
        this.reminderId = -1;
    }


    public int getReminderId() {
        return reminderId;
    }

    public void setReminderId(int reminderId) {
        this.reminderId = reminderId;
    }
}
