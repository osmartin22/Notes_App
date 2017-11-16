package com.ozmar.notes.NoteEditor;


import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.ozmar.notes.FrequencyChoices;
import com.ozmar.notes.SingleNote;

public interface NoteEditorView {

    void setupNoteEditTexts(@NonNull SingleNote note);

    void showReminder(@NonNull SingleNote note, long reminderTime);

    void hideReminder();

    void updateReminderDisplay(@NonNull String newReminderText, @Nullable FrequencyChoices choices);

    void goBackToMainActivity(@Nullable SingleNote note, int result, int listUsed);

    void updateFavoriteIcon(boolean favorite);

    void requestFocusOnContent();

    void setupReminder(@NonNull SingleNote note);

    void cancelReminder(int noteId);
}
