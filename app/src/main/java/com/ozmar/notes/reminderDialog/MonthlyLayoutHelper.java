package com.ozmar.notes.reminderDialog;

import android.support.annotation.IdRes;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.ozmar.notes.R;
import com.ozmar.notes.utils.FormatUtils;

import org.joda.time.DateTime;


public class MonthlyLayoutHelper {
    private RadioGroup radioGroup;
    private RadioButton topRadioButton;
    private RadioButton bottomRadioButton;
    private int checkedButton = 0;
    private DateTime dateTime = DateTime.now();

    public MonthlyLayoutHelper(View view) {
        this.radioGroup = (RadioGroup) view;
        this.topRadioButton = radioGroup.findViewById(R.id.topRadioButton);
        this.bottomRadioButton = radioGroup.findViewById(R.id.bottomRadioButton);
        setTextOfSecondRadioButton();
        setRadioGroupListener();
    }

    public View getMainView() {
        return radioGroup;
    }

    private void setTextOfSecondRadioButton() {
        String nthDay = bottomRadioButton.getText().toString() + " " + FormatUtils.formatNthDayOfMonthItIs(dateTime);
        bottomRadioButton.setText(nthDay);
    }

    public void setRadioGroupListener() {
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
                switch (checkedId) {
                    case R.id.topRadioButton:
                        checkedButton = 0;
                        break;

                    case R.id.bottomRadioButton:
                        checkedButton = 1;
                        break;
                }

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
}
