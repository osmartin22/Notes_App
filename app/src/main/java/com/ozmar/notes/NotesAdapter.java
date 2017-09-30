package com.ozmar.notes;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.Collections;
import java.util.List;

public class NotesAdapter extends RecyclerView.Adapter<NotesHolder> {

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

        Log.d("Multi", "Min -> " + minViewPositionChanged);
        Log.d("Multi", "Max -> " + maxViewPositionChanged);

        // Deleted entire list
        if (remainingViews == 0) {
            Log.d("Multi", "Entire list removed");
            clearView();

            // Deleted notes have views no longer in use
        } else if (remainingViews <= minViewPositionChanged) {
            Log.d("Multi", "End notes removed");
            notes.subList(minViewPositionChanged, notes.size()).clear();
            notifyItemRangeRemoved(minViewPositionChanged, amountOfViews);

            // Deleted notes were consecutive
        } else if (maxViewPositionChanged - minViewPositionChanged == amountOfViewsDeleted - 1) {
            Log.d("Multi", "Consecutive notes removed");
            notes.subList(minViewPositionChanged, maxViewPositionChanged + 1).clear();
            notifyItemRangeRemoved(minViewPositionChanged, maxViewPositionChanged + 1);
            notifyItemRangeChanged(maxViewPositionChanged, amountOfViews);

            // Random deletes
        } else {
            Log.d("Multi", "Random notes removed");
            for (int i = amountOfViewsDeleted - 1; i >= 0; i--) {
                int pos = position.get(i);
                Log.d("Multi", "Remove Position " + pos);
                notes.remove(pos);
                notifyItemRemoved(pos);
            }
            notifyItemRangeChanged(minViewPositionChanged, amountOfViews);
        }
    }

    public void getList(List<SingleNote> newList) {
        clearView();
        notes.addAll(newList);
        notifyItemRangeInserted(0, notes.size());
    }

    @Override
    public void onBindViewHolder(NotesHolder holder, int position) {
        SingleNote note = this.notes.get(position);
        holder.bindNote(note);
    }

    @Override
    public NotesHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(itemResource, parent, false);
        return new NotesHolder(this.context, view);
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
