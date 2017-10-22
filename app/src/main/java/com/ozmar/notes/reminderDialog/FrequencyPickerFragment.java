package com.ozmar.notes.reminderDialog;

import android.os.Bundle;
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
import android.widget.CompoundButton;
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


public class FrequencyPickerFragment extends DialogFragment implements TextWatcher, View.OnClickListener,
        DatePickerFragment.OnDatePickedListener {

    private static final float TRANSPARENCY_ON = 0.5f;
    private static final float TRANSPARENCY_OFF = 1;

    private int topSpinnerPosition;
    private int bottomSpinnerPosition;

    private int timeUnitNumber = 1;

    private Switch mySwitch;
    private Spinner topSpinner;
    private Spinner bottomSpinner;
    private Button doneButton;
    private EditText numberEditText;
    private TextView timeUnitTextView;

    private View contentView;
    private ViewSwitcher viewSwitcher;

    private ViewSwitcher bottomViewSwitcher;
    private EditText numberOfEventsEditText;
    private TextView eventsTextView;
    private TextView calendarButton;
    private View eventsMainView;

    private MonthlyLayoutHelper monthlyHelper;
    private WeeklyLayoutHelper weeklyHelper;

    private int year;
    private int month;
    private int day;

    private boolean topEmpty = false;
    private boolean bottomEmpty = false;

    onFrequencyPickedListener myCallback;

    public interface onFrequencyPickedListener {
        void onFrequencyPicked(FrequencyChoices choices);
    }

    public void onAttachParentFragment(Fragment fragment) {
        try {
            myCallback = (onFrequencyPickedListener) fragment;
        } catch (ClassCastException e) {
            throw new ClassCastException(fragment.toString() + " must implement onFrequencyPickedListener.");
        }
    }

    public static FrequencyPickerFragment newInstance(int year, int month, int day) {
        Bundle bundle = new Bundle();
        bundle.putInt("Year", year);
        bundle.putInt("Month", month);
        bundle.putInt("Day", day);
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
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View mainView = inflater.inflate(R.layout.reminder_frequency_picker, container, false);
        mySwitch = mainView.findViewById(R.id.reminderSwitch);
        topSpinner = mainView.findViewById(R.id.topSpinner);
        bottomSpinner = mainView.findViewById(R.id.bottomSpinner);
        doneButton = mainView.findViewById(R.id.reminderDoneButton);
        mySwitch = mainView.findViewById(R.id.reminderSwitch);

        contentView = mainView.findViewById(R.id.reminderDialogContent);
        numberEditText = contentView.findViewById(R.id.everyNumberEditText);
        timeUnitTextView = contentView.findViewById(R.id.timeUnitTextView);
        viewSwitcher = contentView.findViewById(R.id.viewSwitcher);

        bottomViewSwitcher = contentView.findViewById(R.id.nextToBottomSpinner);
        numberOfEventsEditText = bottomViewSwitcher.findViewById(R.id.numberOfEventsEditText);
        eventsTextView = bottomViewSwitcher.findViewById(R.id.eventsTextView);
        calendarButton = bottomViewSwitcher.findViewById(R.id.calendarButton);
        eventsMainView = bottomViewSwitcher.findViewById(R.id.eventsView);

        weeklyHelper = new WeeklyLayoutHelper(viewSwitcher.findViewById(R.id.repeatWeeklyLayout), doneButton);
        monthlyHelper = new MonthlyLayoutHelper(viewSwitcher.findViewById(R.id.repeatMonthlyLayout));

        SimpleAdapter adapter = new SimpleAdapter(getContext(), android.R.layout.simple_spinner_item,
                getResources().getStringArray(R.array.bottomArrayListItem));
        bottomSpinner.setAdapter(adapter);

        numberEditText.setOnClickListener(this);
        numberOfEventsEditText.setOnClickListener(this);

        numberEditText.addTextChangedListener(this);
        numberOfEventsEditText.addTextChangedListener(this);

        setUpDataPassedFromBundle();
        setUpSpinnerListeners();
        setUpSwitchListener();
        setUpDoneListener();
        setUpCalendarListener();
        return mainView;
    }

    private void setUpDataPassedFromBundle() {
        LocalDate localDate = LocalDate.now();
        Bundle bundle = getArguments();
        year = bundle.getInt("Year", localDate.getYear());
        month = bundle.getInt("Month", localDate.getMonthOfYear() - 1);
        day = bundle.getInt("Day", localDate.getDayOfMonth());
    }

    private void setUpSpinnerListeners() {
        topSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:     // Daily View
                        viewSwitcher.setVisibility(View.GONE);
                        break;

                    case 1:     // Weekly View
                        viewSwitcher.setVisibility(View.VISIBLE);
                        if (viewSwitcher.getNextView() == weeklyHelper.getMainView()) {
                            viewSwitcher.showNext();
                        }
                        break;

                    case 2:     // Monthly View
                        viewSwitcher.setVisibility(View.VISIBLE);
                        if (viewSwitcher.getNextView() == monthlyHelper.getMainView()) {
                            viewSwitcher.showNext();
                        }
                        break;

                    case 3:     // Yearly View
                        viewSwitcher.setVisibility(View.GONE);
                        break;
                }
                topSpinnerPosition = position;
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
                        bottomViewSwitcher.setVisibility(View.VISIBLE);
                        if (bottomViewSwitcher.getNextView() == calendarButton) {
                            bottomViewSwitcher.showNext();
                        }
                        break;
                    case 2:
                        bottomViewSwitcher.setVisibility(View.VISIBLE);
                        if (bottomViewSwitcher.getNextView() == eventsMainView) {
                            bottomViewSwitcher.showNext();
                        }
                        break;
                }
                bottomSpinnerPosition = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void setUpSwitchListener() {
        mySwitch.setChecked(true);
        mySwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    if (topEmpty || bottomEmpty) {
                        doneButton.setEnabled(false);
                    } else {
                        doneButton.setEnabled(true);
                    }
                    setViewEnabled(true, TRANSPARENCY_OFF);

                } else {
                    doneButton.setEnabled(true);
                    setViewEnabled(false, TRANSPARENCY_ON);
                }
            }
        });
    }

    private void setViewEnabled(boolean flag, float transparency) {
        contentView.setAlpha(transparency);
        topSpinner.setAlpha(transparency);
        monthlyHelper.setViewEnabled(flag);
        weeklyHelper.setViewEnabled(flag);
        numberEditText.setEnabled(flag);
        contentView.setEnabled(flag);
        topSpinner.setEnabled(flag);
        bottomSpinner.setEnabled(flag);
        numberOfEventsEditText.setEnabled(flag);
    }

    private void setUpDoneListener() {
        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                FrequencyChoices choices = null;
                if (mySwitch.isChecked()) {
                    choices = new FrequencyChoices();
                    choices.setRepeatType(topSpinnerPosition);
                    choices.setRepeatTypeHowOften(Integer.parseInt(numberEditText.getText().toString()));

                    if (topSpinnerPosition == 1) {
                        choices.setDaysChosen(weeklyHelper.getCheckedButtons());
                    } else if (topSpinnerPosition == 2) {
                        choices.setMonthRepeatType(monthlyHelper.getCheckedButton());
                    }

                    switch (bottomSpinnerPosition) {
                        case 1:
                            choices.setRepeatToSpecificDate(new DateTime(year, month, day, 0, 0, 0).getMillis());
                            break;
                        case 2:
                            choices.setHowManyRepeatEvents(Integer.parseInt(numberOfEventsEditText.getText().toString()));
                            break;
                    }
                }

                if (myCallback != null) {
                    myCallback.onFrequencyPicked(choices);
                }
                dismiss();
            }
        });
    }

    private void setUpCalendarListener() {
        calendarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment newFragment = DatePickerFragment.newInstance(year, month - 1, day);
                newFragment.show(getChildFragmentManager(), "datePicker");
            }
        });
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

        if (s == numberEditText.getText()) {
            topEmpty = numberEditText.getText().toString().isEmpty();
            if (!topEmpty) {
                timeUnitNumber = Integer.parseInt(s.toString());
                setTimeUnitString();
            }

        } else if (s == numberOfEventsEditText.getText()) {
            bottomEmpty = numberOfEventsEditText.getText().toString().isEmpty();
            if (!bottomEmpty) {
                eventsTextView.setText(getResources().getQuantityString(R.plurals.event, Integer.parseInt(s.toString())));
            }
        }

        doneButton.setEnabled(!topEmpty && !bottomEmpty);
    }

    @Override
    public void onClick(View v) {
        // TODO: Highlight text when user first clicks on text
        if (v == numberEditText && !numberEditText.hasFocus()) {
            numberEditText.selectAll();
        } else if (v == numberOfEventsEditText && !numberOfEventsEditText.hasFocus()) {
            numberOfEventsEditText.selectAll();
        }
    }

    private void setTimeUnitString() {
        switch (topSpinnerPosition) {
            case 0:
                timeUnitTextView.setText(getResources().getQuantityString(R.plurals.day, timeUnitNumber));
                break;
            case 1:
                timeUnitTextView.setText(getResources().getQuantityString(R.plurals.week, timeUnitNumber));
                break;
            case 2:
                timeUnitTextView.setText(getResources().getQuantityString(R.plurals.month, timeUnitNumber));
                break;
            case 3:
                timeUnitTextView.setText(getResources().getQuantityString(R.plurals.year, timeUnitNumber));
                break;
        }
    }

    @Override
    public void onDatePicked(int year, int month, int day) {
        this.year = year;
        this.month = month;
        this.day = day;
        calendarButton.setText(FormatUtils.getMonthDayFormatShort(new DateTime(year, month, day, 0, 0, 0)));
    }

    @Override
    public void onDateCancel() {

    }
}
