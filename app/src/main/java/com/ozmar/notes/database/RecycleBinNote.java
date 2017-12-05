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

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
