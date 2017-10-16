package com.ozmar.notes.utils;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.MenuItem;

import com.ozmar.notes.NotesAdapter;
import com.ozmar.notes.R;
import com.ozmar.notes.SingleNote;

public class MainActivityUtils {

    private MainActivityUtils() {

    }

    public static void restoreLayout(Context context, RecyclerView rv, MenuItem layoutItem, int layoutChoice) {
        switch (layoutChoice) {
            case 0:
            default:
                layoutItem.setIcon(R.drawable.ic_linear_layout);
                rv.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
                break;
            case 1:
                layoutItem.setIcon(R.drawable.ic_staggered_grid_layout);
                rv.setLayoutManager(new LinearLayoutManager(context));
        }
    }

    public static int swapLayout(Context context, RecyclerView rv, MenuItem layoutItem, int layoutChoice) {
        switch (layoutChoice) {
            case 0:
            default:
                layoutItem.setIcon(R.drawable.ic_staggered_grid_layout);
                rv.setLayoutManager(new LinearLayoutManager(context));
                return 1;
            case 1:
                layoutItem.setIcon(R.drawable.ic_linear_layout);
                rv.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
                return 0;
        }
    }

    public static void updateAdapter(Context context, Bundle bundle, NotesAdapter adapter) {
        // Array used to check NoteEditor outcome
        String[] noteResult = context.getResources().getStringArray(R.array.noteResultArray);

        String save = bundle.getString("Note Success", "");
        int position = bundle.getInt("Note Position", -1);
        boolean favorite = bundle.getBoolean("Note Favorite", false);
        SingleNote note = bundle.getParcelable("Note");
        int listUsed = adapter.getListUsed();

        if (save.equals(noteResult[0])) {    // Update rv with noteChanges to the note
            adapter.updateAt(position, note);

            // TODO: Optimize this, right now getting the entire list just for the newest note
        } else if (save.equals(noteResult[1])) {    // Update rv with new note
            if (listUsed == 0) {
                adapter.addAt(position, note);
            } else if (listUsed == 1 && favorite) {
                adapter.addAt(position, note);
            }

        } else if (save.equals(noteResult[2])) {    // Remove note from rv (Delete Forever)
            adapter.removeAt(position);

        } else if (save.equals(noteResult[3])) {    // Title/Content not modified but note is no longer a favorite
            if (listUsed == 1) {
                adapter.removeAt(position);
            }
        }
    }
}