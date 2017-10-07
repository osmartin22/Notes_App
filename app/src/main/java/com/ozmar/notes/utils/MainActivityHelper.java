package com.ozmar.notes.utils;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.MenuItem;

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

    public List<SingleNote> updateAdapter(Bundle bundle, NotesAdapter adapter, List<SingleNote> noteList) {
        // Array used to check NoteEditor outcome
        String[] noteResult = context.getResources().getStringArray(R.array.noteResultArray);

        String save = bundle.getString("Note Success", "");
        int position = bundle.getInt("Note Position", -1);
        boolean favorite = bundle.getBoolean("Note Favorite", false);
        SingleNote note = bundle.getParcelable("Note");

        if (save.equals(noteResult[1])) {    // Update rv with changes to the note
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
}