package com.ozmar.notes.utils;

import android.view.MenuItem;

/**
 * Created by ozmar on 10/7/2017.
 */

public class MultiSelectFlagHelper {
    private boolean undoFlag = false;
    private boolean multiSelectFlag = false;
    private boolean noteEditorAction = false;
    private boolean anotherMultiSelect = false;

    private int newNoteAction = -1;
    private int editorAction = -1;
    private MenuItem item = null;

    private boolean inAsync = true;

    public MultiSelectFlagHelper() {

    }

    public boolean isInAsync() {
        return inAsync;
    }

    public void setInAsync(boolean inAsync) {
        this.inAsync = inAsync;
    }

    public int getNewNoteAction() {
        return newNoteAction;
    }

    public void setNewNoteAction(int newNoteAction) {
        this.newNoteAction = newNoteAction;
    }

    public int getEditorAction() {
        return editorAction;
    }

    public void setEditorAction(int editorAction) {
        this.editorAction = editorAction;
    }

    public MenuItem getItem() {
        return item;
    }

    public void setItem(MenuItem item) {
        this.item = item;
    }

    public boolean isAnotherMultiSelect() {
        return anotherMultiSelect;
    }

    public void setAnotherMultiSelect(boolean anotherMultiSelect) {
        this.anotherMultiSelect = anotherMultiSelect;
    }

    public boolean isUndoFlag() {
        return undoFlag;
    }

    public void setUndoFlag(boolean undoFlag) {
        this.undoFlag = undoFlag;
    }

    public boolean isMultiSelectFlag() {
        return multiSelectFlag;
    }

    public void setMultiSelectFlag(boolean multiSelectFlag) {
        this.multiSelectFlag = multiSelectFlag;
    }

    public boolean isNoteEditorAction() {
        return noteEditorAction;
    }

    public void setNoteEditorAction(boolean noteEditorAction) {
        this.noteEditorAction = noteEditorAction;
    }
}
