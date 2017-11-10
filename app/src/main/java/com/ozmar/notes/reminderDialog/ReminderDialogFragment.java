package com.ozmar.notes.reminderDialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import com.ozmar.notes.FrequencyChoices;
import com.ozmar.notes.Preferences;
import com.ozmar.notes.R;
import com.ozmar.notes.utils.FormatUtils;
import com.ozmar.notes.utils.ReminderUtils;

import org.joda.time.DateTime;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class ReminderDialogFragment extends DialogFragment
        implements DatePickerFragment.OnDatePickedListener, TimePickerFragment.OnTimePickedListener,
        FrequencyPickerFragment.onFrequencyPickedListener {

    private final DateTime dateTimeNow = DateTime.now();
    private DateTime chosenDateTime;
    private int year;
    private int month;
    private int day;
    private int hour;
    private int minute;

    private NDSpinner dateSpinner;
    private NDSpinner timeSpinner;
    private NDSpinner frequencySpinner;

    private final String[] dateArray = new String[5];
    private final String[] timeArray = new String[5];
    private String[] frequencyArray = new String[6];

    private ReminderAdapter timeSpinnerAdapter;
    private ReminderAdapter dateSpinnerAdapter;
    private ReminderFrequencyAdapter frequencySpinnerAdapter;

    private boolean dateSpinnerTouched = false;
    private boolean timeSpinnerTouched = false;
    private boolean frequencySpinnerTouched = false;

    private int currentDateSelection = 0;
    private int currentTimeSelection = 0;
    private int currentFrequencySelection = 0;

    private FrequencyChoices choices = null;

    private Context mContext;
    private Preferences preferences;
    private OnReminderPickedListener myCallback;

    public interface OnReminderPickedListener {
        void onReminderPicked(DateTime dateTime, FrequencyChoices choices);

        void onReminderDelete();
    }

    public ReminderDialogFragment() {

    }

    public static ReminderDialogFragment newInstance(FrequencyChoices choices, long nextReminderTime) {
        Bundle bundle = new Bundle();
        bundle.putParcelable("Frequency Choices", choices);
        bundle.putLong("Next Reminder Time", nextReminderTime);
        ReminderDialogFragment fragment = new ReminderDialogFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
        try {
            myCallback = (OnReminderPickedListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement OnReminderPickedListener.");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle bundle = getArguments();
        if (bundle != null) {
            choices = bundle.getParcelable("Frequency Choices");
            long reminderTimeMillis = bundle.getLong("Next Reminder Time", 0);
            if (reminderTimeMillis != 0) {
                chosenDateTime = new DateTime(reminderTimeMillis);
            }
        }

        View view = View.inflate(getActivity(), R.layout.reminder_dialog_layout, null);
        preferences = new Preferences(mContext);
        setUpSpinnerStrings();

        dateSpinner = view.findViewById(R.id.spinnerDate);
        timeSpinner = view.findViewById(R.id.spinnerTime);
        frequencySpinner = view.findViewById(R.id.spinnerReminder);

        String[] dropDownArray = getResources().getStringArray(R.array.dateXMLArray);
        dropDownArray[2] += " " + FormatUtils.getCurrentDayOfWeek(dateTimeNow.toLocalDate(), 1);
        dateSpinnerAdapter = new ReminderAdapter(mContext, android.R.layout.simple_spinner_item,
                dateArray, dropDownArray, 0);
        dateSpinner.setAdapter(dateSpinnerAdapter);

        dropDownArray = getResources().getStringArray(R.array.timeXMLArray);
        timeSpinnerAdapter = new ReminderAdapter(mContext, android.R.layout.simple_spinner_item,
                timeArray, dropDownArray, 0);
        timeSpinner.setAdapter(timeSpinnerAdapter);

        frequencySpinnerAdapter = new ReminderFrequencyAdapter(mContext,
                R.layout.multi_line_spinner_item, frequencyArray);
        frequencySpinner.setAdapter(frequencySpinnerAdapter);

        setSpinnerListener();
        setSpinnerPosition(choices);

        AlertDialog.Builder reminderDialog = new AlertDialog.Builder(mContext);
        reminderDialog.setView(view);
        reminderDialog.setTitle(getString(R.string.reminderDialogTitle));

        reminderDialog.setPositiveButton(getString(R.string.reminderDialogPositive),
                (dialogInterface, i) -> {

                });

        reminderDialog.setNegativeButton(getString(R.string.reminderDialogNegative),
                (dialogInterface, i) -> {

                });

        reminderDialog.setNeutralButton(getString(R.string.reminderDialogNeutral), (
                dialogInterface, i) -> {
            if (myCallback != null) {
                myCallback.onReminderDelete();
            }
        });

        return reminderDialog.create();
    }

    @Override
    public void onStart() {
        super.onStart();

        // Use a different OnClickListener than default to allow checking of user input
        final AlertDialog dialog = (AlertDialog) getDialog();
        if (dialog != null) {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {

                DateTime dateTime = new DateTime(year, month, day, hour, minute);
                if (dateTime.getMillis() < System.currentTimeMillis()) {
                    if (getActivity() != null) {
                        Snackbar snackBar = Snackbar.make(getActivity().findViewById(R.id.noteEditorLayout)
                                , "That Time Has Already Passed", Snackbar.LENGTH_LONG);
                        snackBar.show();
                    }

                } else {
                    if (myCallback != null) {

                        if (currentFrequencySelection != 5) { // User selected a preset Frequency
                            choices = makeFrequencyChoiceForPreset(currentFrequencySelection);
                        }

                        if (choices != null) {
                            dateTime = checkFrequencySelection(dateTime, choices);
                        }


                        Toast.makeText(getActivity(), dateTime.toString(DateTimeFormat.mediumDateTime()), Toast.LENGTH_SHORT).show();
                        myCallback.onReminderPicked(dateTime, choices);
                    }
                    dialog.dismiss();
                }
            });
        }
    }

    // Set up strings that will be used for the list items view of each spinner
    private void setUpSpinnerStrings() {
        dateArray[0] = FormatUtils.getMonthDayFormatLong(dateTimeNow);
        dateArray[1] = FormatUtils.getMonthDayFormatLong(dateTimeNow.plusDays(1));
        dateArray[2] = FormatUtils.getMonthDayFormatLong(dateTimeNow.plusWeeks(1));
        dateArray[3] = FormatUtils.getMonthDayFormatLong(dateTimeNow.plusMonths(1));
        dateArray[4] = "Pick A Date...";

        timeArray[0] = FormatUtils.getTimeFormat(mContext, preferences.getMorningTime());
        timeArray[1] = FormatUtils.getTimeFormat(mContext, preferences.getAfternoonTime());
        timeArray[2] = FormatUtils.getTimeFormat(mContext, preferences.getEveningTime());
        timeArray[3] = FormatUtils.getTimeFormat(mContext, preferences.getNightTime());
        timeArray[4] = "Pick A Time...";

        frequencyArray = getResources().getStringArray(R.array.frequencyXMLArrayListItem);
        frequencyArray[2] += " on " + FormatUtils.getCurrentDayOfWeek(dateTimeNow.toLocalDate(), 1);
    }

    // Set Spinner positions to reflect previous user reminder if available
    // or to x hours into the future
    private void setSpinnerPosition(FrequencyChoices choices) {
        if (choices != null) {
            frequencyArray[5] = FormatUtils.formatFrequencyText(mContext, choices, chosenDateTime);
            frequencySpinner.setSelection(5);
        }

        if (chosenDateTime != null) {
            dateSpinner.setSelection(4);
            year = chosenDateTime.getYear();
            month = chosenDateTime.getMonthOfYear();
            day = chosenDateTime.getDayOfMonth();
            dateArray[4] = FormatUtils.getMonthDayFormatLong(chosenDateTime);

            timeSpinner.setSelection(4);
            hour = chosenDateTime.getHourOfDay();
            minute = chosenDateTime.getMinuteOfHour();
            timeArray[4] = FormatUtils.getTimeFormat(mContext, new LocalTime(chosenDateTime.getMillis()));
        } else {

            LocalTime localTime = LocalTime.now();
            LocalTime futureTime = FormatUtils.roundToTime(localTime.plusHours(3), 15);

            LocalTime firstPreset = preferences.getMorningTime();
            if (localTime.isAfter(futureTime) && localTime.isBefore(firstPreset)) {
                dateSpinner.setSelection(1);
                timeSpinner.setSelection(0);

            } else if (localTime.isBefore(firstPreset)) {
                dateSpinner.setSelection(0);
                timeSpinner.setSelection(0);
            } else {
                timeSpinner.setSelection(4);
                timeArray[4] = FormatUtils.getTimeFormat(mContext, futureTime);
                hour = futureTime.getHourOfDay();
                minute = futureTime.getMinuteOfHour();
            }
        }
    }

    private void setSpinnerListener() {
        //------------------------------------------------------------------------------------------
        // Date Spinner Listeners
        //------------------------------------------------------------------------------------------
        dateSpinner.setOnTouchListener((v, event) -> {
            v.performClick();
            dateSpinnerTouched = true;
            return false;
        });

        dateSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                switch (i) {
                    case 0:                                 // Today
                        chosenDateTime = dateTimeNow;
                        currentDateSelection = 0;
                        dateArray[4] = "";
                        break;

                    case 1:                                 // Tomorrow
                        chosenDateTime = dateTimeNow.plusDays(1);
                        currentDateSelection = 1;
                        dateArray[4] = "";
                        break;

                    case 2:                                 // Nex Week
                        chosenDateTime = dateTimeNow.plusWeeks(1);
                        currentDateSelection = 2;
                        dateArray[4] = "";
                        break;

                    case 3:                                 // Next Month
                        chosenDateTime = dateTimeNow.plusMonths(1);
                        currentDateSelection = 3;
                        dateArray[4] = "";
                        break;

                    case 4:                                 // Date chosen
                        if (dateSpinnerTouched) {
                            // Subtract 1 from month since CalendarPicker starts at month 0
                            DialogFragment newFragment = DatePickerFragment.newInstance(year, month - 1, day);
                            newFragment.show(getChildFragmentManager(), "datePicker");
                            dateSpinnerTouched = false;
                        }
                        break;
                }

                if (chosenDateTime != null) {
                    year = chosenDateTime.getYear();
                    month = chosenDateTime.getMonthOfYear();
                    day = chosenDateTime.getDayOfMonth();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });


        //------------------------------------------------------------------------------------------
        // Time Spinner Listeners
        //------------------------------------------------------------------------------------------
        timeSpinner.setOnTouchListener((v, event) -> {
            v.performClick();
            timeSpinnerTouched = true;
            return false;
        });


        timeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                LocalTime localTime = null;
                switch (i) {
                    case 0:
                        localTime = preferences.getMorningTime();
                        currentTimeSelection = 0;
                        timeArray[4] = "";
                        break;
                    case 1:
                        localTime = preferences.getAfternoonTime();
                        currentTimeSelection = 1;
                        timeArray[4] = "";
                        break;
                    case 2:
                        localTime = preferences.getEveningTime();
                        currentTimeSelection = 2;
                        timeArray[4] = "";
                        break;
                    case 3:
                        localTime = preferences.getNightTime();
                        currentTimeSelection = 3;
                        timeArray[4] = "";
                        break;
                    case 4:
                        if (timeSpinnerTouched) {
                            DialogFragment newFragment = TimePickerFragment.newInstance(hour, minute);
                            newFragment.show(getChildFragmentManager(), "timePicker");
                            timeSpinnerTouched = false;
                        }
                        break;
                }

                if (localTime != null) {
                    hour = localTime.getHourOfDay();
                    minute = localTime.getMinuteOfHour();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });


        //------------------------------------------------------------------------------------------
        // Reminder Spinner Listeners
        //------------------------------------------------------------------------------------------
        frequencySpinner.setOnTouchListener((v, event) -> {
            v.performClick();
            frequencySpinnerTouched = true;
            return false;
        });

        frequencySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                switch (i) {
                    case 0:     // Does Not Repeat
                        currentFrequencySelection = 0;
                        break;
                    case 1:     // Daily
                        currentFrequencySelection = 1;
                        break;

                    case 2:     // Weekly
                        currentFrequencySelection = 2;
                        break;
                    case 3:     // Monthly
                        currentFrequencySelection = 3;
                        break;
                    case 4:     // Yearly
                        currentFrequencySelection = 4;
                        break;

                    case 5:     // Show Custom Reminder Picker
                        currentFrequencySelection = 5;
                        if (frequencySpinnerTouched) {
                            FrequencyPickerFragment newFragment =
                                    FrequencyPickerFragment.newInstance(choices, year, month, day);
                            newFragment.show(getChildFragmentManager(), "frequencyPicker");
                            newFragment.setCancelable(false);
                            frequencySpinnerTouched = false;
                        }
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    @Override
    public void onTimePicked(int hour, int minute) {
        timeArray[4] = FormatUtils.getTimeFormat(mContext, new LocalTime(hour, minute));

        timeSpinnerAdapter.notifyDataSetChanged();

        this.hour = hour;
        this.minute = minute;
    }

    @Override
    public void onTimeCancel() {
        if (timeArray[4].isEmpty()) {
            timeSpinner.setSelection(currentTimeSelection);
        } else {
            timeSpinner.setSelection(4);
        }
    }

    @Override
    public void onDatePicked(int year, int month, int day) {
        dateArray[4] = FormatUtils.getMonthDayFormatLong(new DateTime().withDate(year, month, day));
        dateSpinnerAdapter.notifyDataSetChanged();

        chosenDateTime = new DateTime(year, month, day, hour, minute);

        this.year = year;
        this.month = month;
        this.day = day;
    }

    @Override
    public void onDateCancel() {
        if (dateArray[4].isEmpty()) {
            dateSpinner.setSelection(currentDateSelection);
        } else {
            dateSpinner.setSelection(4);
        }
    }

    @Override
    public void onFrequencyPicked(FrequencyChoices choices) {

        if (this.choices != choices) {
            this.choices = choices;
        }

        if (choices != null) {
            if (chosenDateTime == null) {
                frequencyArray[5] = FormatUtils.formatFrequencyText(mContext, choices, dateTimeNow);
            } else {
                frequencyArray[5] = FormatUtils.formatFrequencyText(mContext, choices, chosenDateTime);
            }

            frequencySpinnerAdapter.notifyDataSetChanged();
        } else {
            frequencySpinner.setSelection(0);
        }
    }

    private FrequencyChoices makeFrequencyChoiceForPreset(int currentFrequencySelection) {
        FrequencyChoices choices;
        switch (currentFrequencySelection) {
            case 0:     // Does not repeat
            default:
                choices = null;
                break;

            case 1:     // Daily
            case 3:     // Monthly
            case 4:     // Yearly
                choices = new FrequencyChoices(currentFrequencySelection - 1, null);
                break;

            case 2:     // Weekly
                List<Integer> list = new ArrayList<>(Collections.singletonList(dateTimeNow.getDayOfWeek()));
                choices = new FrequencyChoices(1, list);
                break;
        }
        return choices;
    }

    // Force date to align with FrequencyChoice
    // i.e. User wants to reminder to repeat weekly on Monday but date chosen is a saturday,
    // change saturday to the next monday to align with repeat on Monday
    // i.e Repeat on the second Monday of a month but date chosen is the third Monday
    // Change date to the second Monday of next month to align with repeat
    private DateTime checkFrequencySelection(@NonNull DateTime chosenDateTime, @NonNull FrequencyChoices choices) {

        long nextReminderTime = chosenDateTime.getMillis();

        if (choices.getRepeatType() == 1) {     // Weekly

            assert choices.getDaysChosen() != null;
            List<Integer> daysChosen = choices.getDaysChosen();

            if (!daysChosen.contains(dateTimeNow.getDayOfWeek())) {
                int currentDayOfWeek = chosenDateTime.getDayOfWeek();
                Collections.sort(daysChosen);
                nextReminderTime += ReminderUtils.getNextWeeklyReminderTime(daysChosen, currentDayOfWeek, 1);
            }

        } else if (choices.getRepeatType() == 2) {   // Monthly

            int days = ReminderUtils.nthWeekDayOfMonth(chosenDateTime.toLocalDate(),
                    choices.getMonthDayOfWeekToRepeat(), choices.getMonthWeekToRepeat());

            if (chosenDateTime.getDayOfMonth() != days) {
                nextReminderTime = ReminderUtils.getNextMonthlyReminder(chosenDateTime, choices.getRepeatEvery(),
                        choices.getMonthWeekToRepeat(), choices.getMonthDayOfWeekToRepeat());
            }
        }

        return new DateTime(nextReminderTime);
    }
}
