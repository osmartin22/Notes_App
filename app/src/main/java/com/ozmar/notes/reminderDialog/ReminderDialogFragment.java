package com.ozmar.notes.reminderDialog;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import com.ozmar.notes.FrequencyChoices;
import com.ozmar.notes.Preferences;
import com.ozmar.notes.R;
import com.ozmar.notes.utils.FormatUtils;

import org.joda.time.DateTime;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormat;


public class ReminderDialogFragment extends DialogFragment
        implements DatePickerFragment.OnDatePickedListener, TimePickerFragment.OnTimePickedListener,
        FrequencyPickerFragment.onFrequencyPickedListener {

    private DateTime dateTimeNow = DateTime.now();
    private DateTime chosenDateTime;
    private int year;
    private int month;
    private int day;
    private int hour;
    private int minute;

    private NDSpinner dateSpinner;
    private NDSpinner timeSpinner;
    private NDSpinner frequencySpinner;

    private String[] dateArray = new String[5];
    private String[] timeArray = new String[5];
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

    private Preferences preferences;
    private OnReminderPickedListener myCallback;

    public interface OnReminderPickedListener {
        void onReminderPicked(DateTime dateTime, int frequencyPicked, FrequencyChoices choices);

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
        choices = bundle.getParcelable("Frequency Choices");
        long reminderTimeMillis = bundle.getLong("Next Reminder Time", 0);
        if (reminderTimeMillis != 0) {
            chosenDateTime = new DateTime(reminderTimeMillis);
        }

        View view = View.inflate(getActivity(), R.layout.reminder_dialog_layout, null);
        preferences = new Preferences(getContext());
        setUpSpinnerStrings();

        dateSpinner = view.findViewById(R.id.spinnerDate);
        timeSpinner = view.findViewById(R.id.spinnerTime);
        frequencySpinner = view.findViewById(R.id.spinnerReminder);

        String[] dropDownArray = getActivity().getResources().getStringArray(R.array.dateXMLArray);
        dropDownArray[2] += " " + FormatUtils.getCurrentDayOfWeek(1);
        dateSpinnerAdapter = new ReminderAdapter(getContext(), android.R.layout.simple_spinner_item,
                dateArray, dropDownArray, 0);
        dateSpinner.setAdapter(dateSpinnerAdapter);

        dropDownArray = getActivity().getResources().getStringArray(R.array.timeXMLArray);
        timeSpinnerAdapter = new ReminderAdapter(getContext(), android.R.layout.simple_spinner_item,
                timeArray, dropDownArray, 0);
        timeSpinner.setAdapter(timeSpinnerAdapter);

        frequencySpinnerAdapter = new ReminderFrequencyAdapter(getContext(),
                android.R.layout.simple_spinner_item, frequencyArray);
        frequencySpinner.setAdapter(frequencySpinnerAdapter);

        setSpinnerListener();
        setSpinnerPosition();

        AlertDialog.Builder reminderDialog = new AlertDialog.Builder(getActivity());
        reminderDialog.setView(view);
        reminderDialog.setTitle("Add Reminder");

        reminderDialog.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });

        reminderDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });

        reminderDialog.setNeutralButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (myCallback != null) {
                    myCallback.onReminderDelete();
                }
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
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    DateTime dateTime = new DateTime(year, month, day, hour, minute);
                    if (dateTime.getMillis() < System.currentTimeMillis()) {

                        Snackbar snackBar = Snackbar.make(getActivity().findViewById(R.id.noteEditorLayout)
                                , "The Time Has Already Passed", Snackbar.LENGTH_LONG);
                        snackBar.show();

                    } else {
                        Toast.makeText(getActivity(), dateTime.toString(DateTimeFormat.mediumDateTime()), Toast.LENGTH_SHORT).show();
                        if (myCallback != null) {
                            myCallback.onReminderPicked(dateTime, currentFrequencySelection, choices);
                        }
                        dialog.dismiss();
                    }
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
        dateArray[4] = "Pick A Date...";      // TODO: Set with reminderDate if available

        timeArray[0] = FormatUtils.getTimeFormat(getContext(), preferences.getMorningTime());
        timeArray[1] = FormatUtils.getTimeFormat(getContext(), preferences.getAfternoonTime());
        timeArray[2] = FormatUtils.getTimeFormat(getContext(), preferences.getEveningTime());
        timeArray[3] = FormatUtils.getTimeFormat(getContext(), preferences.getNightTime());
        timeArray[4] = "Pick A Time...";      // TODO: Set with reminderTime if available

        frequencyArray = getActivity().getResources().getStringArray(R.array.frequencyXMLArrayListItem);
        frequencyArray[2] += " on " + FormatUtils.getCurrentDayOfWeek(1);
        // TODO: Set with reminderFrequency if available
    }

    // Set Spinner positions to reflect previous user reminder if available
    // or to x hours into the future
    private void setSpinnerPosition() {
        if (chosenDateTime != null) {
            dateSpinner.setSelection(4);
            year = chosenDateTime.getYear();
            month = chosenDateTime.getMonthOfYear();
            day = chosenDateTime.getDayOfMonth();
            dateArray[4] = FormatUtils.getMonthDayFormatLong(chosenDateTime);

            timeSpinner.setSelection(4);
            hour = chosenDateTime.getHourOfDay();
            minute = chosenDateTime.getMinuteOfHour();
            timeArray[4] = FormatUtils.getTimeFormat(getContext(), new LocalTime(chosenDateTime.getMillis()));
        } else {

            LocalTime localTime = LocalTime.now();
            LocalTime futureTime = FormatUtils.roundToTime(localTime.plusHours(3), 15);

            LocalTime firstPreset = preferences.getMorningTime();
            if (localTime.isAfter(futureTime) || localTime.isBefore(firstPreset)) {
                dateSpinner.setSelection(1);
                timeSpinner.setSelection(0);
            } else {
                timeSpinner.setSelection(4);
                timeArray[4] = FormatUtils.getTimeFormat(getContext(), futureTime);
                hour = futureTime.getHourOfDay();
                minute = futureTime.getMinuteOfHour();
            }
        }
    }

    private void setSpinnerListener() {
        //------------------------------------------------------------------------------------------
        // Date Spinner Listeners
        //------------------------------------------------------------------------------------------
        dateSpinner.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                dateSpinnerTouched = true;
                return false;
            }
        });

        dateSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                DateTime futureTime = null;
                switch (i) {
                    case 0:                                 // Today
                        year = dateTimeNow.getYear();
                        month = dateTimeNow.getMonthOfYear();
                        day = dateTimeNow.getDayOfMonth();
                        currentDateSelection = 0;
                        dateArray[4] = "";
                        break;

                    case 1:                                 // Tomorrow
                        futureTime = dateTimeNow.plusDays(1);
                        currentDateSelection = 1;
                        dateArray[4] = "";
                        break;

                    case 2:                                 // Nex Week (7 Days)
                        futureTime = dateTimeNow.plusDays(7);
                        currentDateSelection = 2;
                        dateArray[4] = "";
                        break;

                    case 3:                                 // Next Month
                        futureTime = dateTimeNow.plusMonths(1);
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

                if (futureTime != null) {
                    year = futureTime.getYear();
                    month = futureTime.getMonthOfYear();
                    day = futureTime.getDayOfMonth();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });


        //------------------------------------------------------------------------------------------
        // Time Spinner Listeners
        //------------------------------------------------------------------------------------------
        timeSpinner.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                timeSpinnerTouched = true;
                return false;
            }
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
        frequencySpinner.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                frequencySpinnerTouched = true;
                return false;
            }
        });

        frequencySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                // TODO: Have selections for other positions that are not the FrequencyPicker
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
        timeArray[4] = FormatUtils.getTimeFormat(getContext(), new LocalTime(hour, minute));

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
        this.choices = choices;

        if (choices == null) {
            this.choices = null;
        } else {
            frequencyArray[5] = FormatUtils.formatFrequencyText(getContext(), choices);
            frequencySpinnerAdapter.notifyDataSetChanged();
        }
    }
}
