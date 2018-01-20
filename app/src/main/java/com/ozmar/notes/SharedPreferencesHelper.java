package com.ozmar.notes;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.preference.PreferenceManager;

import org.joda.time.LocalTime;


public class SharedPreferencesHelper {

    private final Context context;
    private final SharedPreferences preferences;
    private SharedPreferences.Editor editor;


    public SharedPreferencesHelper(Context context) {
        this.context = context;
        this.preferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public int getLayoutChoice() {
        return preferences.getInt(context.getString(R.string.layoutChoicePreference), 0);
    }

    public void saveLayoutChoice(int layoutChoice) {
        editor = preferences.edit();
        editor.putInt(context.getString(R.string.layoutChoicePreference), layoutChoice);
        editor.apply();
    }

    public int getDaysInTrash() {
        // Preference becomes string even though only numbers are allowed for the settings
        // Must get string then convert to int to avoid ClassCastException
        return Integer.valueOf(preferences.getString(context.getString(R.string.emptyTrashInXDays), "7"));
    }

    public void setDaysInTrash(int days) {
        editor = preferences.edit();
        editor.putInt(context.getString(R.string.emptyTrashInXDays), days);
        editor.apply();
    }


    @NonNull
    private LocalTime createLocalTime(int timeInMinutes) {
        int hour = timeInMinutes / 60;
        int minute = timeInMinutes % 60;
        return new LocalTime(hour, minute);
    }

    public LocalTime getMorningTime() {
        int timeInMinutes = preferences.getInt(context.getString(R.string.morningTimePreference), 480);
        return createLocalTime(timeInMinutes);
    }

    public int getMorningTimeInMinutes() {
        return preferences.getInt(context.getString(R.string.morningTimePreference), 480);
    }

    public void setMorningTime(int timeInMinutes) {
        editor = preferences.edit();
        editor.putInt(context.getString(R.string.morningTimePreference), timeInMinutes);
        editor.apply();
    }

    public LocalTime getAfternoonTime() {
        int timeInMinutes = preferences.getInt(context.getString(R.string.afternoonTimePreference), 720);
        return createLocalTime(timeInMinutes);
    }

    public int getAfternoonTimeInMinutes() {
        return preferences.getInt(context.getString(R.string.afternoonTimePreference), 780);
    }

    public void setAfternoonTime(int timeInMinutes) {
        editor = preferences.edit();
        editor.putInt(context.getString(R.string.afternoonTimePreference), timeInMinutes);
        editor.apply();
    }

    public LocalTime getEveningTime() {
        int timeInMinutes = preferences.getInt(context.getString(R.string.eveningTimePreference), 1020);
        return createLocalTime(timeInMinutes);
    }

    public int getEveningTimeInMinutes() {
        return preferences.getInt(context.getString(R.string.eveningTimePreference), 1080);
    }

    public void setEveningTime(int timeInMinutes) {
        editor = preferences.edit();
        editor.putInt(context.getString(R.string.eveningTimePreference), timeInMinutes);
        editor.apply();
    }

    public LocalTime getNightTime() {
        int timeInMinutes = preferences.getInt(context.getString(R.string.nightTimePreference), 1200);
        return createLocalTime(timeInMinutes);
    }

    public int getNightTimeInMinutes() {
        return preferences.getInt(context.getString(R.string.nightTimePreference), 1200);
    }

    public void setNightTime(int timeInMinutes) {
        editor = preferences.edit();
        editor.putInt(context.getString(R.string.nightTimePreference), timeInMinutes);
        editor.apply();
    }
}