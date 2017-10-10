package com.ozmar.notes.utils;


import android.util.Log;

import com.ozmar.notes.SingleNote;

import java.util.List;

public class UndoBuffer {
    private final BufferHelper buffer0;
    private final BufferHelper buffer1;
    private int currentBuffer;

    private int bufferToProcess = 0;     // This value should be stored as an int if needed as it
                                        // will change with user interaction

    public UndoBuffer() {
        this.buffer0 = new BufferHelper();
        this.buffer1 = new BufferHelper();
        currentBuffer = 0;
    }

    // TODO: Use in code, if false, throw message to wait maybe
    public boolean isBufferAvailable() {

        Log.d("Process", "Buffer0 -> " + buffer0.getSize());
        Log.d("Process", "Buffer1 -> " + buffer1.getSize());

        return (buffer0.checkIfEmpty() || buffer1.checkIfEmpty());
    }

    public void swapBuffer() {
        if (currentBuffer == 0 && !buffer0.checkIfEmpty()) {
            currentBuffer = 1;
            Log.d("Process", "Swap to Buffer1");

        } else if (currentBuffer == 1 && !buffer1.checkIfEmpty()) {
            currentBuffer = 0;
            Log.d("Process", "Swap to Buffer0");
        }
    }

    // Returns int to which buffer is set to be processed
    public int getBufferToProcess() {
        return bufferToProcess;
    }

    // Return buffer to start processing
    public void bufferToStartProcessing() {
        if (currentBuffer == 0) {
            bufferToProcess = 0;
        } else {
            bufferToProcess = 1;
        }
    }

    // Only use if you are sure that the current buffer in use is the one to be cleared
    public void clearBuffer() {
        if (currentBuffer == 0) {
            buffer0.clearLists();
        } else {
            buffer1.clearLists();
        }
    }

    public void clearBuffer(int num) {
        if(num == 0) {
            buffer0.clearLists();
        } else if (num == 1) {
            buffer1.clearLists();
        }
    }

    // Add note and position to buffer
    public void addDataToBuffer(SingleNote note, int position) {
        if (currentBuffer == 0) {
            buffer0.addToLists(note, position);
        } else {
            buffer1.addToLists(note, position);
        }
    }

    // Remove note from buffer
    public void removeDataFromBuffer(SingleNote note) {
        if (currentBuffer == 0) {
            buffer0.removeFromLists(note);
        } else {
            buffer1.removeFromLists(note);
        }
    }

    // Remove position and note from buffer
    public void removeDataFromBuffer(int position) {
        if (currentBuffer == 0) {
            buffer0.removeFromPosition(position);
        } else {
            buffer1.removeFromPosition(position);
        }
    }

    // Get buffer size of the current buffer in use
    public int currentBufferSize() {
        if (currentBuffer == 0) {
            return buffer0.getSize();
        }

        return buffer1.getSize();
    }

    // Get notes of the current buffer in use
    public List<SingleNote> currentBufferNotes() {
        if (currentBuffer == 0) {
            return buffer0.getNotes();
        }

        return buffer1.getNotes();
    }

    // Get positions of the current buffer in use
    public List<Integer> currentBufferPositions() {
        if (currentBuffer == 0) {
            return buffer0.getPositions();
        }

        return buffer1.getPositions();
    }
}