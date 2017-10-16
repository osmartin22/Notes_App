package com.ozmar.notes;

import android.content.Context;
import android.content.SharedPreferences;


public class Preferences {

    private Context context;
    private SharedPreferences.Editor editor;
    private SharedPreferences preferences;

    public Preferences(Context context) {
        this.context = context;
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
}