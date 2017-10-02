package com.ozmar.notes;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

/**
 * Created by ozmar on 10/1/2017.
 */

public class NotesViewHolderTitle  extends RecyclerView.ViewHolder{
    public TextView noteTitle;

    public NotesViewHolderTitle(View itemView) {
        super(itemView);

        this.noteTitle = itemView.findViewById(R.id.title);
    }
}
