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

    public void onReminderPicked(@Nonnull Reminder reminder, @Nonnull String newReminderText) {
        if (mReminder != reminder) {
            mReminder = reminder;

            // Keep the id of the previous reminder
            if (mMainNote != null && mMainNote.getReminderId() != -1) {
                mReminder.setId(mMainNote.getReminderId());
            }

            reminderModified = true;
            mEditorView.updateReminderDisplay(newReminderText, mReminder.getFrequencyChoices());
        }
    }

    public void onReminderDeleted() {
        if (mReminder != null) {

            if (mMainNote != null && mMainNote.getReminderId() != -1) {
                reminderModified = true;
                mEditorInteractor.deleteReminder(mReminder);
            }

            mReminder = null;
            mEditorView.hideReminder();
        }
    }

    /// Deletion of note is handled in NotePreviews
    public void onDeleteNoteForever() {
        if (mMainNote != null) {

            if (mMainNote.getReminderId() != -1) {
                observableDeleteReminder(mMainNote.getReminderId());
            }

            mEditorView.goBackToMainActivity(mMainNote, 2, listUsed);
        }
    }

    public void onSaveNote(@NonNull String title, @NonNull String content) {

        if (mMainNote != null) {

            ChangesInNote changesInNote = checkForDifferences(mMainNote, title, content);
            noteChangesResult = updateNoteWithNewChanges(mMainNote, changesInNote, title, content);

            if (reminderModified) {
                boolean needToUpdateNote = changesInNote.checkIfAllValuesFalse();
                saveChangesToReminder(mMainNote, mReminder, needToUpdateNote);

                // Only update the note if the note was modified
            } else {
                if (noteChangesResult != -1) {
                    observableUpdateNote(mMainNote, listUsed);

                } else {
                    mEditorView.goBackToMainActivity(mMainNote, -1, listUsed);
                }
            }

        } else {
            saveNewNote(title, content);
        }
    }

    private void saveNewNote(@NonNull String title, @NonNull String content) {
        boolean titleEmpty = title.isEmpty();
        boolean contentEmpty = content.isEmpty();

        if (!(titleEmpty && contentEmpty)) {
            createNewNote(title, content, mReminder);

        } else {
            mEditorView.goBackToMainActivity(null, -1, listUsed);
        }
    }

    private void createNewNote(@NonNull String title, @NonNull String content,
                               @Nullable Reminder reminder) {
        isNewNote = true;
        if (reminder != null) {

            mMainNote = new MainNote(title, content, System.currentTimeMillis(),
                    favorite ? 1 : 0, -1);
            observableAddReminder(reminder);
            mEditorView.setupReminderNotification(mMainNote, reminder);

        } else {
            mMainNote = new MainNote(title, content, System.currentTimeMillis(),
                    favorite ? 1 : 0, -1);
            observableAddNote(mMainNote);
        }

    }

    @NonNull
    private ChangesInNote checkForDifferences(@NonNull MainNote note, @NonNull String title,
                                              @NonNull String content) {

        boolean titleChanged = !note.getTitle().equals(title);
        boolean contentChanged = !note.getContent().equals(content);
        boolean favoriteChanged = note.getFavorite() == 1 != favorite;

        return new ChangesInNote(titleChanged, contentChanged, favoriteChanged);
    }

    private int updateNoteWithNewChanges(@NonNull MainNote note, @NonNull ChangesInNote changes,
                                         @NonNull String title, @NonNull String content) {

        int result = -1;

        if (changes.checkIfAllValuesFalse()) {
            result = 0;

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

    private void saveChangesToReminder(@NonNull MainNote note, @Nullable Reminder reminder,
                                       boolean needToUpdateNote) {

        if (note.getReminderId() != -1) {
            if (reminder == null) {     // Delete reminder
                needToUpdateNote = true;
                deleteReminder(note);

            } else {        // Update reminder
                updateReminder(note, reminder);
            }

            if (needToUpdateNote) {
                observableUpdateNote(note, listUsed);

            } else {
                mEditorView.goBackToMainActivity(note, noteChangesResult, listUsed);
            }

        } else if (reminder != null) {      // Add reminder
            addReminder(note, reminder);
        }
    }

    private void addReminder(@Nonnull MainNote note, @Nonnull Reminder reminder) {
        observableAddReminder(reminder);
        mEditorView.setupReminderNotification(note, reminder);
    }

    private void updateReminder(@Nonnull MainNote note, @Nonnull Reminder reminder) {
        observableUpdateReminder(reminder);
        mEditorView.setupReminderNotification(note, reminder);
    }

    private void deleteReminder(@Nonnull MainNote note) {
        observableDeleteReminder(note.getReminderId());
        mEditorView.cancelReminderNotification(note.getReminderId());
        note.setReminderId(-1);
    }


    // Note observables
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

    private void observableGetNote(int noteId, int listUsed) {
        mDisposable.add(mEditorInteractor.getNote(noteId, listUsed)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::resultGetNoteObservable));
    }


    // Reminder observables
    private void observableUpdateReminder(@Nonnull Reminder reminder) {
        mDisposable.add(mEditorInteractor.updateReminder(reminder)
                .subscribeOn(Schedulers.io())
                .subscribe());
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

    private void observableGetReminder(int reminderId) {
        mDisposable.add(mEditorInteractor.getReminder(reminderId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::resultGetReminderObservable));
    }


    // Observable result methods
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


    public void onDestroy() {
        mEditorView = null;
        if (!mDisposable.isDisposed()) {
            mDisposable.clear();
        }
    }
}
