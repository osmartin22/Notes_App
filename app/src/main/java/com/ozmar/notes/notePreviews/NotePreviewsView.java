package com.ozmar.notes.notePreviews;


import android.support.annotation.NonNull;

import com.ozmar.notes.database.NoteAndReminderPreview;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface NotePreviewsView {


    // Possible problem, FAB has the function launchNoteEditor()
//    public void launchNoteEditor();

//    public void bufferIsFull();

    void openNoteEditorActivity(int notePosition, int listUsed);

    void swapLayout(int layout);

    void removeAPreview(int position);

    void addAPreview(@NonNull NoteAndReminderPreview preview, int position);

    void updateAPreview(@NonNull NoteAndReminderPreview preview, int position);


    void updateAdapterList(@NonNull List<NoteAndReminderPreview> list);

    void startMultiSelect(int position);

    void multiSelect(int position);

    void showSnackBar(int cabAction, int numOfNotesSelected);

    void notifyEntireAdapter();

    void clearSelectedPositions();

    void removeSelectedPreviews();

    void addBackSelectedPreviews(@NonNull List<Integer> selectedPositions,
                                 @NotNull List<NoteAndReminderPreview> selectedPreviews);

    void finishMultiSelectCAB();

    void dismissSnackBar();

    void cancelReminderNotifications(@NonNull List<Integer> ReminderIds);


}
