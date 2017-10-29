package com.ozmar.notes.reminderDialog;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.text.format.DateFormat;
import android.widget.TimePicker;

import org.joda.time.LocalTime;


public class TimePickerFragment extends DialogFragment
        implements TimePickerDialog.OnTimeSetListener {

    OnTimePickedListener myCallback;

    public interface OnTimePickedListener {
        void onTimePicked(int hour, int minute);

        void onTimeCancel();
    }

    public void onAttachToParentFragment(Fragment fragment) {
        try {
            myCallback = (OnTimePickedListener) fragment;
        } catch (ClassCastException e) {
            throw new ClassCastException(fragment.toString() + " must implement OnTimePickedListener.");
        }
    }

    public static TimePickerFragment newInstance(int hour, int minute) {
        Bundle bundle = new Bundle();
        bundle.putInt("Hour", hour);
        bundle.putInt("Minute", minute);
        TimePickerFragment fragment = new TimePickerFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        onAttachToParentFragment(getParentFragment());
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle bundle = getArguments();
        LocalTime time = LocalTime.now().plusMinutes(1);
        int hour = bundle.getInt("Hour", time.getHourOfDay());
        int minute = bundle.getInt("Minute", time.getMinuteOfHour());

        return new TimePickerDialog(getActivity(), this, hour, minute, DateFormat.is24HourFormat(getActivity()));
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        if (myCallback != null) {
            myCallback.onTimeCancel();
        }
    }

    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        if (myCallback != null) {
            myCallback.onTimePicked(hourOfDay, minute);
        }
    }
}