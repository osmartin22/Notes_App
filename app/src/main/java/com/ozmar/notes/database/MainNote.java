package com.ozmar.notes.database;


import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;

import javax.annotation.Nonnull;

@Entity(tableName = "userNotes")
public class MainNote extends BaseNote implements Cloneable {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    private int id;

    @ColumnInfo(name = "favorite")
    private int favorite;

    @ColumnInfo(name = "reminderId")
    private int reminderId;


    public MainNote(int id, @Nonnull String title, @Nonnull String content, long timeCreated,
                    long timeModified, int favorite, int reminderId) {
        super(title, content, timeCreated, timeModified);
        this.id = id;
        this.favorite = favorite;
        this.reminderId = reminderId;
    }

    @Ignore
    public MainNote(@Nonnull String title, @Nonnull String content, long timeCreated,
                    int favorite, int reminderId) {
        super(title, content, timeCreated);
        this.favorite = favorite;
        this.reminderId = reminderId;
    }

    @Ignore
    public MainNote(@Nonnull ArchiveNote note) {
        super(note);
        this.id = note.getId();
        this.favorite = 0;
        this.reminderId = note.getReminderId();
    }

    @Ignore
    public MainNote(@Nonnull RecycleBinNote note) {
        super(note);
        this.id = note.getId();
        this.favorite = 0;
        this.reminderId = -1;
    }

    @Override
    public MainNote clone() throws CloneNotSupportedException {
        return (MainNote) super.clone();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (!(obj instanceof MainNote)) {
            return false;
        }

        MainNote note = (MainNote) obj;

        return note.getTitle().equals(getTitle()) &&
                note.getContent().equals(getContent()) &&
                note.getTimeCreated() == getTimeCreated() &&
                note.getTimeModified() == getTimeModified() &&
                note.favorite == favorite &&
                note.reminderId == reminderId;
    }

    @Override
    public int hashCode() {
        int prime = 31;
        int result = 17;
        result = prime * result + id;
        result = prime * result + getTitle().hashCode();
        result = prime * result + getContent().hashCode();
        result = prime * result + getFavorite();
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
        sb.append(",    Favorite: ").append(getFavorite());
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

    public int getFavorite() {
        return favorite;
    }

    public void setFavorite(int favorite) {
        this.favorite = favorite;
    }

    public int getReminderId() {
        return reminderId;
    }

    public void setReminderId(int reminderId) {
        this.reminderId = reminderId;
    }
}
