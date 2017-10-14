package com.ozmar.notes;

import android.os.Parcel;
import android.os.Parcelable;

// TODO: Update constructors to always include time modified parameter

public class SingleNote implements Parcelable {

    private int _id;
    private String _title;
    private String _content;
    private int _favorite;

    private long _timeModified;

    // TODO: Update serializable
    private long _reminderTime = 0;

    public SingleNote() {

    }

    public SingleNote(String title, String content, int favorite, long timeModified) {
        this._title = title;
        this._content = content;
        this._favorite = favorite;
        this._timeModified = timeModified;
    }

    public SingleNote(String title, String content, int favorite, long timeModified, long reminderTime) {
        this(title,content,favorite,timeModified);
        this._reminderTime = reminderTime;
    }

    @Override
    public int hashCode() {
        int result = 22;
        result = 31 * result + _id;
        result = 31 * result + _title.hashCode();
        result = 31 * result + _content.hashCode();
        result = 31 * result + _favorite;
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
                note._timeModified == _timeModified;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.getClass().getSimpleName()).append("[ ");
        sb.append("Id: ").append(_id);
        sb.append(",    Title: ").append(_title);
        sb.append(",    Content: ").append(_content);
        sb.append(",    Favorite: ").append(_favorite);
        sb.append(",   TimeModified: ").append((_timeModified));
        sb.append(" ]");

        return sb.toString();
    }

    public SingleNote(Parcel in) {
        this._id = in.readInt();
        this._title = in.readString();
        this._content = in.readString();
        this._favorite = in.readInt();
        this._timeModified = in.readLong();
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
        dest.writeInt(_favorite);
        dest.writeLong(_timeModified);
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

    public int get_favorite() {
        return _favorite;
    }

    public void set_favorite(int _favorite) {
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