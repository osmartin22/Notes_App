package com.ozmar.notes.reminderDialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import com.ozmar.notes.FrequencyChoices;
import com.ozmar.notes.Preferences;
import com.ozmar.notes.R;
import com.ozmar.notes.Reminder;
import com.ozmar.notes.utils.FormatUtils;
import com.ozmar.notes.utils.ReminderUtils;

import org.joda.time.DateTime;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;


public class ReminderDialogFragment extends DialogFragment
        implements DatePickerFragment.OnDatePickedListener, TimePickerFragment.OnTimePickedListener,
        FrequencyPickerFragment.onFrequencyPickedListener {

    private final DateTime dateTimeNow = DateTime.now();
    private int year, month, day;
    private int hour, minute;

    private NDSpinner dateSpinner, timeSpinner, frequencySpinner;
    private ReminderAdapter dateSpinnerAdapter, timeSpinnerAdapter;
    private ReminderFrequencyAdapter frequencySpinnerAdapter;

    private final String[] dateArray = new String[5], timeArray = new String[5];
    private int currentDateSelection = 0, currentTimeSelection = 0;

    private String[] frequencyArray = new String[6];
    private boolean dateSpinnerTouched = false;
    private boolean timeSpinnerTouched = false;
    private boolean frequencySpinnerTouched = false;

    private Context mContext;
    private Preferences preferences;
    private OnReminderPickedListener myCallback;


    private DateTime chosenDateTime;
    private FrequencyChoices chosenFrequency;


    public interface OnReminderPickedListener {
        void onReminderPicked(Reminder reminder);

        void onReminderDelete();
    }

    public ReminderDialogFragment() {

    }

    public static ReminderDialogFragment newInstance(Reminder reminder) {
        Bundle bundle = new Bundle();
        bundle.putParcelable("Reminder", reminder);
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
        Reminder receivedReminder = null;
        if (bundle != null) {
            receivedReminder = bundle.getParcelable("Reminder");
        }

        if (receivedReminder != null) {
            chosenDateTime = receivedReminder.getDateTime();
            chosenFrequency = receivedReminder.getFrequencyChoices();
        }

        View view = View.inflate(getActivity(), R.layout.reminder_dialog_layout, null);
        preferences = new Preferences(mContext);
        setUpSpinnerStrings();

        createSpinners(view);

        setSpinnerListener();
        setSpinnerPosition(chosenDateTime);

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

    private void createSpinners(View view) {
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
    }

    @Override
    public void onStart() {
        super.onStart();

        // Use a different OnClickListener than default to allow checking of user input
        final AlertDialog dialog = (AlertDialog) getDialog();
        if (dialog != null) {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {

                chosenDateTime = new DateTime(year, month, day, hour, minute);

                if (chosenDateTime.getMillis() < System.currentTimeMillis()) {
                    if (getActivity() != null) {
                        Snackbar snackBar = Snackbar.make(getActivity().findViewById(R.id.noteEditorLayout)
                                , "That Time Has Already Passed", Snackbar.LENGTH_LONG);
                        snackBar.show();
                    }

                } else {
                    if (myCallback != null) {

                        if (chosenFrequency != null) {
                            chosenDateTime = checkFrequencySelection(chosenDateTime, chosenFrequency);
                        }


                        Reminder chosenReminder = new Reminder(chosenDateTime, chosenFrequency);
                        Toast.makeText(getActivity(), chosenDateTime.toString(DateTimeFormat.mediumDateTime()), Toast.LENGTH_SHORT).show();
                        myCallback.onReminderPicked(chosenReminder);
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
        dateArray[4] = getString(R.string.pickADate);

        timeArray[0] = FormatUtils.getTimeFormat(mContext, preferences.getMorningTime());
        timeArray[1] = FormatUtils.getTimeFormat(mContext, preferences.getAfternoonTime());
        timeArray[2] = FormatUtils.getTimeFormat(mContext, preferences.getEveningTime());
        timeArray[3] = FormatUtils.getTimeFormat(mContext, preferences.getNightTime());
        timeArray[4] = getString(R.string.pickATime);

        frequencyArray = getResources().getStringArray(R.array.frequencyXMLArrayListItem);
        frequencyArray[2] += " on " + FormatUtils.getCurrentDayOfWeek(dateTimeNow.toLocalDate(), 1);
    }

    private void setYearMonthDay(@Nonnull DateTime dateTime) {
        year = dateTime.getYear();
        month = dateTime.getMonthOfYear();
        day = dateTime.getDayOfMonth();
    }

    private void setHourMinute(@Nonnull LocalTime localTime) {
        hour = localTime.getHourOfDay();
        minute = localTime.getMinuteOfHour();
    }

    private void setDateAndTimeSpinnerPosition(int datePosition, int timePosition) {
        dateSpinner.setSelection(datePosition);
        timeSpinner.setSelection(timePosition);
    }

    // TODO: Not displaying the next day properly
    // Set Spinner positions to reflect previous user reminder if available
    // or to x hours into the future
    private void setSpinnerPosition(@Nullable DateTime presetDateTime) {

        if (presetDateTime != null) {
            setDateAndTimeSpinnerPosition(4, 4);
            setYearMonthDay(presetDateTime);
            dateArray[4] = FormatUtils.getMonthDayFormatLong(presetDateTime);

            setHourMinute(presetDateTime.toLocalTime());
            timeArray[4] = FormatUtils.getTimeFormat(mContext, presetDateTime.toLocalTime());

            if (chosenFrequency != null) {
                frequencyArray[5] = FormatUtils.formatFrequencyText(mContext,
                        chosenFrequency, presetDateTime);
                frequencySpinner.setSelection(5);
            }

        } else {

            LocalTime localTime = dateTimeNow.toLocalTime();
            LocalTime futureTime = FormatUtils.roundToTime(localTime.plusHours(3), 15);

            LocalTime firstPreset = preferences.getMorningTime();
            if (localTime.isAfter(futureTime) && localTime.isBefore(firstPreset)) {
                setDateAndTimeSpinnerPosition(1, 0);

            } else if (localTime.isBefore(firstPreset)) {
                setDateAndTimeSpinnerPosition(0, 0);

            } else {
                timeSpinner.setSelection(4);
                timeArray[4] = FormatUtils.getTimeFormat(mContext, futureTime);
                setHourMinute(futureTime);
            }
        }
    }

    private void setSpinnerListener() {
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
                        break;

                    case 1:                                 // Tomorrow
                        chosenDateTime = dateTimeNow.plusDays(1);
                        break;

                    case 2:                                 // Nex Week
                        chosenDateTime = dateTimeNow.plusWeeks(1);
                        break;

                    case 3:                                 // Next Month
                        chosenDateTime = dateTimeNow.plusMonths(1);
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

                if (i != 4) {
                    currentDateSelection = i;
                    dateArray[4] = "";
                }

                if (chosenDateTime != null) {
                    setYearMonthDay(chosenDateTime);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

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
                        break;
                    case 1:
                        localTime = preferences.getAfternoonTime();
                        break;
                    case 2:
                        localTime = preferences.getEveningTime();
                        break;
                    case 3:
                        localTime = preferences.getNightTime();
                        break;
                    case 4:
                        if (timeSpinnerTouched) {
                            DialogFragment newFragment = TimePickerFragment.newInstance(hour, minute);
                            newFragment.show(getChildFragmentManager(), "timePicker");
                            timeSpinnerTouched = false;
                        }
                        break;
                }

                if (i != 4) {
                    currentTimeSelection = i;
                    timeArray[4] = "";
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

        frequencySpinner.setOnTouchListener((v, event) -> {
            v.performClick();
            frequencySpinnerTouched = true;
            return false;
        });

        frequencySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                switch (i) {
                    case 0:
                        chosenFrequency = null;
                        break;

                    case 1:
                    case 3:
                    case 4:
                        chosenFrequency = new FrequencyChoices(i - 1, null);
                        break;

                    case 2:
                        List<Integer> list = new ArrayList<>(Collections.singletonList(dateTimeNow.getDayOfWeek()));
                        chosenFrequency = new FrequencyChoices(1, list);
                        break;

                    case 5:
                        if (frequencySpinnerTouched) {
                            FrequencyPickerFragment newFragment = FrequencyPickerFragment
                                    .newInstance(chosenFrequency, year, month, day);
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
        setYearMonthDay(chosenDateTime);
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
    public void onFrequencyPicked(FrequencyChoices newFrequencyChoice) {

        if (chosenFrequency != newFrequencyChoice) {
            chosenFrequency = newFrequencyChoice;
        }

        FrequencyChoices currentChoices = chosenFrequency;
        if (currentChoices != null) {
            if (chosenDateTime == null) {
                frequencyArray[5] = FormatUtils.formatFrequencyText(mContext,
                        currentChoices, dateTimeNow);

            } else {
                frequencyArray[5] = FormatUtils.formatFrequencyText(mContext,
                        currentChoices, chosenDateTime);
            }

            frequencySpinnerAdapter.notifyDataSetChanged();
        } else {
            frequencySpinner.setSelection(0);
        }
    }

    // Force date to align with FrequencyChoice
    // i.e. User wants to reminder to repeat weekly on Monday but date chosen is a saturday,
    // change saturday to the next monday to align with repeat on Monday
    // i.e Repeat on the second Monday of a month but date chosen is the third Monday
    // Change date to the second Monday of next month to align with repeat
    @NonNull
    private DateTime checkFrequencySelection(DateTime chosenDateTime, FrequencyChoices chosenFrequency) {

        long nextReminderTime = chosenDateTime.getMillis();

        if (chosenFrequency.getRepeatType() == 1) {     // Weekly

            assert chosenFrequency.getDaysChosen() != null;
            List<Integer> daysChosen = chosenFrequency.getDaysChosen();

            if (!daysChosen.contains(dateTimeNow.getDayOfWeek())) {
                int currentDayOfWeek = chosenDateTime.getDayOfWeek();
                Collections.sort(daysChosen);
                nextReminderTime += ReminderUtils.getNextWeeklyReminderTime(daysChosen,
                        currentDayOfWeek, 1);
            }

        } else if (chosenFrequency.getRepeatType() == 2) {   // Monthly

            if (chosenFrequency.getMonthRepeatType() != 0) {
                int days = ReminderUtils.nthWeekDayOfMonth(chosenDateTime.toLocalDate(),
                        chosenFrequency.getMonthDayOfWeekToRepeat(), chosenFrequency.getMonthWeekToRepeat());

                if (chosenDateTime.getDayOfMonth() != days) {
                    nextReminderTime = ReminderUtils.getNextMonthlyReminder(chosenDateTime,
                            chosenFrequency.getRepeatEvery(), chosenFrequency.getMonthWeekToRepeat(),
                            chosenFrequency.getMonthDayOfWeekToRepeat());
                }
            }
        }

        return new DateTime(nextReminderTime);
    }
}
