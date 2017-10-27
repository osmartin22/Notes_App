package com.ozmar.notes.viewHolders;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.ozmar.notes.R;


public class NotesViewHolderTitle  extends RecyclerView.ViewHolder{
    public TextView noteTitle;
    public TextView reminderText;

    public NotesViewHolderTitle(View itemView) {
        super(itemView);
        this.noteTitle = itemView.findViewById(R.id.title);
        this.reminderText = itemView.findViewById(R.id.reminderText);
    }
}
