package com.ozmar.notes.noteEditor;


import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.ozmar.notes.FrequencyChoices;
import com.ozmar.notes.Reminder;
import com.ozmar.notes.database.MainNote;

public interface NoteEditorView {

    void setupNoteEditTexts(@NonNull MainNote note);

    void showReminder(@NonNull Reminder reminder);

    void hideReminder();

    void updateReminderDisplay(@NonNull String newReminderText, @Nullable FrequencyChoices choices);

    void goBackToMainActivity(@Nullable MainNote note, int result, int listUsed);

    void updateFavoriteIcon(boolean favorite);

    void requestFocusOnContent();

    void setupReminder(@NonNull MainNote note);

    void cancelReminder(int noteId);
}
