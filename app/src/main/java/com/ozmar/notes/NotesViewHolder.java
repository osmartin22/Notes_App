package com.ozmar.notes;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

/**
 * Created by ozmar on 9/27/2017.
 */

public class NotesViewHolder extends RecyclerView.ViewHolder {
    public TextView noteTitle;
    public TextView noteContent;
    public boolean multiSelect;

    public int position = -1;

    public NotesViewHolder(View itemView) {
        super(itemView);

        this.noteTitle = itemView.findViewById(R.id.title);
        this.noteContent = itemView.findViewById(R.id.content);
    }
}
