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

import java.util.Calendar;


public class ReminderDialogFragment extends DialogFragment
        implements DatePickerFragment.OnDatePickedListener, TimePickerFragment.OnTimePickedListener {

    private NDSpinner spinnerDate;
    private NDSpinner spinnerTime;
    private NDSpinner spinnerReminder;

    private Calendar calendar;

    OnReminderPickedListener myCallback;

    public interface OnReminderPickedListener {
        void onReminderPicked(Calendar calendar);
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
        calendar = Calendar.getInstance();

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
                if (myCallback != null) {
                    myCallback.onReminderPicked(calendar);
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
                if (i == 3) {
                    DialogFragment newFragment = new DatePickerFragment();
                    newFragment.show(getChildFragmentManager(), "datePicker");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        spinnerTime.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (i == 4) {
                    DialogFragment newFragment = new TimePickerFragment();
                    newFragment.show(getChildFragmentManager(), "timePicker");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        spinnerReminder.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
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
        calendar.set(Calendar.HOUR, hour);
        calendar.set(Calendar.MINUTE, minute);

        Toast.makeText(getActivity(), hour + ":" + minute, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDatePicked(int year, int month, int day) {
        calendar.set(year, month, day);

        Toast.makeText(getActivity(), month + 1 + "/" + day + "/" + year, Toast.LENGTH_SHORT).show();
    }
}