package com.ozmar.notes;

/**
 * Created by ozmar on 9/21/2017.
 */

public class SingleNote {

    int _id;
    String _title;
    String _content;

    public SingleNote() {

    }

    public SingleNote(String title, String content) {
        this._title = title;
        this._content = content;
    }

    public SingleNote(int id, String title, String content) {
        this._id = id;
        this._title = title;
        this._content = content;
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
