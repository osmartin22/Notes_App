package com.ozmar.notes.reminderDialog;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import com.ozmar.notes.Preferences;
import com.ozmar.notes.R;
import com.ozmar.notes.ReminderAdapter;
import com.ozmar.notes.utils.FormatUtils;

import org.joda.time.DateTime;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormat;


public class ReminderDialogFragment extends DialogFragment
        implements DatePickerFragment.OnDatePickedListener, TimePickerFragment.OnTimePickedListener {

    private NDSpinner dateSpinner;
    private NDSpinner timeSpinner;
    private NDSpinner reminderSpinner;

    private DateTime dateTime = DateTime.now();

    private int hour;
    private int minute;
    private int day;
    private int month;
    private int year;

    private DateTime dateTimeNow = DateTime.now();

    private String[] dateArray = new String[5];
    private String[] timeArray = new String[5];
    private ReminderAdapter timeSpinnerAdapter;
    private ReminderAdapter dateSpinnerAdapter;
    private ReminderAdapter reminderSpinnerAdapter;

    private boolean dateSpinnerTouched = false;
    private boolean timeSpinnerTouched = false;
    private boolean reminderSpinnerTouched = false;

    private int previousDateSelection = 0;
    private int previousTimeSelection = 0;
    private int previousReminderSelection = 0;

    // TODO: Set spinner for reminder/create dialog
    // TODO: Fix reminder being set when only text changes
    // TODO: On cancel, show previous selection
        // If it was custom, set to custom selection with desired time
    // TODO: Round futureTime to nearest quarter at least
    // TODO: Pass reminderTime to fragment to set as initial values
        // Make sure when creating spinner listener, number not overwritten
        // Or set initial values after creating spinner and set to custom position(4)



    private Preferences preferences;

    private OnReminderPickedListener myCallback;

    public interface OnReminderPickedListener {
        void onReminderPicked(DateTime dateTime);

        void onReminderDelete();
    }

    public ReminderDialogFragment() {

    }

    public static ReminderDialogFragment newInstance() {
        return new ReminderDialogFragment();
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

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = View.inflate(getActivity(), R.layout.reminder_dialog_layout, null);
        preferences = new Preferences(getContext());
        setUpSpinnerStringDisplay();

        dateSpinner = view.findViewById(R.id.spinnerDate);
        timeSpinner = view.findViewById(R.id.spinnerTime);
        reminderSpinner = view.findViewById(R.id.spinnerReminder);

        String[] dropDownArray = getActivity().getResources().getStringArray(R.array.dateXMLArray);
        dateSpinnerAdapter = new ReminderAdapter(getContext(), android.R.layout.simple_spinner_item,
                dateArray, dropDownArray, 0);
        dateSpinner.setAdapter(dateSpinnerAdapter);

        dropDownArray = getActivity().getResources().getStringArray(R.array.timeXMLArray);

        timeSpinnerAdapter = new ReminderAdapter(getContext(), android.R.layout.simple_spinner_item,
                timeArray, dropDownArray, 1);
        timeSpinner.setAdapter(timeSpinnerAdapter);

        // TODO: Set spinner for reminder frequency


        setSpinnerListener();
//        setSpinnerPosition();

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
                            myCallback.onReminderPicked(dateTime);
                        }
                        dialog.dismiss();
                    }
                }
            });
        }
    }

    private void setUpSpinnerStringDisplay() {
        dateArray[0] = FormatUtils.getMonthDayFormat(dateTimeNow);
        dateArray[1] = FormatUtils.getMonthDayFormat(dateTimeNow, dateTimeNow.plusDays(1));
        dateArray[2] = FormatUtils.getMonthDayFormat(dateTimeNow, dateTimeNow.plusWeeks(1));
        dateArray[3] = FormatUtils.getMonthDayFormat(dateTimeNow, dateTimeNow.plusMonths(1));
        dateArray[4] = "Pick A Date...";      // TODO: Set with reminderDate if available

        timeArray[0] = FormatUtils.getTimeFormat(getContext(), preferences.getMorningTime());
        timeArray[1] = FormatUtils.getTimeFormat(getContext(), preferences.getAfternoonTime());
        timeArray[2] = FormatUtils.getTimeFormat(getContext(), preferences.getEveningTime());
        timeArray[3] = FormatUtils.getTimeFormat(getContext(), preferences.getNightTime());
        timeArray[4] = "Pick A Time...";      // TODO: Set with reminderTime if available
    }

    private void setSpinnerPosition() {
        LocalTime localTime = LocalTime.now();
        LocalTime futureTime = localTime.plusHours(3);

        // TODO: Round to nearest minute (maybe 10/15/30 minute marks)
        LocalTime firstPreset = preferences.getMorningTime();
        if (localTime.isAfter(futureTime) || localTime.isBefore(firstPreset)) {
            timeSpinner.setSelection(0);
        } else {
            timeSpinner.setSelection(4);
            hour = futureTime.getHourOfDay();
            minute = futureTime.getMinuteOfHour();
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
                        year = dateTime.getYear();
                        month = dateTime.getMonthOfYear();
                        day = dateTime.getDayOfMonth();
                        previousDateSelection = 0;
                        break;

                    case 1:                                 // Tomorrow
                        futureTime = dateTime.plusDays(1);
                        previousDateSelection = 1;
                        break;

                    case 2:                                 // Nex Week (7 Days)
                        futureTime = dateTime.plusDays(7);
                        previousDateSelection = 2;
                        break;

                    case 3:                                 // Next Month
                        futureTime = dateTime.plusMonths(1);
                        previousDateSelection = 3;
                        break;

                    case 4:                                 // Date chosen
                        if (dateSpinnerTouched) {
                            DialogFragment newFragment = DatePickerFragment.newInstance(year, month, day);
                            newFragment.show(getChildFragmentManager(), "datePicker");
//                            previousDateSelection = 4;
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
                        previousTimeSelection = 0;
                        break;
                    case 1:
                        localTime = preferences.getAfternoonTime();
                        previousTimeSelection = 1;
                        break;
                    case 2:
                        localTime = preferences.getEveningTime();
                        previousTimeSelection = 2;
                        break;
                    case 3:
                        localTime = preferences.getNightTime();
                        previousTimeSelection = 3;
                        break;
                    case 4:
                        if (timeSpinnerTouched) {
                            DialogFragment newFragment = TimePickerFragment.newInstance(hour, minute);
                            newFragment.show(getChildFragmentManager(), "timePicker");
//                            previousTimeSelection = 4;
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
        reminderSpinner.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                reminderSpinnerTouched = true;
                return false;
            }
        });

        reminderSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                switch (i) {
                    case 0:     // Does Not Repeat

                        break;
                    case 1:     // Daily
                        break;

                    case 2:     // Weekly
                        break;
                    case 3:     // Monthly

                        break;
                    case 4:     // Yearly
                        break;

                    case 5:     // Show Custom Reminder Picker

                        break;
                }

                // TODO: Return chosen repetition for reminder
                if (i == 5) {
                    // Show custom reminder selection
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
//        timeSpinner.setSelection(previousTimeSelection);
    }

    @Override
    public void onDatePicked(int year, int month, int day) {
        dateArray[4] = FormatUtils.getMonthDayFormat(dateTimeNow, new DateTime().withDate(year, month, day));
        dateSpinnerAdapter.notifyDataSetChanged();

        this.year = year;
        this.month = month;
        this.day = day;
    }

    @Override
    public void onDateCancel() {
//        dateSpinner.setSelection(previousDateSelection);
    }
}