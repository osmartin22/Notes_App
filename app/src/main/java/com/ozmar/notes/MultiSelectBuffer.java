package com.ozmar.notes;


import android.util.Log;
import android.view.View;

import java.util.List;

public class MultiSelectBuffer {
    private MultiSelectHelper buffer1;
    private MultiSelectHelper buffer2;
    private int currentBuffer;

    public MultiSelectBuffer() {
        this.buffer1 = new MultiSelectHelper();
        this.buffer2 = new MultiSelectHelper();
        currentBuffer = 0;
    }

    // Only swap buffers if the current buffer is not empty (i.e. has data to process)
    public void swapBuffer() {
        if (currentBuffer == 0 && buffer1.getSize() != 0) {
            Log.d("Buffer", "Swap to Buffer1");
            currentBuffer = 1;
        } else if (currentBuffer == 1 && buffer2.getSize() != 0){
            Log.d("Buffer", "Swap to Buffer0");
            currentBuffer = 0;
        }
    }

    // Clears buffer that is not currently being used
    public void clearOtherBuffer() {
        if(currentBuffer == 0) {
            buffer2.clearLists();
        } else {
            buffer1.clearLists();
        }
    }

    // Clears buffer currently in use
    public void clearCurrentBuffer() {
        if(currentBuffer == 0) {
            buffer1.clearLists();
        } else {
            buffer2.clearLists();
        }
    }

    public void addDataToBuffer(View view, SingleNote note, int position) {
        if (currentBuffer == 0) {
            buffer1.addToLists(view, note, position);
        } else {
            buffer2.addToLists(view, note, position);
        }
    }

    public void removeDataFromBuffer(View view, SingleNote note) {
        if (currentBuffer == 0) {
            buffer1.removeFromLists(view, note);
        } else {
            buffer2.removeFromLists(view, note);
        }
    }

    // TODO: Remove Possibly
//    public MultiSelectHelper getCurrentBuffer() {
//        if(currentBuffer == 0) {
//            return  buffer1;
//        }
//
//        return buffer2;
//    }

    public int currentBufferSize() {
        if(currentBuffer == 0) {
            return buffer1.getSize();
        }

        return buffer2.getSize();
    }

    public List<SingleNote> currentBufferNotes() {
        if(currentBuffer == 0) {
            return buffer1.getNotes();
        }

        return buffer2.getNotes();
    }

    public List<Integer> currentBufferPositions() {
        if(currentBuffer == 0) {
            return buffer1.getPositions();
        }

        return buffer2.getPositions();
    }

    public List<View> currentBufferViews() {
        if(currentBuffer == 0) {
            return buffer1.getViews();
        }

        return buffer2.getViews();
    }

    // TODO: Possible change
    public boolean isBufferAvailable() {
        return (!buffer1.checkIfEmpty() && !buffer2.checkIfEmpty());
    }
}
