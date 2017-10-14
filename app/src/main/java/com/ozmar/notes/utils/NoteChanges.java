package com.ozmar.notes.utils;


public class NoteChanges {
    private int noteTextChanges = 0;
    private boolean favoriteChanged = false;

    public NoteChanges() {

    }

    public int getNoteTextChanges() {
        return noteTextChanges;
    }

    public void setNoteTextChanges(int noteTextChanges) {
        this.noteTextChanges = noteTextChanges;
    }

    public boolean isFavoriteChanged() {
        return favoriteChanged;
    }

    public void setFavoriteChanged(boolean favoriteChanged) {
        this.favoriteChanged = favoriteChanged;
    }
}
