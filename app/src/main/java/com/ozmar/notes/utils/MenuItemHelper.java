package com.ozmar.notes.utils;

import android.content.Context;
import android.view.Menu;
import android.view.MenuItem;

import com.ozmar.notes.DatabaseHandler;
import com.ozmar.notes.R;
import com.ozmar.notes.SingleNote;

import java.util.List;

/**
 * Created by ozmar on 10/6/2017.
 */

public class MenuItemHelper {

    private Context context;
    private DatabaseHandler db;

    public MenuItemHelper(Context context, DatabaseHandler db) {
        this.context = context;
        this.db = db;
    }

    public String multiSelectMessage(MenuItem item, int size) {
        switch (item.getItemId()) {
            case R.id.contextualArchive:
                return (size == 1) ? context.getString(R.string.snackBarArchiveSingle) : context.getString(R.string.snackBarArchiveMultiple);
            case R.id.contextualUnarchive:
                return (size == 1) ? context.getString(R.string.snackBarUnarchiveSingle) : context.getString(R.string.snackBarUnarchiveMultiple);
            case R.id.contextualDelete:
                return (size == 1) ? context.getString(R.string.snackBarDeleteSingle) : context.getString(R.string.snackBarDeleteMultiple);
            case R.id.contextualRestore:
                return (size == 1) ? context.getString(R.string.snackBarRestoreSingle) : context.getString(R.string.snackBarRestoreMultiple);
            case R.id.contextualDeleteForever:
                return (size == 1) ? context.getString(R.string.deleteForeverSingle) : context.getString(R.string.deleteForeverMultiple);
        }
        return null;
    }

    public String noteEditorMessage(int num) {
        switch (num) {
            case 0:     // Archive
                return context.getString(R.string.snackBarArchiveSingle);
            case 1:     // Unarchive
                return context.getString(R.string.snackBarUnarchiveSingle);
            case 2:     // Delete
                return context.getString(R.string.snackBarDeleteSingle);
            case 3:     // Restore
                return context.getString(R.string.snackBarRestoreSingle);
        }
        return  null;
    }

    public void doEditorAction(int action, List<SingleNote> list, int listUsed) {
        switch (action) {
            case 0:         // Archive
                doArchive(list);
                break;
            case 1:         // Unarchive
                doUnarchive(list);
                break;
            case 2:         // Delete
                doDelete(list, listUsed);
                break;
            case 3:         // Restore
                doRestore(list);
                break;
        }
    }

    public void setCABMenuItems(Menu menu, int listUsed) {
        switch (listUsed) {
            case 0:
            case 1:
            default:
                break;
            case 2:
                menu.findItem(R.id.contextualArchive).setVisible(false);
                menu.findItem(R.id.contextualUnarchive).setVisible(true);
                break;
            case 3:
                menu.findItem(R.id.contextualArchive).setVisible(false);
                menu.findItem(R.id.contextualDelete).setVisible(false);
                menu.findItem(R.id.contextualRestore).setVisible(true);
                menu.findItem(R.id.contextualDeleteForever).setVisible(true);
        }
    }

    public void doCABAction(MenuItem choice, List<SingleNote> list, int listUsed) {
        switch (choice.getItemId()) {
            case R.id.contextualArchive:
                doArchive(list);      // Only in all notes / favorites
                break;
            case R.id.contextualUnarchive:
                doUnarchive(list);    // Only in Archive list
                break;
            case R.id.contextualDelete:
                doDelete(list, listUsed);       // In all lists
                break;
            case R.id.contextualRestore:
                doRestore(list);      // Only in Trash List
                break;
        }
    }

    private void doArchive(List<SingleNote> list) {
        db.deleteListFromUserList(list);
        db.addListToArchive(list);
    }

    private void doUnarchive(List<SingleNote> list) {
        db.deleteListFromArchive(list);
        db.addListToUserList(list);
    }

    private void doDelete(List<SingleNote> list, int listUsed) {
        switch (listUsed) {
            case 0:
            case 1:
                db.deleteListFromUserList(list);
                break;

            case 2:
                db.deleteListFromArchive(list);
                break;
        }

        db.addListToRecycleBin(list);
    }

    private void doRestore(List<SingleNote> list) {
        db.deleteListFromRecycleBin(list);
        db.addListToUserList(list);
    }
}
