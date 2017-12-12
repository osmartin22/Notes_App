package com.ozmar.notes;


import android.support.annotation.NonNull;

import com.ozmar.notes.database.NoteAndReminderPreview;

import java.util.List;

public interface MainActivityView {


    // Possible problem, FAB has the function launchNoteEditor()
//    public void launchNoteEditor();

//    public void bufferIsFull();

    void openNoteEditorActivity(int notePosition, int listUsed);

    void swapLayout(int layout);

    void noteModifiedInNoteEditor(@NonNull NoteAndReminderPreview preview, int notePosition, int listUsed,
                                  int noteModifiedResult, boolean noteIsFavorite);

    void updateAdapterList(List<NoteAndReminderPreview> list);

    void startMultiSelect(int position);

    void multiSelect(int position);

    void showSnackBar(int cabAction);

    void notifyEntireAdapter();

    List<Integer> getSelectedPositions();

    void clearSelectedPositions();

    void removeSelectedPreviews();

    void addBackSelectedPreviews(List<Integer> selectedPositions, List<NoteAndReminderPreview> selectedPreviews);

    void finishMultiSelectCAB();

    void dismissSnackBar();


//     void onDeleteForeverDialog();


}
