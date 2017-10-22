package com.ozmar.notes;

import android.os.Parcel;
import android.os.Parcelable;


public class SingleNote implements Parcelable {

    private int _id;
    private String _title;
    private String _content;
    private boolean _favorite;

    private long _timeModified;
    private long _reminderTime = 0; // TODO: this should only be the time until the next reminder alarm
    private int _reminderId = 0;

    public SingleNote() {

    }

    public SingleNote(String title, String content, boolean favorite, long timeModified, long reminderTime) {
        this._title = title;
        this._content = content;
        this._favorite = favorite;
        this._timeModified = timeModified;
        this._reminderTime = reminderTime;
    }



    public SingleNote(String title, String content, boolean favorite, long timeModified, long reminderTime, int reminderId) {
        this._title = title;
        this._content = content;
        this._favorite = favorite;
        this._timeModified = timeModified;
        this._reminderTime = reminderTime;
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
        result = 31 * result + Long.valueOf(_timeModified).hashCode();
        result = 31 * result + Long.valueOf(_reminderTime).hashCode();
        result = 31 * result + _reminderId;
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
                note._timeModified == _timeModified &&
                note._reminderTime == _reminderTime &&
                note._reminderId == _reminderId;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.getClass().getSimpleName()).append("[ ");
        sb.append("Id: ").append(_id);
        sb.append(",    Title: ").append(_title);
        sb.append(",    Content: ").append(_content);
        sb.append(",    Favorite: ").append(_favorite);
        sb.append(",    TimeModified: ").append(_timeModified);
        sb.append(",    ReminderTime: ").append(_reminderTime);
        sb.append(",    ReminderId: ").append(_reminderId);
        sb.append(" ]");

        return sb.toString();
    }

    public SingleNote(Parcel in) {
        this._id = in.readInt();
        this._title = in.readString();
        this._content = in.readString();
        this._favorite = (in.readInt() == 1);
        this._timeModified = in.readLong();
        this._reminderTime = in.readLong();
        this._reminderId = in.readInt();
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
        dest.writeLong(_timeModified);
        dest.writeLong(_reminderTime);
        dest.writeInt(_reminderId);
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

    public int get_reminderId() {
        return _reminderId;
    }

    public void set_reminderId(int _reminderId) {
        this._reminderId = _reminderId;
    }

    public long get_reminderTime() {
        return _reminderTime;
    }

    public void set_reminderTime(long _reminderTime) {
        this._reminderTime = _reminderTime;
    }

    public long get_timeModified() {
        return _timeModified;
    }

    public void set_timeModified(long _timeModified) {
        this._timeModified = _timeModified;
    }

    public boolean is_favorite() {
        return _favorite;
    }

    public void set_favorite(boolean _favorite) {
        this._favorite = _favorite;
    }

    public String get_title() {
        return _title;
    }

    public void set_title(String _title) {
        this._title = _title;
    }

    public int get_id() {
        return _id;
    }

    public void set_id(int _id) {
        this._id = _id;
    }

    public String get_content() {
        return _content;
    }

    public void set_content(String _content) {
        this._content = _content;
    }
}