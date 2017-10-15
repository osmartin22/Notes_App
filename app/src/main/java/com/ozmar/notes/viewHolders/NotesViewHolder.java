package com.ozmar.notes.viewHolders;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.ozmar.notes.R;


public class NotesViewHolder extends RecyclerView.ViewHolder {
    public TextView noteTitle;
    public TextView noteContent;
    public TextView reminderText;
    public View reminderView;

    public NotesViewHolder(View itemView) {
        super(itemView);
        this.noteTitle = itemView.findViewById(R.id.title);
        this.noteContent = itemView.findViewById(R.id.content);
        this.reminderText = itemView.findViewById(R.id.reminderText);
        this.reminderView = itemView.findViewById(R.id.reminder_layout);
    }
}
