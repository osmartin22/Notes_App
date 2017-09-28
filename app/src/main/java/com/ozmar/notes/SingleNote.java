package com.ozmar.notes;

public class SingleNote {

    private int _id;
    private String _title;
    private String _content;
    private int _favorite;

    public SingleNote() {

    }

    public SingleNote(String title, String content) {
        this(-1, title, content, 0);
    }

    public SingleNote(String title, String content, int favorite) {
        this(-1, title, content, favorite);
    }

    public SingleNote(int id, String title, String content, int favorite) {
        this._id = id;
        this._title = title;
        this._content = content;
        this._favorite = favorite;
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
