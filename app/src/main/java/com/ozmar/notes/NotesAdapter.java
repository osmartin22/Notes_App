package com.ozmar.notes;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

public class NotesAdapter extends RecyclerView.Adapter<NotesHolder> {

    private Context context;
    private final List<SingleNote> notes;
    private int itemResource;


    public NotesAdapter(Context context, int itemResource, List<SingleNote> notes) {
        this.notes = notes;
        this.context = context;
        this.itemResource = itemResource;
        //Log.d("Notes Size:", Integer.toString(notes.size()));
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

    public void updaateAt(int position) {
        notes.remove(position);
        notifyItemChanged(position);
    }

    public void clearView() {
        int size = notes.size();
        notes.clear();
        notifyItemRangeRemoved(0, size);
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
