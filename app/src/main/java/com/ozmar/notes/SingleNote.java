package com.ozmar.notes;

import android.os.Parcel;
import android.os.Parcelable;


public class SingleNote implements Parcelable {

    private int _id;
    private String _title;
    private String _content;
    private boolean _favorite;

    private long _timeCreated;
    private long _timeModified;
    private long _nextReminderTime;
    private int _reminderId;
    private boolean _hasFrequencyChoices = false;

    public SingleNote() {

    }

    // New note does not have a reminder
    public SingleNote(String title, String content, boolean favorite, long timeCreated) {
        this(title, content, favorite, timeCreated, 0, -1);
    }

    // New note has a reminder
    public SingleNote(String title, String content, boolean favorite, long timeCreated, long _nextReminderTime, int reminderId) {
        this._title = title;
        this._content = content;
        this._favorite = favorite;
        this._timeCreated = timeCreated;
        this._timeModified = timeCreated;
        this._nextReminderTime = _nextReminderTime;
        this._reminderId = reminderId;
    }

    @Override
    public int hashCode() {
        int result = 22;
        result = 31 * result + _id;
        result = 31 * result + _title.hashCode();
        result = 31 * result + _content.hashCode();
        if (_favorite) {
            result = 31 * result + 1;
        }
        result = 31 * result + Long.valueOf(_timeCreated).hashCode();
        result = 31 * result + Long.valueOf(_timeModified).hashCode();
        result = 31 * result + Long.valueOf(_nextReminderTime).hashCode();
        result = 31 * result + _reminderId;
        if (_hasFrequencyChoices) {
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

        return note._content.equals(_content) &&
                note._title.equals(_title) &&
                note._favorite == _favorite &&
                note._timeCreated == _timeCreated &&
                note._timeModified == _timeModified &&
                note._nextReminderTime == _nextReminderTime &&
                note._reminderId == _reminderId &&
                note._hasFrequencyChoices == _hasFrequencyChoices;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.getClass().getSimpleName()).append("[ ");
        sb.append("Id: ").append(_id);
        sb.append(",    Title: ").append(_title);
        sb.append(",    Content: ").append(_content);
        sb.append(",    Favorite: ").append(_favorite);
        sb.append(",    TimeCreated: ").append(_timeCreated);
        sb.append(",    TimeModified: ").append(_timeModified);
        sb.append(",    ReminderTime: ").append(_nextReminderTime);
        sb.append(",    ReminderId: ").append(_reminderId);
        sb.append(",    HasFrequencyChoice: ").append(_hasFrequencyChoices);
        sb.append(" ]");

        return sb.toString();
    }

    public SingleNote(Parcel in) {
        this._id = in.readInt();
        this._title = in.readString();
        this._content = in.readString();
        this._favorite = (in.readInt() == 1);
        this._timeCreated = in.readLong();
        this._timeModified = in.readLong();
        this._nextReminderTime = in.readLong();
        this._reminderId = in.readInt();
        this._hasFrequencyChoices = (in.readInt() == 1);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(_id);
        dest.writeString(_title);
        dest.writeString(_content);

        if (_favorite) {
            dest.writeInt(1);
        } else {
            dest.writeInt(0);
        }

        dest.writeLong(_timeCreated);
        dest.writeLong(_timeModified);
        dest.writeLong(_nextReminderTime);
        dest.writeInt(_reminderId);

        if (_hasFrequencyChoices) {
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

    public int get_id() {
        return _id;
    }

    public void set_id(int _id) {
        this._id = _id;
    }

    public String get_title() {
        return _title;
    }

    public void set_title(String _title) {
        this._title = _title;
    }

    public String get_content() {
        return _content;
    }

    public void set_content(String _content) {
        this._content = _content;
    }

    public boolean is_favorite() {
        return _favorite;
    }

    public void set_favorite(boolean _favorite) {
        this._favorite = _favorite;
    }

    public long get_timeCreated() {
        return _timeCreated;
    }

    public void set_timeCreated(long _timeCreated) {
        this._timeCreated = _timeCreated;
    }

    public long get_timeModified() {
        return _timeModified;
    }

    public void set_timeModified(long _timeModified) {
        this._timeModified = _timeModified;
    }

    public long get_nextReminderTime() {
        return _nextReminderTime;
    }

    public void set_nextReminderTime(long _nextReminderTime) {
        this._nextReminderTime = _nextReminderTime;
    }

    public int get_reminderId() {
        return _reminderId;
    }

    public void set_reminderId(int _reminderId) {
        this._reminderId = _reminderId;
    }

    public boolean hasFrequencyChoices() {
        return _hasFrequencyChoices;
    }

    public void set_hasFrequencyChoices(boolean _hasFrequencyChoices) {
        this._hasFrequencyChoices = _hasFrequencyChoices;
    }
}