package com.ozmar.notes.notePreviews;


import android.support.annotation.NonNull;

import com.ozmar.notes.NoteResult;
import com.ozmar.notes.database.ArchiveNote;
import com.ozmar.notes.database.MainNote;
import com.ozmar.notes.database.NoteAndReminderPreview;
import com.ozmar.notes.database.RecycleBinNote;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;


public class NotePreviewsPresenter {

    private static final int USER_NOTES = 0;
    private static final int FAVORITE_NOTES = 1;
    private static final int ARCHIVE_NOTES = 2;
    private static final int RECYCLE_BIN_NOTES = 3;

    private int listUsed = -1;

    private int listToAddTo = -1;
    private int listRequestedBeforeProcessingDone = -1;
    private int processingMenuActionCount = 0;
    private boolean isMultiSelect = false;
    private boolean undoClicked = false;
    private boolean processingMenuAction = false;
    private boolean menuActionIconClicked = false;

    private NotePreviewsView mActivityView;
    private NotePreviewsInteractor mInteractor;
    private CompositeDisposable mDisposable;

    private List<Integer> selectedPositions;
    private List<NoteAndReminderPreview> selectedPreviews;


    public NotePreviewsPresenter(@NonNull NotePreviewsView mActivityView,
                                 @NonNull NotePreviewsInteractor notePreviewsInteractor) {
        this.mActivityView = mActivityView;
        this.mInteractor = notePreviewsInteractor;
        this.mDisposable = new CompositeDisposable();
    }

    public int getListUsed() {
        return listUsed;
    }


    public void onAttach(NotePreviewsActivity notePreviewsActivityView) {
        this.mActivityView = notePreviewsActivityView;
    }

    public void onDestroy() {
        mActivityView = null;
        mDisposable.clear();
    }

