package com.ozmar.notes.database;


import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;

import javax.annotation.Nonnull;

@Entity(tableName = "archiveNotes")
public class ArchiveNote extends BaseNote implements Cloneable{

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    private int id;

    @ColumnInfo(name = "reminderId")
    private int reminderId;


    public ArchiveNote(int id, @Nonnull String title, @Nonnull String content, long timeCreated,
                       long timeModified, int reminderId) {
        super(title, content, timeCreated, timeModified);
        this.id = id;
        this.reminderId = reminderId;
    }

    @Ignore
    public ArchiveNote(MainNote note) {
        super(note);
        this.id = note.getId();
        this.reminderId = note.getReminderId();
    }


    @Override
    public ArchiveNote clone() throws CloneNotSupportedException {
        return (ArchiveNote) super.clone();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (!(obj instanceof ArchiveNote)) {
            return false;
        }

        ArchiveNote note = (ArchiveNote) obj;

        return note.getTitle().equals(getTitle()) &&
                note.getContent().equals(getContent()) &&
                note.getTimeCreated() == getTimeCreated() &&
                note.getTimeModified() == getTimeModified() &&
                note.reminderId == reminderId;
    }

    @Override
    public int hashCode() {
        int prime = 31;
        int result = 17;
        result = prime * result + id;
        result = prime * result + getTitle().hashCode();
        result = prime * result + getContent().hashCode();
        result = prime * result + Long.valueOf(getTimeCreated()).hashCode();
        result = prime * result + Long.valueOf(getTimeModified()).hashCode();
        result = prime * result + reminderId;
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.getClass().getSimpleName()).append("[ ");
        sb.append("Id: ").append(id);
        sb.append(",    Title: ").append(getTitle());
        sb.append(",    Content: ").append(getContent());
        sb.append(",    TimeModified: ").append(getTimeCreated());
        sb.append(",    ReminderTime: ").append(getTimeModified());
        sb.append(",    ReminderId: ").append(reminderId);
        sb.append(" ]");

        return sb.toString();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getReminderId() {
        return reminderId;
    }

    public void setReminderId(int reminderId) {
        this.reminderId = reminderId;
    }
}
