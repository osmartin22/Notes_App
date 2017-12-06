package com.ozmar.notes.reminderDialog;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.ozmar.notes.FrequencyChoices;
import com.ozmar.notes.R;
import com.ozmar.notes.databinding.ReminderFrequencyPickerBinding;
import com.ozmar.notes.utils.FormatUtils;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import java.util.List;


// TODO: Choosing one of the bottom spinner selections for the first time does not show the view
// Only after selecting it again or switching to another selection
// Setting the selection from a previous FrequencyChoice works fine
public class FrequencyPickerFragment extends DialogFragment implements TextWatcher, View.OnClickListener,
        DatePickerFragment.OnDatePickedListener {

    private static final float TRANSPARENCY_ON = 0.5f;
    private static final float TRANSPARENCY_OFF = 1f;
    private static final int REPEAT_DAILY = 1;
    private static final int REPEAT_WEEKLY = 2;
    private static final int REPEAT_MONTHLY = 3;
    private static final int REPEAT_YEARLY = 4;

    private int timeUnitNumber = 1;

    private EditText numberOfEventsEditText;
    private TextView eventsTextView;
    private TextView calendarTextView;
    private View eventsMainView;

    private WeeklyLayoutHelper weeklyHelper;
    private MonthlyLayoutHelper monthlyHelper;

    private int year, month, day;

    private DateTime mDateTimeRepeatTo;
    private FrequencyChoices choices;
    private FrequencyPickerFlagHelper mFlagHelper;
    private ReminderFrequencyPickerBinding mBinding;

    private onFrequencyPickedListener myCallback;


    public interface onFrequencyPickedListener {
        void onFrequencyPicked(FrequencyChoices choices);
    }

    private void onAttachParentFragment(Fragment fragment) {
        try {
            myCallback = (onFrequencyPickedListener) fragment;
        } catch (ClassCastException e) {
            throw new ClassCastException(fragment.toString() + " must implement onFrequencyPickedListener.");
        }
    }

    public static FrequencyPickerFragment newInstance(FrequencyChoices choices, int year, int month, int day) {
        Bundle bundle = new Bundle();
        bundle.putInt("Year", year);
        bundle.putInt("Month", month);
        bundle.putInt("Day", day);
        bundle.putParcelable("Frequency Choices", choices);
        FrequencyPickerFragment fragment = new FrequencyPickerFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        onAttachParentFragment(getParentFragment());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        Bundle bundle = getArguments();
        if (bundle != null) {
            LocalDate nowTime = LocalDate.now();
            choices = bundle.getParcelable("Frequency Choices");
            year = bundle.getInt("Year", nowTime.getYear());
            month = bundle.getInt("Month", nowTime.getMonthOfYear());
            day = bundle.getInt("Day", nowTime.getDayOfMonth());
            mDateTimeRepeatTo = new DateTime(year, month, day, 0, 0);
        }


        mBinding = DataBindingUtil.inflate(inflater, R.layout.reminder_frequency_picker, container, false);

        SimpleAdapter adapter = new SimpleAdapter(getContext(), android.R.layout.simple_spinner_item,
                getResources().getStringArray(R.array.bottomArrayListItem));
        mBinding.bottomSpinner.setAdapter(adapter);

        mBinding.everyNumberEditText.setOnClickListener(this);
        mBinding.everyNumberEditText.addTextChangedListener(this);

        mFlagHelper = new FrequencyPickerFlagHelper();

        setUpFrequencyChoices(choices);
        setUpSpinnerListeners();
        setUpSwitchListener();
        setUpDoneListener();

        return mBinding.getRoot();
    }

    private void setUpFrequencyChoices(FrequencyChoices choices) {
        if (choices != null) {
            if (choices.getRepeatType() == REPEAT_WEEKLY) {
                switchToWeekly();
            } else if (choices.getRepeatType() == REPEAT_MONTHLY) {
                switchToMonthly();
            }

            mBinding.everyNumberEditText.setText(String.valueOf(choices.getRepeatEvery()));
            mBinding.topSpinner.setSelection(choices.getRepeatType() - 1);

            if (choices.getRepeatToDate() != 0) {
                setUpCalendarTextView();
                mBinding.bottomSpinner.setSelection(1);

                calendarTextView.setText(FormatUtils.getMonthDayFormatShort(choices.getRepeatToDate()));
                mBinding.bottomViewSwitcher.setVisibility(View.VISIBLE);

            } else if (choices.getRepeatEvents() != 0) {
                setUpEventsTextViews();
                mBinding.bottomSpinner.setSelection(2);
                numberOfEventsEditText.setText(String.valueOf(choices.getRepeatEvents()));
                mBinding.bottomViewSwitcher.setVisibility(View.VISIBLE);
            }
        }
    }

    private void switchToWeekly() {
        if (weeklyHelper == null) {
            if (choices != null) {
                weeklyHelper = new WeeklyLayoutHelper(mBinding.typeViewSwitcher,
                        mBinding.reminderDoneButton, choices.getDaysChosen(), mFlagHelper);
            } else {
                weeklyHelper = new WeeklyLayoutHelper(mBinding.typeViewSwitcher,
                        mBinding.reminderDoneButton, mFlagHelper);
            }
        }
        showNextView(mBinding.typeViewSwitcher, weeklyHelper.getMainView());
    }

    private void switchToMonthly() {
        if (monthlyHelper == null) {
            if (choices != null) {
                monthlyHelper = new MonthlyLayoutHelper(mBinding.typeViewSwitcher,
                        choices.getMonthRepeatType(), mDateTimeRepeatTo);
            } else {
                monthlyHelper = new MonthlyLayoutHelper(mBinding.typeViewSwitcher, mDateTimeRepeatTo);
            }
        }
        showNextView(mBinding.typeViewSwitcher, monthlyHelper.getMainView());
    }

    private void setUpSpinnerListeners() {
        mBinding.topSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:     // Daily View
                    case 3:     // Yearly View
                        mBinding.typeViewSwitcher.setVisibility(View.GONE);
                        break;

                    case 1:     // Weekly View
                        switchToWeekly();
                        break;

                    case 2:     // Monthly View
                        switchToMonthly();
                        break;
                }

                setDoneButtonEnabled();
                setTimeUnitString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        mBinding.bottomSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        mBinding.bottomViewSwitcher.setVisibility(View.GONE);
                        break;
                    case 1:
                        if (calendarTextView == null) {
                            setUpCalendarTextView();
                            calendarTextView.setText(FormatUtils.getMonthDayFormatShort(mDateTimeRepeatTo.plusMonths(1)));
                        }
                        showNextView((ViewSwitcher) mBinding.bottomViewSwitcher, calendarTextView);
                        break;
                    case 2:
                        if (eventsMainView == null) {
                            setUpEventsTextViews();
                        }
                        showNextView((ViewSwitcher) mBinding.bottomViewSwitcher, eventsMainView);
                        break;
                }

                setDoneButtonEnabled();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void showNextView(@NonNull ViewSwitcher viewSwitcher, @NonNull View view) {
        viewSwitcher.setVisibility(View.VISIBLE);
        if (viewSwitcher.getNextView() == view) {
            viewSwitcher.showNext();
        }
    }

    private void setUpSwitchListener() {
        mBinding.reminderSwitch.setChecked(true);
        mBinding.reminderSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                setDoneButtonEnabled();
                setViewEnabled(true, TRANSPARENCY_OFF);

            } else {
                mBinding.reminderDoneButton.setEnabled(true);
                setViewEnabled(false, TRANSPARENCY_ON);
            }
        });
    }

    private void setViewEnabled(boolean flag, float transparency) {
        mBinding.reminderDialogContent.setAlpha(transparency);
        mBinding.topSpinner.setAlpha(transparency);

        if (monthlyHelper != null) {
            monthlyHelper.setViewEnabled(flag);
        }
        if (weeklyHelper != null) {
            weeklyHelper.setViewEnabled(flag);
        }
        if (numberOfEventsEditText != null) {
            numberOfEventsEditText.setEnabled(flag);
        }

        mBinding.everyNumberEditText.setEnabled(flag);
        mBinding.reminderDialogContent.setEnabled(flag);
        mBinding.topSpinner.setEnabled(flag);
        mBinding.bottomSpinner.setEnabled(flag);
    }

    private void setUpDoneListener() {
        mBinding.reminderDoneButton.setOnClickListener(v -> {

            if (mBinding.reminderSwitch.isChecked()) {
                List<Integer> list = null;
                int monthRepeatType = -1;
                long repeatToDate = 0;
                int repeatEvents = 0;
                int repeatForever = 0;
                int weekToRepeat = 0;
                int dayOfWeekToRepeat = 0;

                int topSpinnerPosition = mBinding.topSpinner.getSelectedItemPosition() + 1;

                if (topSpinnerPosition == REPEAT_WEEKLY) {
                    list = weeklyHelper.getCheckedButtons();

                } else if (topSpinnerPosition == REPEAT_MONTHLY) {
                    monthRepeatType = monthlyHelper.getCheckedButton();
                    weekToRepeat = monthlyHelper.getWeekToRepeat();
                    dayOfWeekToRepeat = monthlyHelper.getDayOfWeekToRepeat();
                }

                switch (mBinding.bottomSpinner.getSelectedItemPosition()) {
                    case 0:
                        repeatForever = 1;
                        break;
                    case 1:
                        repeatToDate = mDateTimeRepeatTo.getMillis();
                        break;
                    case 2:
                        repeatEvents = Integer.parseInt(numberOfEventsEditText.getText().toString());
                        break;
                }

                int repeatEvery = Integer.parseInt(mBinding.everyNumberEditText.getText().toString());
                choices = new FrequencyChoices(topSpinnerPosition, repeatEvery, repeatForever, repeatToDate,
                        repeatEvents, monthRepeatType, weekToRepeat, dayOfWeekToRepeat, list);

            } else {
                choices = null;
            }

            if (myCallback != null) {
                myCallback.onFrequencyPicked(choices);
            }
            dismiss();
        });
    }

    private void setUpCalendarTextView() {
        calendarTextView = mBinding.bottomViewSwitcher.findViewById(R.id.calendarTextView);
        calendarTextView.setOnClickListener(v -> {
            DialogFragment newFragment = DatePickerFragment.newInstance(year, month - 1, day);
            newFragment.show(getChildFragmentManager(), "datePicker");
        });
    }

    private void setUpEventsTextViews() {
        eventsMainView = mBinding.bottomViewSwitcher.findViewById(R.id.eventsView);
        eventsTextView = mBinding.bottomViewSwitcher.findViewById(R.id.eventsTextView);
        numberOfEventsEditText = mBinding.bottomViewSwitcher.findViewById(R.id.numberOfEventsEditText);

        numberOfEventsEditText.setOnClickListener(FrequencyPickerFragment.this);

        numberOfEventsEditText.addTextChangedListener(FrequencyPickerFragment.this);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        if (s.toString().equals("0")) {
            s.replace(0, 1, "1");
        }

        if (s == mBinding.everyNumberEditText.getText()) {
            mFlagHelper.topEmpty = mBinding.everyNumberEditText.getText().toString().isEmpty();
            if (!mFlagHelper.topEmpty) {
                timeUnitNumber = Integer.parseInt(s.toString());
                setTimeUnitString();
            }

        } else if (s == numberOfEventsEditText.getText()) {
            mFlagHelper.bottomEmpty = numberOfEventsEditText.getText().toString().isEmpty();
            if (!mFlagHelper.bottomEmpty) {
                eventsTextView.setText(getResources().getQuantityString(R.plurals.event,
                        Integer.parseInt(s.toString())));
            }
        }
        setDoneButtonEnabled();
    }

    private void setDoneButtonEnabled() {
        // Case for weekly view
        if (mBinding.topSpinner.getSelectedItemPosition() == 1) {
            if (weeklyHelper.getCurrentDaysChecked() != 0) {
                checkIfEditTextsAreEmpty();
            } else {
                mBinding.reminderDoneButton.setEnabled(false);
            }

            // Case for the other views
        } else {
            checkIfEditTextsAreEmpty();
        }
    }

    private void checkIfEditTextsAreEmpty() {
        if (mBinding.bottomSpinner.getSelectedItemPosition() == 2) {
            mFlagHelper.bottomUsed = true;
            mBinding.reminderDoneButton.setEnabled(!mFlagHelper.topEmpty && !mFlagHelper.bottomEmpty);
        } else {
            mFlagHelper.bottomUsed = false;
            mBinding.reminderDoneButton.setEnabled(!mFlagHelper.topEmpty);
        }
    }

    // TODO: Highlight text when user first clicks on text
    @Override
    public void onClick(View v) {
        if (v == mBinding.everyNumberEditText && !mBinding.everyNumberEditText.hasFocus()) {
            mBinding.everyNumberEditText.selectAll();
        } else if (v == numberOfEventsEditText && !numberOfEventsEditText.hasFocus()) {
            numberOfEventsEditText.selectAll();
        }
    }

    private void setTimeUnitString() {
        switch (mBinding.topSpinner.getSelectedItemPosition()) {
            case 0:
                mBinding.typeTextView.setText(getResources().getQuantityString(R.plurals.day, timeUnitNumber));
                break;
            case 1:
                mBinding.typeTextView.setText(getResources().getQuantityString(R.plurals.week, timeUnitNumber));
                break;
            case 2:
                mBinding.typeTextView.setText(getResources().getQuantityString(R.plurals.month, timeUnitNumber));
                break;
            case 3:
                mBinding.typeTextView.setText(getResources().getQuantityString(R.plurals.year, timeUnitNumber));
                break;
        }
    }

    @Override
    public void onDatePicked(int year, int month, int day) {
        this.year = year;
        this.month = month;
        this.day = day;

        mDateTimeRepeatTo = new DateTime(year, month, day, 0, 0);
        calendarTextView.setText(FormatUtils.getMonthDayFormatShort(mDateTimeRepeatTo));
    }

    @Override
    public void onDateCancel() {

    }
}
