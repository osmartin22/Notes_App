package com.ozmar.notes.reminderDialog;


public class FrequencyPickerFlagHelper {
    public boolean topEmpty;        // Always used
    public boolean bottomEmpty;     // Used depending on spinner
    public boolean bottomUsed;

    public FrequencyPickerFlagHelper() {

    }

    public boolean getBooleanResult() {
        if (bottomUsed) {
            return !(topEmpty || bottomEmpty);
        }

        return !topEmpty;
    }

}
