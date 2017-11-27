package com.ozmar.notes;


public class MainActivityPresenter {
    private MainActivityView mainActivityView;

    public MainActivityPresenter(MainActivityView mainActivityView) {
        this.mainActivityView = mainActivityView;
    }

    public void onAttach(MainActivity mainActivityView) {
        this.mainActivityView = mainActivityView;
    }

    public void onDestroy() {
        mainActivityView = null;
    }

    public void onNoteLongClick() {

    }

    public void onNoteClick(int noteId, int notePosition, int listUsed) {
        mainActivityView.openNoteEditorActivity(noteId, notePosition, listUsed);
    }

    public void onLayoutIconClicked(int layoutChoice) {
        mainActivityView.swapLayout(layoutChoice);
    }

    public void onNavMenuSelection() {

    }

    public void onMenuActionIconClicked() {

    }

    public void onDeleteIconClicked() {

    }

    public void onActivityResult() {

    }
}
