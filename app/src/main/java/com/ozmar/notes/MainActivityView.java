package com.ozmar.notes;


import android.support.annotation.NonNull;

import com.ozmar.notes.database.NoteAndReminderPreview;

import java.util.List;

public interface MainActivityView {


    // Possible problem, FAB has the function launchNoteEditor()
//    public void launchNoteEditor();

//    public void bufferIsFull();

    void openNoteEditorActivity(int noteId, int notePosition, int listUsed);

     void swapLayout(int layout);

     void noteModifiedInNoteEditor(@NonNull NoteAndReminderPreview preview, int notePosition, int listUsed,
                                   int noteModifiedResult, boolean noteIsFavorite);

     void updateAdapterList(List<NoteAndReminderPreview> list);

//     void onDeleteForeverDialog();


}
