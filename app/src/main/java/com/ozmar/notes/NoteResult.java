package com.ozmar.notes;


public class NoteResult {

    private final int noteId;
    private final int notePosition;
    private final int listUsed;
    private final int noteModification;
    private final int noteEditorAction;
    private final boolean isFavoriteNote;
    private final boolean isNewNote;

    public NoteResult(int noteId, int notePosition, int listUsed, int noteModification,
                      int noteEditorAction, boolean isFavoriteNote, boolean isNewNote) {
        this.noteId = noteId;
        this.notePosition = notePosition;
        this.listUsed = listUsed;
        this.noteModification = noteModification;
        this.noteEditorAction = noteEditorAction;
        this.isFavoriteNote = isFavoriteNote;
        this.isNewNote = isNewNote;
    }

    public int getNoteId() {
        return noteId;
    }

    public int getNotePosition() {
        return notePosition;
    }

    public int getListUsed() {
        return listUsed;
    }

    public int getNoteModification() {
        return noteModification;
    }

    public int getNoteEditorAction() {
        return noteEditorAction;
    }

    public boolean isFavoriteNote() {
        return isFavoriteNote;
    }

    public boolean isNewNote() {
        return isNewNote;
    }
}
