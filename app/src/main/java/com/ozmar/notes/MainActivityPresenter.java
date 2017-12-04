package com.ozmar.notes;


import android.support.annotation.NonNull;

import com.ozmar.notes.database.AppDatabase;
import com.ozmar.notes.database.NoteAndReminderPreview;

import io.reactivex.Maybe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class MainActivityPresenter {
    private MainActivityView mainActivityView;

    private CompositeDisposable mDisposable;

    public MainActivityPresenter(MainActivityView mainActivityView) {
        this.mainActivityView = mainActivityView;
        this.mDisposable = new CompositeDisposable();
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

    private void tempResult(@NonNull NoteAndReminderPreview preview, int notePosition, int listUsed,
                            int noteModifiedResult, boolean noteIsFavorite) {
        mainActivityView.noteModifiedInNoteEditor(preview, notePosition, listUsed,
                noteModifiedResult, noteIsFavorite);
    }

    public void getNotePreview(int noteId, int listUsed, int notePosition, int noteModifiedResult,
                               boolean noteIsFavorite) {
        mDisposable.add(Maybe.fromCallable(() -> AppDatabase.getAppDatabase().previewsDao().getANotePreview(
                noteId, listUsed))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(noteAndReminderPreview -> tempResult(noteAndReminderPreview,
                        notePosition, listUsed, noteModifiedResult, noteIsFavorite)));
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
