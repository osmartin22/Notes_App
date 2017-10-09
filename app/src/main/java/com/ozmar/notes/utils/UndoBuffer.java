package com.ozmar.notes.utils;


import android.util.Log;

import com.ozmar.notes.SingleNote;

import java.util.List;

public class UndoBuffer {
    private final BufferHelper buffer0;
    private final BufferHelper buffer1;
    private int currentBuffer;

    private boolean buffer0Processing = false;
    private boolean buffer1Processing = false;

    public UndoBuffer() {
        this.buffer0 = new BufferHelper();
        this.buffer1 = new BufferHelper();
        currentBuffer = 0;
    }

    // Only swap buffers if the current buffer is not empty (i.e. has data to process)
    public void swapBuffer() {
        if (currentBuffer == 0 && buffer0.getSize() != 0) {
            Log.d("Buffer", "Swap to Buffer2");
            currentBuffer = 1;
        } else if (currentBuffer == 1 && buffer1.getSize() != 0) {
            Log.d("Buffer", "Swap to Buffer1");
            currentBuffer = 0;
        }
    }

    // Clears buffer that is not currently being used
    public void clearOtherBuffer() {
        if (currentBuffer == 0) {
            buffer1.clearLists();
        } else {
            buffer0.clearLists();
        }
    }

    // Clears buffer currently in use
    public void clearCurrentBuffer() {
        if (currentBuffer == 0) {
            buffer0.clearLists();
        } else {
            buffer1.clearLists();
        }
    }

    public void addDataToBuffer(SingleNote note, int position) {
        if (currentBuffer == 0) {
            buffer0.addToLists(note, position);
        } else {
            buffer1.addToLists(note, position);
        }
    }

    public void removeDataFromBuffer(SingleNote note) {
        if (currentBuffer == 0) {
            buffer0.removeFromLists(note);
        } else {
            buffer1.removeFromLists(note);
        }
    }

    public void removeDataFromPosition(int position) {
        if (currentBuffer == 0) {
            buffer0.removeFromPosition(position);
        } else {
            buffer1.removeFromPosition(position);
        }
    }

    public int currentBufferSize() {
        if (currentBuffer == 0) {
            return buffer0.getSize();
        }

        return buffer1.getSize();
    }

    public List<SingleNote> currentBufferNotes() {
        if (currentBuffer == 0) {
            return buffer0.getNotes();
        }

        return buffer1.getNotes();
    }

    public List<Integer> currentBufferPositions() {
        if (currentBuffer == 0) {
            return buffer0.getPositions();
        }

        return buffer1.getPositions();
    }

    public BufferHelper currentBuffer() {
        if (currentBuffer == 0) {
            return buffer0;
        }

        return buffer1;
    }

    public BufferHelper otherBuffer() {
        if (currentBuffer == 0) {
            return buffer1;
        }

        return buffer0;
    }

    // TODO: Use in code, if false, throw message to wait maybe
    public boolean isBufferAvailable() {

        Log.d("H", "Buffer0 -> " + buffer0.getSize());
        Log.d("H", "Buffer0 Empty -> " + buffer0.checkIfEmpty());
        Log.d("H", "Buffer1 -> " + buffer1.getSize());
        Log.d("H", "Buffer1 Empty -> " + buffer1.checkIfEmpty());

        return (buffer0.checkIfEmpty() || buffer1.checkIfEmpty());
    }
}