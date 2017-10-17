package com.ozmar.notes;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.concurrent.TimeUnit;


public class Preferences {

    private SharedPreferences.Editor editor;
    private SharedPreferences preferences;

    public Preferences(Context context) {
        this.preferences = context.getSharedPreferences("User Settings", Context.MODE_PRIVATE);
    }

    public int getLayoutChoice() {
        return preferences.getInt("Layout Choice", 0);
    }

    public void saveLayoutChoice(int layoutChoice) {
        editor = preferences.edit();
        editor.putInt("Layout Choice", layoutChoice);
        editor.apply();
    }

    public int getReminderID() {
        int id = preferences.getInt("reminderID", 1) + 1;
        setReminderID(id);
        return id;
    }

    public void setReminderID(int reminderId) {
        editor = preferences.edit();
        editor.putInt("reminderID", reminderId);
        editor.apply();
    }

    public int getDaysInTrash() {
        return preferences.getInt("Day In Trash", 1);
    }

    public void setDaysInTrash(int days) {
        editor = preferences.edit();
        editor.putInt("Day In Trash", days);
        editor.apply();
    }

    public long getMorningTime() {
        long time = TimeUnit.HOURS.toMillis(preferences.getLong("Morning Time Hour", 8));
        time += TimeUnit.MINUTES.toMillis(preferences.getLong("Morning Time Minute", 0));
        return time;
    }

    public void setMorningTime(int hour, int minute) {
        editor = preferences.edit();
        editor.putInt("Morning Time Hour", hour);
        editor.putInt("Morning Time Minute", minute);
        editor.apply();
    }

    public long getAfternoonTime() {
        long time = TimeUnit.HOURS.toMillis(preferences.getLong("Afternoon Time Hour", 13));
        time += TimeUnit.MINUTES.toMillis(preferences.getLong("Afternoon Time Minute", 0));
        return time;
    }

    public void setAfternoonTime(int hour, int minute) {
        editor = preferences.edit();
        editor.putInt("Afternoon Time Hour", hour);
        editor.putInt("Afternoon Time Minute", minute);
        editor.apply();
    }

    public long getEveningTime() {
        long time = TimeUnit.HOURS.toMillis(preferences.getLong("Evening Time Hour", 18));
        time += TimeUnit.MINUTES.toMillis(preferences.getLong("Evening Time Minute", 0));
        return time;
    }

    public void setEveningTime(int hour, int minute) {
        editor = preferences.edit();
        editor.putInt("Evening Time Hour", hour);
        editor.putInt("Evening Time Minute", minute);
        editor.apply();
    }

    public long getNightTime() {
        long time = TimeUnit.HOURS.toMillis(preferences.getLong("Night Time Hour", 20));
        time += TimeUnit.MINUTES.toMillis(preferences.getLong("Night Time Minute", 0));
        return time;
    }

    public void setNightTime(int hour, int minute) {
        editor = preferences.edit();
        editor.putInt("Night Time Hour", hour);
        editor.putInt("Night Time Minute", minute);
        editor.apply();
    }
}