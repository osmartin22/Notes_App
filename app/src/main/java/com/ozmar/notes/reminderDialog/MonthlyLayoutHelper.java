package com.ozmar.notes.reminderDialog;

import android.support.annotation.IdRes;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.ozmar.notes.R;
import com.ozmar.notes.utils.FormatUtils;

import org.joda.time.LocalDate;


public class MonthlyLayoutHelper {
    private final RadioGroup radioGroup;
    private final RadioButton topRadioButton;
    private final RadioButton bottomRadioButton;
    private int checkedButton;

    public MonthlyLayoutHelper(View view) {
        this(view, 0);
    }

    public MonthlyLayoutHelper(View view, int checkedButton) {
        this.radioGroup = (RadioGroup) view;
        this.topRadioButton = radioGroup.findViewById(R.id.topRadioButton);
        this.bottomRadioButton = radioGroup.findViewById(R.id.bottomRadioButton);

        if (checkedButton == 0) {
            topRadioButton.setChecked(true);
        } else {
            bottomRadioButton.setChecked(true);
        }

        this.checkedButton = checkedButton;
        setTextOfSecondRadioButton();
        setRadioGroupListener();
    }

    public View getMainView() {
        return radioGroup;
    }

    private void setTextOfSecondRadioButton() {
        String nthDay = bottomRadioButton.getText().toString() + " " + FormatUtils.formatNthDayOfMonthItIs(LocalDate.now());
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
