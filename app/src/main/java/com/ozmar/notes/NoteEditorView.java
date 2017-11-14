package com.ozmar.notes;


import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public interface NoteEditorView {

    void setupNoteEditTexts(@NonNull SingleNote note);

    void displayReminder(@NonNull SingleNote note);

    void updateDisplayReminder(@NonNull String newReminderText, @Nullable FrequencyChoices choices);

    void setupReminder(@NonNull SingleNote note);

    void cancelReminder(int noteId);

    void noteResult(@Nullable SingleNote note, int result);
}
