package com.ozmar.notes.database;


import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;

import javax.annotation.Nonnull;

@Entity(tableName = "userNotes")
public class MainNote extends BaseNote {

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
