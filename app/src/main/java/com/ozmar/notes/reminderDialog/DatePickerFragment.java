package com.ozmar.notes.reminderDialog;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
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
    }

    public void onAttachParentFragment(Fragment fragment) {
        try {
            myCallback = (OnDatePickedListener) fragment;
        } catch (ClassCastException e) {
            throw new ClassCastException(fragment.toString() + " must implement OnDatePickedListener.");
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        onAttachParentFragment(getParentFragment());
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        DateTime dateTime = DateTime.now();
        int year = dateTime.getYear();
        int month = dateTime.getMonthOfYear() - 1;  // Set correct month for DatePicker
        int day = dateTime.getDayOfMonth();

        DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(), this, year, month, day);
        datePickerDialog.getDatePicker().setMinDate(dateTime.getMillis());

        return datePickerDialog;
    }

    public void onDateSet(DatePicker view, int year, int month, int day) {
        month += 1;
        if (myCallback != null) {
            myCallback.onDatePicked(year, month, day);
        }
        this.dismiss();
    }
}