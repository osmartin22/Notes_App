package com.ozmar.notes;

import java.util.ArrayList;
import java.util.List;

// Class made to easily manage a view, its note, and position
public class MultiSelectHelper {
    private final List<SingleNote> notes;
    private final List<Integer> positions;

    public MultiSelectHelper() {
        this.notes = new ArrayList<>();
        this.positions = new ArrayList<>();
    }

    public List<SingleNote> getNotes() {
        return notes;
    }

    public List<Integer> getPositions() {
        return positions;
    }

    public boolean checkIfEmpty() {
        if(!notes.isEmpty()) {
            return false;
        }
        return true;
    }

    public void addToLists(SingleNote note, int position) {
        this.notes.add(note);
        this.positions.add(position);
    }

    public void removeFromLists(SingleNote note) {
        int position = this.notes.indexOf(note);
        this.notes.remove(note);
        this.positions.remove(position);
    }

    public void clearLists() {
        this.notes.clear();
        this.positions.clear();
    }

    public int getSize() {
        return notes.size();
    }


}

