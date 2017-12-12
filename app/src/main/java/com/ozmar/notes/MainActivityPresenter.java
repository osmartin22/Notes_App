package com.ozmar.notes;


import android.support.annotation.NonNull;

import com.ozmar.notes.database.ArchiveNote;
import com.ozmar.notes.database.MainNote;
import com.ozmar.notes.database.NoteAndReminderPreview;
import com.ozmar.notes.database.RecycleBinNote;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class MainActivityPresenter {

    private static final String TAG = "MultiSelect";

    private static final int USER_NOTES = 0;
    private static final int FAVORITE_NOTES = 1;
    private static final int ARCHIVE_NOTES = 2;
    private static final int RECYCLE_BIN_NOTES = 3;

    private int listUsed = -1;

    private int listToAddTo = -1;
    private int listRequestedDuringMultiSelect = -1;
    private int processMultiSelectCount = 0;
    private boolean isMultiSelect = false;
    private boolean undoClicked = false;
    private boolean processingMultiSelect = false;
    private boolean menuActionIconClicked = false;

    private MainActivityView mActivityView;
    private MainActivityInteractor mInteractor;
    private CompositeDisposable mDisposable;

    private List<Integer> selectedIds;
    private List<Integer> selectedPositions;
    private List<NoteAndReminderPreview> selectedPreviews;


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

    public void onDrawerSlide() {
        mActivityView.finishMultiSelectCAB();
    }

    public void onNoteClick(int notePosition) {
        if (isMultiSelect) {
            mActivityView.multiSelect(notePosition);

        } else {
            mActivityView.finishMultiSelectCAB();
            mActivityView.dismissSnackBar();
            mActivityView.openNoteEditorActivity(notePosition, listUsed);
        }
    }

    public void onNoteLongClick(int position) {
        if (isMultiSelect) {
            mActivityView.multiSelect(position);

        } else {
            isMultiSelect = true;
            mActivityView.dismissSnackBar();
            mActivityView.startMultiSelect(position);
        }
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
        if (processingMultiSelect) {
            mActivityView.dismissSnackBar();
            listRequestedDuringMultiSelect = listUsed;

        } else {
            retrievePreviewList(listUsed);
        }
    }

    private void retrievePreviewList(int listUsed) {
        if (this.listUsed != listUsed) {
            this.listUsed = listUsed;
            mDisposable.add(mInteractor.getListOfPreviewsToShow(listUsed)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(list -> mActivityView.updateAdapterList(list)));
        }
    }

    public void onMenuActionIconClicked(List<NoteAndReminderPreview> selectedPreviews, int cabAction, int listToAddTo) {
        menuActionIconClicked = true;
        this.selectedPreviews = selectedPreviews;
        this.selectedIds = getIdsOfSelectedPreviews(selectedPreviews);
        this.listToAddTo = listToAddTo;
        mActivityView.removeSelectedPreviews();
        mActivityView.showSnackBar(cabAction);
        mActivityView.finishMultiSelectCAB();
    }

    private List<Integer> getIdsOfSelectedPreviews(List<NoteAndReminderPreview> list){
        List<Integer> idsList = new ArrayList<>();
        for (NoteAndReminderPreview preview : list){
            idsList.add(preview.getNotePreview().getId());
        }
        return idsList;
    }


    public void onUndoClicked() {
        undoClicked = true;
        isMultiSelect = false;

//        mActivityView.dismissSnackBar();
            mActivityView.addBackSelectedPreviews(selectedPositions, selectedPreviews);
//        mActivityView.clearSelectedPositions(); // Maybe not necessary

        listToAddTo = -1;
        selectedIds = null;
        selectedPreviews = null;
        selectedPositions = null;


        // TODO: Make function to get NoteAndReminderPreviews from selectedIds
        // Store them here upon starting a multi select
        // Set the list to null if I start processing the list

        // TODO: Decide how to handle a new note undo from NoteEditor
        // Can maybe add a new note to the database
        // Add note to notesAdapter but don't update views

        // TODO: add back views
    }

    public void onEndMultiSelect() {
        isMultiSelect = false;
        mActivityView.finishMultiSelectCAB();
    }

    public void onDeleteIconClicked() {

    }

    public void onActivityResult() {

    }

    public void onMultiSelectDestroy() {
        isMultiSelect = false;
//        mActivityView.clearSelectedPositions();

        // Set highlighted notes to unhighlighted if the user did an action that closed
        // the CAB but was not one of the menu items
        if (!menuActionIconClicked) {
            mActivityView.notifyEntireAdapter();
            mActivityView.clearSelectedPositions();
        } else {
            selectedPositions = mActivityView.getSelectedPositions();
            mActivityView.clearSelectedPositions();
        }

        menuActionIconClicked = false;
    }

    // TODO: Remember to cancel reminder notifications when deleting reminders
    public void processChosenNotes() {

        mActivityView.clearSelectedPositions();
        if (undoClicked) {
            undoClicked = false;

        } else {
            processingMultiSelect = true;
            processMultiSelectCount = 2;
            selectedPreviews = null;

            if (listUsed == USER_NOTES || listUsed == FAVORITE_NOTES) {
                getListOfMainNotes(selectedIds, listToAddTo);
            } else if (listUsed == ARCHIVE_NOTES) {
                getListOfArchiveNotes(selectedIds, listToAddTo);
            } else {
                getListOfRecycleBinNotes(selectedIds);
            }

            deleteList(selectedIds, listUsed);
        }
    }

    private void deleteList(@NonNull List<Integer> noteIds, int listUsed) {
        mDisposable.add(mInteractor.deleteListOfNotes(noteIds, listUsed)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::onProcessChosenNotesFinished));
    }

    private void onProcessChosenNotesFinished() {
        --processMultiSelectCount;
        if (processMultiSelectCount == 0) {
            listToAddTo = -1;
            processingMultiSelect = false;
            selectedIds = null;

            if (listRequestedDuringMultiSelect != -1) {
                retrievePreviewList(listRequestedDuringMultiSelect);
                listRequestedDuringMultiSelect = -1;
            }
        }
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
