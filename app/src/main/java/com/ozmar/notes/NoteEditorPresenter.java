package com.ozmar.notes;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.ozmar.notes.utils.NoteChanges;


public class NoteEditorPresenter {
    private NoteEditorView noteEditorView;

    public NoteEditorPresenter(NoteEditorView noteEditorView) {
        this.noteEditorView = noteEditorView;
    }

    public void setUpNoteView(@NonNull SingleNote note) {
        noteEditorView.setUpNoteEditTexts(note);
    }

    public void setUpReminderDisplay(@NonNull SingleNote note) {
        if (note.get_reminderId() != -1) {
            noteEditorView.displayReminder(note);
        }
    }

    public void updateReminderDisplay(Long newReminderTime, @NonNull String newReminderText, @Nullable FrequencyChoices choices) {
        noteEditorView.updateDisplayReminder(newReminderTime, newReminderText, choices);
    }

    public SingleNote createNewNote(@NonNull DatabaseHandler db, @Nullable FrequencyChoices choices, @NonNull String title,
                                    @NonNull String content, boolean favorite, long reminderTime) {
        SingleNote newNote;

        if (reminderTime != 0) {
            int reminderId = db.addReminder(choices, reminderTime);

            newNote = new SingleNote(title, content, favorite, System.currentTimeMillis(), reminderTime, reminderId);

            if (choices != null) {
                newNote.set_hasFrequencyChoices(true);
            }

            noteEditorView.setupReminder(newNote);

        } else {
            newNote = new SingleNote(title, content, favorite, System.currentTimeMillis());
        }

        newNote.set_id(db.addNoteToUserList(newNote));

        return newNote;
    }

    public void upDateNote(@NonNull SingleNote note, @Nullable NoteChanges noteChanges,
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

    public void saveReminder(@NonNull DatabaseHandler db, @NonNull SingleNote note, @Nullable FrequencyChoices choices,
                             @Nullable NoteChanges changes, long reminderTime) {

        boolean idChanged = false;

        if (choices != null) {
            note.set_hasFrequencyChoices(true);
        } else {
            note.set_hasFrequencyChoices(false);
        }

        note.set_nextReminderTime(reminderTime);

        // New reminder
        if (note.get_reminderId() == -1 && reminderTime != 0) {
            idChanged = true;
            int newId = db.addReminder(choices, reminderTime);
            note.set_reminderId(newId);
            noteEditorView.setupReminder(note);

        } else if (note.get_reminderId() != -1) {

            // Delete reminder
            if (reminderTime == 0) {
                idChanged = true;
                noteEditorView.cancelReminder(note.get_reminderId());
                db.deleteReminder(note.get_reminderId());
                note.set_reminderId(-1);

                // Updating reminder
            } else {
                if (reminderTime != note.get_nextReminderTime()) {
                    note.set_nextReminderTime(reminderTime);
                    noteEditorView.setupReminder(note);
                }

                db.updateReminder(note.get_reminderId(), choices, reminderTime);
            }
        }

        if (changes != null) {
            changes.setReminderIdChanged(idChanged);
        }

    }

    public void onDestroy() {
        noteEditorView = null;
    }
}
