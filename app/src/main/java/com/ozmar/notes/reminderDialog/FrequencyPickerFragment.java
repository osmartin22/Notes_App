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

import java.util.List;


public class FrequencyPickerFragment extends DialogFragment implements TextWatcher, View.OnClickListener,
        DatePickerFragment.OnDatePickedListener {

    private static final float TRANSPARENCY_ON = 0.5f;
    private static final float TRANSPARENCY_OFF = 1;

    private int timeUnitNumber = 1;

    // These Views are always shown
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

    private MonthlyLayoutHelper monthlyHelper;
    private WeeklyLayoutHelper weeklyHelper;

    private int year, month, day;

    private boolean topEmpty = false, bottomEmpty = false;

    private FrequencyChoices choices;

    onFrequencyPickedListener myCallback;

    DateTime mDateTime;


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
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        Bundle bundle = getArguments();
        mDateTime = DateTime.now();
        choices = bundle.getParcelable("Frequency Choices");
        year = bundle.getInt("Year", mDateTime.getYear());
        month = bundle.getInt("Month", mDateTime.getMonthOfYear());
        day = bundle.getInt("Day", mDateTime.getDayOfMonth());

        // These views will always be shown
        View mainView = inflater.inflate(R.layout.reminder_frequency_picker, container, false);
        contentView = mainView.findViewById(R.id.reminderDialogContent);
        topSpinner = mainView.findViewById(R.id.topSpinner);
        mySwitch = mainView.findViewById(R.id.reminderSwitch);
        everyNumberEditText = contentView.findViewById(R.id.everyNumberEditText);
        typeTextView = contentView.findViewById(R.id.typeTextView);
        bottomSpinner = mainView.findViewById(R.id.bottomSpinner);
        doneButton = mainView.findViewById(R.id.reminderDoneButton);

        // These views will be displayed based om spinner position
        typeViewSwitcher = contentView.findViewById(R.id.typeViewSwitcher);
        bottomViewSwitcher = contentView.findViewById(R.id.nextToBottomSpinner);

        SimpleAdapter adapter = new SimpleAdapter(getContext(), android.R.layout.simple_spinner_item,
                getResources().getStringArray(R.array.bottomArrayListItem));
        bottomSpinner.setAdapter(adapter);

        everyNumberEditText.setOnClickListener(this);
        everyNumberEditText.addTextChangedListener(this);

        // Not Sure why but I have to call showNextView() twice if I want to show the previous
        // reminder choices
        // I do not have to do this if this is a new reminder being created instead of being modified
        // Could be something having to do with ViewSwitcher
        // This should have been taken care of by just setting the necessary spinner positions
        if (choices != null) {
            if (choices.getRepeatType() == 1) {
                weeklyHelper = new WeeklyLayoutHelper(typeViewSwitcher.findViewById(R.id.repeatWeeklyLayout), doneButton, choices.getDaysChosen());
                showNextView(typeViewSwitcher, weeklyHelper.getMainView());
            } else if (choices.getRepeatType() == 2) {
                monthlyHelper = new MonthlyLayoutHelper(typeViewSwitcher.findViewById(R.id.repeatMonthlyLayout), choices.getMonthRepeatType());
                showNextView(typeViewSwitcher, monthlyHelper.getMainView());
            }
            everyNumberEditText.setText(String.valueOf(choices.getRepeatEvery()));
            topSpinner.setSelection(choices.getRepeatType());

            if (choices.getRepeatToDate() != 0) {
                bottomSpinner.setSelection(1);
                bottomViewSwitcher.setVisibility(View.VISIBLE);
                setUpCalendarTextView();
                calendarTextView.setText(FormatUtils.getMonthDayFormatShort(choices.getRepeatToDate()));

            } else if (choices.getRepeatEvents() != 0) {
                setUpEventsTextViews();
                bottomSpinner.setSelection(2);
                numberOfEventsEditText.setText(String.valueOf(choices.getRepeatEvents()));
            }
        }

        setUpSpinnerListeners();
        setUpSwitchListener();
        setUpDoneListener();

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
                        if (weeklyHelper == null) {
                            weeklyHelper = new WeeklyLayoutHelper(typeViewSwitcher
                                    .findViewById(R.id.repeatWeeklyLayout), doneButton);
                        }
                        showNextView(typeViewSwitcher, weeklyHelper.getMainView());
                        break;

                    case 2:     // Monthly View
                        if (monthlyHelper == null) {
                            monthlyHelper = new MonthlyLayoutHelper(typeViewSwitcher
                                    .findViewById(R.id.repeatMonthlyLayout));
                        }
                        showNextView(typeViewSwitcher, monthlyHelper.getMainView());
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
                            calendarTextView.setText(FormatUtils.getMonthDayFormatShort(mDateTime));
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

                int topSpinnerPosition = topSpinner.getSelectedItemPosition();

                if (topSpinnerPosition == 1) {
                    list = weeklyHelper.getCheckedButtons();
                } else if (topSpinnerPosition == 2) {
                    monthRepeatType = monthlyHelper.getCheckedButton();
                }

                switch (bottomSpinner.getSelectedItemPosition()) {
                    case 1:
                        repeatToDate = new DateTime(year, month, day, 0, 0, 0).getMillis();
                        break;
                    case 2:
                        repeatEvents = Integer.parseInt(numberOfEventsEditText.getText().toString());
                        break;
                }

                choices = new FrequencyChoices(topSpinnerPosition, Integer.parseInt(everyNumberEditText.getText().toString()),
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
            topEmpty = everyNumberEditText.getText().toString().isEmpty();
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
        if (topSpinner.getSelectedItemPosition() == 1) {
            if (weeklyHelper.getCurrentDaysChecked() != 0) {
                if (bottomSpinner.getSelectedItemPosition() == 2) {
                    doneButton.setEnabled(!topEmpty && !bottomEmpty);
                } else {
                    doneButton.setEnabled(!topEmpty);
                }
            } else {
                doneButton.setEnabled(false);
            }

            // Case for the other views
        } else if (bottomSpinner.getSelectedItemPosition() == 2) {
            doneButton.setEnabled(!topEmpty && !bottomEmpty);
        } else {
            doneButton.setEnabled(!topEmpty);
        }
    }

    @Override
    public void onClick(View v) {
        // TODO: Highlight text when user first clicks on text
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
        calendarTextView.setText(FormatUtils.getMonthDayFormatShort(new DateTime(year, month, day, 0, 0, 0)));
    }

    @Override
    public void onDateCancel() {

    }
}
