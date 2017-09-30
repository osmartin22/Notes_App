package com.ozmar.notes;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.Collections;
import java.util.List;

public class NotesAdapter extends RecyclerView.Adapter<NotesViewHolder> {

    private Context context;
    private final List<SingleNote> notes;
    private int itemResource;


    public NotesAdapter(Context context, int itemResource, List<SingleNote> notes) {
        this.notes = notes;
        this.context = context;
        this.itemResource = itemResource;
    }

    public void removeAt(int position) {
        notes.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, notes.size());
    }

    public void addAt(int position, SingleNote note) {
        notes.add(position, note);
        notifyItemInserted(position);
        notifyItemRangeChanged(position, notes.size());
    }

    public void updateAt(int position, SingleNote note) {
        notes.remove(position);
        notes.add(position, note);
        notifyItemChanged(position);
    }

    public void clearView() {
        int size = notes.size();
        notes.clear();
        notifyItemRangeRemoved(0, size);
    }

    // Optimize removal of views
    public void removeSelectedViews(List<View> views, List<Integer> position) {
        Collections.sort(position);
        int amountOfViews = notes.size();
        int amountOfViewsDeleted = views.size();
        int minViewPositionChanged = position.get(0);
        int maxViewPositionChanged = position.get(position.size() - 1);
        int remainingViews = amountOfViews - amountOfViewsDeleted;

        // Deleted entire list
        if (remainingViews == 0) {
            clearView();

            // Deleted notes have views no longer in use
        } else if (remainingViews <= minViewPositionChanged) {
            notes.subList(minViewPositionChanged, notes.size()).clear();
            notifyItemRangeRemoved(minViewPositionChanged, amountOfViews);

            // Deleted notes were consecutive
        } else if (maxViewPositionChanged - minViewPositionChanged == amountOfViewsDeleted - 1) {
            notes.subList(minViewPositionChanged, maxViewPositionChanged + 1).clear();
            notifyItemRangeRemoved(minViewPositionChanged, maxViewPositionChanged + 1);
            notifyItemRangeChanged(maxViewPositionChanged, amountOfViews);

            // Random deletes
        } else {
            for (int i = amountOfViewsDeleted - 1; i >= 0; i--) {
                int pos = position.get(i);
                notes.remove(pos);
                notifyItemRemoved(pos);
            }
            notifyItemRangeChanged(minViewPositionChanged, amountOfViews);
        }
    }

    // Set views that were changed to gray back to white
    public void setToWhite(List<View> views) {
        for (int i = 0; i < views.size(); i++) {
            views.get(i).setBackgroundColor(Color.WHITE);
        }
    }

    public void getList(List<SingleNote> newList) {
        clearView();
        notes.addAll(newList);
        notifyItemRangeInserted(0, notes.size());
    }

    @Override
    public void onBindViewHolder(NotesViewHolder holder, int position) {
        Log.d("Multi", "Bind at position " + position);
        SingleNote note = this.notes.get(position);
        holder.noteTitle.setText(note.get_title());
        holder.noteContent.setText(note.get_content());
    }

    @Override
    public NotesViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Log.d("Multi", "Create at position " + viewType);
        View view = LayoutInflater.from(parent.getContext()).inflate(itemResource, parent, false);
        return new NotesViewHolder(view);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return this.notes.size();
    }
} // NotesAdapter end
