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
import com.ozmar.notes.database.NotePreviewWithReminderId;
import com.ozmar.notes.database.ReminderPreview;
import com.ozmar.notes.utils.FormatUtils;
import com.ozmar.notes.viewHolders.NotesViewHolder;
import com.ozmar.notes.viewHolders.NotesViewHolderContent;
import com.ozmar.notes.viewHolders.NotesViewHolderTitle;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

public class NotesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final Context context;
    private int listUsed = 0;

//    private final List<SingleNote> oldList;

    private List<NoteAndReminderPreview> notes = new ArrayList<>();

    private final List<Integer> selectedIds = new ArrayList<>();

    private final int showTitle = 0, showContent = 1, showAll = 2;

    // TODO: Remove database from constructor
    // Call function to get data from database from presenter instead
    public NotesAdapter(Context context) {
        this.context = context;

//        oldList = db.getUserNotes();

        convertList();
    }

    // TODO: Temp position of function(move to another place later)
    private void convertList() {
        if (listUsed == 3) {
            ReminderPreview reminderPreview = new ReminderPreview(0, -1);
            List<NotePreview> recycleBinList = AppDatabase.getAppDatabase(context).notesDao().getRecycleBinPreviews();
            for (NotePreview notePreview : recycleBinList) {
                NotePreviewWithReminderId newPreview = new NotePreviewWithReminderId(notePreview);
                notes.add(new NoteAndReminderPreview(newPreview, reminderPreview));
            }

        } else if (listUsed == 0 || listUsed == 1 || listUsed == 2) {
            List<NotePreviewWithReminderId> notePreviewList;
            if (listUsed == 0) {
                notePreviewList = AppDatabase.getAppDatabase(context).notesDao().getMainPreviews();
            } else if (listUsed == 1) {
                notePreviewList = AppDatabase.getAppDatabase(context).notesDao().getFavoritePreviews();
            } else {
                notePreviewList = AppDatabase.getAppDatabase(context).notesDao().getArchivePreviews();
            }

            for (NotePreviewWithReminderId note : notePreviewList) {
                ReminderPreview reminderPreview;

                if (note.getReminderId() != -1) {
                    reminderPreview = AppDatabase.getAppDatabase(context).remindersDao().getReminderPreview(note.getReminderId());
                } else {
                    reminderPreview = new ReminderPreview(0, -1);
                }

                notes.add(new NoteAndReminderPreview(note, reminderPreview));
            }
        }
    }

//    public void getList(List<SingleNote> i) {
//        oldList.addAll(i);
//        notifyDataSetChanged();
//    }

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

    public void addAt(int position, SingleNote note) {
//        oldList.add(position, note);
        notifyItemInserted(position);
        notifyItemRangeChanged(position, notes.size());
    }

    public void updateAt(int position, SingleNote note) {
//        oldList.set(position, note);
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

    private void displayReminder(ReminderPreview reminderPreview, TextView reminderText) {
        if (reminderPreview.getNextReminderTime() != 0) {
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
}