package com.ozmar.notes;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public interface NoteEditorView {

    void setUpNoteEditTexts(@NonNull SingleNote note);

    void displayReminder(@NonNull SingleNote note);

    void updateDisplayReminder(Long newReminderTime, String newReminderText, @Nullable FrequencyChoices choices);

    void setupReminder(@NonNull SingleNote note);

    void cancelReminder(int noteId);
}
