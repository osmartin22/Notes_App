package com.ozmar.notes;

import android.view.View;

import java.util.ArrayList;
import java.util.List;

// Class made to easily manage a view, its note, and position
public class MultiSelectHelper {
    private List<SingleNote> notes;
    private List<Integer> positions;
    private List<View> views;

    public MultiSelectHelper() {
        this.notes = new ArrayList<>();
        this.positions = new ArrayList<>();
        this.views = new ArrayList<>();
    }

    public List<SingleNote> getNotes() {
        return notes;
    }

    public void setNotes(List<SingleNote> notes) {
        this.notes = notes;
    }

    public List<Integer> getPositions() {
        return positions;
    }

    public void setPositions(List<Integer> positions) {
        this.positions = positions;
    }

    public List<View> getViews() {
        return views;
    }

    public void setViews(List<View> views) {
        this.views = views;
    }

    public boolean checkIfEmpty() {
        if(!notes.isEmpty()) {
            return false;
        }
        return true;
    }

    public void addToLists(View view, SingleNote note, int position) {
        this.notes.add(note);
        this.positions.add(position);
        this.views.add(view);
    }

    public void removeFromLists(View view, SingleNote note) {
        int position = this.notes.indexOf(note);
        this.notes.remove(note);
        this.positions.remove(position);
        this.views.remove(view);
    }

    public void clearLists() {
        this.notes.clear();
        this.positions.clear();
        this.views.clear();
    }

    public int getSize() {
        return notes.size();
    }


}

