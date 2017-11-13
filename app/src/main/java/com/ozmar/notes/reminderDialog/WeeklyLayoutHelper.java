package com.ozmar.notes.reminderDialog;

import android.databinding.DataBindingUtil;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;

import com.ozmar.notes.R;
import com.ozmar.notes.databinding.RepeatWeeklyLayoutBinding;

import org.joda.time.LocalDate;

import java.util.ArrayList;
import java.util.List;


public class WeeklyLayoutHelper implements CompoundButton.OnCheckedChangeListener {

    private int buttonsChecked = 0;
    private final Button doneButton;
    private final FrequencyPickerFlagHelper mFlagHelper;

    private final RepeatWeeklyLayoutBinding mBinding;

    public WeeklyLayoutHelper(View view, Button doneButton, FrequencyPickerFlagHelper flagHelper) {
        this(view, doneButton, null, flagHelper);
    }

    public WeeklyLayoutHelper(View view, Button doneButton, List<Integer> daysLastSelected, FrequencyPickerFlagHelper flagHelper) {
        this.mBinding = DataBindingUtil.getBinding(view.findViewById(R.id.repeatWeeklyLayout));
        this.doneButton = doneButton;
        this.mFlagHelper = flagHelper;

        mBinding.toggleButtonSunday.setOnCheckedChangeListener(this);
        mBinding.toggleButtonMonday.setOnCheckedChangeListener(this);
        mBinding.toggleButtonTuesday.setOnCheckedChangeListener(this);
        mBinding.toggleButtonWednesday.setOnCheckedChangeListener(this);
        mBinding.toggleButtonThursday.setOnCheckedChangeListener(this);
        mBinding.toggleButtonFriday.setOnCheckedChangeListener(this);
        mBinding.toggleButtonSaturday.setOnCheckedChangeListener(this);

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
                mBinding.toggleButtonMonday.setChecked(true);
                break;
            case 2:
                mBinding.toggleButtonTuesday.setChecked(true);
                break;
            case 3:
                mBinding.toggleButtonWednesday.setChecked(true);
                break;
            case 4:
                mBinding.toggleButtonThursday.setChecked(true);
                break;
            case 5:
                mBinding.toggleButtonFriday.setChecked(true);
                break;
            case 6:
                mBinding.toggleButtonSaturday.setChecked(true);
                break;
            case 7:
                mBinding.toggleButtonSunday.setChecked(true);
                break;
        }
    }

    public void setViewEnabled(boolean flag) {
        mBinding.toggleButtonSunday.setEnabled(flag);
        mBinding.toggleButtonMonday.setEnabled(flag);
        mBinding.toggleButtonTuesday.setEnabled(flag);
        mBinding.toggleButtonWednesday.setEnabled(flag);
        mBinding.toggleButtonThursday.setEnabled(flag);
        mBinding.toggleButtonFriday.setEnabled(flag);
        mBinding.toggleButtonSaturday.setEnabled(flag);
    }

    public View getMainView() {
        return mBinding.getRoot();
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
        if (mBinding.toggleButtonSunday.isChecked()) {
            chosen.add(7);
        }
        if (mBinding.toggleButtonMonday.isChecked()) {
            chosen.add(1);
        }
        if (mBinding.toggleButtonTuesday.isChecked()) {
            chosen.add(2);
        }
        if (mBinding.toggleButtonWednesday.isChecked()) {
            chosen.add(3);
        }
        if (mBinding.toggleButtonThursday.isChecked()) {
            chosen.add(4);
        }
        if (mBinding.toggleButtonFriday.isChecked()) {
            chosen.add(5);
        }
        if (mBinding.toggleButtonSaturday.isChecked()) {
            chosen.add(6);
        }

        return chosen;
    }
}
