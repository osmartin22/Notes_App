package com.ozmar.notes;

import android.graphics.Color;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ozmar.notes.viewHolders.NotesViewHolder;
import com.ozmar.notes.viewHolders.NotesViewHolderContent;
import com.ozmar.notes.viewHolders.NotesViewHolderTitle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NotesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

//    private DatabaseHandler db;
    private int listUsed = 0;

    private final List<SingleNote> notes;
    private final List<Integer> selectedIds = new ArrayList<>();

    private final List<SingleNote> tempNotes = new ArrayList<>();

    private final int showTitle = 0, showContent = 1, showAll = 2;

    // TODO: Use AsyncTask to get notes at the start
    public NotesAdapter(DatabaseHandler db) {
//        this.db = db;
        notes = db.getUserNotes();
    }

    public void getList(List<SingleNote> i) {
        notes.addAll(i);
        notifyDataSetChanged();
    }

    public void setListUsed(int listUsed) {
        this.listUsed = listUsed;
    }

    public int getListUsed() {
        return listUsed;
    }

    public SingleNote getNoteAt(int position) {
        return notes.get(position);
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
        notes.set(position, note);
        notifyItemChanged(position);
    }

    public void clearView() {
        int size = notes.size();
        notes.clear();
        notifyItemRangeRemoved(0, size);
    }

    public void removeSelectedViews(List<Integer> position) {
        Collections.sort(position);
        int amountOfViews = notes.size();
        int amountOfViewsRemoved = position.size();
        int minViewPositionChanged = position.get(0);
        int maxViewPositionChanged = position.get(position.size() - 1);
        int remainingViews = amountOfViews - amountOfViewsRemoved;

        // Deleted entire list
        if (remainingViews == 0) {
            clearView();

            // Deleted notes have views no longer in use
        } else if (remainingViews <= minViewPositionChanged) {
            notes.subList(minViewPositionChanged, notes.size()).clear();
            notifyItemRangeRemoved(minViewPositionChanged, amountOfViews);

            // Deleted notes were consecutive
        } else if (maxViewPositionChanged - minViewPositionChanged == amountOfViewsRemoved - 1) {
            notes.subList(minViewPositionChanged, maxViewPositionChanged + 1).clear();
            notifyItemRangeRemoved(minViewPositionChanged, maxViewPositionChanged + 1);
            notifyItemRangeChanged(maxViewPositionChanged, amountOfViews);

            // Random deletes
        } else {
            tempNotes.addAll(notes);
            for (int i = amountOfViewsRemoved - 1; i >= 0; i--) {
                int pos = position.get(i);
                notes.remove(pos);
                notifyItemRemoved(pos);
            }
            notifyItemRangeChanged(minViewPositionChanged, amountOfViews);
        }
    }

    public void addSelectedViews(List<Integer> position, List<SingleNote> addList) {
        Collections.sort(position);
        int size = notes.size();
        int amountOfViewAdding = position.size();
        int minViewPositionChanged = position.get(0);
        int maxViewPositionChanged = position.get(position.size() - 1);

        // Notes being added are consecutive
        if (maxViewPositionChanged - minViewPositionChanged == amountOfViewAdding - 1) {
            if (minViewPositionChanged != 0) {
                notes.addAll(minViewPositionChanged, addList);
            } else {
                notes.addAll(0, addList);
            }

            notifyItemRangeInserted(minViewPositionChanged, maxViewPositionChanged);
            notifyItemRangeChanged(minViewPositionChanged, notes.size());

        } else {        // Notes added at random
            notes.clear();
            notes.addAll(tempNotes);
            notifyItemRangeChanged(0, size);
        }
        tempNotes.clear();
    }

    public void clearTempNotes() {
        this.tempNotes.clear();
    }

    public void addSelectedId(int position) {
        this.selectedIds.add(position);
    }

    public void removeSelectedId(int position) {
        this.selectedIds.remove(Integer.valueOf(position));
    }

    public void clearSelectedIds() {
        selectedIds.clear();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder;
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        switch (viewType) {
            case showTitle:
                View viewShowTitle = inflater.inflate(R.layout.note_preview_title, parent, false);
                viewHolder = new NotesViewHolderTitle(viewShowTitle);
                break;

            case showContent:
                View viewShowContent = inflater.inflate(R.layout.note_preview_content, parent, false);
                viewHolder = new NotesViewHolderContent(viewShowContent);
                break;

            case showAll:
            default:
                View viewShowAll = inflater.inflate(R.layout.note_preview, parent, false);
                viewHolder = new NotesViewHolder(viewShowAll);
                break;
        }

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        SingleNote note = this.notes.get(position);

        if (selectedIds.contains(position)) {
            ((CardView) viewHolder.itemView).setCardBackgroundColor(Color.GRAY);
        } else {
            ((CardView) viewHolder.itemView).setCardBackgroundColor(Color.WHITE);
        }

        switch (viewHolder.getItemViewType()) {
            case 0:
                NotesViewHolderTitle viewHolderTitle = (NotesViewHolderTitle) viewHolder;
                viewHolderTitle.noteTitle.setText(note.get_title());
                break;

            case 1:
                NotesViewHolderContent viewHolderContent = (NotesViewHolderContent) viewHolder;
                viewHolderContent.noteContent.setText(note.get_content());
                break;

            case 2:
            default:
                NotesViewHolder notesViewHolder = (NotesViewHolder) viewHolder;
                notesViewHolder.noteTitle.setText(note.get_title());
                notesViewHolder.noteContent.setText(note.get_content());
                break;
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        boolean titleTextEmpty = notes.get(position).get_title().isEmpty();
        boolean titleContentEmpty = notes.get(position).get_content().isEmpty();

        if (titleTextEmpty && titleContentEmpty || titleContentEmpty) {
            return showTitle;
        } else if (titleTextEmpty) {
            return showContent;
        } else {
            return showAll;
        }
    }

    @Override
    public int getItemCount() {
        return this.notes.size();
    }
} // NotesAdapter end