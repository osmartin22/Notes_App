package com.ozmar.notes.noteEditor;


public class ChangesInNote {

    private final boolean titleChanged;
    private final boolean contentChanged;
    private final boolean favoriteChanged;


    public ChangesInNote(boolean titleChanged, boolean contentChanged, boolean favoriteChanged) {
        this.titleChanged = titleChanged;
        this.contentChanged = contentChanged;
        this.favoriteChanged = favoriteChanged;
    }

    public boolean isTitleChanged() {
        return titleChanged;
    }

    public boolean isContentChanged() {
        return contentChanged;
    }

    public boolean isFavoriteChanged() {
        return favoriteChanged;
    }

    public boolean checkIfAllValuesFalse() {
        return titleChanged || contentChanged || favoriteChanged;
    }
}
