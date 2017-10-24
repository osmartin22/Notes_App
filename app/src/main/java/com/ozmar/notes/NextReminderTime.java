package com.ozmar.notes;

/**
 * Created by ozmar on 10/23/2017.
 */

public class NextReminderTime {
    public final long nextReminderTime;
    public final boolean hasFrequencyChoices;
    public NextReminderTime(long nextReminderTime, boolean hasFrequencyChoices) {
        this.nextReminderTime = nextReminderTime;
        this.hasFrequencyChoices = hasFrequencyChoices;
    }
}
