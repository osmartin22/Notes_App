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


    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (!(obj instanceof NotePreviewWithReminderId)) {
            return false;
        }

        NotePreviewWithReminderId previewWithReminderId = (NotePreviewWithReminderId) obj;

        return previewWithReminderId.getId() == getId() &&
                previewWithReminderId.getTitle().equals(getTitle()) &&
                previewWithReminderId.getContent().equals(getContent()) &&
                previewWithReminderId.reminderId == reminderId;
    }

    @Override
    public int hashCode() {
        int prime = 31;
        int result = 17;
        result = prime * result + getId();
        result = prime * result + getTitle().hashCode();
        result = prime * result + getContent().hashCode();
        result = prime * result + getReminderId();

        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.getClass().getSimpleName()).append("[ ");
        sb.append("Id: ").append(getId());
        sb.append(",    Title: ").append(getTitle());
        sb.append(",    Content: ").append(getContent());
        sb.append(",    ReminderId: ").append(reminderId);
        sb.append(" ]");

        return sb.toString();
    }


    public int getReminderId() {
        return reminderId;
    }

    public void setReminderId(int reminderId) {
        this.reminderId = reminderId;
    }
}
