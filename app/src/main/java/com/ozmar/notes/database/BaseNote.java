package com.ozmar.notes.database;


import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Ignore;

import javax.annotation.Nonnull;

public class BaseNote {

    @ColumnInfo(name = "title")
    private String title;

    @ColumnInfo(name = "content")
    private String content;

    @ColumnInfo(name = "timeCreated")
    private long timeCreated;

    @ColumnInfo(name = "timeModified")
    private long timeModified;


    public BaseNote(@Nonnull String title, @Nonnull String content, long timeCreated, long timeModified) {
        this.title = title;
        this.content = content;
        this.timeCreated = timeCreated;
        this.timeModified = timeModified;
    }

    @Ignore
    public BaseNote(@Nonnull String title, @Nonnull String content, long timeCreated) {
        this.title = title;
        this.content = content;
        this.timeCreated = timeCreated;
        this.timeModified = timeCreated;
    }

    @Ignore
    BaseNote(@Nonnull MainNote note) {
        this.title = note.getTitle();
        this.content = note.getContent();
        this.timeCreated = note.getTimeCreated();
        this.timeModified = note.getTimeModified();
    }

    @Ignore
    BaseNote(@Nonnull ArchiveNote note) {
        this.title = note.getTitle();
        this.content = note.getContent();
        this.timeCreated = note.getTimeCreated();
        this.timeModified = note.getTimeModified();
    }

    @Ignore
    BaseNote(@Nonnull RecycleBinNote note) {
        this.title = note.getTitle();
        this.content = note.getContent();
        this.timeCreated = note.getTimeCreated();
        this.timeModified = note.getTimeModified();
    }


    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public long getTimeCreated() {
        return timeCreated;
    }

    public void setTimeCreated(long timeCreated) {
        this.timeCreated = timeCreated;
    }

    public long getTimeModified() {
        return timeModified;
    }

    public void setTimeModified(long timeModified) {
        this.timeModified = timeModified;
    }
}
