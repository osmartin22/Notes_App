package com.ozmar.notes;


import com.ozmar.notes.database.ArchiveNote;
import com.ozmar.notes.database.MainNote;
import com.ozmar.notes.database.RecycleBinNote;

import java.util.ArrayList;
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


    public void processChosenNotes(List<Integer> noteIds, int listToAddTo) {
        mDisposable.add(mInteractor.getListOfNotes(noteIds, listUsed)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(list -> onRetrievedNotes(list, noteIds, listUsed, listToAddTo)));
    }

    private void onRetrievedNotes(List<Object> list, List<Integer> noteIds, int listUsed, int listToAddTo) {

        deleteList(noteIds, listUsed);
        if (listToAddTo == RECYCLE_BIN_NOTES) {
            // TODO: Make sure to cancel reminder notifications
            deleteReminders(list, listUsed);
            addToRecycleBin(convertToRecycleBinList(list));

        } else if (listToAddTo == ARCHIVE_NOTES) {
            addToArchive(convertToArchiveList(list), listUsed);

        } else if (listToAddTo == USER_NOTES) {
            addToMain(convertToMainList(list), listUsed);
        }
    }


    private void deleteReminders(List<Object> list, int listUsed) {
        if (listUsed == USER_NOTES || listUsed == FAVORITE_NOTES) {
            mDisposable.add(mInteractor.deleteRemindersFromMain(convertToMainList(list))
                    .subscribeOn(Schedulers.io())
                    .subscribe());

        } else if (listUsed == ARCHIVE_NOTES) {
            mDisposable.add(mInteractor.deleteRemindersFromArchive(convertToArchiveList(list))
                    .subscribeOn(Schedulers.io())
                    .subscribe());
        }
    }

    private void deleteList(List<Integer> noteIds, int listUsed) {
        mDisposable.add(mInteractor.deleteListOfNotes(noteIds, listUsed)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe());
    }


    private void addToMain(List<MainNote> list, int listUsed) {
        mDisposable.add(mInteractor.addMainList(list, listUsed)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe());
    }

    private void addToArchive(List<ArchiveNote> list, int listUsed) {
        mDisposable.add(mInteractor.addArchiveList(list, listUsed)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe());
    }

    private void addToRecycleBin(List<RecycleBinNote> list) {
        mDisposable.add(mInteractor.addRecycleBinList(list)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe());
    }


    private List<MainNote> convertToMainList(List<Object> list) {
        List<MainNote> mainNoteList = new ArrayList<>();
        for (Object o : list) {
            mainNoteList.add((MainNote) o);
        }
        return mainNoteList;
    }

    private List<ArchiveNote> convertToArchiveList(List<Object> list) {
        List<ArchiveNote> archiveNoteList = new ArrayList<>();
        for (Object o : list) {
            archiveNoteList.add((ArchiveNote) o);
        }
        return archiveNoteList;
    }

    private List<RecycleBinNote> convertToRecycleBinList(List<Object> list) {
        List<RecycleBinNote> recycleBinNoteList = new ArrayList<>();
        for (Object o : list) {
            recycleBinNoteList.add((RecycleBinNote) o);
        }
        return recycleBinNoteList;
    }
}
