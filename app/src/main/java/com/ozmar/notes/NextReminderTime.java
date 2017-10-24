package com.ozmar.notes;


public class NextReminderTime {
    public final long nextReminderTime;
    public final boolean hasFrequencyChoices;
    public NextReminderTime(long nextReminderTime, boolean hasFrequencyChoices) {
        this.nextReminderTime = nextReminderTime;
        this.hasFrequencyChoices = hasFrequencyChoices;
    }
}
