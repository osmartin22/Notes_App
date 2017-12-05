package com.ozmar.notes;


import android.support.annotation.Nullable;

import com.ozmar.notes.database.MainNote;

import javax.annotation.Nonnull;

public class NoteWithReminder {

    @Nullable
    private MainNote mNote;

    @Nullable
    private Reminder mReminder;

    public NoteWithReminder() {

    }

    @Nullable
    public MainNote getNote() {
        return mNote;
    }

    public void setNote(@Nonnull MainNote note) {
        mNote = note;
    }

    @Nullable
    public Reminder getReminder() {
        return mReminder;
    }

    public void setReminder(@Nullable Reminder reminder) {
        mReminder = reminder;
    }
}
