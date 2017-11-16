package com.ozmar.notes;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.ozmar.notes.async.UpdateNoteAsync;
import com.ozmar.notes.utils.NoteChanges;


public class NoteEditorPresenter {
    private NoteEditorView noteEditorView;

    private static final int USER_NOTES = 0;
    private static final int FAVORITE_NOTES = 1;
    private static final int ARCHIVE_NOTES = 2;
    private static final int RECYCLE_BIN_NOTES = 3;

    private int listUsed;
    private SingleNote mNote;
    private long reminderTime = 0;
    private boolean favorite = false;
    private boolean frequencyChanged = false;
    private FrequencyChoices mFrequencyChoices;


    private ChangesInNote mChangesInNote;   // TODO: Can probably be more local

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
        return reminderTime;
    }

    public int getListUsed() {
        return listUsed;
    }

    public FrequencyChoices getFrequencyChoices() {
        return mFrequencyChoices;
    }


    public void initialize(DatabaseHandler db, int noteId, int listUsed) {
        this.listUsed = listUsed;
        requestNote(db, noteId);

        if (mNote != null) {


            if (listUsed == ARCHIVE_NOTES && mNote.is_favorite()) {
                favorite = false;
                mNote.set_favorite(false);
            } else {
                favorite = mNote.is_favorite();
            }


            noteEditorView.setupNoteEditTexts(mNote);
            if (mNote.get_reminderId() != -1) {


                mFrequencyChoices = db.getFrequencyChoice(mNote.get_reminderId());
                reminderTime = mNote.get_nextReminderTime();

                noteEditorView.showReminder(mNote, reminderTime);
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

        if (reminderTime != 0) {
            int reminderId = db.addReminder(mFrequencyChoices, reminderTime);

            newNote = new SingleNote(title, content, favorite, System.currentTimeMillis(), reminderTime, reminderId);

            if (mFrequencyChoices != null) {
                newNote.set_hasFrequencyChoices(true);
            }

            noteEditorView.setupReminder(newNote);

        } else {
            newNote = new SingleNote(title, content, favorite, System.currentTimeMillis());
        }

        newNote.set_id(db.addNoteToUserList(newNote));

        return newNote;
    }

    @NonNull
    private ChangesInNote checkForDifferences(@NonNull SingleNote note, @NonNull String title,
                                              @NonNull String content) {

        boolean titleChanged = !note.get_title().equals(title);
        boolean contentChanged = !note.get_content().equals(content);
        boolean favoriteChanged = note.is_favorite() != favorite;

        boolean reminderChanged = false;
        if (note.get_nextReminderTime() != 0) {
            reminderChanged = note.get_nextReminderTime() != reminderTime;
        }

        return new ChangesInNote(titleChanged, contentChanged, favoriteChanged,
                reminderChanged, frequencyChanged);

    }

    public void onSaveNote(@NonNull String title, @NonNull String content, @NonNull DatabaseHandler db) {
        int result = 0;

        if (listUsed == ARCHIVE_NOTES) {
            onFavoriteClicked();
        }

        if (mNote != null) {
            result = update(mNote, title, content);
            saveReminder(db, mNote, reminderTime);
            new UpdateNoteAsync(db, null, mNote, listUsed, mChangesInNote).execute();
        } else {

            boolean titleEmpty = title.isEmpty();
            boolean contentEmpty = content.isEmpty();
            if (!(titleEmpty && contentEmpty)) {    // New note
                mNote = createNewNote(db, title, content);
                result = 1;
            }
        }

        // TODO: Change to create bundle in presenter before sending to View
        noteEditorView.goBackToMainActivity(mNote, result);
    }


    private int update(@NonNull SingleNote note, @NonNull String title, @NonNull String content) {

        int result;
        mChangesInNote = checkForDifferences(note, title, content);

        if (mChangesInNote.checkIfAllValuesFalse()) {
            if (mChangesInNote.isTitleChanged()) {
                note.set_title(title);
            }
            if (mChangesInNote.isContentChanged()) {
                note.set_content(content);
            }
            if (mChangesInNote.isFavoriteChanged()) {
                note.set_favorite(favorite);
            }
            if (mChangesInNote.isReminderTimeChanged()) {
                note.set_nextReminderTime(reminderTime);
            }
            if (mChangesInNote.isFrequencyChanged()) {

            }
            note.set_timeModified(System.currentTimeMillis());
        }

        if (listUsed == FAVORITE_NOTES && mChangesInNote.isFavoriteChanged()) {       // Note not a favorite anymore
            result = 3;
        } else {
            result = 0;
        }

        return result;
    }

    private void saveReminder(@NonNull DatabaseHandler db, @NonNull SingleNote note, long reminderTime) {

        if (mFrequencyChoices != null) {
            note.set_hasFrequencyChoices(true);
        } else {
            note.set_hasFrequencyChoices(false);
        }

        // New reminder
        if (note.get_reminderId() == -1 && reminderTime != 0) {
            int newId = db.addReminder(mFrequencyChoices, reminderTime);
            note.set_nextReminderTime(reminderTime);
            note.set_reminderId(newId);
            noteEditorView.setupReminder(note);

        } else if (note.get_reminderId() != -1) {

            // Delete reminder
            if (reminderTime == 0) {
                noteEditorView.cancelReminder(note.get_reminderId());
                db.deleteReminder(note.get_reminderId());
                note.set_reminderId(-1);
                note.set_nextReminderTime(reminderTime);

                // Updating reminder
            } else {
                // TODO: Temp solution
                db.updateReminder(note.get_reminderId(), mFrequencyChoices, reminderTime);
            }
        }
    }

    public void onDestroy() {
        noteEditorView = null;
    }


    public void onReminderPicked(@Nullable FrequencyChoices choices, long nextReminderTime,
                                 @NonNull String newReminderText) {
        if (this.mFrequencyChoices != choices) {
            this.mFrequencyChoices = choices;
            frequencyChanged = true;
        }

        if (this.reminderTime != nextReminderTime) {
            this.reminderTime = nextReminderTime;
        }

        noteEditorView.updateReminderDisplay(newReminderText, mFrequencyChoices);
    }

    public void onReminderDeleted() {
        mFrequencyChoices = null;
        reminderTime = 0;
        noteEditorView.hideReminder();
    }


    public void testMenu() {

    }


    public void updateNote(@NonNull SingleNote note, @Nullable NoteChanges noteChanges,
                           @NonNull String title, @NonNull String content) {
        boolean titleTheSame = title.equals(note.get_title());
        boolean contentTheSame = content.equals(note.get_content());

        int changeNumber;
        if (!(titleTheSame && contentTheSame)) {
            if (titleTheSame) {
                changeNumber = 2;
                note.set_content(content);
            } else if (contentTheSame) {
                changeNumber = 1;
                note.set_title(title);
            } else {
                changeNumber = 3;
                note.set_title(title);
                note.set_content(content);
            }

            if (noteChanges != null) {
                noteChanges.setNoteTextChanges(changeNumber);
            }

            note.set_timeModified(System.currentTimeMillis());
        }
    }

    public void saveReminder(@NonNull DatabaseHandler db, @NonNull SingleNote note,
                             @Nullable NoteChanges changes, long reminderTime) {

        boolean idChanged = false;

        if (mFrequencyChoices != null) {
            note.set_hasFrequencyChoices(true);
        } else {
            note.set_hasFrequencyChoices(false);
        }

        // New reminder
        if (note.get_reminderId() == -1 && reminderTime != 0) {
            idChanged = true;
            int newId = db.addReminder(mFrequencyChoices, reminderTime);
            note.set_nextReminderTime(reminderTime);
            note.set_reminderId(newId);
            noteEditorView.setupReminder(note);

        } else if (note.get_reminderId() != -1) {

            // Delete reminder
            if (reminderTime == 0) {
                idChanged = true;
                noteEditorView.cancelReminder(note.get_reminderId());
                db.deleteReminder(note.get_reminderId());
                note.set_reminderId(-1);
                note.set_nextReminderTime(reminderTime);

                // Updating reminder
            } else {
                if (reminderTime != note.get_nextReminderTime()) {
                    note.set_nextReminderTime(reminderTime);
                    noteEditorView.setupReminder(note);
                    db.updateReminder(note.get_reminderId(), mFrequencyChoices, reminderTime);

                } else if (changes != null && changes.isFrequencyChoiceChanged()) {
                    db.updateReminder(note.get_reminderId(), mFrequencyChoices, reminderTime);
                }
            }
        }

        if (changes != null) {
            changes.setReminderIdChanged(idChanged);
        }

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
    }   // TODO: Should be in model
}
