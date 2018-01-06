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


    @Override
    public NotePreview clone() throws CloneNotSupportedException {
        return (NotePreview) super.clone();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (!(obj instanceof NotePreview)) {
            return false;
        }

        NotePreview notePreview = (NotePreview) obj;

        return notePreview.id == id &&
                notePreview.title.equals(title) &&
                notePreview.content.equals(content);
    }

    @Override
    public int hashCode() {
        int prime = 31;
        int result = 17;
        result = prime * result + id;
        result = prime * result + title.hashCode();
        result = prime * result + content.hashCode();
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.getClass().getSimpleName()).append("[ ");
        sb.append("Id: ").append(id);
        sb.append(",    Title: ").append(title);
        sb.append(",    Content: ").append(content);
        sb.append(" ]");

        return sb.toString();
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
