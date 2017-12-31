package com.ozmar.notes.noteEditorTests;

import android.support.annotation.NonNull;

import com.ozmar.notes.Reminder;
import com.ozmar.notes.database.MainNote;
import com.ozmar.notes.noteEditor.NoteEditorInteractor;
import com.ozmar.notes.noteEditor.NoteEditorPresenter;
import com.ozmar.notes.noteEditor.NoteEditorView;

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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class NoteEditorPresenterTests {

    private static final int USER_NOTES = 0;
    private static final int FAVORITE_NOTES = 1;
    private static final int ARCHIVE_NOTES = 2;
    private static final int RECYCLE_BIN_NOTES = 3;

    private static final String newReminderText = "New Reminder Text";
    private static final String updateTitle = "Updated Title";
    private static final String updateContent = "Updated Content";

    @Mock
    private NoteEditorView mNoteEditorView;

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
        mEditorPresenter = new NoteEditorPresenter(mNoteEditorView, mEditorInteractor);

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

    private void addReminderToPresenter(Reminder reminder) {
        mEditorPresenter.onReminderPicked(reminder, newReminderText);
    }


    // Tests involving initializing the presenter with/without a note and reminder
    @Test
    public void initializeWithoutNote_IsCorrect() throws Exception {
        int listUsed = USER_NOTES;
        initializeWithoutNoteAndReminder(listUsed);
        Assert.assertEquals(listUsed, mEditorPresenter.getListUsed());
        verify(mNoteEditorView, times(1)).requestFocusOnContent();
    }

    @Test
    public void initializeWithReminder_IsCorrect() throws Exception {
        int listUsed = FAVORITE_NOTES;
        initializeWithReminder(listUsed);
        assertInitialization(mNote, false, listUsed);
    }

    @Test
    public void initializeWithoutReminder_IsCorrect() throws Exception {
        int listUsed = ARCHIVE_NOTES;
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

        verify(mNoteEditorView, times(1)).setupNoteEditTexts(note);
    }

    @Test
    public void receivedCorrectReminder() throws Exception {
        initializeWithReminder(USER_NOTES);

        Assert.assertEquals(mReminder, mEditorPresenter.getReminder());
        verify(mNoteEditorView, times(1)).showReminder(mReminder);
    }


    // Tests fore adding a reminder
    @Test
    public void onReminderPicked_AddingReminder_IsCorrect() throws Exception {
        initializeWithoutReminder(0);
        addReminderToPresenter(mReminder);

        Assert.assertEquals(mReminder, mEditorPresenter.getReminder());
        verify(mNoteEditorView, times(1)).updateReminderDisplay(newReminderText,
                mReminder.getFrequencyChoices());
    }

    @Test
    public void onReminderPicked_ModifyReminder_IsCorrect() throws Exception {
        initializeWithReminder(0);

        Reminder modifiedReminder = mReminder.clone();
        modifiedReminder.setDateTime(DateTime.now());
        addReminderToPresenter(modifiedReminder);

        Assert.assertEquals(modifiedReminder, mEditorPresenter.getReminder());
        verify(mNoteEditorView, times(1)).updateReminderDisplay(newReminderText,
                modifiedReminder.getFrequencyChoices());

    }

    @Test
    public void onReminderPicked_SameReminder_IsCorrect() throws Exception {
        initializeWithReminder(0);
        addReminderToPresenter(mReminder);

        Assert.assertEquals(mReminder, mEditorPresenter.getReminder());
        verify(mNoteEditorView, times(0)).updateReminderDisplay(newReminderText,
                mReminder.getFrequencyChoices());
    }


    // Tests for deleting a reminder
    @Test
    public void onReminderDeleted_NoReminder_IsCorrect() throws Exception {
        initializeWithoutReminder(0);

        mEditorPresenter.onReminderDeleted();
        verify(mEditorInteractor, times(0)).deleteReminder(any(Reminder.class));
        verify(mNoteEditorView, times(0)).hideReminder();

    }

    @Test
    public void onReminderDeleted_WithNoteAndReminder_IsCorrect() throws Exception {
        initializeWithReminder(0);

        when(mEditorInteractor.deleteReminder(mReminder)).thenReturn(Completable.complete());
        mEditorPresenter.onReminderDeleted();

        verify(mEditorInteractor, times(1)).deleteReminder(mReminder);
        verify(mNoteEditorView, times(1)).hideReminder();
    }

    @Test
    public void onReminderDeleted_WithoutNoteAndReminder_IsCorrect() throws Exception {
        initializeWithoutNoteAndReminder(0);
        addReminderToPresenter(mReminder);

        mEditorPresenter.onReminderDeleted();
        verify(mEditorInteractor, times(0)).deleteReminder(mReminder);
        verify(mNoteEditorView, times(1)).hideReminder();
    }


    // Tests for saving a new note with/without a reminder
    @Test
    public void onSaveNote_NewEmptyNote_IsCorrect() throws Exception {
        int listUsed = USER_NOTES;
        initializeWithoutNoteAndReminder(listUsed);
        mEditorPresenter.onSaveNote("", "");
        verify(mNoteEditorView, times(1)).goBackToMainActivity(null, -1, listUsed);
    }

    @Test
    public void onSaveNote_NewNoteWithoutReminder_IsCorrect() throws Exception {
        int listUsed = USER_NOTES;
        initializeWithoutNoteAndReminder(listUsed);

        // Using argument matcher since the MainNote is created in a private function and when() does
        // not have an instance of the new note at this point
        when(mEditorInteractor.addNote(any(MainNote.class))).thenReturn(Single.just((anyLong())));
        mEditorPresenter.onSaveNote("New Title", "New Content");

        verify(mEditorInteractor, times(1)).addNote(any(MainNote.class));
        verify(mNoteEditorView, times(1)).goBackToMainActivity(mEditorPresenter.getNote(), 1, listUsed);
    }

    @Test
    public void onSaveNote_NewNoteWithReminder_IsCorrect() throws Exception {
        int listUsed = USER_NOTES;
        initializeWithoutNoteAndReminder(listUsed);

        // Using argument matcher since the MainNote is created in a private function and when() does
        // not have an instance of the new note at this point
        when(mEditorInteractor.addNote(any(MainNote.class))).thenReturn(Single.just((long) mNote.getId()));
        when(mEditorInteractor.addReminder(mReminder)).thenReturn(Single.just((long) mReminder.getId()));

        addReminderToPresenter(mReminder);
        mEditorPresenter.onSaveNote("New Title", "New Content");

        assert mEditorPresenter.getNote() != null;
        verify(mEditorInteractor, times(1)).addNote(any(MainNote.class));
        verify(mEditorInteractor, times(1)).addReminder(any(Reminder.class));
        verify(mNoteEditorView, times(1)).setupReminderNotification(mEditorPresenter.getNote(), mReminder);
        verify(mNoteEditorView, times(1)).goBackToMainActivity(mEditorPresenter.getNote(), 1, listUsed);
    }


    // Tests for saving a preexisting note with/without a reminder
    @Test
    public void onSaveNote_SameNote_IsCorrect() throws Exception {
        int listUsed = USER_NOTES;
        initializeWithoutReminder(listUsed);

        mEditorPresenter.onSaveNote(mNote.getTitle(), mNote.getContent());

        verify(mEditorInteractor, times(0)).updateNote(mNote, listUsed);
        verify(mNoteEditorView, times(1)).goBackToMainActivity(mNote, -1, listUsed);
    }

    @Test
    public void onSaveNote_UpdateNoteWithoutReminder_IsCorrect() throws Exception {
        int listUsed = USER_NOTES;
        initializeWithoutReminder(listUsed);

        when(mEditorInteractor.updateNote(mNote, listUsed)).thenReturn(Completable.complete());
        mEditorPresenter.onSaveNote(updateTitle, updateContent);

        verify(mEditorInteractor, times(1)).updateNote(mNote, listUsed);
        verify(mNoteEditorView, times(1)).goBackToMainActivity(mNote, 0, listUsed);
    }

    @Test
    public void onSaveNote_UpdateNoteWithReminder_IsCorrect() throws Exception {
        int listUsed = USER_NOTES;
        initializeWithReminder(listUsed);

        when(mEditorInteractor.updateNote(mNote, listUsed)).thenReturn(Completable.complete());
        mEditorPresenter.onSaveNote(updateTitle, updateContent);

        verify(mEditorInteractor, times(0)).updateReminder(any(Reminder.class));
        verify(mEditorInteractor, times(1)).updateNote(mNote, listUsed);
        verify(mNoteEditorView, times(1)).goBackToMainActivity(mNote, 0, listUsed);
    }

    @Test
    public void onSaveNote_UpdateNoteAndReminder_IsCorrect() throws Exception {
        int listUsed = ARCHIVE_NOTES;
        initializeWithReminder(listUsed);

        Reminder newReminder = mReminder.clone();
        newReminder.setDateTime(DateTime.now());
        addReminderToPresenter(newReminder);

        when(mEditorInteractor.updateNote(mNote, listUsed)).thenReturn(Completable.complete());
        when(mEditorInteractor.updateReminder(newReminder)).thenReturn(Completable.complete());
        mEditorPresenter.onSaveNote(updateTitle, updateContent);

        verify(mEditorInteractor, times(1)).updateReminder(newReminder);
        verify(mEditorInteractor, times(1)).updateNote(mNote, listUsed);
        verify(mNoteEditorView, times(1)).goBackToMainActivity(mNote, 0, listUsed);
    }


    // Tests for updating/deleting/adding the reminder
    @Test
    public void onSaveNote_SameNoteUpdateReminder_IsCorrect() throws Exception {
        int listUsed = FAVORITE_NOTES;
        initializeWithReminder(listUsed);

        Reminder newReminder = mReminder.clone();
        newReminder.setDateTime(DateTime.now());
        addReminderToPresenter(newReminder);

        when(mEditorInteractor.updateReminder(newReminder)).thenReturn(Completable.complete());
        mEditorPresenter.onSaveNote(mNote.getTitle(), mNote.getContent());

        verify(mEditorInteractor, times(1)).updateReminder(newReminder);
        verify(mEditorInteractor, times(0)).updateNote(mNote, listUsed);
        verify(mNoteEditorView, times(1)).goBackToMainActivity(mNote, 0, listUsed);
    }

    @Test
    public void onSaveNote_SameNoteDeleteReminder_IsCorrect() throws Exception {
        int listUsed = USER_NOTES;
        initializeWithReminder(listUsed);

        mEditorPresenter.onReminderDeleted();
        when(mEditorInteractor.updateNote(mNote, listUsed)).thenReturn(Completable.complete());
        when(mEditorInteractor.deleteReminder(mNote.getReminderId())).thenReturn(Completable.complete());
        mEditorPresenter.onSaveNote(mNote.getTitle(), mNote.getContent());

        verify(mEditorInteractor, times(1)).deleteReminder(mReminder.getId());
        verify(mNoteEditorView, times(1)).cancelReminderNotification(mReminder.getId());
        verify(mEditorInteractor, times(1)).updateNote(mNote, listUsed);
        verify(mNoteEditorView, times(1)).goBackToMainActivity(mNote, 0, listUsed);
    }

    @Test
    public void onSaveNote_UpdateNoteDeleteReminder_IsCorrect() throws Exception {
        int listUsed = USER_NOTES;
        initializeWithReminder(listUsed);

        mEditorPresenter.onReminderDeleted();
        when(mEditorInteractor.updateNote(mNote, listUsed)).thenReturn(Completable.complete());
        when(mEditorInteractor.deleteReminder(mNote.getReminderId())).thenReturn(Completable.complete());
        mEditorPresenter.onSaveNote(updateTitle, updateContent);

        verify(mEditorInteractor, times(1)).deleteReminder(mReminder.getId());
        verify(mNoteEditorView, times(1)).cancelReminderNotification(mReminder.getId());
        verify(mEditorInteractor, times(1)).updateNote(mNote, listUsed);
        verify(mNoteEditorView, times(1)).goBackToMainActivity(mNote, 0, listUsed);
    }

    @Test
    public void onSaveNote_UpdateNoteAddReminder_IsCorrect() throws Exception {
        int listUsed = USER_NOTES;
        initializeWithoutReminder(listUsed);

        addReminderToPresenter(mReminder);
        when(mEditorInteractor.updateNote(mNote, listUsed)).thenReturn(Completable.complete());
        when(mEditorInteractor.addReminder(mReminder)).thenReturn(Single.just((long) mReminder.getId()));
        mEditorPresenter.onSaveNote(updateTitle, updateContent);

        verify(mEditorInteractor, times(1)).addReminder(mReminder);
        verify(mEditorInteractor, times(1)).updateNote(mNote, listUsed);
        verify(mNoteEditorView, times(1)).setupReminderNotification(mNote, mReminder);
        verify(mNoteEditorView, times(1)).goBackToMainActivity(mNote, 0, listUsed);
    }


    // Test if the correct value is returned when a note is unfavorited while the current list
    // looked at in NotePreviewsActivity is the favorite list
    @Test
    public void onSaveNote_UnFavoriteNote_IsCorrect() throws Exception {
        int listUsed = FAVORITE_NOTES;
        initializeWithoutReminder(listUsed);

        addReminderToPresenter(mReminder);
        when(mEditorInteractor.updateNote(mNote, listUsed)).thenReturn(Completable.complete());
        when(mEditorInteractor.addReminder(mReminder)).thenReturn(Single.just((long) mReminder.getId()));
        mEditorPresenter.onFavoriteClicked();
        mEditorPresenter.onSaveNote(updateTitle, updateContent);

        verify(mEditorInteractor, times(1)).addReminder(mReminder);
        verify(mEditorInteractor, times(1)).updateNote(mNote, listUsed);
        verify(mNoteEditorView, times(1)).setupReminderNotification(mNote, mReminder);
        verify(mNoteEditorView, times(1)).goBackToMainActivity(mNote, 3, listUsed);
    }


    // Method should only be called when the list viewed is the Recycle Bin list
    @Test
    public void onDeleteForever_RecycleBin_IsCorrect() throws Exception {
        int listUsed = RECYCLE_BIN_NOTES;
        initializeWithoutReminder(listUsed);

        mEditorPresenter.onDeleteNoteForever();

        verify(mNoteEditorView, times(1)).goBackToMainActivity(mNote, 2, listUsed);
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

        verify(mNoteEditorView, times(1)).updateFavoriteIcon(!favorite);
    }
}