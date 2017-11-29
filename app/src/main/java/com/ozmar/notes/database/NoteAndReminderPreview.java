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
