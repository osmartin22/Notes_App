package com.ozmar.notes.reminderDialog;

import android.support.annotation.NonNull;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.ozmar.notes.R;
import com.ozmar.notes.utils.FormatUtils;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;


public class MonthlyLayoutHelper {
    private final RadioGroup radioGroup;
    private final RadioButton topRadioButton;
    private final RadioButton bottomRadioButton;
    private int checkedButton;
    private final LocalDate mLocalDate;

    public MonthlyLayoutHelper(@NonNull View view, @NonNull DateTime dateTime) {
        this(view, 0, dateTime);
    }

    public MonthlyLayoutHelper(@NonNull View view, int checkedButton, @NonNull DateTime dateTime) {
        this.mLocalDate = dateTime.toLocalDate();
        this.radioGroup = (RadioGroup) view;
        this.topRadioButton = radioGroup.findViewById(R.id.topRadioButton);
        this.bottomRadioButton = radioGroup.findViewById(R.id.bottomRadioButton);

        if (checkedButton == 0 || checkedButton == -1) {
            topRadioButton.setChecked(true);
        } else {
            bottomRadioButton.setChecked(true);
        }

        this.checkedButton = checkedButton;
        setTextOfSecondRadioButton();
        setRadioGroupListener();
    }

    @NonNull
    public View getMainView() {
        return radioGroup;
    }

    private void setTextOfSecondRadioButton() {
        String nthDay = bottomRadioButton.getText().toString() + " " + FormatUtils.formatNthWeekOfMonth(mLocalDate);
        bottomRadioButton.setText(nthDay);
    }

    private void setRadioGroupListener() {
        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            switch (checkedId) {
                case R.id.topRadioButton:
                    checkedButton = 0;
                    break;

                case R.id.bottomRadioButton:
                    checkedButton = 1;
                    break;
            }
        });
    }

    public void setViewEnabled(boolean flag) {
        topRadioButton.setEnabled(flag);
        bottomRadioButton.setEnabled(flag);
    }

    public int getCheckedButton() {
        return checkedButton;
    }

    public int getWeekToRepeat() {
        return FormatUtils.getNthWeekOfMonth(mLocalDate);
    }

    public int getDayOfWeekToRepeat() {
        return mLocalDate.getDayOfWeek();
    }
}
