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


    @Override
    public ReminderPreview clone() throws CloneNotSupportedException {
        return (ReminderPreview) super.clone();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (!(obj instanceof ReminderPreview)) {
            return false;
        }

        ReminderPreview reminderPreview = (ReminderPreview) obj;

        return reminderPreview.nextReminderTime == nextReminderTime &&
                reminderPreview.isRepeating == isRepeating;
    }

    @Override
    public int hashCode() {
        int prime = 31;
        int result = 17;
        result = (int) (prime * result + nextReminderTime);
        result = prime * result + isRepeating;
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.getClass().getSimpleName()).append("[ ");
        sb.append("Next reminder time: ").append(nextReminderTime);
        sb.append(",    Reminder is repeating: ").append(isRepeating);
        sb.append(" ]");

        return sb.toString();
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
