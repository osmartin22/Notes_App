package com.ozmar.notes;

import android.os.Parcel;
import android.os.Parcelable;


public class SingleNote implements Parcelable {

    private int id;
    private String title;
    private String content;
    private boolean favorite;

    private long timeCreated;
    private long timeModified;
    private int reminderId = -1;

    private long nextReminderTime = 0;
    private boolean hasFrequencyChoices = false;

    public SingleNote() {

    }

    // New note does not have a reminder
    public SingleNote(String title, String content, boolean favorite, long timeCreated) {
        this(title, content, favorite, timeCreated, 0, -1);
    }

    // New note has a reminder
    public SingleNote(String title, String content, boolean favorite, long timeCreated, long nextReminderTime, int reminderId) {
        this.title = title;
        this.content = content;
        this.favorite = favorite;
        this.timeCreated = timeCreated;
        this.timeModified = timeCreated;
        this.nextReminderTime = nextReminderTime;
        this.reminderId = reminderId;
    }

    @Override
    public int hashCode() {
        int result = 22;
        result = 31 * result + id;
        result = 31 * result + title.hashCode();
        result = 31 * result + content.hashCode();
        if (favorite) {
            result = 31 * result + 1;
        }
        result = 31 * result + Long.valueOf(timeCreated).hashCode();
        result = 31 * result + Long.valueOf(timeModified).hashCode();
        result = 31 * result + Long.valueOf(nextReminderTime).hashCode();
        result = 31 * result + reminderId;
        if (hasFrequencyChoices) {
            result = 31 * result + 1;
        }
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (!(obj instanceof SingleNote)) {
            return false;
        }

        SingleNote note = (SingleNote) obj;

        return note.content.equals(content) &&
                note.title.equals(title) &&
                note.favorite == favorite &&
                note.timeCreated == timeCreated &&
                note.timeModified == timeModified &&
                note.nextReminderTime == nextReminderTime &&
                note.reminderId == reminderId &&
                note.hasFrequencyChoices == hasFrequencyChoices;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "[ " +
                "Id: " + id +
                ",    Title: " + title +
                ",    Content: " + content +
                ",    Favorite: " + favorite +
                ",    TimeCreated: " + timeCreated +
                ",    TimeModified: " + timeModified +
                ",    ReminderTime: " + nextReminderTime +
                ",    ReminderId: " + reminderId +
                ",    HasFrequencyChoice: " + hasFrequencyChoices +
                " ]";
    }

    public SingleNote(Parcel in) {
        this.id = in.readInt();
        this.title = in.readString();
        this.content = in.readString();
        this.favorite = (in.readInt() == 1);
        this.timeCreated = in.readLong();
        this.timeModified = in.readLong();
        this.nextReminderTime = in.readLong();
        this.reminderId = in.readInt();
        this.hasFrequencyChoices = (in.readInt() == 1);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(title);
        dest.writeString(content);

        if (favorite) {
            dest.writeInt(1);
        } else {
            dest.writeInt(0);
        }

        dest.writeLong(timeCreated);
        dest.writeLong(timeModified);
        dest.writeLong(nextReminderTime);
        dest.writeInt(reminderId);

        if (hasFrequencyChoices) {
            dest.writeInt(1);
        } else {
            dest.writeInt(0);
        }
    }

    public static final Parcelable.Creator<SingleNote> CREATOR = new Parcelable.Creator<SingleNote>() {
        @Override
        public SingleNote createFromParcel(Parcel in) {
            return new SingleNote(in);
        }

        @Override
        public SingleNote[] newArray(int size) {
            return new SingleNote[size];
        }
    };

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean isFavorite() {
        return favorite;
    }

    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
    }

    public long getTimeCreated() {
        return timeCreated;
    }

    public void setTimeCreated(long timeCreated) {
        this.timeCreated = timeCreated;
    }

    public long getTimeModified() {
        return timeModified;
    }

    public void setTimeModified(long timeModified) {
        this.timeModified = timeModified;
    }

    public long getNextReminderTime() {
        return nextReminderTime;
    }

    public void setNextReminderTime(long nextReminderTime) {
        this.nextReminderTime = nextReminderTime;
    }

    public int getReminderId() {
        return reminderId;
    }

    public void setReminderId(int reminderId) {
        this.reminderId = reminderId;
    }

    public boolean hasFrequencyChoices() {
        return hasFrequencyChoices;
    }

    public void setHasFrequencyChoices(boolean hasFrequencyChoices) {
        this.hasFrequencyChoices = hasFrequencyChoices;
    }
}