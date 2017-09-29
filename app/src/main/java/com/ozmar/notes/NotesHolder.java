package com.ozmar.notes;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

/**
 * Created by ozmar on 9/27/2017.
 */

public class NotesHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    private TextView noteTitle;
    private TextView noteContent;

    private SingleNote note;
    private Context context;

    public NotesHolder(Context context, View itemView) {
        super(itemView);

        this.context = context;
        this.noteTitle = itemView.findViewById(R.id.title);
        this.noteContent = itemView.findViewById(R.id.content);

        itemView.setOnClickListener(this);
    }

    public void bindNote(SingleNote note) {
        this.note = note;
        this.noteTitle.setText(note.get_title());
        this.noteContent.setText(note.get_content());
    }

    @Override
    public void onClick(View view) {

    }

}
