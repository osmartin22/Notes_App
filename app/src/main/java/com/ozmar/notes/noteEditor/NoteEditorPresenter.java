package com.ozmar.notes.noteEditor;

import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.ozmar.notes.Reminder;
import com.ozmar.notes.database.MainNote;

import javax.annotation.Nonnull;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

// TODO: Deleting a note through multi select does not cancel reminder notification

public class NoteEditorPresenter {

    private int listUsed;
    private boolean favorite = false;
    private boolean isNewNote = false;

    private boolean reminderModified = false;
    private boolean noteModified = false;
    private int noteChangesResult = -1;

    private NoteEditorView mEditorView;
    private NoteEditorInteractor mEditorInteractor;


    private final CompositeDisposable mDisposable = new CompositeDisposable();

    @Nullable
    private MainNote mMainNote;

    @Nullable
    private Reminder mReminder;


    public NoteEditorPresenter(@Nonnull NoteEditorView noteEditorView,
                               @NonNull NoteEditorInteractor noteEditorInteractor) {
        mEditorView = noteEditorView;
        mEditorInteractor = noteEditorInteractor;
    }

    @Nullable
    public MainNote getNote() {
        return mMainNote;
    }

    @Nullable
    public Reminder getReminder() {
        return mReminder;
    }

    public boolean getFavorite() {
        return favorite;
    }

    public int getListUsed() {
        return listUsed;
    }


    public void initialize(int noteId, @IntRange(from = 0, to = 3) int listUsed) {
        this.listUsed = listUsed;

        if (noteId != -1) {
            observableGetNote(noteId, listUsed);
        } else {
            mEditorView.requestFocusOnContent();
        }
    }

    private void setUpView(@Nonnull MainNote note) {
        favorite = note.getFavorite() == 1;
        mEditorView.setupNoteEditTexts(note);

        if (note.getReminderId() != -1) {
            observableGetReminder(note.getReminderId());
        }
    }

    public void onFavoriteClicked() {
        favorite = !favorite;
        mEditorView.updateFavoriteIcon(favorite);
    }


    public void onSaveNote(@NonNull String title, @NonNull String content) {

        if (mMainNote != null) {

            modifyNote(mMainNote, title, content);

            if (reminderModified) {
                modifyReminder(mMainNote, mReminder);

            } else {
                observableUpdateNote(mMainNote, listUsed);
            }

        } else {
            boolean titleEmpty = title.isEmpty();
            boolean contentEmpty = content.isEmpty();

            if (!(titleEmpty && contentEmpty)) {
                createNewNote(title, content);
            } else {
                mEditorView.goBackToMainActivity(null, -1, listUsed);
            }
        }
    }

    private void modifyNote(@NonNull MainNote note, @NonNull String title, @NonNull String content) {
        ChangesInNote changesInNote = checkForDifferences(note, title, content);
        noteChangesResult = updateNote(note, changesInNote, title, content);
    }

    @NonNull
    private ChangesInNote checkForDifferences(@NonNull MainNote note, @NonNull String title,
                                              @NonNull String content) {

        boolean titleChanged = !note.getTitle().equals(title);
        boolean contentChanged = !note.getContent().equals(content);
        boolean favoriteChanged = note.getFavorite() == 1 != favorite;

        return new ChangesInNote(titleChanged, contentChanged, favoriteChanged);
    }

    private int updateNote(@NonNull MainNote note, @NonNull ChangesInNote changes,
                           @NonNull String title, @NonNull String content) {

        int result = -1;

        if (changes.checkIfAllValuesFalse()) {
            result = 0;
            noteModified = true;

            if (changes.isTitleChanged() || changes.isContentChanged()) {
                if (changes.isTitleChanged()) {
                    note.setTitle(title);
                }
                if (changes.isContentChanged()) {
                    note.setContent(content);
                }
                note.setTimeModified(System.currentTimeMillis());
            }

            if (changes.isFavoriteChanged()) {
                note.setFavorite(favorite ? 1 : 0);
            }
        }

        if (listUsed == 1 && changes.isFavoriteChanged()) {
            result = 3;
        } else if (reminderModified) {
            result = 0;
        }

        return result;
    }

    private void createNewNote(@NonNull String title,
                               @NonNull String content) {
        isNewNote = true;
        if (mReminder != null) {

            mMainNote = new MainNote(title, content, System.currentTimeMillis(),
                    favorite ? 1 : 0, -1);
            observableAddReminder(mReminder);
            mEditorView.setupReminderNotification(mMainNote, mReminder);

        } else {
            mMainNote = new MainNote(title, content, System.currentTimeMillis(),
                    favorite ? 1 : 0, -1);
            observableAddNote(mMainNote);
        }

    }

