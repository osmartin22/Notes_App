package com.ozmar.notes.utils;

import com.ozmar.notes.database.NoteAndReminderPreview;

import java.util.ArrayList;
import java.util.List;


public class BufferHelper {
    private final List<NoteAndReminderPreview> notes;
    private final List<Integer> positions;

    public BufferHelper() {
        this.notes = new ArrayList<>();
        this.positions = new ArrayList<>();
    }

    public List<NoteAndReminderPreview> getNotes() {
        return notes;
    }

    public List<Integer> getPositions() {
        return positions;
    }

    public boolean checkIfEmpty() {
        return notes.isEmpty();
    }

    public void addToLists(NoteAndReminderPreview note, int position) {
        this.notes.add(note);
        this.positions.add(position);
    }

    public void removeFromLists(NoteAndReminderPreview note) {
        int position = this.notes.indexOf(note);
        this.notes.remove(note);
        this.positions.remove(position);
    }

    public void removeFromPosition(int position) {
        int index = this.positions.indexOf(position);
        this.positions.remove(Integer.valueOf(position));
        this.notes.remove(index);
    }

    public void clearLists() {
        this.notes.clear();
        this.positions.clear();
    }

    public int getSize() {
        return notes.size();
    }

}

