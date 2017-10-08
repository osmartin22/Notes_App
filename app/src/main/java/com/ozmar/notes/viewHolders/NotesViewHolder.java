package com.ozmar.notes.viewHolders;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.ozmar.notes.R;

/**
 * Created by ozmar on 9/27/2017.
 */

public class NotesViewHolder extends RecyclerView.ViewHolder {
    public TextView noteTitle;
    public TextView noteContent;

    public NotesViewHolder(View itemView) {
        super(itemView);

        this.noteTitle = itemView.findViewById(R.id.title);
        this.noteContent = itemView.findViewById(R.id.content);
    }
}
