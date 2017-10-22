package com.ozmar.notes.reminderDialog;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ToggleButton;

import com.ozmar.notes.R;

import java.util.ArrayList;
import java.util.List;


public class WeeklyLayoutHelper implements CompoundButton.OnCheckedChangeListener {

    private final View view;
    private final Button doneButton;
    private final ToggleButton sunday;
    private final ToggleButton monday;
    private final ToggleButton tuesday;
    private final ToggleButton wednesday;
    private final ToggleButton thursday;
    private final ToggleButton friday;
    private final ToggleButton saturday;
    private int buttonsChecked = 0;

    public WeeklyLayoutHelper(View view, Button doneButton) {
        this.view = view;
        this.doneButton = doneButton;
        this.sunday = view.findViewById(R.id.toggleButtonSunday);
        this.monday = view.findViewById(R.id.toggleButtonMonday);
        this.tuesday = view.findViewById(R.id.toggleButtonTuesday);
        this.wednesday = view.findViewById(R.id.toggleButtonWednesday);
        this.thursday = view.findViewById(R.id.toggleButtonThursday);
        this.friday = view.findViewById(R.id.toggleButtonFriday);
        this.saturday = view.findViewById(R.id.toggleButtonSaturday);
    }

    public void setViewEnabled(boolean flag) {
        sunday.setEnabled(flag);
        monday.setEnabled(flag);
        tuesday.setEnabled(flag);
        wednesday.setEnabled(flag);
        thursday.setEnabled(flag);
        friday.setEnabled(flag);
        saturday.setEnabled(flag);
    }

    public View getMainView() {
        return view;
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (buttonView == sunday) {
            check(buttonView, "sunday");

        } else if (buttonView == monday) {
            check(buttonView, "monday");

        } else if (buttonView == tuesday) {
            check(buttonView, "tuesday");

        } else if (buttonView == wednesday) {
            check(buttonView, "wednesday");

        } else if (buttonView == thursday) {
            check(buttonView, "thursday");

        } else if (buttonView == friday) {
            check(buttonView, "friday");

        } else if (buttonView == saturday) {
            check(buttonView, "saturday");
        }
    }

    private void check(CompoundButton button, String checked) {
        if (button.isChecked()) {
            buttonsChecked++;
            Log.d("Button", checked + " Checked");
        } else {
            buttonsChecked--;
            Log.d("Button", checked + " UnChecked");
            if (buttonsChecked == 0) {
            }
        }
    }

    public List<Integer> getCheckedButtons() {
        List<Integer> chosen = new ArrayList<>();

        if (sunday.isChecked()) {
            chosen.add(7);
        }
        if (monday.isChecked()) {
            chosen.add(1);
        }
        if (tuesday.isChecked()) {
            chosen.add(2);
        }
        if (wednesday.isChecked()) {
            chosen.add(3);
        }
        if (thursday.isChecked()) {
            chosen.add(4);
        }
        if (friday.isChecked()) {
            chosen.add(5);
        }
        if (saturday.isChecked()) {
            chosen.add(6);
        }

        return chosen;
    }
}
