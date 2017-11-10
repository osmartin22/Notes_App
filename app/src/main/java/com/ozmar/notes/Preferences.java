package com.ozmar.notes;

import android.content.Context;
import android.content.SharedPreferences;

import org.joda.time.LocalTime;


public class Preferences {

    private final Context context;
    private final SharedPreferences preferences;
    private SharedPreferences.Editor editor;

    public Preferences(Context context) {
        this.context = context;
        this.preferences = context.getSharedPreferences(context.getString(R.string.userSettingsPreference),
                Context.MODE_PRIVATE);
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
        return preferences.getInt(context.getString(R.string.deleteTrashXDaysPreference), 1);
    }

    public void setDaysInTrash(int days) {
        editor = preferences.edit();
        editor.putInt(context.getString(R.string.deleteTrashXDaysPreference), days);
        editor.apply();
    }

    public LocalTime getMorningTime() {
        int hour = preferences.getInt(context.getString(R.string.morningHourPreference), 8);
        int minute = preferences.getInt(context.getString(R.string.morningMinutePreference), 0);
        return new LocalTime(hour, minute);
    }

    public void setMorningTime(int hour, int minute) {
        editor = preferences.edit();
        editor.putInt(context.getString(R.string.morningHourPreference), hour);
        editor.putInt(context.getString(R.string.morningMinutePreference), minute);
        editor.apply();
    }

    public LocalTime getAfternoonTime() {
        int hour = preferences.getInt(context.getString(R.string.afternoonHourPreference), 13);
        int minute = preferences.getInt(context.getString(R.string.afternoonMinutePreference), 0);
        return new LocalTime(hour, minute);
    }

    public void setAfternoonTime(int hour, int minute) {
        editor = preferences.edit();
        editor.putInt(context.getString(R.string.afternoonHourPreference), hour);
        editor.putInt(context.getString(R.string.afternoonMinutePreference), minute);
        editor.apply();
    }

    public LocalTime getEveningTime() {
        int hour = preferences.getInt(context.getString(R.string.eveningHourPreference), 18);
        int minute = preferences.getInt(context.getString(R.string.eveningMinutePreference), 0);
        return new LocalTime(hour, minute);
    }

    public void setEveningTime(int hour, int minute) {
        editor = preferences.edit();
        editor.putInt(context.getString(R.string.eveningHourPreference), hour);
        editor.putInt(context.getString(R.string.eveningMinutePreference), minute);
        editor.apply();
    }

    public LocalTime getNightTime() {
        int hour = preferences.getInt(context.getString(R.string.nightHourPreference), 20);
        int minute = preferences.getInt(context.getString(R.string.nightMinutePreference), 0);
        return new LocalTime(hour, minute);
    }

    public void setNightTime(int hour, int minute) {
        editor = preferences.edit();
        editor.putInt(context.getString(R.string.nightHourPreference), hour);
        editor.putInt(context.getString(R.string.nightMinutePreference), minute);
        editor.apply();
    }
}