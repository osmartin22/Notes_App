package com.ozmar.notes.noteEditor;


public class ChangesInNote {

    private final boolean titleChanged;
    private final boolean contentChanged;
    private final boolean favoriteChanged;
    private final boolean reminderTimeChanged;
    private final boolean frequencyChanged;


    public ChangesInNote(boolean titleChanged, boolean contentChanged, boolean favoriteChanged,
                         boolean reminderTimeChanged, boolean frequencyChanged) {
        this.titleChanged = titleChanged;
        this.contentChanged = contentChanged;
        this.favoriteChanged = favoriteChanged;
        this.reminderTimeChanged = reminderTimeChanged;
        this.frequencyChanged = frequencyChanged;
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

    public boolean isReminderTimeChanged() {
        return reminderTimeChanged;
    }

    public boolean isFrequencyChanged() {
        return frequencyChanged;
    }

    public boolean checkIfAllValuesFalse() {
        return titleChanged || contentChanged || favoriteChanged || reminderTimeChanged || frequencyChanged;
    }
}
