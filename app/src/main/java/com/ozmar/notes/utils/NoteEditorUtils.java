package com.ozmar.notes.utils;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.Menu;
import android.view.MenuItem;

import com.ozmar.notes.R;
import com.ozmar.notes.SingleNote;

public class NoteEditorUtils {

    private NoteEditorUtils() {
    }

    @NonNull
    public static String differenceFromOriginal(Context context, String title, String content, SingleNote note) {
        String[] noteChanges = context.getResources().getStringArray(R.array.noteChangesArray);

        if (title.isEmpty() && content.isEmpty() && note == null) {
            return "";

        } else if (note != null) {
            boolean titleTheSame = title.equals(note.get_title());
            boolean contentTheSame = content.equals(note.get_content());

            if (!(titleTheSame && contentTheSame)) {
                if (titleTheSame) {
                    note.set_content(content);
                } else if (contentTheSame) {
                    note.set_title(title);
                } else {
                    note.set_title(title);
                    note.set_content(content);
                }
                return noteChanges[0];      // Note modified
            }

            return noteChanges[2];      // Check if favorite changed later
        }

        return noteChanges[1];      // New note
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

            if (note != null && note.get_favorite() == 1) {
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