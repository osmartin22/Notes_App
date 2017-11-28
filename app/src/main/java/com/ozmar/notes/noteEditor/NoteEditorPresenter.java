package com.ozmar.notes.noteEditor;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.ozmar.notes.ChangesInNote;
import com.ozmar.notes.DatabaseHandler;
import com.ozmar.notes.Reminder;
import com.ozmar.notes.SingleNote;
import com.ozmar.notes.database.AppDatabase;
import com.ozmar.notes.database.MainNote;
import com.ozmar.notes.database.NoteConversion;

import javax.annotation.Nonnull;

// TODO: Separate database calls from presenter

// TODO: Deleting a note through multi select does not cancel reminder notification

// TODO: Opening a note without a reminder time crashes on return(reminder.datetime == null)
// TODO: Exiting NoteEditorActivity with a reminder causes a notification to be created with the reminder time
// Should not happen as the reminder already occurred

public class NoteEditorPresenter {
    private NoteEditorView noteEditorView;

    private static final int USER_NOTES = 0;
    private static final int FAVORITE_NOTES = 1;
    private static final int ARCHIVE_NOTES = 2;
    private static final int RECYCLE_BIN_NOTES = 3;

    private int listUsed;
    private boolean favorite = false;
    private boolean reminderChanged = false;
    private boolean frequencyChanged = false;

    @Nullable
    private SingleNote mNote;

    @Nullable
    private Reminder mReminder;

    public NoteEditorPresenter(NoteEditorView noteEditorView) {
        this.noteEditorView = noteEditorView;
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

    public void initialize(int noteId, int listUsed) {
        this.listUsed = listUsed;
        mNote = requestNote(noteId);

        if (mNote != null) {
            favorite = mNote.isFavorite();
            noteEditorView.setupNoteEditTexts(mNote);

            if (mNote.getReminderId() != -1) {
                mReminder = AppDatabase.getAppDatabase().remindersDao().getReminder(mNote.getReminderId());
                noteEditorView.showReminder(mNote, mReminder.getDateTime().getMillis());
            }

        } else {
            noteEditorView.requestFocusOnContent();
        }
    }

    public void onFavoriteClicked() {
        favorite = !favorite;
        noteEditorView.updateFavoriteIcon(favorite);
    }

    private SingleNote createNewNote(@NonNull String title,
                                     @NonNull String content) {
        SingleNote newNote;

        if (mReminder != null) {

            int reminderId = (int) AppDatabase.getAppDatabase().remindersDao().addReminder(mReminder);
            newNote = new SingleNote(title, content, favorite, System.currentTimeMillis(),
                    mReminder.getDateTime().getMillis(), reminderId);
            noteEditorView.setupReminder(newNote);

        } else {
            newNote = new SingleNote(title, content, favorite, System.currentTimeMillis());
        }

        newNote.setId((int) AppDatabase.getAppDatabase().notesDao().addToUserNotes(NoteConversion.getMainNoteFromSingleNote(newNote)));

        return newNote;
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

    public void onSaveNote(@NonNull String title, @NonNull String content, @NonNull DatabaseHandler db) {
        int result = -1;

        if (mNote != null) {
            ChangesInNote changesInNote = checkForDifferences(mNote, title, content);
            result = updateNote(mNote, changesInNote, title, content);

            if (reminderChanged) {
                updateReminder(mNote, mReminder);
            }

            MainNote temp = NoteConversion.getMainNoteFromSingleNote(mNote);
            AppDatabase.getAppDatabase().notesDao().updateAUserNote(temp);

        } else {
            boolean titleEmpty = title.isEmpty();
            boolean contentEmpty = content.isEmpty();
            if (!(titleEmpty && contentEmpty)) {    // New note
                mNote = createNewNote(title, content);
                result = 1;
            }
        }

        noteEditorView.goBackToMainActivity(mNote, result, listUsed);
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
            if (reminder == null) {
                // Delete reminder
                noteEditorView.cancelReminder(note.getReminderId());
                AppDatabase.getAppDatabase().remindersDao().deleteReminder(note.getReminderId());
                note.setReminderId(-1);
            } else {
                // Update reminder
                AppDatabase.getAppDatabase().remindersDao().updateReminder(reminder);
                noteEditorView.setupReminder(note);
            }
        } else if (reminder != null) {
            // Add reminder to database
            int newId = (int) AppDatabase.getAppDatabase().remindersDao().addReminder(reminder);
            note.setNextReminderTime(reminder.getDateTime().getMillis());
            note.setReminderId(newId);
            noteEditorView.setupReminder(note);
        }
    }


    public void onDestroy() {
        noteEditorView = null;
    }


    public void onReminderPicked(@Nonnull Reminder reminder, @Nonnull String newReminderText) {
        if (mReminder != reminder) {
            mReminder = reminder;
            reminderChanged = true;
        }
        noteEditorView.updateReminderDisplay(newReminderText, mReminder.getFrequencyChoices());
    }

    public void onReminderDeleted() {
        if (mNote != null) {
            AppDatabase.getAppDatabase().remindersDao().deleteReminder(mReminder);
        }
        mReminder = null;
        noteEditorView.hideReminder();
    }

    // TODO: Move out of presenter
    private SingleNote requestNote(int noteId) {
        if (noteId != -1) {
            Object object = null;
            if (listUsed == 0 || listUsed == 1) {
                object = AppDatabase.getAppDatabase().notesDao().getAUserNote(noteId);
            } else if (listUsed == 2) {
                object = AppDatabase.getAppDatabase().notesDao().getAnArchiveNote(noteId);
            } else if (listUsed == 3) {
                object = AppDatabase.getAppDatabase().notesDao().getARecycleBinNotes(noteId);
            }

            return NoteConversion.getToSingleNoteConversion(object, listUsed);
        }

        return null;
    }
}
