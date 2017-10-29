package com.ozmar.notes.reminderDialog;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.widget.DatePicker;

import org.joda.time.DateTime;


public class DatePickerFragment extends DialogFragment
        implements DatePickerDialog.OnDateSetListener {

    OnDatePickedListener myCallback;

    public interface OnDatePickedListener {
        void onDatePicked(int year, int month, int day);

        void onDateCancel();
    }

    public void onAttachParentFragment(Fragment fragment) {
        try {
            myCallback = (OnDatePickedListener) fragment;
        } catch (ClassCastException e) {
            throw new ClassCastException(fragment.toString() + " must implement OnDatePickedListener.");
        }
    }

    public static DatePickerFragment newInstance(int year, int month, int day) {
        Bundle bundle = new Bundle();
        bundle.putInt("Year", year);
        bundle.putInt("Month", month);
        bundle.putInt("Day", day);
        DatePickerFragment fragment = new DatePickerFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        onAttachParentFragment(getParentFragment());
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        DateTime dateTime = DateTime.now();
        Bundle bundle = getArguments();
        int year = bundle.getInt("Year", dateTime.getYear());
        int month = bundle.getInt("Month", dateTime.getMonthOfYear() - 1);
        int day = bundle.getInt("Day", dateTime.getDayOfMonth());

        DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(), this, year, month, day);
        datePickerDialog.getDatePicker().setMinDate(dateTime.getMillis());

        return datePickerDialog;
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        if (myCallback != null) {
            myCallback.onDateCancel();
        }
    }

    public void onDateSet(DatePicker view, int year, int month, int day) {
        month += 1;
        if (myCallback != null) {
            myCallback.onDatePicked(year, month, day);
        }
    }
}