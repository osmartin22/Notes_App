package com.ozmar.notes.utils;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.ozmar.notes.DatabaseHandler;
import com.ozmar.notes.NotesAdapter;
import com.ozmar.notes.R;
import com.ozmar.notes.SingleNote;

import java.util.List;

public class MainActivityHelper {

    private Context context;
    private DatabaseHandler db;
    private int listUsed;

    public MainActivityHelper(Context context, DatabaseHandler db) {
        this.context = context;
        this.db = db;
        this.listUsed = 0;
    }

    private List<SingleNote> getNotesList() {       // Used to get current list in use
        switch (listUsed) {
            case 0:
            default:
                return db.getUserNotes();
            case 1:
                return db.getFavoriteNotes();
            case 2:
                return db.getArchiveNotes();
            case 3:
                return db.getRecycleBinNotes();
        }
    }

    public List<SingleNote> getNotesList(int list) {        // Used to get specific list
        switch (list) {
            case 0:
            default:
                listUsed = 0;
                return db.getUserNotes();
            case 1:
                listUsed = 1;
                return db.getFavoriteNotes();
            case 2:
                listUsed = 2;
                return db.getArchiveNotes();
            case 3:
                listUsed = 3;
                return db.getRecycleBinNotes();
        }
    }

    public void restoreLayout(RecyclerView rv, MenuItem layoutItem, int layoutChoice) {
        switch (layoutChoice) {
            case 0:
            default:
                layoutItem.setIcon(R.drawable.ic_linear_layout);
                rv.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
                break;
            case 1:
                layoutItem.setIcon(R.drawable.ic_staggered_grid_layout);
                rv.setLayoutManager(new LinearLayoutManager(context.getApplicationContext()));
        }
    }

    public int swapLayout(RecyclerView rv, MenuItem layoutItem, int layoutChoice) {
        switch (layoutChoice) {
            case 0:
            default:
                layoutItem.setIcon(R.drawable.ic_staggered_grid_layout);
                rv.setLayoutManager(new LinearLayoutManager(context.getApplicationContext()));
                return 1;
            case 1:
                layoutItem.setIcon(R.drawable.ic_linear_layout);
                rv.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
                return 0;
        }
    }

    public List<SingleNote> updateAdapter(Intent data, NotesAdapter adapter, List<SingleNote> noteList) {
        // Array used to check NoteEditor outcome
        String[] noteResult = context.getResources().getStringArray(R.array.noteResultArray);

        Bundle bundle = data.getExtras();
        String save = bundle.getString("Note Success", "");
        int position = bundle.getInt("Note Position", -1);
        boolean favorite = bundle.getBoolean("Note Favorite", false);
        SingleNote note = bundle.getParcelable("Note");

        if (save.equals(noteResult[0])) {
            Toast.makeText(context, "No content to save. Note discarded.", Toast.LENGTH_SHORT).show();

        } else if (save.equals(noteResult[1])) {    // Update rv with changes to the note
            noteList.set(position, note);
            adapter.updateAt(position, note);

        } else if (save.equals(noteResult[3])) {    // Update rv with new note
            noteList = getNotesList();

            if (listUsed == 0) {
                adapter.addAt(position, noteList.get(position));
            } else if (listUsed == 1 && favorite) {
                adapter.addAt(position, noteList.get(position));
            }

        } else if (save.equals(noteResult[4])) {    // Remove note from rv
            noteList.remove(position);
            adapter.removeAt(position);

        } else if (save.equals(noteResult[5])) {    // Note title/content not modified but note is no longer a favorite
            if (listUsed == 1) {
                noteList.remove(position);
                adapter.removeAt(position);
            }
        }

        return noteList;
    }

    public int getListUsed() {
        return listUsed;
    }

    public String messageToDisplay(MenuItem item, int size) {
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

    public void setCABMenuItems(Menu menu) {
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

    public void doCABAction(MenuItem choice, List<SingleNote> list) {
        switch (choice.getItemId()) {
            case R.id.contextualArchive:
                doContextualArchive(list);      // Only in all notes / favorites
                break;
            case R.id.contextualUnarchive:
                doContextualUnarchive(list);    // Only in Archive list
                break;
            case R.id.contextualDelete:
                doContextualDelete(list);       // In all lists
                break;
            case R.id.contextualRestore:
                doContextualRestore(list);      // Only in Trash List
                break;
        }
    }

    private void doContextualArchive(List<SingleNote> list) {
        db.deleteListFromUserList(list);
        db.addListToArchive(list);
    }

    private void doContextualUnarchive(List<SingleNote> list) {
        db.deleteListFromArchive(list);
        db.addListToUserList(list);
    }

    private void doContextualDelete(List<SingleNote> list) {
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

    private void doContextualRestore(List<SingleNote> list) {
        db.deleteListFromRecycleBin(list);
        db.addListToUserList(list);
    }
}