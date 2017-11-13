package com.ozmar.notes.reminderDialog;

import android.databinding.DataBindingUtil;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.RadioGroup;

import com.ozmar.notes.R;
import com.ozmar.notes.databinding.RepeatMonthlyLayoutBinding;
import com.ozmar.notes.utils.FormatUtils;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;


public class MonthlyLayoutHelper {
    private int checkedButton;
    private final LocalDate mLocalDate;
    private final RepeatMonthlyLayoutBinding mBinding;

    public MonthlyLayoutHelper(@NonNull View view, @NonNull DateTime dateTime) {
        this(view, 0, dateTime);
    }

    public MonthlyLayoutHelper(@NonNull View view, int checkedButton, @NonNull DateTime dateTime) {

        mBinding = DataBindingUtil.getBinding(view.findViewById(R.id.repeatMonthlyLayout));

        this.mLocalDate = dateTime.toLocalDate();

        if (checkedButton == 0 || checkedButton == -1) {
            mBinding.topRadioButton.setChecked(true);
        } else {
            mBinding.bottomRadioButton.setChecked(true);
        }

        this.checkedButton = checkedButton;
        setTextOfSecondRadioButton();
        setRadioGroupListener();
    }

    @NonNull
    public View getMainView() {
        return mBinding.getRoot();
    }

    private void setTextOfSecondRadioButton() {
        String nthDay = mBinding.bottomRadioButton.getText().toString() + " " + FormatUtils.formatNthWeekOfMonth(mLocalDate);
        mBinding.bottomRadioButton.setText(nthDay);
    }

    private void setRadioGroupListener() {
        RadioGroup radioGroup = (RadioGroup) mBinding.getRoot();
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
        mBinding.topRadioButton.setEnabled(flag);
        mBinding.bottomRadioButton.setEnabled(flag);
    }

    public int getCheckedButton() {
        return checkedButton;
    }

    public int getWeekToRepeat() {
        return FormatUtils.getNthWeekOfMonth(mLocalDate.getDayOfMonth());
    }

    public int getDayOfWeekToRepeat() {
        return mLocalDate.getDayOfWeek();
    }
}
