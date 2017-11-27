package com.ozmar.notes.database;

import android.arch.persistence.room.ColumnInfo;
import android.support.annotation.IntRange;


public class ReminderPreview {

    @ColumnInfo(name = "reminderTime")
    private long nextReminderTime;

    @ColumnInfo(name = "repeatType")
    @IntRange(from = -1, to = 4)
    private int isRepeating;


    public ReminderPreview(long nextReminderTime, int isRepeating) {
        this.nextReminderTime = nextReminderTime;
        this.isRepeating = isRepeating;
    }


    public long getNextReminderTime() {
        return nextReminderTime;
    }

    public void setNextReminderTime(long nextReminderTime) {
        this.nextReminderTime = nextReminderTime;
    }

    public int getIsRepeating() {
        return isRepeating;
    }

    public void setIsRepeating(int isRepeating) {
        this.isRepeating = isRepeating;
    }
}
