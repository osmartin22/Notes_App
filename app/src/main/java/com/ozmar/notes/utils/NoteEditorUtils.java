package com.ozmar.notes.utils;

import android.support.annotation.NonNull;
import android.view.MenuItem;
import android.widget.EditText;

import com.ozmar.notes.DatabaseHandler;
import com.ozmar.notes.R;
import com.ozmar.notes.SingleNote;

public class NoteEditorUtils {

    private NoteEditorUtils() {}

    @NonNull
    public static String differenceFromOriginal(String title, String content, SingleNote note) {
        if (title.isEmpty() && content.isEmpty() && note == null) {
            return "discardNote";

        } else if (note != null) {

            boolean titleTheSame = title.equals(note.get_title());
            boolean contentTheSame = content.equals(note.get_content());

            if (!(titleTheSame && contentTheSame)) {

                if (titleTheSame) {
                    return "contentChanged";
                } else if (contentTheSame) {
                    return "titleChanged";
                } else {
                    return "titleAndContentChanged";
                }
            }

            return "notChanged";
        } // else if() end

        return "newNote";
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

    public static int archiveNote(EditText title, EditText content, SingleNote note, DatabaseHandler db) {
        if (note != null) {
            db.addNoteToArchive(note);
            db.deleteNoteFromUserList(note);
            return 4;
        } else {
            db.addNoteToArchive(new SingleNote(title.getText().toString(), content.getText().toString()));
            return 2;
        }
    }

    public static void unArchiveNote(boolean favorite, SingleNote note, DatabaseHandler db) {
        db.deleteNoteFromArchive(note);
        if (favorite) {
            note.set_favorite(1);
        }
        db.addNoteToUserList(0, note);
    }

    public static void restoreNote(SingleNote note, DatabaseHandler db) {
        db.deleteNoteFromRecycleBin(note);
        db.addNoteToUserList(0, note);
    }

}
