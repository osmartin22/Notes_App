package com.ozmar.notes;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ozmar.notes.database.AppDatabase;
import com.ozmar.notes.database.NoteAndReminderPreview;
import com.ozmar.notes.database.NotePreview;
import com.ozmar.notes.database.ReminderPreview;
import com.ozmar.notes.utils.FormatUtils;
import com.ozmar.notes.viewHolders.NotesViewHolder;
import com.ozmar.notes.viewHolders.NotesViewHolderContent;
import com.ozmar.notes.viewHolders.NotesViewHolderTitle;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import io.reactivex.Maybe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class NotesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int SHOW_TITLE = 0, SHOW_CONTENT = 1, SHOW_ALL = 2;

    private final Context context;
    private int listUsed = 0;

    private final List<NoteAndReminderPreview> notes = new ArrayList<>();
    private final List<Integer> selectedIds = new ArrayList<>();

    public NotesAdapter(Context context) {
        this.context = context;
    }


    private void getPreviewListResult(List<NoteAndReminderPreview> list) {
        notes.addAll(list);
        notifyDataSetChanged();
    }

    public void updateAdapterList() {
        Maybe.fromCallable(() -> AppDatabase.getAppDatabase().previewsDao().getListOfNotePreviews(listUsed))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::getPreviewListResult);
    }

    public void setListUsed(int listUsed) {
        this.listUsed = listUsed;
    }

    public int getListUsed() {
        return listUsed;
    }

    public int getNoteIdAt(int position) {
        return notes.get(position).getNotePreview().getId();
    }

    public void removeAt(int position) {
        notes.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, notes.size());
    }


    public void addAt(int position, NoteAndReminderPreview notePreview) {
        notes.add(position, notePreview);
        notifyItemInserted(position);
        notifyItemRangeChanged(position, notes.size());
    }

    public void updateAt(int position, NoteAndReminderPreview notePreview) {
        notes.set(position, notePreview);
        notifyItemChanged(position);
    }

    public void clearView() {
        int size = notes.size();
        notes.clear();
        notifyItemRangeRemoved(0, size);
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

        if (selectedIds.contains(position)) {
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

    private void displayReminder(@Nullable ReminderPreview reminderPreview, TextView reminderText) {
        if (reminderPreview != null) {
            if (reminderPreview.getIsRepeating() != -1) {
                reminderText.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_repeat_dark_gray_small,
                        0, 0, 0);
            } else {
                reminderText.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_reminder_dark_gray_small,
                        0, 0, 0);
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