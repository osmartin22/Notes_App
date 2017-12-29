package com.ozmar.notes.noteEditorTests;

import android.support.annotation.NonNull;

import com.ozmar.notes.Reminder;
import com.ozmar.notes.database.MainNote;
import com.ozmar.notes.noteEditor.NoteEditorActivity;
import com.ozmar.notes.noteEditor.NoteEditorInteractor;
import com.ozmar.notes.noteEditor.NoteEditorPresenter;

import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.concurrent.TimeUnit;

import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Scheduler;
import io.reactivex.Single;
import io.reactivex.android.plugins.RxAndroidPlugins;
import io.reactivex.disposables.Disposable;
import io.reactivex.internal.schedulers.ExecutorScheduler;
import io.reactivex.plugins.RxJavaPlugins;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class NoteEditorPresenterInitializeTest {

    @Mock
    private NoteEditorActivity mEditorActivity;

    @Mock
    private NoteEditorInteractor mEditorInteractor;

    private NoteEditorPresenter mEditorPresenter;

    private MainNote mNote;
    private Reminder mReminder;


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
    public void setUpNoteEditorPresenterWithNoteAndReminder() throws Exception {
        mEditorPresenter = new NoteEditorPresenter(mEditorActivity, mEditorInteractor);

        mNote = new MainNote(1, "Test Title", "Test Content", 1, 1, 0, -1);
        mReminder = new Reminder(2, new DateTime(2017, 12, 22, 11, 30), null);

    }

    private void initializeWithoutNoteAndReminder(int listUsed) {
        mEditorPresenter.initialize(-1, listUsed);
    }

    private void initializeWithReminder(int listUsed) {
        mNote.setReminderId(mReminder.getId());
        when(mEditorInteractor.getNote(mNote.getId(), listUsed)).thenReturn(Maybe.just(mNote));
        when(mEditorInteractor.getReminder(mReminder.getId())).thenReturn(Single.just(mReminder));
        mEditorPresenter.initialize(mNote.getId(), listUsed);
    }

    private void initializeWithoutReminder(int listUsed) {
        when(mEditorInteractor.getNote(mNote.getId(), listUsed)).thenReturn(Maybe.just(mNote));
        mEditorPresenter.initialize(mNote.getId(), listUsed);
    }


    // Tests involving initializing the presenter with/without a note and reminder
    @Test
    public void initializeWithoutNote_IsCorrect() throws Exception {
        initializeWithoutNoteAndReminder(0);
        Assert.assertEquals(0, mEditorPresenter.getListUsed());
        verify(mEditorActivity, times(1)).requestFocusOnContent();
    }

    @Test
    public void initializeWithReminder_IsCorrect() throws Exception {
        int listUsed = 1;
        initializeWithReminder(listUsed);
        assertInitialization(mNote, false, listUsed);
    }

    @Test
    public void initializeWithoutReminder_IsCorrect() throws Exception {
        int listUsed = 0;
        initializeWithoutReminder(listUsed);
        assertInitialization(mNote, true, listUsed);
    }

    private void assertInitialization(MainNote note, boolean reminderNull, int listUsed) throws Exception {
        assert mEditorPresenter.getNote() != null;
        Assert.assertEquals(listUsed, mEditorPresenter.getListUsed());
        Assert.assertEquals(note.getTitle(), mEditorPresenter.getNote().getTitle());

        if (reminderNull) {
            Assert.assertNull(mEditorPresenter.getReminder());

        } else {
            Assert.assertNotNull(mEditorPresenter.getReminder());
        }

        Assert.assertEquals(note.getFavorite(), mEditorPresenter.getFavorite() ? 1 : 0);

        verify(mEditorActivity, times(1)).setupNoteEditTexts(note);
    }

    @Test
    public void receivedCorrectReminder() throws Exception {
        int listUsed = 0;
        initializeWithReminder(listUsed);

        Assert.assertEquals(mReminder, mEditorPresenter.getReminder());
        verify(mEditorActivity, times(1)).showReminder(mReminder);
    }


    // Tests fore adding a reminder
    @Test
    public void onReminderPicked_AddingReminder_IsCorrect() throws Exception {
        initializeWithoutReminder(0);
        String newReminderText = "New Reminder Text";

        mEditorPresenter.onReminderPicked(mReminder, newReminderText);

        Assert.assertEquals(mReminder, mEditorPresenter.getReminder());
        verify(mEditorActivity, times(1)).updateReminderDisplay(newReminderText,
                mReminder.getFrequencyChoices());
    }

    @Test
    public void onReminderPicked_ModifyReminder_IsCorrect() throws Exception {
        initializeWithReminder(0);

        Reminder newReminder = mReminder.clone();
        newReminder.setDateTime(DateTime.now());
        String newReminderText = "New Reminder Text";

        mEditorPresenter.onReminderPicked(newReminder, newReminderText);

        Assert.assertEquals(newReminder, mEditorPresenter.getReminder());
        verify(mEditorActivity, times(1)).updateReminderDisplay(newReminderText,
                newReminder.getFrequencyChoices());

    }

    @Test
    public void onReminderPicked_SameReminder_IsCorrect() throws Exception {
        initializeWithReminder(0);
        String newReminderText = "New Reminder Text";

        mEditorPresenter.onReminderPicked(mReminder, newReminderText);

        Assert.assertEquals(mReminder, mEditorPresenter.getReminder());
        verify(mEditorActivity, times(0)).updateReminderDisplay(newReminderText,
                mReminder.getFrequencyChoices());
    }


    // Tests for deleting a reminder
    @Test
    public void onReminderDeleted_NoReminder_IsCorrect() throws Exception {
        initializeWithoutReminder(0);

        mEditorPresenter.onReminderDeleted();
        verify(mEditorActivity, times(0)).hideReminder();

    }

    @Test
    public void onReminderDeleted_WithNoteAndReminder_IsCorrect() throws Exception {
        initializeWithReminder(0);

        when(mEditorInteractor.deleteReminder(mReminder)).thenReturn(Completable.complete());
        mEditorPresenter.onReminderDeleted();
        verify(mEditorActivity, times(1)).hideReminder();
    }

    @Test
    public void onReminderDeleted_WithoutNoteAndReminder_IsCorrect() throws Exception {
        initializeWithoutNoteAndReminder(0);
        mEditorPresenter.onReminderPicked(mReminder, "New Reminder Text");

        mEditorPresenter.onReminderDeleted();
        verify(mEditorActivity, times(1)).hideReminder();
    }


    // Tests for saving a note
    @Test
    public void onSaveNote_NewEmptyNote_IsCorrect() throws Exception {
        int listUsed = 0;
        initializeWithoutNoteAndReminder(listUsed);
        mEditorPresenter.onSaveNote("", "");
        verify(mEditorActivity, times(1)).goBackToMainActivity(null, -1, listUsed);
    }

    @Test
    public void onSaveNote_NewNoteWithoutReminder_IsCorrect() throws Exception {
//        int listUsed = 0;
//        initializeWithoutNoteAndReminder(listUsed);
//        when(mEditorInteractor.addNote(mEditorPresenter.getNote())).thenReturn(Single.just((long) mNote.getId()));
//        mEditorPresenter.onSaveNote("New Title", "New Content");
//
//        verify(mEditorActivity, times(1)).goBackToMainActivity(mEditorPresenter.getNote(), 1, listUsed);
    }


    // TODO: Change to onSaveNote_UpdateNote
    @Test
    public void onSaveNote_NewTEMPNote_IsCorrect() throws Exception {
//        initializeWithoutNoteAndReminder();
        initializeWithoutReminder(0);
        int listUsed = 0;

        when(mEditorInteractor.updateNote(mNote, listUsed)).thenReturn(Completable.complete());
        mEditorPresenter.onSaveNote("Updated Title", "Updated Content");

        verify(mEditorActivity, times(1)).goBackToMainActivity(mNote, 0, listUsed);
    }


    @Test
    public void onFavoriteClicked_IsCorrect() throws Exception {
        initializeWithoutReminder(0);
        boolean favorite = mEditorPresenter.getFavorite();
        mEditorPresenter.onFavoriteClicked();

        if (favorite) {
            Assert.assertFalse(mEditorPresenter.getFavorite());
        } else {
            Assert.assertTrue(mEditorPresenter.getFavorite());
        }

        verify(mEditorActivity, times(1)).updateFavoriteIcon(!favorite);
    }
}