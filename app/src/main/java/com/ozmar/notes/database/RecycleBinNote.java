package com.ozmar.notes.database;


import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;

import javax.annotation.Nonnull;

@Entity(tableName = "recycleBinNotes")
public class RecycleBinNote extends BaseNote {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    private int id;

    public RecycleBinNote(int id, @Nonnull String title, @Nonnull String content, long timeCreated,
                          long timeModified) {
        super(title, content, timeCreated, timeModified);
        this.id = id;
    }

    @Ignore
    public RecycleBinNote(MainNote note) {
        super(note);
        this.id = note.getId();
    }

    @Ignore
    public RecycleBinNote(ArchiveNote note) {
        super(note);
        this.id = note.getId();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (!(obj instanceof RecycleBinNote)) {
            return false;
        }

        RecycleBinNote note = (RecycleBinNote) obj;

        return note.getTitle().equals(getTitle()) &&
                note.getContent().equals(getContent()) &&
                note.getTimeCreated() == getTimeCreated() &&
                note.getTimeModified() == getTimeModified();
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
        sb.append(" ]");

        return sb.toString();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
