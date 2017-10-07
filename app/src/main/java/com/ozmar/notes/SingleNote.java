package com.ozmar.notes;

import android.os.Parcel;
import android.os.Parcelable;

public class SingleNote implements Parcelable {

    private int _id;
    private String _title;
    private String _content;
    private int _favorite;

    public SingleNote() {

    }

    public SingleNote(String title, String content) {
        this(title, content, 0);
    }

    public SingleNote(String title, String content, int favorite) {
        this._title = title;
        this._content = content;
        this._favorite = favorite;
    }

    public SingleNote(int id, String title, String content, int favorite) {
        this._id = id;
        this._title = title;
        this._content = content;
        this._favorite = favorite;
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
                note._favorite == _favorite;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.getClass().getSimpleName()).append("[ ");
        sb.append("Id: ").append(_id);
        sb.append(",    Title: ").append(_title);
        sb.append(",    Content: ").append(_content);
        sb.append(",    Favorite: ").append(_favorite).append(" ]");

        return sb.toString();
    }

    public SingleNote(Parcel in) {
        this._id = in.readInt();
        this._title = in.readString();
        this._content = in.readString();
        this._favorite = in.readInt();
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