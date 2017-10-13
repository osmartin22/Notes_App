package com.ozmar.notes.utils;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.Menu;
import android.view.MenuItem;

import com.ozmar.notes.R;
import com.ozmar.notes.SingleNote;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

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

    public static String lastUpdated(Context context, long timeLastUpdated) {

        Date current = new Date(System.currentTimeMillis());
        Date lastUpdated = new Date(timeLastUpdated);
        SimpleDateFormat sameYearFormat = new SimpleDateFormat("yyyy", Locale.getDefault());
        SimpleDateFormat sameDayFormat = new SimpleDateFormat("MMdd", Locale.getDefault());

        boolean sameYear = sameYearFormat.format(current).equals(sameYearFormat.format(lastUpdated));
        boolean sameDay = sameDayFormat.format(current).equals(sameDayFormat.format(lastUpdated));
        boolean yesterday = false;

        String timeModified = "Last Updated \n";

        // TODO: Find a better method so that 2:30 am today and 2:31 am tomorrow still shows as yesterday
        long timeDifference = current.getTime() - lastUpdated.getTime();
        if (timeDifference <= TimeUnit.DAYS.toMillis(1)) {
            yesterday = true;
        }

        if (sameYear) {
            if (sameDay) {
                DateFormat timeFormat = android.text.format.DateFormat.getTimeFormat(context);
                timeModified += "Today, " + timeFormat.format(lastUpdated);
            } else if (yesterday) {
                DateFormat timeFormat = android.text.format.DateFormat.getTimeFormat(context);
                timeModified += "Yesterday, " + timeFormat.format(lastUpdated);

            } else {
                SimpleDateFormat df = new SimpleDateFormat("MMM  dd", Locale.getDefault());
                timeModified += df.format(lastUpdated);
            }
        } else {
            SimpleDateFormat df = new SimpleDateFormat("MMM  dd, yyy", Locale.getDefault());
            timeModified += df.format(lastUpdated);
        }

        return timeModified;
    }

    public static String getReminderText(Context contex, Calendar c) {
        String reminderTime = "";

        // Same Year
            // Today month, date, time
            // Yesterday month date time
            // else
            // month date time

        // different year && not yesterday
            // month date year time

        return reminderTime;
    }
}