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

    public void saveLayoutChoice(int layoutChoice) {
        editor = preferences.edit();
        editor.putInt("Layout Choice", layoutChoice);
        editor.apply();
    }

    public int getLayoutChoice() {
        return preferences.getInt("Layout Choice", 0);
    }
}