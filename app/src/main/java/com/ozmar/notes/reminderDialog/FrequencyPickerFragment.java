package com.ozmar.notes.reminderDialog;

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
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.ozmar.notes.FrequencyChoices;
import com.ozmar.notes.R;
import com.ozmar.notes.utils.FormatUtils;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import java.util.List;


public class FrequencyPickerFragment extends DialogFragment implements TextWatcher, View.OnClickListener,
        DatePickerFragment.OnDatePickedListener {

    private static final float TRANSPARENCY_ON = 0.5f;
    private static final float TRANSPARENCY_OFF = 1f;

    private int timeUnitNumber = 1;

    // These views are always shown
    private Switch mySwitch;
    private Spinner topSpinner, bottomSpinner;
    private Button doneButton;
    private EditText everyNumberEditText;
    private TextView typeTextView;
    private View contentView;

    // These views are shown based on user input
    private ViewSwitcher typeViewSwitcher;
    private ViewSwitcher bottomViewSwitcher;
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

        // These views will always be shown
        View mainView = inflater.inflate(R.layout.reminder_frequency_picker, container, false);
        contentView = mainView.findViewById(R.id.reminderDialogContent);
        topSpinner = mainView.findViewById(R.id.topSpinner);
        mySwitch = mainView.findViewById(R.id.reminderSwitch);
        everyNumberEditText = contentView.findViewById(R.id.everyNumberEditText);
        typeTextView = contentView.findViewById(R.id.typeTextView);
        bottomSpinner = mainView.findViewById(R.id.bottomSpinner);
        doneButton = mainView.findViewById(R.id.reminderDoneButton);

        // These views will be displayed based on spinner position
        typeViewSwitcher = contentView.findViewById(R.id.typeViewSwitcher);
        bottomViewSwitcher = contentView.findViewById(R.id.nextToBottomSpinner);

        SimpleAdapter adapter = new SimpleAdapter(getContext(), android.R.layout.simple_spinner_item,
                getResources().getStringArray(R.array.bottomArrayListItem));
        bottomSpinner.setAdapter(adapter);

        everyNumberEditText.setOnClickListener(this);
        everyNumberEditText.addTextChangedListener(this);

        mFlagHelper = new FrequencyPickerFlagHelper();

        setUpFrequencyChoices(choices);
        setUpSpinnerListeners();
        setUpSwitchListener();
        setUpDoneListener();

        return mainView;
    }

    private void setUpFrequencyChoices(FrequencyChoices choices) {
        if (choices != null) {
            if (choices.getRepeatType() == 1) {
                switchToWeekly();
            } else if (choices.getRepeatType() == 2) {
                switchToMonthly();
            }
            everyNumberEditText.setText(String.valueOf(choices.getRepeatEvery()));
            topSpinner.setSelection(choices.getRepeatType());

            if (choices.getRepeatToDate() != 0) {
                setUpCalendarTextView();
                bottomSpinner.setSelection(1);
                calendarTextView.setText(FormatUtils.getMonthDayFormatShort(choices.getRepeatToDate()));
                bottomViewSwitcher.setVisibility(View.VISIBLE);

            } else if (choices.getRepeatEvents() != 0) {
                setUpEventsTextViews();
                bottomSpinner.setSelection(2);
                numberOfEventsEditText.setText(String.valueOf(choices.getRepeatEvents()));
                bottomViewSwitcher.setVisibility(View.VISIBLE);
            }
        }
    }

    private void switchToWeekly() {
        if (weeklyHelper == null) {
            if (choices != null) {
                weeklyHelper = new WeeklyLayoutHelper(typeViewSwitcher
                        .findViewById(R.id.repeatWeeklyLayout), doneButton, choices.getDaysChosen(), mFlagHelper);
            } else {
                weeklyHelper = new WeeklyLayoutHelper(typeViewSwitcher
                        .findViewById(R.id.repeatWeeklyLayout), doneButton, mFlagHelper);
            }
        }
        showNextView(typeViewSwitcher, weeklyHelper.getMainView());
    }

    private void switchToMonthly() {
        if (monthlyHelper == null) {
            if (choices != null) {
                monthlyHelper = new MonthlyLayoutHelper(typeViewSwitcher
                        .findViewById(R.id.repeatMonthlyLayout), choices.getMonthRepeatType(), mDateTimeRepeatTo);
            } else {
                monthlyHelper = new MonthlyLayoutHelper(typeViewSwitcher
                        .findViewById(R.id.repeatMonthlyLayout), mDateTimeRepeatTo);
            }
        }
        showNextView(typeViewSwitcher, monthlyHelper.getMainView());
    }

    private void setUpSpinnerListeners() {
        topSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:     // Daily View
                    case 3:     // Yearly View
                        typeViewSwitcher.setVisibility(View.GONE);
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

        bottomSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        bottomViewSwitcher.setVisibility(View.GONE);
                        break;
                    case 1:
                        if (calendarTextView == null) {
                            setUpCalendarTextView();
                            calendarTextView.setText(FormatUtils.getMonthDayFormatShort(mDateTimeRepeatTo.plusMonths(1)));
                        }
                        showNextView(bottomViewSwitcher, calendarTextView);
                        break;
                    case 2:
                        if (eventsMainView == null) {
                            setUpEventsTextViews();
                        }
                        showNextView(bottomViewSwitcher, eventsMainView);
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
        mySwitch.setChecked(true);
        mySwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                setDoneButtonEnabled();
                setViewEnabled(true, TRANSPARENCY_OFF);

            } else {
                doneButton.setEnabled(true);
                setViewEnabled(false, TRANSPARENCY_ON);
            }
        });
    }

    private void setViewEnabled(boolean flag, float transparency) {
        contentView.setAlpha(transparency);
        topSpinner.setAlpha(transparency);

        if (monthlyHelper != null) {
            monthlyHelper.setViewEnabled(flag);
        }
        if (weeklyHelper != null) {
            weeklyHelper.setViewEnabled(flag);
        }
        if (numberOfEventsEditText != null) {
            numberOfEventsEditText.setEnabled(flag);
        }

        everyNumberEditText.setEnabled(flag);
        contentView.setEnabled(flag);
        topSpinner.setEnabled(flag);
        bottomSpinner.setEnabled(flag);
    }

    private void setUpDoneListener() {
        doneButton.setOnClickListener(v -> {

            if (mySwitch.isChecked()) {
                List<Integer> list = null;
                int monthRepeatType = -1;
                long repeatToDate = 0;
                int repeatEvents = 0;
                int repeatForever = 0;
                int weekToRepeat = 0;
                int dayOfWeekToRepeat = 0;

                int topSpinnerPosition = topSpinner.getSelectedItemPosition();

                if (topSpinnerPosition == 1) {
                    list = weeklyHelper.getCheckedButtons();
                } else if (topSpinnerPosition == 2) {
                    monthRepeatType = monthlyHelper.getCheckedButton();
                    weekToRepeat = monthlyHelper.getWeekToRepeat();
                    dayOfWeekToRepeat = monthlyHelper.getDayOfWeekToRepeat();
                }

                switch (bottomSpinner.getSelectedItemPosition()) {
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

                int repeatEvery = Integer.parseInt(everyNumberEditText.getText().toString());
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
        calendarTextView = bottomViewSwitcher.findViewById(R.id.calendarTextView);
        calendarTextView.setOnClickListener(v -> {
            DialogFragment newFragment = DatePickerFragment.newInstance(year, month - 1, day);
            newFragment.show(getChildFragmentManager(), "datePicker");
        });
    }

    private void setUpEventsTextViews() {
        eventsMainView = bottomViewSwitcher.findViewById(R.id.eventsView);
        eventsTextView = bottomViewSwitcher.findViewById(R.id.eventsTextView);
        numberOfEventsEditText = bottomViewSwitcher.findViewById(R.id.numberOfEventsEditText);

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

        if (s == everyNumberEditText.getText()) {
            mFlagHelper.topEmpty = everyNumberEditText.getText().toString().isEmpty();
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
        if (topSpinner.getSelectedItemPosition() == 1) {
            if (weeklyHelper.getCurrentDaysChecked() != 0) {
                checkIfEditTextsAreEmpty();
            } else {
                doneButton.setEnabled(false);
            }

            // Case for the other views
        } else {
            checkIfEditTextsAreEmpty();
        }
    }

    private void checkIfEditTextsAreEmpty() {
        if (bottomSpinner.getSelectedItemPosition() == 2) {
            mFlagHelper.bottomUsed = true;
            doneButton.setEnabled(!mFlagHelper.topEmpty && !mFlagHelper.bottomEmpty);
        } else {
            mFlagHelper.bottomUsed = false;
            doneButton.setEnabled(!mFlagHelper.topEmpty);
        }
    }

    // TODO: Highlight text when user first clicks on text
    @Override
    public void onClick(View v) {
        if (v == everyNumberEditText && !everyNumberEditText.hasFocus()) {
            everyNumberEditText.selectAll();
        } else if (v == numberOfEventsEditText && !numberOfEventsEditText.hasFocus()) {
            numberOfEventsEditText.selectAll();
        }
    }

    private void setTimeUnitString() {
        switch (topSpinner.getSelectedItemPosition()) {
            case 0:
                typeTextView.setText(getResources().getQuantityString(R.plurals.day, timeUnitNumber));
                break;
            case 1:
                typeTextView.setText(getResources().getQuantityString(R.plurals.week, timeUnitNumber));
                break;
            case 2:
                typeTextView.setText(getResources().getQuantityString(R.plurals.month, timeUnitNumber));
                break;
            case 3:
                typeTextView.setText(getResources().getQuantityString(R.plurals.year, timeUnitNumber));
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
