package com.ozmar.notes.noteEditor;

import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.ozmar.notes.Reminder;
import com.ozmar.notes.database.MainNote;

import javax.annotation.Nonnull;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

// TODO: Separate database calls from presenter

// TODO: Deleting a note through multi select does not cancel reminder notification

// TODO: Decide if favorite of MainNote should be an int or boolean

public class NoteEditorPresenter {

    private int listUsed;
    private boolean favorite = false;
    private boolean isNewNote = false;

    private boolean reminderChanged = false;

    private NoteEditorView mEditorView;
    private NoteEditorInteractor mEditorInteractor;
    private final CompositeDisposable mDisposable = new CompositeDisposable();

    @Nullable
    private MainNote mMainNote;

    @Nullable
    private Reminder mReminder;


    public NoteEditorPresenter(@Nonnull NoteEditorView mEditorView) {
        this.mEditorView = mEditorView;
        this.mEditorInteractor = new NoteEditorInteractor();
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
            ChangesInNote changesInNote = checkForDifferences(mMainNote, title, content);
            int result = updateNote(mMainNote, changesInNote, title, content);

            if (reminderChanged) {
                updateReminder(mMainNote, mReminder);
            }

            observableUpdateNote(mMainNote, listUsed);
            mEditorView.goBackToMainActivity(mMainNote, result, listUsed);

        } else {
            boolean titleEmpty = title.isEmpty();
            boolean contentEmpty = content.isEmpty();

            if (!(titleEmpty && contentEmpty)) {
                isNewNote = true;
                createNewNote(title, content);
            } else {
                mEditorView.goBackToMainActivity(null, -1, listUsed);
            }
        }
    }

    private int updateNote(@NonNull MainNote note, @NonNull ChangesInNote changes,
                           @NonNull String title, @NonNull String content) {

        int result = -1;

        if (changes.checkIfAllValuesFalse()) {

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
        } else if (changes.checkIfAllValuesFalse()) {
            result = 0;
        }

        return result;
    }

    private void updateReminder(@NonNull MainNote note, @Nullable Reminder reminder) {

        if (note.getReminderId() != -1) {
            if (reminder == null) {     // Delete reminder
                mEditorView.cancelReminder(note.getReminderId());
                observableDeleteReminder(note.getReminderId());
                note.setReminderId(-1);

            } else {        // Update reminder
                mEditorInteractor.updateReminder(reminder);
                observableUpdateReminder(reminder);
                mEditorView.setupReminder(note);
            }

        } else if (reminder != null) {      // Add reminder
            observableAddReminder(reminder);
//            note.setNextReminderTime(reminder.getDateTime().getMillis());
            mEditorView.setupReminder(note);
        }
    }

    private void createNewNote(@NonNull String title,
                               @NonNull String content) {
        if (mReminder != null) {

            mMainNote = new MainNote(title, content, System.currentTimeMillis(),
                    favorite ? 1 : 0, -1);

            observableAddReminder(mReminder);
            mEditorView.setupReminder(mMainNote);

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

    public void onDeleteNoteForever() {
        if (mMainNote != null) {

            if (mMainNote.getReminderId() != -1) {
                observableDeleteReminder(mMainNote.getReminderId());
            }
            mEditorView.goBackToMainActivity(mMainNote, 2, listUsed);
        }
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

    private void resultAddReminderObservable(int reminderId) {
        assert mReminder != null;
        assert mMainNote != null;
        mReminder.setId(reminderId);
        mMainNote.setReminderId(reminderId);

        if (isNewNote) {
            observableAddNote(mMainNote);
        }
    }

    private void resultGetReminderObservable(Reminder reminder) {

        mEditorView.showReminder(reminder);
    }


    private void observableUpdateNote(@Nonnull MainNote note, int listUsed) {
        mDisposable.add(Single.fromCallable(() -> mEditorInteractor.updateNote(note, listUsed))
                .subscribeOn(Schedulers.io())
                .subscribe());
    }

    private void observableAddNote(@Nonnull MainNote note) {
        mDisposable.add(Single.fromCallable(() -> mEditorInteractor.addNote(note))
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
        mDisposable.add(Single.fromCallable(() -> mEditorInteractor.updateReminder(reminder))
                .subscribeOn(Schedulers.io())
                .subscribe());
    }

    private void observableAddReminder(@Nonnull Reminder reminder) {
        mDisposable.add(Single.fromCallable(() -> mEditorInteractor.addReminder(reminder))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(aLong -> resultAddReminderObservable(aLong.intValue())));
    }

    private void observableDeleteReminder(int reminderId) {
        mDisposable.add(Single.fromCallable(() -> mEditorInteractor.deleteReminder(reminderId))
                .subscribeOn(Schedulers.io())
                .subscribe());
    }

    private void observableDeleteReminder(@Nonnull Reminder reminder) {
        mDisposable.add(Single.fromCallable(() -> mEditorInteractor.deleteReminder(reminder))
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
            reminderChanged = true;
        }
        mEditorView.updateReminderDisplay(newReminderText, mReminder.getFrequencyChoices());
    }

    public void onReminderDeleted() {
        if (mMainNote != null) {
            mEditorInteractor.deleteReminder(mReminder);
        }
        mReminder = null;
        mEditorView.hideReminder();
    }
}
