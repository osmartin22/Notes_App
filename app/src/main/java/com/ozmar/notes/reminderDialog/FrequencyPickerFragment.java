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
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.ozmar.notes.FrequencyChoices;
import com.ozmar.notes.R;
import com.ozmar.notes.utils.FormatUtils;

import org.joda.time.DateTime;

import java.util.List;


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
    private ViewSwitcher typeViewSwitcher;

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

    private FrequencyChoices choices;

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
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        Bundle bundle = getArguments();
        choices = bundle.getParcelable("Frequency Choices");
        DateTime dateTime = DateTime.now();
        year = bundle.getInt("Year", dateTime.getYear());
        month = bundle.getInt("Month", dateTime.getMonthOfYear());
        day = bundle.getInt("Day", dateTime.getDayOfMonth());

        View mainView = inflater.inflate(R.layout.reminder_frequency_picker, container, false);
        mySwitch = mainView.findViewById(R.id.reminderSwitch);
        topSpinner = mainView.findViewById(R.id.topSpinner);
        bottomSpinner = mainView.findViewById(R.id.bottomSpinner);
        doneButton = mainView.findViewById(R.id.reminderDoneButton);
        mySwitch = mainView.findViewById(R.id.reminderSwitch);

        contentView = mainView.findViewById(R.id.reminderDialogContent);
        numberEditText = contentView.findViewById(R.id.everyNumberEditText);
        timeUnitTextView = contentView.findViewById(R.id.timeUnitTextView);
        typeViewSwitcher = contentView.findViewById(R.id.viewSwitcher);

        bottomViewSwitcher = contentView.findViewById(R.id.nextToBottomSpinner);
        numberOfEventsEditText = bottomViewSwitcher.findViewById(R.id.numberOfEventsEditText);
        eventsTextView = bottomViewSwitcher.findViewById(R.id.eventsTextView);
        calendarButton = bottomViewSwitcher.findViewById(R.id.calendarButton);
        eventsMainView = bottomViewSwitcher.findViewById(R.id.eventsView);

        calendarButton.setText(FormatUtils.getMonthDayFormatShort(dateTime));

        if (choices != null) {
            weeklyHelper = new WeeklyLayoutHelper(typeViewSwitcher.findViewById(R.id.repeatWeeklyLayout), doneButton, choices.getDaysChosen());
            monthlyHelper = new MonthlyLayoutHelper(typeViewSwitcher.findViewById(R.id.repeatMonthlyLayout), choices.getMonthRepeatType());
        } else {
            weeklyHelper = new WeeklyLayoutHelper(typeViewSwitcher.findViewById(R.id.repeatWeeklyLayout), doneButton);
            monthlyHelper = new MonthlyLayoutHelper(typeViewSwitcher.findViewById(R.id.repeatMonthlyLayout));
        }

        SimpleAdapter adapter = new SimpleAdapter(getContext(), android.R.layout.simple_spinner_item,
                getResources().getStringArray(R.array.bottomArrayListItem));
        bottomSpinner.setAdapter(adapter);

        numberEditText.setOnClickListener(this);
        numberOfEventsEditText.setOnClickListener(this);

        numberEditText.addTextChangedListener(this);
        numberOfEventsEditText.addTextChangedListener(this);

        // Not Sure why but I have to call showNextView() twice if I want to show the previous
        // reminder choices
        // I do not have to do this if this is a new reminder being created instead of being modified
        // Could be something having to do with ViewSwitcher
        // This should have been taken care of by just setting the necessary spinner positions
        if (choices != null) {
            if (choices.getRepeatType() == 1) {
                showNextView(typeViewSwitcher, weeklyHelper.getMainView());
            } else if (choices.getRepeatType() == 2) {
                showNextView(typeViewSwitcher, monthlyHelper.getMainView());
            }
            numberEditText.setText(String.valueOf(choices.getRepeatEvery()));
            topSpinner.setSelection(choices.getRepeatType());

            if (choices.getRepeatToDate() != 0) {
                bottomSpinner.setSelection(1);
                bottomViewSwitcher.setVisibility(View.VISIBLE);
                calendarButton.setVisibility(View.VISIBLE);
                calendarButton.setText(FormatUtils.getMonthDayFormatShort(choices.getRepeatToDate()));

            } else if (choices.getRepeatEvents() != 0) {
                bottomSpinner.setSelection(2);
                bottomViewSwitcher.setVisibility(View.VISIBLE);
                numberOfEventsEditText.setVisibility(View.VISIBLE);
                numberOfEventsEditText.setText(String.valueOf(choices.getRepeatEvents()));
            }
        }

        setUpSpinnerListeners();
        setUpSwitchListener();
        setUpDoneListener();
        setUpCalendarListener();

        return mainView;
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
                        showNextView(typeViewSwitcher, weeklyHelper.getMainView());
                        break;

                    case 2:     // Monthly View
                        showNextView(typeViewSwitcher, monthlyHelper.getMainView());
                        break;
                }

                topSpinnerPosition = position;
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
                        showNextView(bottomViewSwitcher, calendarButton);
                        break;
                    case 2:
                        showNextView(bottomViewSwitcher, eventsMainView);
                        break;
                }

                bottomSpinnerPosition = position;
                setDoneButtonEnabled();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void showNextView(ViewSwitcher viewSwitcher, View view) {
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
        monthlyHelper.setViewEnabled(flag);
        weeklyHelper.setViewEnabled(flag);
        numberEditText.setEnabled(flag);
        contentView.setEnabled(flag);
        topSpinner.setEnabled(flag);
        bottomSpinner.setEnabled(flag);
        numberOfEventsEditText.setEnabled(flag);
    }

    private void setUpDoneListener() {
        doneButton.setOnClickListener(v -> {

            if (mySwitch.isChecked()) {
                List<Integer> list = null;
                int monthRepeatType = -1;
                long repeatToDate = 0;
                int repeatEvents = 0;

                if (topSpinnerPosition == 1) {
                    list = weeklyHelper.getCheckedButtons();
                } else if (topSpinnerPosition == 2) {
                    monthRepeatType = monthlyHelper.getCheckedButton();
                }

                switch (bottomSpinnerPosition) {
                    case 1:
                        repeatToDate = new DateTime(year, month, day, 0, 0, 0).getMillis();
                        break;
                    case 2:
                        repeatEvents = Integer.parseInt(numberOfEventsEditText.getText().toString());
                        break;
                }

                choices = new FrequencyChoices(topSpinnerPosition, Integer.parseInt(numberEditText.getText().toString()),
                        repeatToDate, repeatEvents, monthRepeatType, list);
            } else {
                choices = null;
            }

            if (myCallback != null) {
                myCallback.onFrequencyPicked(choices);
            }
            dismiss();
        });
    }

    private void setUpCalendarListener() {
        calendarButton.setOnClickListener(v -> {
            DialogFragment newFragment = DatePickerFragment.newInstance(year, month - 1, day);
            newFragment.show(getChildFragmentManager(), "datePicker");
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
        setDoneButtonEnabled();
    }

    private void setDoneButtonEnabled() {
        // Case for weekly view
        if (topSpinnerPosition == 1) {
            if (weeklyHelper.getCurrentDaysChecked() != 0) {
                if (bottomSpinnerPosition == 2) {
                    doneButton.setEnabled(!topEmpty && !bottomEmpty);
                } else {
                    doneButton.setEnabled(!topEmpty);
                }
            } else {
                doneButton.setEnabled(false);
            }

            // Case for the other views
        } else if (bottomSpinnerPosition == 2) {
            doneButton.setEnabled(!topEmpty && !bottomEmpty);
        } else {
            doneButton.setEnabled(!topEmpty);
        }
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
