package com.ozmar.notes.database;


import android.arch.persistence.room.ColumnInfo;

import javax.annotation.Nonnull;

public class NotePreview {

    @ColumnInfo(name = "id")
    private int id;

    @Nonnull
    @ColumnInfo(name = "title")
    private String title;

    @Nonnull
    @ColumnInfo(name = "content")
    private String content;

    public NotePreview(int id, @Nonnull String title, @Nonnull String content) {
        this.id = id;
        this.title = title;
        this.content = content;
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Nonnull
    public String getTitle() {
        return title;
    }

    public void setTitle(@Nonnull String title) {
        this.title = title;
    }

    @Nonnull
    public String getContent() {
        return content;
    }

    public void setContent(@Nonnull String content) {
        this.content = content;
    }
}
