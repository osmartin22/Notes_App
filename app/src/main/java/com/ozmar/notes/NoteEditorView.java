package com.ozmar.notes;


import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public interface NoteEditorView {

    void setupNoteEditTexts(@NonNull SingleNote note);

    void showReminder(@NonNull SingleNote note, long reminderTime);

    void hideReminder();

    void updateReminderDisplay(@NonNull String newReminderText, @Nullable FrequencyChoices choices);

    void goBackToMainActivity(@Nullable SingleNote note, int result);

    void updateFavoriteIcon(boolean favorite);

    void requestFocusOnContent();

    void setupReminder(@NonNull SingleNote note);

    void cancelReminder(int noteId);
}
