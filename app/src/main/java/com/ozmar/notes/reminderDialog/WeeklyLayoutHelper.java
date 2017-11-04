package com.ozmar.notes.reminderDialog;

import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ToggleButton;

import com.ozmar.notes.R;

import org.joda.time.LocalDate;

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
    private FrequencyPickerFlagHelper mFlagHelper;

    public WeeklyLayoutHelper(View view, Button doneButton, FrequencyPickerFlagHelper flagHelper) {
        this(view, doneButton, null, flagHelper);
    }

    public WeeklyLayoutHelper(View view, Button doneButton, List<Integer> daysLastSelected, FrequencyPickerFlagHelper flagHelper) {
        this.view = view;
        this.doneButton = doneButton;
        this.sunday = view.findViewById(R.id.toggleButtonSunday);
        this.monday = view.findViewById(R.id.toggleButtonMonday);
        this.tuesday = view.findViewById(R.id.toggleButtonTuesday);
        this.wednesday = view.findViewById(R.id.toggleButtonWednesday);
        this.thursday = view.findViewById(R.id.toggleButtonThursday);
        this.friday = view.findViewById(R.id.toggleButtonFriday);
        this.saturday = view.findViewById(R.id.toggleButtonSaturday);
        this.mFlagHelper = flagHelper;

        sunday.setOnCheckedChangeListener(this);
        monday.setOnCheckedChangeListener(this);
        tuesday.setOnCheckedChangeListener(this);
        wednesday.setOnCheckedChangeListener(this);
        thursday.setOnCheckedChangeListener(this);
        friday.setOnCheckedChangeListener(this);
        saturday.setOnCheckedChangeListener(this);

        setUpStartingDaysChecked(daysLastSelected);
    }

    private void setUpStartingDaysChecked(List<Integer> daysLastSelected) {
        if (daysLastSelected == null) {
            setCheckedDay(LocalDate.now().getDayOfWeek());
        } else {
            for (Integer day : daysLastSelected) {
                setCheckedDay(day);
            }
        }


    }

    private void setCheckedDay(int dayOfWeek) {
        switch (dayOfWeek) {
            case 1:
                monday.setChecked(true);
                break;
            case 2:
                tuesday.setChecked(true);
                break;
            case 3:
                wednesday.setChecked(true);
                break;
            case 4:
                thursday.setChecked(true);
                break;
            case 5:
                friday.setChecked(true);
                break;
            case 6:
                saturday.setChecked(true);
                break;
            case 7:
                sunday.setChecked(true);
                break;
        }
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
        if (isChecked) {
            if (mFlagHelper.getBooleanResult()) {
                doneButton.setEnabled(true);
            }
            buttonsChecked++;
        } else {
            buttonsChecked--;
            if (buttonsChecked == 0) {
                doneButton.setEnabled(false);
            }
        }
    }

    public int getCurrentDaysChecked() {
        return buttonsChecked;
    }

    public List<Integer> getCheckedButtons() {
        List<Integer> chosen = new ArrayList<>();

        // Use ISO Standard for day numbering
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
