package com.ozmar.notes;


import android.support.annotation.NonNull;
import android.util.Log;

import com.ozmar.notes.database.ArchiveNote;
import com.ozmar.notes.database.MainNote;
import com.ozmar.notes.database.NoteAndReminderPreview;
import com.ozmar.notes.database.RecycleBinNote;

import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class MainActivityPresenter {

    private static final int USER_NOTES = 0;
    private static final int FAVORITE_NOTES = 1;
    private static final int ARCHIVE_NOTES = 2;
    private static final int RECYCLE_BIN_NOTES = 3;

    private int listUsed = -1;

    private int listToAddTo = -1;
    private boolean isMultiSelect = false;
    private boolean undoFlagPressed = false;

    private MainActivityView mActivityView;
    private MainActivityInteractor mInteractor;
    private CompositeDisposable mDisposable;

    private List<Integer> selectedIds;
    private List<NoteAndReminderPreview> listOfMultiSelect;


    public MainActivityPresenter(MainActivityView mActivityView) {
        this.mActivityView = mActivityView;
        this.mInteractor = new MainActivityInteractor();
        this.mDisposable = new CompositeDisposable();
    }

    public int getListUsed() {
        return listUsed;
    }

    public int getListToAddTo() {
        return listToAddTo;
    }

    public void setListToAddTo(int listToAddTo) {
        this.listToAddTo = listToAddTo;
    }

    public boolean isUndoFlagPressed() {
        return undoFlagPressed;
    }

    public void setUndoFlagPressed(boolean undoFlagPressed) {
        this.undoFlagPressed = undoFlagPressed;
    }

    public boolean isMultiSelect() {
        return isMultiSelect;
    }

    public void setMultiSelect(boolean multiSelect) {
        isMultiSelect = multiSelect;
    }

    public void onAttach(MainActivity mainActivityView) {
        this.mActivityView = mainActivityView;
        this.mInteractor = new MainActivityInteractor();
    }

    public void onDestroy() {
        mActivityView = null;
        mDisposable.clear();
    }

    public void onNoteLongClick(int position) {
        if (isMultiSelect) {
            mActivityView.multiSelect(position);

        } else {
            isMultiSelect = true;
            mActivityView.startMultiSelect(position);
        }
    }

    public void onNoteClick(int noteId, int notePosition) {
        if (isMultiSelect) {
            mActivityView.multiSelect(notePosition);

        } else {
            mActivityView.openNoteEditorActivity(noteId, notePosition, listUsed);
        }
    }

    public void onUndoClicked() {
        undoFlagPressed = true;
        // TODO: Decide how to handle a new note undo from NoteEditor
        // Can maybe add a new note to the database
        // Add note to notesAdapter but don't update views

        // TODO: add back views
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

    public void onMenuActionIconClicked(List<NoteAndReminderPreview> list, int cabAction, int listToAddTo) {
        listOfMultiSelect = list;
        this.listToAddTo = listToAddTo;
        mActivityView.removeSelectedPreviews();
        mActivityView.showSnackBar(cabAction);
    }

    public void onDeleteIconClicked() {

    }

    public void onActivityResult() {

    }


    // TODO: Remember to cancel reminder notifications when deleting reminders
    public void processChosenNotes(@NonNull List<Integer> noteIds) {

        if (listUsed == USER_NOTES || listUsed == FAVORITE_NOTES) {
            getListOfMainNotes(noteIds, listToAddTo);
        } else if (listUsed == ARCHIVE_NOTES) {
            getListOfArchiveNotes(noteIds, listToAddTo);
        } else {
            getListOfRecycleBinNotes(noteIds);
        }

        deleteList(noteIds, listUsed);
    }

    private void deleteList(@NonNull List<Integer> noteIds, int listUsed) {
        mDisposable.add(mInteractor.deleteListOfNotes(noteIds, listUsed)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe());
    }

    private void onProcessChosenNotesFinished() {
        Log.d("Process Notes", "Finished processing");
        listToAddTo = -1;
        isMultiSelect = false;
        undoFlagPressed = false;
        listOfMultiSelect = null;
    }


    //---------------------------------------------------------------------------------------//
    // Main specific
    //---------------------------------------------------------------------------------------//
    private void getListOfMainNotes(@NonNull List<Integer> noteIds, int listToAddTo) {
        mDisposable.add(mInteractor.getListOfMainNotes(noteIds)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(list -> addMainListTo(list, listToAddTo)));
    }

    private void addMainListTo(@NonNull List<MainNote> list, int listToAddTo) {
        if (listToAddTo == RECYCLE_BIN_NOTES) {
            deleteRemindersFromMain(list);
        }

        mDisposable.add(mInteractor.addMainListTo(list, listToAddTo)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::onProcessChosenNotesFinished));
    }

    private void deleteRemindersFromMain(@NonNull List<MainNote> list) {
        mDisposable.add(mInteractor.deleteRemindersFromMain(list)
                .subscribeOn(Schedulers.io())
                .subscribe());
    }


    //---------------------------------------------------------------------------------------//
    // Archive specific
    //---------------------------------------------------------------------------------------//
    private void getListOfArchiveNotes(@NonNull List<Integer> noteIds, int listToAddTo) {
        mDisposable.add(mInteractor.getListOfArchiveNotes(noteIds)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(list -> addArchiveListTo(list, listToAddTo)));
    }

    private void addArchiveListTo(@NonNull List<ArchiveNote> list, int listToAddTo) {
        if (listToAddTo == RECYCLE_BIN_NOTES) {
            deleteRemindersFromArchive(list);
        }

        mDisposable.add(mInteractor.addArchiveListTo(list, listToAddTo)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::onProcessChosenNotesFinished));
    }

    private void deleteRemindersFromArchive(@NonNull List<ArchiveNote> list) {
        mDisposable.add(mInteractor.deleteRemindersFromArchive(list)
                .subscribeOn(Schedulers.io())
                .subscribe());
    }


    //---------------------------------------------------------------------------------------//
    // RecycleBin specific
    //---------------------------------------------------------------------------------------//
    private void getListOfRecycleBinNotes(@NonNull List<Integer> noteIds) {
        mDisposable.add(mInteractor.getListOfRecycleBinNotes(noteIds)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::addRecycleBinListTo));
    }

    private void addRecycleBinListTo(@NonNull List<RecycleBinNote> list) {
        mDisposable.add(mInteractor.addRecycleBinListToMain(list)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::onProcessChosenNotesFinished));
    }
}