    public void onDrawerSlide() {
        if (mActivityView != null) {
            mActivityView.finishMultiSelectCAB();
        }
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


    public void onNoteEditorActivityResult(@NonNull NoteResult noteResult) {
        mDisposable.add(mInteractor.getNotePreview(noteResult.getNoteId(), noteResult.getListUsed())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(preview -> decideActivityResultAction(preview, noteResult)));
    }

    private void decideActivityResultAction(@NonNull NoteAndReminderPreview preview,
                                            @NonNull NoteResult noteResult) {

        if (noteResult.getNoteEditorAction() != -1) {
            menuActionInEditor(preview, noteResult);
        } else {
            noteModifiedInEditor(preview, noteResult);
        }
    }

    private void menuActionInEditor(@NonNull NoteAndReminderPreview preview,
                                    @NonNull NoteResult noteResult) {

        int numOfSelectedNotes;

        // Check if pressing the undo button in SnackBar will add back the note preview to the adapter
        // i.e. Unfavoriting a note in the note editor and then archiving will remove the note preview
        // when going back to the main activity. If looking at the list of all favorite notes,
        // pressing Undo should not add the preview as the archived note is no longer a favorite
        if (!(!noteResult.isFavoriteNote() && noteResult.getListUsed() == FAVORITE_NOTES)) {
            selectedPreviews = new ArrayList<>(Collections.singleton(preview));
            selectedPositions = new ArrayList<>(Collections.singleton(noteResult.getNotePosition()));
            listToAddTo = getListToAddToFromMenuAction(noteResult.getNoteEditorAction());
            numOfSelectedNotes = selectedPositions.size();

        } else {
            numOfSelectedNotes = 1;
        }

        if (!noteResult.isNewNote()) {
            mActivityView.removeAPreview(noteResult.getNotePosition());
        }

        mActivityView.showSnackBar(noteResult.getNoteEditorAction(), numOfSelectedNotes);
    }

    private void noteModifiedInEditor(@NonNull NoteAndReminderPreview preview,
                                      @NonNull NoteResult noteResult) {
        int noteModification = noteResult.getNoteModification();
        int notePosition = noteResult.getNotePosition();
        int listUsed = noteResult.getListUsed();

        if (noteModification == 0) {    // Update rv with noteChanges to the note
            mActivityView.updateAPreview(preview, notePosition);

        } else if (noteModification == 1) {    // Update rv with new note
            if (listUsed == 0) {
                mActivityView.addAPreview(preview, notePosition);

            } else if (listUsed == FAVORITE_NOTES && noteResult.isFavoriteNote()) {
                mActivityView.addAPreview(preview, notePosition);
            }

        } else if (noteModification == 2) {    // Remove note from rv (Delete Forever)
            mActivityView.removeAPreview(notePosition);
            mDisposable.add(mInteractor.deleteNoteFromRecycleBin(preview.getNotePreview().getId())
                    .subscribeOn(Schedulers.io())
                    .subscribe());

        } else if (noteModification == 3) {    // Title/Content not modified but note is no longer a favorite
            if (listUsed == FAVORITE_NOTES) {
                mActivityView.removeAPreview(notePosition);
            }
        }
    }

    private int getListToAddToFromMenuAction(int menuAction) {
        int listToAddTo = 0;

        if (menuAction == 0) {
            listToAddTo = ARCHIVE_NOTES;
        } else if (menuAction == 1 || menuAction == 3) {
            listToAddTo = USER_NOTES;
        } else if (menuAction == 2) {
            listToAddTo = RECYCLE_BIN_NOTES;
        }

        return listToAddTo;
    }


    public void onGetPreviewList(int listUsed) {
        if (processingMenuAction) {
            mActivityView.dismissSnackBar();
            listRequestedBeforeProcessingDone = listUsed;

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

    public void onMenuActionIconClicked(@NonNull List<Integer> selectedPositions,
                                        @NonNull List<NoteAndReminderPreview> selectedPreviews,
                                        int cabAction, int listToAddTo) {
        menuActionIconClicked = true;
        processingMenuAction = true;
        this.selectedPositions = selectedPositions;
        this.selectedPreviews = selectedPreviews;
        this.listToAddTo = listToAddTo;

        mActivityView.removeSelectedPreviews();
        mActivityView.showSnackBar(cabAction, selectedPositions.size());
        mActivityView.finishMultiSelectCAB();
    }

    private List<Integer> getIdsOfSelectedPreviews(@NonNull List<NoteAndReminderPreview> list) {
        List<Integer> idsList = new ArrayList<>();
        for (NoteAndReminderPreview preview : list) {
            idsList.add(preview.getNotePreview().getId());
        }
        return idsList;
    }


    public void onUndoClicked() {
        undoClicked = true;
        isMultiSelect = false;
        if (selectedPreviews != null && selectedPositions != null) {
            mActivityView.addBackSelectedPreviews(selectedPositions, selectedPreviews);
        }
    }

    public void onEndMultiSelect() {
        isMultiSelect = false;
        mActivityView.finishMultiSelectCAB();
    }

    public void onDeleteIconClicked(@NonNull List<NoteAndReminderPreview> selectedPositions) {
        mActivityView.removeSelectedPreviews();
        mActivityView.finishMultiSelectCAB();
        List<Integer> selectedIds = getIdsOfSelectedPreviews(selectedPositions);
        deleteList(selectedIds, RECYCLE_BIN_NOTES);
        processingMenuActionCount++;
    }

    public void onMultiSelectDestroy() {
        isMultiSelect = false;

        // Unhighlight notes if the user did an action that closed
        // the CAB before the user pressed one of the CAB menu items
        if (!menuActionIconClicked) {
            mActivityView.notifyEntireAdapter();
        }

        mActivityView.clearSelectedPositions();
        menuActionIconClicked = false;
    }


    public void processChosenNotes() {

        if (undoClicked) {
            undoClicked = false;
            processingMenuAction = false;
            listToAddTo = -1;

        } else {
            // Increase threads to wait for by two for every multi select process, decreases as
            // each process completes
            processingMenuActionCount += 2;
            List<Integer> selectedIds = getIdsOfSelectedPreviews(selectedPreviews);

            if (listUsed == USER_NOTES || listUsed == FAVORITE_NOTES) {
                getListOfMainNotes(selectedIds, listToAddTo);
            } else if (listUsed == ARCHIVE_NOTES) {
                getListOfArchiveNotes(selectedIds, listToAddTo);
            } else {
                getListOfRecycleBinNotes(selectedIds);
            }
        }

        selectedPositions = null;
        selectedPreviews = null;
    }

    private void deleteList(@NonNull List<Integer> noteIds, int listUsed) {
        mDisposable.add(mInteractor.deleteListOfNotes(noteIds, listUsed)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::onProcessChosenNotesFinished));
    }

    private void onProcessChosenNotesFinished() {
        if (--processingMenuActionCount == 0) {
            listToAddTo = -1;
            processingMenuAction = false;

            if (listRequestedBeforeProcessingDone != -1) {
                retrievePreviewList(listRequestedBeforeProcessingDone);
                listRequestedBeforeProcessingDone = -1;
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
                .subscribe(list -> addMainListTo(noteIds, list, listToAddTo)));
    }

    private void addMainListTo(@NonNull List<Integer> noteIds, @NonNull List<MainNote> list,
                               int listToAddTo) {
        deleteList(noteIds, listUsed);

        if (listToAddTo == RECYCLE_BIN_NOTES) {
            deleteRemindersFromMain(list);
            mActivityView.cancelReminderNotifications(getReminderIdsMain(list));
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

    private List<Integer> getReminderIdsMain(@NonNull List<MainNote> list) {
        List<Integer> reminderIds = new ArrayList<>();
        for (MainNote note : list) {
            reminderIds.add(note.getReminderId());
        }

        return reminderIds;
    }

    private List<Integer> getReminderIdsArchive(@NonNull List<ArchiveNote> list) {
        List<Integer> reminderIds = new ArrayList<>();
        for (ArchiveNote note : list) {
            reminderIds.add(note.getReminderId());
        }

        return reminderIds;
    }


    //---------------------------------------------------------------------------------------//
    // Archive specific
    //---------------------------------------------------------------------------------------//
    private void getListOfArchiveNotes(@NonNull List<Integer> noteIds, int listToAddTo) {
        mDisposable.add(mInteractor.getListOfArchiveNotes(noteIds)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(list -> addArchiveListTo(noteIds, list, listToAddTo)));
    }

    private void addArchiveListTo(@NonNull List<Integer> noteIds, @NonNull List<ArchiveNote> list,
                                  int listToAddTo) {
        deleteList(noteIds, listUsed);

        if (listToAddTo == RECYCLE_BIN_NOTES) {
            deleteRemindersFromArchive(list);
            mActivityView.cancelReminderNotifications(getReminderIdsArchive(list));
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
                .subscribe(list -> addRecycleBinListTo(noteIds, list)));
    }

    private void addRecycleBinListTo(@NonNull List<Integer> noteIds,
                                     @NonNull List<RecycleBinNote> list) {
        deleteList(noteIds, listUsed);

        mDisposable.add(mInteractor.addRecycleBinListToMain(list)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::onProcessChosenNotesFinished));
    }
}
