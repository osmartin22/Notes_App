package com.ozmar.notes;

public class SingleNote {

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

        if(!(obj instanceof SingleNote)) {
            return  false;
        }

        SingleNote note = (SingleNote) obj;

        return  note._content.equals(_content) &&
                note._title.equals(_title) &&
                note._favorite == _favorite;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.getClass().getSimpleName()).append("[");
        sb.append("Id: ").append(_id);
        sb.append(" Title: ").append(_title);
        sb.append(" Content: ").append(_content);
        sb.append(" Favorite: ").append(_favorite).append("]");

        return sb.toString();
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
