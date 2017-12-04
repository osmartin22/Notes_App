package com.ozmar.notes.noteEditor;

import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.ozmar.notes.Reminder;
import com.ozmar.notes.SingleNote;

import javax.annotation.Nonnull;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

// TODO: Separate database calls from presenter

// TODO: Deleting a note through multi select does not cancel reminder notification


public class NoteEditorPresenter {

    private NoteEditorView mEditorView;
    private NoteEditorInteractor mEditorInteractor;

    private int listUsed;
    private boolean favorite = false;
    private boolean reminderChanged = false;
    private boolean frequencyChanged = false;

    private final CompositeDisposable mDisposable;

    @Nullable
    private SingleNote mNote;

    @Nullable
    private Reminder mReminder;

    public NoteEditorPresenter(NoteEditorView mEditorView) {
        this.mEditorView = mEditorView;
        this.mEditorInteractor = new NoteEditorInteractor();
        this.mDisposable = new CompositeDisposable();
    }

    @Nullable
    public SingleNote getNote() {
        return mNote;
    }

    public boolean getFavorite() {
        return favorite;
    }

    @Nullable
    public Reminder getReminder() {
        return mReminder;
    }

    public int getListUsed() {
        return listUsed;
    }

    public void initialize(int noteId, @IntRange(from = 0, to = 3) int listUsed) {
        this.listUsed = listUsed;
        observableGetNote(noteId, listUsed);
    }

    private void setUpView(@Nullable SingleNote note) {
        if (note != null) {
            favorite = note.isFavorite();
            mEditorView.setupNoteEditTexts(note);

            if (note.getReminderId() != -1) {
                observableGetReminderWithId(note.getReminderId());
            }

        } else {
            mEditorView.requestFocusOnContent();
        }
    }

    public void onFavoriteClicked() {
        favorite = !favorite;
        mEditorView.updateFavoriteIcon(favorite);
    }

    private void createNewNote(@NonNull String title,
                               @NonNull String content) {

        if (mReminder != null) {
            mNote = new SingleNote(title, content, favorite, System.currentTimeMillis(),
                    mReminder.getDateTime().getMillis(), -1);

            observableAddReminder(mReminder);
            mEditorView.setupReminder(mNote);

        } else {
            mNote = new SingleNote(title, content, favorite, System.currentTimeMillis());
        }

        observableAddNote(mNote);
    }

    @NonNull
    private ChangesInNote checkForDifferences(@NonNull SingleNote note, @NonNull String title,
                                              @NonNull String content) {

        boolean titleChanged = !note.getTitle().equals(title);
        boolean contentChanged = !note.getContent().equals(content);
        boolean favoriteChanged = note.isFavorite() != favorite;
//        boolean reminderTimeChanged = note.getNextReminderTime() != mReminder.getDateTime().getMillis();
        boolean reminderTimeChanged = false;

        return new ChangesInNote(titleChanged, contentChanged, favoriteChanged,
                reminderTimeChanged, frequencyChanged);
    }

    public void onSaveNote(@NonNull String title, @NonNull String content) {

        if (mNote != null) {
            ChangesInNote changesInNote = checkForDifferences(mNote, title, content);
            int result = updateNote(mNote, changesInNote, title, content);

            if (reminderChanged) {
                updateReminder(mNote, mReminder);
            }

            observableUpdateNote(mNote, listUsed);
            mEditorView.goBackToMainActivity(mNote, result, listUsed);

        } else {
            boolean titleEmpty = title.isEmpty();
            boolean contentEmpty = content.isEmpty();
            if (!(titleEmpty && contentEmpty)) {
                createNewNote(title, content);
            }
        }
    }


    private int updateNote(@NonNull SingleNote note, @NonNull ChangesInNote changes,
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
                note.setFavorite(favorite);
            }
            if (changes.isReminderTimeChanged()) {
                note.setNextReminderTime(mReminder.getDateTime().getMillis());
            }
            if (changes.isFrequencyChanged()) {
                note.setHasFrequencyChoices(mReminder.getFrequencyChoices() != null);
            }
        }

        if (listUsed == 1 && changes.isFavoriteChanged()) {
            result = 3;
        } else if (changes.checkIfAllValuesFalse()) {
            result = 0;
        }

        return result;
    }

    private void updateReminder(@NonNull SingleNote note, @Nullable Reminder reminder) {

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
            note.setNextReminderTime(reminder.getDateTime().getMillis());
            mEditorView.setupReminder(note);
        }
    }

    public void onDeleteNoteForever() {
        if (mNote != null) {

            if (mNote.getReminderId() != -1) {
                observableDeleteReminder(mNote.getReminderId());
            }
            mEditorView.goBackToMainActivity(mNote, 2, listUsed);
        }
    }


    private void resultAddNoteObservable(int id) {
        assert mNote != null;
        mNote.setId(id);
        mEditorView.goBackToMainActivity(mNote, 1, listUsed);
    }

    private void resultGetNoteObservable(@Nullable SingleNote note) {
        mNote = note;
        setUpView(note);
    }

    private void resultAddReminderObservable(int reminderId) {
        assert mReminder != null;
        assert mNote != null;
        mReminder.setId(reminderId);
        mNote.setId(reminderId);
    }

    private void resultGetReminderObservable(Reminder reminder) {
        mReminder = reminder;
        assert mNote != null;
        mEditorView.showReminder(mNote, mReminder.getDateTime().getMillis());
    }


    private void observableUpdateNote(@Nonnull SingleNote note, int listUsed) {
        mDisposable.add(Single.fromCallable(() -> mEditorInteractor.updateNote(note, listUsed))
                .subscribeOn(Schedulers.io())
                .subscribe());
    }

    private void observableAddNote(@Nonnull SingleNote note){
        mDisposable.add(Single.fromCallable(() -> mEditorInteractor.addNote(note))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(aLong -> resultAddNoteObservable(aLong.intValue())));
    }

    private void observableDeleteNote(){

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

    private void observableGetReminderWithId(int reminderId) {
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
        if (mNote != null) {
            mEditorInteractor.deleteReminder(mReminder);
        }
        mReminder = null;
        mEditorView.hideReminder();
    }
}
