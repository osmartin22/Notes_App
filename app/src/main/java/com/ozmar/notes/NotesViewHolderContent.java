package com.ozmar.notes;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

/**
 * Created by ozmar on 10/1/2017.
 */

public class NotesViewHolderContent extends RecyclerView.ViewHolder{
    public TextView noteContent;

    public NotesViewHolderContent(View itemView) {
        super(itemView);

        this.noteContent = itemView.findViewById(R.id.content);
    }
}
