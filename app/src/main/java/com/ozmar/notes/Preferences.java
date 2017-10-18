package com.ozmar.notes;

import android.content.Context;
import android.content.SharedPreferences;

import org.joda.time.LocalTime;


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

    public LocalTime getMorningTime() {
        int hour = preferences.getInt("Morning Time Hour", 8);
        int minute = preferences.getInt("Morning Time Minute", 0);
        return new LocalTime(hour, minute);
    }

    public void setMorningTime(int hour, int minute) {
        editor = preferences.edit();
        editor.putInt("Morning Time Hour", hour);
        editor.putInt("Morning Time Minute", minute);
        editor.apply();
    }

    public LocalTime getAfternoonTime() {
        int hour = preferences.getInt("Afternoon Time Hour", 13);
        int minute = preferences.getInt("Afternoon Time Minute", 0);
        return new LocalTime(hour, minute);
    }

    public void setAfternoonTime(int hour, int minute) {
        editor = preferences.edit();
        editor.putInt("Afternoon Time Hour", hour);
        editor.putInt("Afternoon Time Minute", minute);
        editor.apply();
    }

    public LocalTime getEveningTime() {
        int hour = preferences.getInt("Evening Time Hour", 18);
        int minute = preferences.getInt("Evening Time Minute", 0);
        return new LocalTime(hour, minute);
    }

    public void setEveningTime(int hour, int minute) {
        editor = preferences.edit();
        editor.putInt("Evening Time Hour", hour);
        editor.putInt("Evening Time Minute", minute);
        editor.apply();
    }

    public LocalTime getNightTime() {
        int hour = preferences.getInt("Night Time Hour", 20);
        int minute = preferences.getInt("Night Time Minute", 0);
        return new LocalTime(hour, minute);
    }

    public void setNightTime(int hour, int minute) {
        editor = preferences.edit();
        editor.putInt("Night Time Hour", hour);
        editor.putInt("Night Time Minute", minute);
        editor.apply();
    }
}