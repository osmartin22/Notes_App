package com.ozmar.notes.database;

import javax.annotation.Nonnull;

public class NoteAndReminderPreview {

    @Nonnull
    private NotePreviewWithReminderId mNotePreview;

    @Nonnull
    private ReminderPreview mReminderPreview;

    public NoteAndReminderPreview(@Nonnull NotePreviewWithReminderId notePreview,
                                  @Nonnull ReminderPreview reminderPreview) {
        this.mNotePreview = notePreview;
        this.mReminderPreview = reminderPreview;
    }

    public NoteAndReminderPreview(@Nonnull NotePreviewWithReminderId notePreview) {
        this.mNotePreview = notePreview;
        this.mReminderPreview = new ReminderPreview(0, -1);
    }


    @Nonnull
    public NotePreview getNotePreview() {
        return mNotePreview;
    }

    public void setNotePreview(@Nonnull NotePreviewWithReminderId notePreview) {
        this.mNotePreview = notePreview;
    }

    @Nonnull
    public ReminderPreview getReminderPreview() {
        return mReminderPreview;
    }

    public void setReminderPreview(@Nonnull ReminderPreview reminderPreview) {
        this.mReminderPreview = reminderPreview;
    }
}
