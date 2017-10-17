package com.ozmar.notes.utils;

import android.content.Context;
import android.view.Menu;
import android.view.MenuItem;

import com.ozmar.notes.Preferences;
import com.ozmar.notes.R;
import com.ozmar.notes.ReminderManager;
import com.ozmar.notes.SingleNote;


public class NoteEditorUtils {

    private NoteEditorUtils() {
    }

    public static boolean noteChanges(String title, String content, SingleNote note, NoteChanges changes) {
        boolean titleTheSame = title.equals(note.get_title());
        boolean contentTheSame = content.equals(note.get_content());

        if (!(titleTheSame && contentTheSame)) {
            if (titleTheSame) {
                changes.setNoteTextChanges(2);
                note.set_content(content);
            } else if (contentTheSame) {
                changes.setNoteTextChanges(1);
                note.set_title(title);
            } else {
                changes.setNoteTextChanges(3);
                note.set_title(title);
                note.set_content(content);
            }

            note.set_timeModified(System.currentTimeMillis());
            return true;
        }
        return false;
    }

    public static boolean favoriteChanged(boolean favorite, SingleNote note, NoteChanges changes) {
        if (note.is_favorite() == favorite) {
            return false;
        }

        note.set_favorite(favorite);
        changes.setFavoriteChanged(true);
        return true;
    }

    public static boolean reminderChanged(long reminderTime, SingleNote note, NoteChanges changes) {
        if (reminderTime != note.get_reminderTime()) {
            note.set_reminderTime(reminderTime);
            changes.setReminderTimeChanged(true);
            return true;
        }
        return false;
    }

    public static void modifyReminderIntent(Context context, Preferences preferences, SingleNote note, boolean reminderChanged, boolean noteTextChanged) {
        // Adding a new PendingIntent ID
        if (note.get_reminderTime() != 0) {
            if (note.get_reminderId() == 0) {
                note.set_reminderId(preferences.getReminderID());
            }

            if (reminderChanged || noteTextChanged) {
                ReminderManager.start(context, note);
            }

        }

        // Remove Reminder
        else if (note.get_reminderTime() == 0 && reminderChanged) {
            ReminderManager.cancel(context, note.get_reminderId());
            note.set_reminderId(0);
        }
    }

    public static boolean favoriteNote(boolean favorite, MenuItem item) {
        favorite = !favorite;

        if (favorite) {
            item.setIcon(R.drawable.ic_favorite_star_on);
        } else {
            item.setIcon(R.drawable.ic_favorite_star_off);
        }

        return favorite;
    }

    public static void setUpMenu(Menu menu, SingleNote note, int listUsed) {
        if (listUsed != 3) {
            if (listUsed == 2) {
                menu.findItem(R.id.archive_note).setVisible(false);
                menu.findItem(R.id.unarchive_note).setVisible(true);
            }

            if (note != null && note.is_favorite()) {
                menu.findItem(R.id.favorite_note).setIcon(R.drawable.ic_favorite_star_on);
            }

        } else {
            menu.findItem(R.id.restore_note).setVisible(true);
            menu.findItem(R.id.delete_note_forever).setVisible(true);
            menu.findItem(R.id.delete_note).setVisible(false);
            menu.findItem(R.id.archive_note).setVisible(false);
            menu.findItem(R.id.favorite_note).setVisible(false);
        }
    }

    public static void updateNoteObject(SingleNote note, String title, String content, boolean titleChanged, boolean contentChanged) {
        if (titleChanged) {
            note.set_title(title);
        }

        if (contentChanged) {
            note.set_content(content);
        }
    }
}