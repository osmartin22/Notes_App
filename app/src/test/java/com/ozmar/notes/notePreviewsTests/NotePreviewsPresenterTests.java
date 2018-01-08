package com.ozmar.notes.notePreviewsTests;


import android.support.annotation.NonNull;

import com.ozmar.notes.NoteResult;
import com.ozmar.notes.database.ArchiveNote;
import com.ozmar.notes.database.MainNote;
import com.ozmar.notes.database.NoteAndReminderPreview;
import com.ozmar.notes.database.NotePreviewWithReminderId;
import com.ozmar.notes.database.RecycleBinNote;
import com.ozmar.notes.notePreviews.NotePreviewsInteractor;
import com.ozmar.notes.notePreviews.NotePreviewsPresenter;
import com.ozmar.notes.notePreviews.NotePreviewsView;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Scheduler;
import io.reactivex.Single;
import io.reactivex.android.plugins.RxAndroidPlugins;
import io.reactivex.disposables.Disposable;
import io.reactivex.internal.schedulers.ExecutorScheduler;
import io.reactivex.plugins.RxJavaPlugins;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class NotePreviewsPresenterTests {

    @Mock
    private NotePreviewsView mPreviewsView;

    @Mock
    private NotePreviewsInteractor mPreviewsInteractor;

    private NotePreviewsPresenter mPreviewsPresenter;

    private NoteAndReminderPreview mPreview;
    private List<Integer> selectedPositions;
    private List<NoteAndReminderPreview> selectedPreviews;

    private String title = "Title";
    private String content = "Content";


    @BeforeClass
    public static void setUpRxSchedulers() {
        Scheduler immediate = new Scheduler() {
            @Override
            public Disposable scheduleDirect(@NonNull Runnable run, long delay, @NonNull TimeUnit unit) {
                return super.scheduleDirect(run, 0, unit);
            }

            @Override
            public Scheduler.Worker createWorker() {
                return new ExecutorScheduler.ExecutorWorker(Runnable::run);
            }
        };

        RxJavaPlugins.setInitIoSchedulerHandler(scheduler -> immediate);
        RxJavaPlugins.setInitComputationSchedulerHandler(scheduler -> immediate);
        RxJavaPlugins.setInitNewThreadSchedulerHandler(scheduler -> immediate);
        RxJavaPlugins.setInitSingleSchedulerHandler(scheduler -> immediate);
        RxAndroidPlugins.setInitMainThreadSchedulerHandler(scheduler -> immediate);
    }

    @Before
    public void setup() {
        mPreviewsPresenter = new NotePreviewsPresenter(mPreviewsView, mPreviewsInteractor);
    }


    private void createPreview() {
        NotePreviewWithReminderId notePreview = new NotePreviewWithReminderId(1, title, content, -1);
        mPreview = new NoteAndReminderPreview(notePreview, null);
    }

    private void createPreviewAndSelectedLists() {
        int notePosition = 1;
        NotePreviewWithReminderId notePreview = new NotePreviewWithReminderId(1, title, content, -1);
        NoteAndReminderPreview preview = new NoteAndReminderPreview(notePreview, null);

        selectedPositions = Collections.singletonList(notePosition);
        selectedPreviews = Collections.singletonList(preview);
    }

    private void initializeListUsed(int listUsed) {
        createPreview();
        List<NoteAndReminderPreview> previewList = Collections.singletonList(mPreview);
        when(mPreviewsInteractor.getListOfPreviewsToShow(listUsed))
                .thenReturn(Maybe.just(previewList));
        mPreviewsPresenter.onGetPreviewList(listUsed);
    }

    private List<MainNote> createMainList() {
        MainNote note = new MainNote(1, title, content, 1, 1, 0, -1);
        return Collections.singletonList(note);
    }

    private List<ArchiveNote> createArchiveList() {
        ArchiveNote note = new ArchiveNote(1, title, content, 1, 1, -1);
        return Collections.singletonList(note);
    }

    private List<RecycleBinNote> createRecycleBinList() {
        RecycleBinNote note = new RecycleBinNote(1, title, content, 1, 1);
        return Collections.singletonList(note);
    }


    @Test
    public void onDrawerSlide() throws Exception {
        mPreviewsPresenter.onDrawerSlide();
        verify(mPreviewsView, times(1)).finishMultiSelectCAB();
    }


    @Test
    public void onNoteClickNotMultiSelect() throws Exception {
        int notePosition = 1;
        mPreviewsPresenter.onNoteClick(notePosition);
        verify(mPreviewsView, times(1)).finishMultiSelectCAB();
        verify(mPreviewsView, times(1)).dismissSnackBar();
        verify(mPreviewsView, times(1)).openNoteEditorActivity(notePosition, mPreviewsPresenter.getListUsed());
    }

    @Test
    public void onNoteClickIsMultiSelect() throws Exception {
        int notePosition = 1;

        // Call to set multiSelect to true in presenter
        mPreviewsPresenter.onNoteLongClick(notePosition + 1);

        mPreviewsPresenter.onNoteClick(notePosition);
        verify(mPreviewsView, times(1)).multiSelect(notePosition);
    }

    @Test
    public void onNoteLongClickNotMultiSelect() throws Exception {
        int notePosition = 1;
        mPreviewsPresenter.onNoteLongClick(notePosition);

        verify(mPreviewsView, times(1)).dismissSnackBar();
        verify(mPreviewsView, times(1)).startMultiSelect(notePosition);
    }

    @Test
    public void onNoteLongClickIsMultiSelect() throws Exception {
        int notePosition = 1;

        // Call to set multiSelect to true in presenter
        mPreviewsPresenter.onNoteLongClick(notePosition + 1);

        mPreviewsPresenter.onNoteLongClick(notePosition);
        verify(mPreviewsView, times(1)).multiSelect(notePosition);
    }


    @Test
    public void onLayoutIconClicked() throws Exception {
        int layoutChoice = 1;
        mPreviewsPresenter.onLayoutIconClicked(layoutChoice);
        verify(mPreviewsView, times(1)).swapLayout(layoutChoice);
    }


    @Test
    public void onNoteEditorActivityResultNoteModifiedUpdate() throws Exception {
        int notePosition = 1;

        createPreview();
        NoteResult noteResult = new NoteResult(1, notePosition, 0, 0, -1, false, false);
        when(mPreviewsInteractor.getNotePreview(noteResult.getNoteId(), noteResult.getListUsed())).thenReturn(Maybe.just(mPreview));

        mPreviewsPresenter.onNoteEditorActivityResult(noteResult);

        verify(mPreviewsInteractor, times(1)).getNotePreview(noteResult.getNoteId(), noteResult.getListUsed());
        verify(mPreviewsView, times(1)).updateAPreview(mPreview, notePosition);
    }

    @Test
    public void onNoteEditorActivityResultNoteModifiedNewNote() throws Exception {
        int notePosition = 1;

        createPreview();
        NoteResult noteResult = new NoteResult(1, notePosition, 0, 1, -1, false, false);
        when(mPreviewsInteractor.getNotePreview(noteResult.getNoteId(), noteResult.getListUsed())).thenReturn(Maybe.just(mPreview));

        mPreviewsPresenter.onNoteEditorActivityResult(noteResult);

        verify(mPreviewsInteractor, times(1)).getNotePreview(noteResult.getNoteId(), noteResult.getListUsed());
        verify(mPreviewsView, times(1)).addAPreview(mPreview, notePosition);
    }

    @Test
    public void onNoteEditorActivityResultNoteModifiedRemove() throws Exception {
        int notePosition = 1;

        createPreview();
        NoteResult noteResult = new NoteResult(1, notePosition, 0, 2, -1, false, false);
        when(mPreviewsInteractor.getNotePreview(noteResult.getNoteId(), noteResult.getListUsed())).thenReturn(Maybe.just(mPreview));
        when(mPreviewsInteractor.deleteNoteFromRecycleBin(noteResult.getNoteId())).thenReturn(Completable.complete());

        mPreviewsPresenter.onNoteEditorActivityResult(noteResult);

        verify(mPreviewsInteractor, times(1)).getNotePreview(noteResult.getNoteId(), noteResult.getListUsed());
        verify(mPreviewsView, times(1)).removeAPreview(notePosition);
        verify(mPreviewsInteractor, times(1)).deleteNoteFromRecycleBin(notePosition);
    }

    @Test
    public void onNoteEditorActivityResultNoteModifiedRemoveFavorite() throws Exception {
        int notePosition = 1;
        createPreview();

        NoteResult noteResult = new NoteResult(1, notePosition, 1, 3, -1, false, false);
        when(mPreviewsInteractor.getNotePreview(noteResult.getNoteId(), noteResult.getListUsed())).thenReturn(Maybe.just(mPreview));

        mPreviewsPresenter.onNoteEditorActivityResult(noteResult);

        verify(mPreviewsInteractor, times(1)).getNotePreview(noteResult.getNoteId(), noteResult.getListUsed());
        verify(mPreviewsView, times(1)).removeAPreview(notePosition);
    }

    @Test
    public void onNoteEditorActivityResultMenuAction() throws Exception {
        int notePosition = 1;

        createPreview();
        NoteResult noteResult = new NoteResult(1, notePosition, 0, 0, 0, false, false);
        when(mPreviewsInteractor.getNotePreview(noteResult.getNoteId(), noteResult.getListUsed())).thenReturn(Maybe.just(mPreview));

        mPreviewsPresenter.onNoteEditorActivityResult(noteResult);

        verify(mPreviewsView, times(1)).removeAPreview(notePosition);
        verify(mPreviewsView, times(1)).showSnackBar(noteResult.getNoteEditorAction(), 1);
    }

    @Test
    public void onNoteEditorActivityResultMenuActionNewNote() throws Exception {
        int notePosition = 1;

        createPreview();
        NoteResult noteResult = new NoteResult(1, notePosition, 0, 0, 0, false, true);
        when(mPreviewsInteractor.getNotePreview(noteResult.getNoteId(), noteResult.getListUsed())).thenReturn(Maybe.just(mPreview));

        mPreviewsPresenter.onNoteEditorActivityResult(noteResult);

        verify(mPreviewsView, times(0)).removeAPreview(notePosition);
        verify(mPreviewsView, times(1)).showSnackBar(noteResult.getNoteEditorAction(), 1);
    }


    @Test
    public void onGetPreviewListProcessing() throws Exception {
        createPreviewAndSelectedLists();

        // Set processing to true in presenter;
        mPreviewsPresenter.onMenuActionIconClicked(selectedPositions, selectedPreviews, 0, 2);

        mPreviewsPresenter.onGetPreviewList(0);
        verify(mPreviewsView, times(1)).dismissSnackBar();
    }

    @Test
    public void onGetPreviewListNotProcessing() throws Exception {
        initializeListUsed(0);

        verify(mPreviewsView, times(0)).dismissSnackBar();
    }


    @Test
    public void onMenuActionIconClicked() throws Exception {
        int cabAction = 0;
        createPreviewAndSelectedLists();
        mPreviewsPresenter.onMenuActionIconClicked(selectedPositions, selectedPreviews, cabAction, 2);

        verify(mPreviewsView, times(1)).removeSelectedPreviews();
        verify(mPreviewsView, times(1)).showSnackBar(cabAction, selectedPreviews.size());
        verify(mPreviewsView, times(1)).finishMultiSelectCAB();
    }

    @Test
    public void onUndoClicked() throws Exception {
        int cabAction = 0;
        createPreviewAndSelectedLists();
        mPreviewsPresenter.onMenuActionIconClicked(selectedPositions, selectedPreviews, cabAction, 2);

        mPreviewsPresenter.onUndoClicked();
        verify(mPreviewsView, times(1)).addBackSelectedPreviews(selectedPositions, selectedPreviews);
    }

    @Test
    public void onEndMultiSelect() throws Exception {
        mPreviewsPresenter.onEndMultiSelect();
        verify(mPreviewsView, times(1)).finishMultiSelectCAB();
    }


    @Test
    public void onDeleteIconClicked() throws Exception {
        createPreviewAndSelectedLists();
        when(mPreviewsInteractor.deleteListOfNotes(anyList(),anyInt())).thenReturn(Completable.complete());
        mPreviewsPresenter.onDeleteIconClicked(selectedPreviews);

        verify(mPreviewsView, times(1)).removeSelectedPreviews();
        verify(mPreviewsView, times(1)).finishMultiSelectCAB();
        verify(mPreviewsInteractor, times(1)).deleteListOfNotes(anyList(),anyInt());
    }


    @Test
    public void onMultiSelectDestroyMenuIconNotClicked() throws Exception {
        createPreviewAndSelectedLists();
        mPreviewsPresenter.onMenuActionIconClicked(selectedPositions, selectedPreviews, 0, 2);

        mPreviewsPresenter.onMultiSelectDestroy();
        verify(mPreviewsView, times(0)).notifyEntireAdapter();
        verify(mPreviewsView, times(1)).clearSelectedPositions();
    }

    @Test
    public void onMultiSelectDestroyMenuIconClicked() throws Exception {
        mPreviewsPresenter.onMultiSelectDestroy();
        verify(mPreviewsView, times(1)).notifyEntireAdapter();
        verify(mPreviewsView, times(1)).clearSelectedPositions();
    }


    @Test
    public void processChosenNotesMainToArchive() throws Exception {
        int listToAddTo = 2;
        initializeListUsed(0);
        createPreviewAndSelectedLists();
        mPreviewsPresenter.onMenuActionIconClicked(selectedPositions, selectedPreviews, 1, listToAddTo);

        List<MainNote> mainList = createMainList();
        when(mPreviewsInteractor.getListOfMainNotes(selectedPositions)).thenReturn(Single.just(mainList));
        when(mPreviewsInteractor.addMainListTo(mainList, listToAddTo)).thenReturn(Completable.complete());
        when(mPreviewsInteractor.deleteListOfNotes(selectedPositions, mPreviewsPresenter.getListUsed()))
                .thenReturn(Completable.complete());
        mPreviewsPresenter.processChosenNotes();

        verify(mPreviewsView, times(0)).cancelReminderNotifications(anyList());
        verify(mPreviewsInteractor, times(1)).getListOfMainNotes(selectedPositions);
        verify(mPreviewsInteractor, times(1)).addMainListTo(mainList, listToAddTo);
        verify(mPreviewsInteractor, times(1)).deleteListOfNotes(selectedPositions,
                mPreviewsPresenter.getListUsed());
    }

    @Test
    public void processChosenNotesMainToRecycleBin() throws Exception {
        int listToAddTo = 3;
        initializeListUsed(0);
        createPreviewAndSelectedLists();
        mPreviewsPresenter.onMenuActionIconClicked(selectedPositions, selectedPreviews, 1, listToAddTo);

        List<MainNote> mainList = createMainList();
        when(mPreviewsInteractor.getListOfMainNotes(selectedPositions)).thenReturn(Single.just(mainList));
        when(mPreviewsInteractor.addMainListTo(mainList, listToAddTo)).thenReturn(Completable.complete());
        when(mPreviewsInteractor.deleteListOfNotes(selectedPositions, mPreviewsPresenter.getListUsed()))
                .thenReturn(Completable.complete());
        when(mPreviewsInteractor.deleteRemindersFromMain(mainList)).thenReturn(Completable.complete());
        mPreviewsPresenter.processChosenNotes();

        verify(mPreviewsView, times(1)).cancelReminderNotifications(anyList());
        verify(mPreviewsInteractor, times(1)).getListOfMainNotes(selectedPositions);
        verify(mPreviewsInteractor, times(1)).addMainListTo(mainList, listToAddTo);
        verify(mPreviewsInteractor, times(1)).deleteListOfNotes(selectedPositions,
                mPreviewsPresenter.getListUsed());
        verify(mPreviewsInteractor, times(1)).deleteRemindersFromMain(mainList);
    }


    @Test
    public void processChosenNotesArchiveToMain() throws Exception {
        int listToAddTo = 0;
        initializeListUsed(2);
        createPreviewAndSelectedLists();
        mPreviewsPresenter.onMenuActionIconClicked(selectedPositions, selectedPreviews, 1, listToAddTo);

        List<ArchiveNote> archiveList = createArchiveList();
        when(mPreviewsInteractor.getListOfArchiveNotes(selectedPositions)).thenReturn(Single.just(archiveList));
        when(mPreviewsInteractor.addArchiveListTo(archiveList, listToAddTo)).thenReturn(Completable.complete());
        when(mPreviewsInteractor.deleteListOfNotes(selectedPositions, mPreviewsPresenter.getListUsed()))
                .thenReturn(Completable.complete());
        mPreviewsPresenter.processChosenNotes();

        verify(mPreviewsView, times(0)).cancelReminderNotifications(anyList());
        verify(mPreviewsInteractor, times(1)).getListOfArchiveNotes(selectedPositions);
        verify(mPreviewsInteractor, times(1)).addArchiveListTo(archiveList, listToAddTo);
        verify(mPreviewsInteractor, times(1)).deleteListOfNotes(selectedPositions,
                mPreviewsPresenter.getListUsed());
    }

    @Test
    public void processChosenNotesArchiveToRecycleBin() throws Exception {
        int listToAddTo = 3;
        initializeListUsed(2);
        createPreviewAndSelectedLists();
        mPreviewsPresenter.onMenuActionIconClicked(selectedPositions, selectedPreviews, 1, listToAddTo);

        List<ArchiveNote> archiveList = createArchiveList();
        when(mPreviewsInteractor.getListOfArchiveNotes(selectedPositions)).thenReturn(Single.just(archiveList));
        when(mPreviewsInteractor.addArchiveListTo(archiveList, listToAddTo)).thenReturn(Completable.complete());
        when(mPreviewsInteractor.deleteListOfNotes(selectedPositions, mPreviewsPresenter.getListUsed()))
                .thenReturn(Completable.complete());
        when(mPreviewsInteractor.deleteRemindersFromArchive(archiveList)).thenReturn(Completable.complete());
        mPreviewsPresenter.processChosenNotes();

        verify(mPreviewsView, times(1)).cancelReminderNotifications(anyList());
        verify(mPreviewsInteractor, times(1)).getListOfArchiveNotes(selectedPositions);
        verify(mPreviewsInteractor, times(1)).addArchiveListTo(archiveList, listToAddTo);
        verify(mPreviewsInteractor, times(1)).deleteListOfNotes(selectedPositions,
                mPreviewsPresenter.getListUsed());
        verify(mPreviewsInteractor, times(1)).deleteRemindersFromArchive(archiveList);
    }


    @Test
    public void processChosenNotesRecycleBinToMain() throws Exception {
        int listToAddTo = 0;
        initializeListUsed(3);
        createPreviewAndSelectedLists();
        mPreviewsPresenter.onMenuActionIconClicked(selectedPositions, selectedPreviews, 1, listToAddTo);


        List<RecycleBinNote> recycleBinList = createRecycleBinList();
        when(mPreviewsInteractor.getListOfRecycleBinNotes(selectedPositions)).thenReturn(Single.just(recycleBinList));
        when(mPreviewsInteractor.addRecycleBinListToMain(recycleBinList)).thenReturn(Completable.complete());
        when(mPreviewsInteractor.deleteListOfNotes(selectedPositions, mPreviewsPresenter.getListUsed()))
                .thenReturn(Completable.complete());
        mPreviewsPresenter.processChosenNotes();

        verify(mPreviewsInteractor, times(1)).getListOfRecycleBinNotes(selectedPositions);
        verify(mPreviewsInteractor, times(1)).addRecycleBinListToMain(recycleBinList);
        verify(mPreviewsInteractor, times(1)).deleteListOfNotes(selectedPositions,
                mPreviewsPresenter.getListUsed());
    }
}
