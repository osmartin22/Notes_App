package com.ozmar.notes.noteEditor;

import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.ozmar.notes.Reminder;
import com.ozmar.notes.SingleNote;
import com.ozmar.notes.async.AddNoteAsync;
import com.ozmar.notes.async.AddReminderAsync;
import com.ozmar.notes.async.DeleteNoteAsync;

import javax.annotation.Nonnull;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

// TODO: Separate database calls from presenter

// TODO: Deleting a note through multi select does not cancel reminder notification


public class NoteEditorPresenter implements AddNoteAsync.NewNoteResult, AddReminderAsync.NewReminderResult {

    private NoteEditorView mEditorView;
    private NoteEditorInteractor mEditorInteractor;

    private static final int USER_NOTES = 0;
    private static final int FAVORITE_NOTES = 1;
    private static final int ARCHIVE_NOTES = 2;
    private static final int RECYCLE_BIN_NOTES = 3;

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

    private void getNoteResult(@Nullable SingleNote note) {
        mNote = note;
        setUpView(note);
    }

    public void initialize(int noteId, @IntRange(from = 0, to = 3) int listUsed) {
        this.listUsed = listUsed;

        mDisposable.add(mEditorInteractor.getNote(noteId, listUsed)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::getNoteResult));
    }


    private void getReminderResult(Reminder reminder) {
        mReminder = reminder;
        assert mNote != null;
        mEditorView.showReminder(mNote, mReminder.getDateTime().getMillis());
    }

    private void setUpView(@Nullable SingleNote note) {
        if (note != null) {
            favorite = note.isFavorite();
            mEditorView.setupNoteEditTexts(note);

            if (note.getReminderId() != -1) {
                mDisposable.add(mEditorInteractor.getReminder(note.getReminderId())
                        .observeOn(Schedulers.io())
                        .subscribe(this::getReminderResult));
            }

        } else {
            mEditorView.requestFocusOnContent();
        }
    }

    public void onFavoriteClicked() {
        favorite = !favorite;
        mEditorView.updateFavoriteIcon(favorite);
    }

    @Override
    public void getNewId(int id) {
        assert mNote != null;
        mNote.setId(id);
        mEditorView.goBackToMainActivity(mNote, 1, listUsed);
    }

    @Override
    public void getNewReminderId(int reminderId) {
        assert mReminder != null;
        assert mNote != null;
        mReminder.setId(reminderId);
        mNote.setId(reminderId);
    }

    private void createNewNote(@NonNull String title,
                               @NonNull String content) {

        if (mReminder != null) {
            mNote = new SingleNote(title, content, favorite, System.currentTimeMillis(),
                    mReminder.getDateTime().getMillis(), -1);

            new AddReminderAsync(this, mReminder).execute();
            mEditorView.setupReminder(mNote);

        } else {
            mNote = new SingleNote(title, content, favorite, System.currentTimeMillis());
        }

        new AddNoteAsync(this, mNote, listUsed).execute();

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

            mEditorInteractor.updateNote(mNote, listUsed);
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

        if (listUsed == FAVORITE_NOTES && changes.isFavoriteChanged()) {
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
                mEditorInteractor.deleteReminder(note.getReminderId());
                mEditorInteractor.deleteReminder(note.getReminderId());
                note.setReminderId(-1);

            } else {        // Update reminder
                mEditorInteractor.updateReminder(reminder);
                mEditorView.setupReminder(note);
            }

        } else if (reminder != null) {      // Add reminder

//            Single.fromCallable(() -> AppDatabase.getAppDatabase().remindersDao().addReminder(reminder))
//                    .subscribeOn(Schedulers.newThread())
//                    .subscribe();

            new AddReminderAsync(this, reminder).execute();
            note.setNextReminderTime(reminder.getDateTime().getMillis());
            mEditorView.setupReminder(note);
        }
    }

    public void onDeleteNoteForever() {
        if (mNote != null) {
            new DeleteNoteAsync(mNote.getId(), mNote.getReminderId(), listUsed).execute();
            mEditorView.goBackToMainActivity(mNote, 2, listUsed);
        }
    }

    public void onDestroy() {
        mEditorView = null;
        mDisposable.clear();
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
