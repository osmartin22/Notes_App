package com.ozmar.notes;


public interface MainActivityView {


    // Possible problem, FAB has the function launchNoteEditor()
//    public void launchNoteEditor();

//    public void bufferIsFull();

    void openNoteEditorActivity(int noteId, int notePosition, int listUsed);

     void swapLayout(int layout);

//     void onDeleteForeverDialog();


}
