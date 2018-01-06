package com.ozmar.notes.database;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class NoteAndReminderPreview {

    @Nonnull
    private NotePreviewWithReminderId mNotePreview;

    @Nullable
    private ReminderPreview mReminderPreview;

    public NoteAndReminderPreview(@Nonnull NotePreviewWithReminderId notePreview,
                                  @Nullable ReminderPreview reminderPreview) {
        this.mNotePreview = notePreview;
        this.mReminderPreview = reminderPreview;
    }


    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (!(obj instanceof NoteAndReminderPreview)) {
            return false;
        }

        NoteAndReminderPreview reminderPreview = (NoteAndReminderPreview) obj;

        boolean reminderPreviewTheSame = false;
        if (reminderPreview.mReminderPreview != null && mReminderPreview != null) {
            reminderPreviewTheSame = reminderPreview.mReminderPreview.equals(mReminderPreview);

        } else if(reminderPreview.mReminderPreview == null && mReminderPreview == null){
            reminderPreviewTheSame = true;
        }

        return reminderPreview.mNotePreview.equals(mNotePreview) &&
                reminderPreviewTheSame;
    }

    @Override
    public int hashCode() {
        int prime = 31;
        int result = 17;
        result = prime * result + mNotePreview.hashCode();

        if (mReminderPreview != null) {
            result = prime * result + mReminderPreview.hashCode();
        } else {
            result = prime * result;
        }

        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.getClass().getSimpleName()).append("[ ");
        sb.append("NotePreview: ").append(mNotePreview);
        sb.append(",    ReminderPreview: ").append(mReminderPreview);
        sb.append(" ]");

        return sb.toString();
    }


    @Nonnull
    public NotePreview getNotePreview() {
        return mNotePreview;
    }

    public void setNotePreview(@Nonnull NotePreviewWithReminderId notePreview) {
        this.mNotePreview = notePreview;
    }

    @Nullable
    public ReminderPreview getReminderPreview() {
        return mReminderPreview;
    }

    public void setReminderPreview(@Nullable ReminderPreview reminderPreview) {
        this.mReminderPreview = reminderPreview;
    }
}