    private void modifyReminder(@NonNull MainNote note, @Nullable Reminder reminder) {

        if (note.getReminderId() != -1) {
            if (reminder == null) {     // Delete reminder
                deleteReminder(note);

            } else {        // Update reminder
                updateReminder(note, reminder);
            }

            if (noteModified) {
                observableUpdateNote(note, listUsed);
            } else {
                mEditorView.goBackToMainActivity(note, noteChangesResult, listUsed);
            }

        } else if (reminder != null) {      // Add reminder
            addReminder(note, reminder);
        }
    }

    private void addReminder(@Nonnull MainNote note, @Nonnull Reminder reminder) {
        noteModified = true;
        reminder.setId(3);
        observableAddReminder(reminder);
        mEditorView.setupReminderNotification(note, reminder);
    }

    private void updateReminder(@Nonnull MainNote note, @Nonnull Reminder reminder) {
        observableUpdateReminder(reminder);
        mEditorView.setupReminderNotification(note, reminder);
    }

    private void deleteReminder(@Nonnull MainNote note) {
        noteModified = true;
        observableDeleteReminder(note.getReminderId());
        mEditorView.cancelReminderNotification(note.getReminderId());
        note.setReminderId(-1);
    }


    public void onDeleteNoteForever() {
        if (mMainNote != null) {

            if (mMainNote.getReminderId() != -1) {
                observableDeleteReminder(mMainNote.getReminderId());
            }
            mEditorView.goBackToMainActivity(mMainNote, 2, listUsed);
        }
    }


    private void resultUpdateNoteObservable() {
        mEditorView.goBackToMainActivity(mMainNote, noteChangesResult, listUsed);
    }

    private void resultAddNoteObservable(int id) {
        assert mMainNote != null;
        mMainNote.setId(id);
        mEditorView.goBackToMainActivity(mMainNote, 1, listUsed);
    }

    private void resultGetNoteObservable(@Nonnull MainNote note) {
        mMainNote = note;
        setUpView(note);
    }

    private void resultAddReminderObservable(long reminderId) {
        assert mReminder != null;
        assert mMainNote != null;
        mReminder.setId((int) reminderId);
        mMainNote.setReminderId((int) reminderId);

        if (isNewNote) {
            observableAddNote(mMainNote);
        } else {
            observableUpdateNote(mMainNote, listUsed);
        }
    }

    private void resultGetReminderObservable(Reminder reminder) {
        mReminder = reminder;
        mEditorView.showReminder(reminder);
    }


    private void observableUpdateNote(@Nonnull MainNote note, int listUsed) {
        mDisposable.add(mEditorInteractor.updateNote(note, listUsed)
                .subscribeOn(Schedulers.io())
                .subscribe(this::resultUpdateNoteObservable));
    }

    private void observableAddNote(@Nonnull MainNote note) {
        mDisposable.add(mEditorInteractor.addNote(note)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(aLong -> resultAddNoteObservable(aLong.intValue())));
    }

    private void observableDeleteNote() {

    }

    private void observableGetNote(int noteId, int listUsed) {
        mDisposable.add(mEditorInteractor.getNote(noteId, listUsed)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::resultGetNoteObservable));
    }


    private void observableUpdateReminder(@Nonnull Reminder reminder) {
        mDisposable.add(mEditorInteractor.updateReminder(reminder)
                .subscribeOn(Schedulers.io())
                .subscribe(this::resultUpdateNoteObservable));
    }

    private void observableAddReminder(@Nonnull Reminder reminder) {
        mDisposable.add(mEditorInteractor.addReminder(reminder)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::resultAddReminderObservable));
    }

    private void observableDeleteReminder(int reminderId) {
        mDisposable.add(mEditorInteractor.deleteReminder(reminderId)
                .subscribeOn(Schedulers.io())
                .subscribe());
    }

    private void observableDeleteReminder(@Nonnull Reminder reminder) {
        mDisposable.add(mEditorInteractor.deleteReminder(reminder)
                .subscribeOn(Schedulers.io())
                .subscribe());
    }

    private void observableGetReminder(int reminderId) {
        mDisposable.add(mEditorInteractor.getReminder(reminderId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::resultGetReminderObservable));
    }


    public void onDestroy() {
        mEditorView = null;
        if (!mDisposable.isDisposed()) {
            mDisposable.clear();
        }
    }


    public void onReminderPicked(@Nonnull Reminder reminder, @Nonnull String newReminderText) {
        if (mReminder != reminder) {
            mReminder = reminder;

            // Keep the id of the previous reminder
            if (mMainNote != null && mMainNote.getReminderId() != -1) {
                mReminder.setId(mMainNote.getReminderId());
            }

            reminderModified = true;
        }
        mEditorView.updateReminderDisplay(newReminderText, mReminder.getFrequencyChoices());
    }

    public void onReminderDeleted() {
        if (mMainNote != null && mMainNote.getReminderId() != -1) {
            if (mReminder != null) {
                reminderModified = true;
                mEditorInteractor.deleteReminder(mReminder);
            }
        }
        mReminder = null;
        mEditorView.hideReminder();
    }
}
