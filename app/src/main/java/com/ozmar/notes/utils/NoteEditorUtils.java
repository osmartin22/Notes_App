package com.ozmar.notes.utils;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.MenuItem;

import com.ozmar.notes.R;
import com.ozmar.notes.SingleNote;

public class NoteEditorUtils {

    private NoteEditorUtils() {}

    @NonNull
    public static String differenceFromOriginal(Context context, String title, String content, SingleNote note) {
        String[] noteChanges = context.getResources().getStringArray(R.array.noteChanges);

        if (title.isEmpty() && content.isEmpty() && note == null) {
            return "";

        } else if (note != null) {

            boolean titleTheSame = title.equals(note.get_title());
            boolean contentTheSame = content.equals(note.get_content());

            if (!(titleTheSame && contentTheSame)) {

                if (titleTheSame) {
                    return noteChanges[0];

                } else if (contentTheSame) {
                    return noteChanges[1];

                } else {
                    return noteChanges[2];
                }
            }

            return noteChanges[3];
        } // else if() end

        return noteChanges[4];
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
}