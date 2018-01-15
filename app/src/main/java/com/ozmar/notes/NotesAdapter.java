package com.ozmar.notes;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ozmar.notes.database.NoteAndReminderPreview;
import com.ozmar.notes.database.NotePreview;
import com.ozmar.notes.database.ReminderPreview;
import com.ozmar.notes.utils.FormatUtils;
import com.ozmar.notes.viewHolders.NotesViewHolder;
import com.ozmar.notes.viewHolders.NotesViewHolderContent;
import com.ozmar.notes.viewHolders.NotesViewHolderTitle;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

public class NotesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int SHOW_TITLE = 0, SHOW_CONTENT = 1, SHOW_ALL = 2;

    private final Context context;

    private final List<NoteAndReminderPreview> notes = new ArrayList<>();
    private final List<Integer> selectedPositions = new ArrayList<>();

    public NotesAdapter(Context context) {
        this.context = context;
    }

    public boolean isAdapterEmpty() {
        return notes.isEmpty();
    }

    public void updateAdapterList(@NonNull List<NoteAndReminderPreview> list) {
        notes.clear();
        notes.addAll(list);
        notifyDataSetChanged();
    }

    public int getNoteIdAt(int position) {
        return notes.get(position).getNotePreview().getId();
    }

    public void removeAt(int position) {
        notes.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, notes.size());
    }

    public void addAt(@NonNull NoteAndReminderPreview notePreview, int position) {
        notes.add(position, notePreview);
        notifyItemInserted(position);
        notifyItemRangeChanged(position, notes.size());
    }

    public void updateAt(@NonNull NoteAndReminderPreview notePreview, int position) {
        notes.set(position, notePreview);
        notifyItemChanged(position);
    }


    public void addSelectedPosition(int position) {
        this.selectedPositions.add(position);
    }

    public void removeSelectedPosition(int position) {
        this.selectedPositions.remove(Integer.valueOf(position));
    }

    public void clearSelectedPositions() {
        selectedPositions.clear();
    }

    @NonNull
    public List<Integer> getSelectedPositions() {
        return new ArrayList<>(selectedPositions);
    }

    @NonNull
    public List<NoteAndReminderPreview> getSelectedPreviews() {
        List<NoteAndReminderPreview> list = new ArrayList<>();
        for (Integer position : selectedPositions) {
            list.add(notes.get(position));
        }
        return list;
    }


    public void clearView() {
        int size = notes.size();
        notes.clear();
        notifyItemRangeRemoved(0, size);
    }

    public void removeSelectedPreviews() {
        Collections.sort(selectedPositions);
        int amountOfViewsDisplayed = notes.size();
        int amountOfViewsRemoved = selectedPositions.size();
        int minViewPositionChanged = selectedPositions.get(0);
        int maxViewPositionChanged = selectedPositions.get(selectedPositions.size() - 1);
        int remainingViews = amountOfViewsDisplayed - amountOfViewsRemoved;

        // Deleted entire list
        if (remainingViews == 0) {
            clearView();

            // Deleted notes have views no longer in use
        } else if (remainingViews <= minViewPositionChanged) {
            notes.subList(minViewPositionChanged, notes.size()).clear();
            notifyItemRangeRemoved(minViewPositionChanged, amountOfViewsDisplayed);

            // Deleted notes were consecutive
        } else if (maxViewPositionChanged - minViewPositionChanged == amountOfViewsRemoved - 1) {
            notes.subList(minViewPositionChanged, maxViewPositionChanged + 1).clear();
            notifyItemRangeRemoved(minViewPositionChanged, maxViewPositionChanged + 1);
            notifyItemRangeChanged(maxViewPositionChanged, amountOfViewsDisplayed);

            // Random deletes
        } else {
            for (int i = amountOfViewsRemoved - 1; i >= 0; i--) {
                int pos = selectedPositions.get(i);
                notes.remove(pos);
                notifyItemRemoved(pos);
            }
            notifyItemRangeChanged(minViewPositionChanged, amountOfViewsDisplayed);
        }
    }

    public void addSelectedPreviews(@NonNull List<Integer> position,
                                    @NonNull List<NoteAndReminderPreview> addList) {
        int amountOfViewsAdding = addList.size();
        int minViewPositionChanged = Collections.min(position);
        int maxViewPositionChanged = Collections.max(position);

        // Notes being added are consecutive
        if (maxViewPositionChanged - minViewPositionChanged == amountOfViewsAdding - 1) {
            notes.addAll(minViewPositionChanged, addList);

            notifyItemRangeInserted(minViewPositionChanged, maxViewPositionChanged);
            notifyItemRangeChanged(minViewPositionChanged, notes.size());

        } else {        // Notes added at random
            int sizeOfCurrentDisplayedPreviews = notes.size();

            for (int i = 0; i < amountOfViewsAdding; i++) {
                notes.add(position.get(i), addList.get(i));
                notifyItemInserted(position.get(i));
            }
            notifyItemRangeChanged(minViewPositionChanged, sizeOfCurrentDisplayedPreviews);
        }
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder;
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        switch (viewType) {
            case SHOW_TITLE:
                View viewShowTitle = inflater.inflate(R.layout.note_preview_title, parent, false);
                viewHolder = new NotesViewHolderTitle(viewShowTitle);
                break;

            case SHOW_CONTENT:
                View viewShowContent = inflater.inflate(R.layout.note_preview_content, parent, false);
                viewHolder = new NotesViewHolderContent(viewShowContent);
                break;

            case SHOW_ALL:
            default:
                View viewShowAll = inflater.inflate(R.layout.note_preview, parent, false);
                viewHolder = new NotesViewHolder(viewShowAll);
                break;
        }

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {

        NotePreview notePreview = notes.get(position).getNotePreview();
        ReminderPreview reminderPreview = notes.get(position).getReminderPreview();

        if (selectedPositions.contains(position)) {
            ((CardView) viewHolder.itemView).setCardBackgroundColor(Color.GRAY);
        } else {
            ((CardView) viewHolder.itemView).setCardBackgroundColor(Color.WHITE);
        }

        switch (viewHolder.getItemViewType()) {
            case 0:
                NotesViewHolderTitle viewHolderTitle = (NotesViewHolderTitle) viewHolder;
                viewHolderTitle.noteTitle.setText(notePreview.getTitle());
                displayReminder(reminderPreview, viewHolderTitle.reminderText);
                break;

            case 1:
                NotesViewHolderContent viewHolderContent = (NotesViewHolderContent) viewHolder;
                viewHolderContent.noteContent.setText(notePreview.getContent());
                displayReminder(reminderPreview, viewHolderContent.reminderText);
                break;

            case 2:
            default:
                NotesViewHolder notesViewHolder = (NotesViewHolder) viewHolder;
                notesViewHolder.noteTitle.setText(notePreview.getTitle());
                notesViewHolder.noteContent.setText(notePreview.getContent());
                displayReminder(reminderPreview, notesViewHolder.reminderText);
                break;
        }
    }

    private void displayReminder(@Nullable ReminderPreview reminderPreview,
                                 @NonNull TextView reminderText) {
        if (reminderPreview != null) {
            if (reminderPreview.getIsRepeating() != 0) {
                reminderText.setCompoundDrawablesWithIntrinsicBounds(
                        R.drawable.ic_repeat_dark_gray_small, 0, 0, 0);
            } else {
                reminderText.setCompoundDrawablesWithIntrinsicBounds(
                        R.drawable.ic_reminder_dark_gray_small, 0, 0, 0);
            }

            reminderText.setText(FormatUtils.getReminderText(context,
                    new DateTime(reminderPreview.getNextReminderTime())));
            reminderText.setVisibility(View.VISIBLE);

        } else {
            reminderText.setVisibility(View.GONE);
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        boolean titleTextEmpty = notes.get(position).getNotePreview().getTitle().isEmpty();
        boolean titleContentEmpty = notes.get(position).getNotePreview().getContent().isEmpty();

        if (titleTextEmpty && titleContentEmpty || titleContentEmpty) {
            return SHOW_TITLE;
        } else if (titleTextEmpty) {
            return SHOW_CONTENT;
        } else {
            return SHOW_ALL;
        }
    }

    @Override
    public int getItemCount() {
        return this.notes.size();
    }
}