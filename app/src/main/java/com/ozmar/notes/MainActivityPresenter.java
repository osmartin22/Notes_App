package com.ozmar.notes;


import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class MainActivityPresenter {

    private int listUsed = -1;

    private MainActivityView mActivityView;
    private MainActivityInteractor mInteractor;
    private CompositeDisposable mDisposable;


    public MainActivityPresenter(MainActivityView mActivityView) {
        this.mActivityView = mActivityView;
        this.mInteractor = new MainActivityInteractor();
        this.mDisposable = new CompositeDisposable();
    }

    public int getListUsed() {
        return listUsed;
    }

    public void onAttach(MainActivity mainActivityView) {
        this.mActivityView = mainActivityView;
        this.mInteractor = new MainActivityInteractor();
    }

    public void onDestroy() {
        mActivityView = null;
        mDisposable.clear();
    }

    public void onNoteLongClick() {

    }

    public void onNoteClick(int noteId, int notePosition) {
        mActivityView.openNoteEditorActivity(noteId, notePosition, listUsed);
    }

    public void onLayoutIconClicked(int layoutChoice) {
        mActivityView.swapLayout(layoutChoice);
    }

    public void onGetANotePreview(int noteId, int listUsed, int notePosition, int noteModifiedResult,
                                  boolean noteIsFavorite) {
        mDisposable.add(mInteractor.getNotePreview(noteId, listUsed)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(noteAndReminderPreview -> mActivityView.noteModifiedInNoteEditor(
                        noteAndReminderPreview, notePosition, listUsed, noteModifiedResult,
                        noteIsFavorite)));
    }

    public void onGetPreviewList(int listUsed) {
        if (this.listUsed != listUsed) {
            this.listUsed = listUsed;
            mDisposable.add(mInteractor.getListOfPreviewsToShow(listUsed)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(list -> mActivityView.updateAdapterList(list)));
        }
    }


    public void onMenuActionIconClicked() {

    }

    public void onDeleteIconClicked() {

    }

    public void onActivityResult() {

    }
}
