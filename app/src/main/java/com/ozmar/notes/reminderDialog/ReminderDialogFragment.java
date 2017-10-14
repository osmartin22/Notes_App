package com.ozmar.notes.reminderDialog;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import com.ozmar.notes.R;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;


public class ReminderDialogFragment extends DialogFragment
        implements DatePickerFragment.OnDatePickedListener, TimePickerFragment.OnTimePickedListener {

    private NDSpinner spinnerDate;
    private NDSpinner spinnerTime;
    private NDSpinner spinnerReminder;

    private DateTime dateTime = DateTime.now();

    private int hour;
    private int minute;
    private int day;
    private int month;
    private int year;

    OnReminderPickedListener myCallback;

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
        spinnerDate = view.findViewById(R.id.spinnerDate);
        spinnerTime = view.findViewById(R.id.spinnerTime);
        spinnerReminder = view.findViewById(R.id.spinnerReminder);

        setSpinnerListener();

        AlertDialog.Builder reminderDialog = new AlertDialog.Builder(getActivity());
        reminderDialog.setView(view);
        reminderDialog.setTitle("Add Reminder");

        reminderDialog.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                DateTime dateTime = new DateTime(year, month, day, hour, minute);

                Toast.makeText(getActivity(), dateTime.toString(DateTimeFormat.mediumDateTime()), Toast.LENGTH_SHORT).show();

                if (myCallback != null) {
                    myCallback.onReminderPicked(dateTime);
                }
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


    private void setSpinnerListener() {

        spinnerDate.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                switch (i) {
                    case 0:                                 // Today
                        year = dateTime.getYear();
                        month = dateTime.getMonthOfYear();
                        day = dateTime.getDayOfMonth();
                        break;

                    case 1:                                 // Tomorrow
                        DateTime tomorrow = dateTime.plusDays(1);
                        year = tomorrow.getYear();
                        month = tomorrow.getMonthOfYear();
                        day = tomorrow.getDayOfMonth();
                        break;

                    case 2:                                 // Nex Week (7 Days)
                        DateTime nextWeek = dateTime.plusDays(7);
                        year = nextWeek.getYear();
                        month = nextWeek.getMonthOfYear();
                        day = nextWeek.getDayOfMonth();
                        break;

                    case 3:                                 // Date chosen
                        DialogFragment newFragment = new DatePickerFragment();
                        newFragment.show(getChildFragmentManager(), "datePicker");
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        spinnerTime.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                // TODO: Make these into SharedPreferences
                switch (i) {
                    case 0:
                        hour = 8;
                        minute = 0;
                        break;
                    case 1:
                        hour = 13;
                        minute = 0;
                        break;
                    case 2:
                        hour = 18;
                        minute = 0;
                        break;
                    case 3:
                        hour = 20;
                        minute = 0;
                        break;
                    case 4:
                        DialogFragment newFragment = new TimePickerFragment();
                        newFragment.show(getChildFragmentManager(), "timePicker");
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        spinnerReminder.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                // TODO: Return chosen reminder repetition
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
        this.hour = hour;
        this.minute = minute;
    }

    @Override
    public void onDatePicked(int year, int month, int day) {
        this.year = year;
        this.month = month;
        this.day = day;
    }
}