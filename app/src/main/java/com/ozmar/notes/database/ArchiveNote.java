package com.ozmar.notes.database;


import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;

import javax.annotation.Nonnull;

@Entity(tableName = "archiveNotes")
public class ArchiveNote extends BaseNote {

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
