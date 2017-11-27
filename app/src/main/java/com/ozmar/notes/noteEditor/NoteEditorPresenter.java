package com.ozmar.notes.noteEditor;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.ozmar.notes.ChangesInNote;
import com.ozmar.notes.DatabaseHandler;
import com.ozmar.notes.FrequencyChoices;
import com.ozmar.notes.Reminder;
import com.ozmar.notes.SingleNote;
import com.ozmar.notes.async.UpdateNoteAsync;

import org.joda.time.DateTime;

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
    private boolean frequencyChanged = false;

    private SingleNote mNote;
    private Reminder mReminder;

    public NoteEditorPresenter(NoteEditorView noteEditorView) {
        this.noteEditorView = noteEditorView;
    }

    public SingleNote getNote() {
        return mNote;
    }

    public boolean getFavorite() {
        return favorite;
    }

    public long getReminderTime() {
        return mReminder.getDateTime().getMillis();
    }

    public int getListUsed() {
        return listUsed;
    }

    public FrequencyChoices getFrequencyChoices() {
        return mReminder.getFrequencyChoices();
    }

    public void initialize(DatabaseHandler db, int noteId, int listUsed) {
        this.listUsed = listUsed;
        requestNote(db, noteId);

        if (mNote != null) {
            favorite = mNote.isFavorite();
            noteEditorView.setupNoteEditTexts(mNote);

            if (mNote.getReminderId() != -1) {
                FrequencyChoices choices = db.getFrequencyChoice(mNote.getReminderId());
                long reminderTime = mNote.getNextReminderTime();
                mReminder = new Reminder(new DateTime(reminderTime), choices);
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

    private SingleNote createNewNote(@NonNull DatabaseHandler db, @NonNull String title,
                                     @NonNull String content) {
        SingleNote newNote;

        long reminderTime = mReminder.getDateTime().getMillis();
        FrequencyChoices choices = mReminder.getFrequencyChoices();

        if (reminderTime != 0) {
            int reminderId = db.addReminder(choices, reminderTime);
            newNote = new SingleNote(title, content, favorite, System.currentTimeMillis(),
                    reminderTime, reminderId);
            newNote.setHasFrequencyChoices(choices != null);

            noteEditorView.setupReminder(newNote);

        } else {
            newNote = new SingleNote(title, content, favorite, System.currentTimeMillis());
        }

        newNote.setId(db.addNoteToUserList(newNote));

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
//            updateReminder(db, mNote, mReminder.getDateTime().getMillis());
            new UpdateNoteAsync(db, null, mNote, listUsed, changesInNote).execute();

        } else {
            boolean titleEmpty = title.isEmpty();
            boolean contentEmpty = content.isEmpty();
            if (!(titleEmpty && contentEmpty)) {    // New note
                mNote = createNewNote(db, title, content);
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

    private void updateReminder(@NonNull DatabaseHandler db, @NonNull SingleNote note, long reminderTime) {

        // New reminder
        if (note.getReminderId() == -1 && reminderTime != 0) {
            int newId = db.addReminder(mReminder.getFrequencyChoices(), reminderTime);
            note.setNextReminderTime(reminderTime);
            note.setReminderId(newId);
            noteEditorView.setupReminder(note);

        } else if (note.getReminderId() != -1) {

            // Delete reminder
            if (reminderTime == 0) {
                noteEditorView.cancelReminder(note.getReminderId());
                db.deleteReminder(note.getReminderId());
                note.setReminderId(-1);
                note.setNextReminderTime(reminderTime);

                // Updating reminder
            } else {   // TODO: Temp, updates always right now
                db.updateReminder(note.getReminderId(), mReminder.getFrequencyChoices(), reminderTime);
                noteEditorView.setupReminder(note);
            }
        }
    }


    public void onDestroy() {
        noteEditorView = null;
    }


    public void onReminderPicked(@Nullable FrequencyChoices choices, long nextReminderTime,
                                 @NonNull String newReminderText) {
        if (mReminder.getFrequencyChoices() != choices) {
            mReminder.setFrequencyChoices(choices);
            frequencyChanged = true;
        }

        if (mReminder.getDateTime().getMillis() != nextReminderTime) {
            mReminder.setDateTime(new DateTime(nextReminderTime));
        }

        noteEditorView.updateReminderDisplay(newReminderText, mReminder.getFrequencyChoices());
    }

    public void onReminderDeleted() {
        mReminder.setFrequencyChoices(null);
        mReminder.setDateTime(new DateTime(0));
        noteEditorView.hideReminder();
    }

    private void requestNote(DatabaseHandler db, int noteId) {
        if (noteId != -1) {
            if (listUsed == 0 || listUsed == 1) {
                mNote = db.getAUserNote(noteId);
            } else if (listUsed == 2) {
                mNote = db.getAnArchiveNote(noteId);
            } else if (listUsed == 3) {
                mNote = db.getARecycleBinNote(noteId);
            }
        }
    }
}
