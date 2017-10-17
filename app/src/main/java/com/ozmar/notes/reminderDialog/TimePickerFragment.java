package com.ozmar.notes.reminderDialog;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.text.format.DateFormat;
import android.widget.TimePicker;

import org.joda.time.DateTime;


public class TimePickerFragment extends DialogFragment
        implements TimePickerDialog.OnTimeSetListener {

    OnTimePickedListener myCallback;

    public interface OnTimePickedListener {
        void onTimePicked(int hour, int minute);
    }

    public void onAttachToParentFragment(Fragment fragment) {
        try {
            myCallback = (OnTimePickedListener) fragment;
        } catch (ClassCastException e) {
            throw new ClassCastException(fragment.toString() + " must implement OnTimePickedListener.");
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        onAttachToParentFragment(getParentFragment());
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        DateTime dateTime = DateTime.now().plusMinutes(1);
        int minute = dateTime.getMinuteOfHour();
        int hour = dateTime.getHourOfDay();

        return new TimePickerDialog(getActivity(), this, hour, minute, DateFormat.is24HourFormat(getActivity()));
    }

    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        if (myCallback != null) {
            myCallback.onTimePicked(hourOfDay, minute);
        }
        this.dismiss();
    }
}