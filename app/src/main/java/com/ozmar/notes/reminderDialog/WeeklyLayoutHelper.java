package com.ozmar.notes.reminderDialog;

import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ToggleButton;

import com.ozmar.notes.R;

import java.util.ArrayList;
import java.util.List;


public class WeeklyLayoutHelper implements CompoundButton.OnCheckedChangeListener {

    private View view;
    private final ToggleButton sunday;
    private ToggleButton monday;
    private ToggleButton tuesday;
    private ToggleButton wednesday;
    private ToggleButton thursday;
    private ToggleButton friday;
    private ToggleButton saturday;

    public WeeklyLayoutHelper(View view) {
        this.view = view;
        this.sunday = view.findViewById(R.id.toggleButtonSunday);
        this.monday = view.findViewById(R.id.toggleButtonMonday);
        this.tuesday = view.findViewById(R.id.toggleButtonTuesday);
        this.wednesday = view.findViewById(R.id.toggleButtonWednesday);
        this.thursday = view.findViewById(R.id.toggleButtonThursday);
        this.friday = view.findViewById(R.id.toggleButtonFriday);
        this.saturday = view.findViewById(R.id.toggleButtonSaturday);

        this.sunday.setOnCheckedChangeListener(this);
        this.monday.setOnCheckedChangeListener(this);
        this.tuesday.setOnCheckedChangeListener(this);
        this.wednesday.setOnCheckedChangeListener(this);
        this.thursday.setOnCheckedChangeListener(this);
        this.friday.setOnCheckedChangeListener(this);
        this.saturday.setOnCheckedChangeListener(this);
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
            // Set to checked background
            Log.d("Button", checked + " Checked");
        } else {
            // Set to unchecked background
            Log.d("Button", checked + " UnChecked");
        }
    }

    public List<Boolean> getCheckedButtons() {
        List<Boolean> buttonsChecked = new ArrayList<>();

        buttonsChecked.add(monday.isChecked());
        buttonsChecked.add(tuesday.isChecked());
        buttonsChecked.add(wednesday.isChecked());
        buttonsChecked.add(thursday.isChecked());
        buttonsChecked.add(friday.isChecked());
        buttonsChecked.add(saturday.isChecked());
        buttonsChecked.add(sunday.isChecked());

        return buttonsChecked;
    }
}
