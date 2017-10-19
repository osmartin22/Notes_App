package com.ozmar.notes.reminderDialog;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Switch;

import com.ozmar.notes.R;


public class FrequencyPickerFragment extends DialogFragment {

    private Switch mySwitch;
    private Spinner topSpinner;
    private Spinner bottomSpinner;

    public static FrequencyPickerFragment newInstance() {
        FrequencyPickerFragment fragment = new FrequencyPickerFragment();
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.reminder_frequency_picker, container, false);
        mySwitch = view.findViewById(R.id.reminderSwitch);
        topSpinner = view.findViewById(R.id.topSpinner);
        bottomSpinner = view.findViewById(R.id.bottomSpinner);

        ArrayAdapter<CharSequence> adapter1 = ArrayAdapter.createFromResource(getContext(),
                R.array.headerArray, android.R.layout.simple_spinner_item);
        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        topSpinner.setAdapter(adapter1);

        String[] listItems = getResources().getStringArray(R.array.bottomArrayListItem);
        SimpleAdapter adapter = new SimpleAdapter(getContext(), android.R.layout.simple_spinner_item,
                listItems);
        bottomSpinner.setAdapter(adapter);

        setUpOnClickListener();

        return view;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        return super.onCreateDialog(savedInstanceState);
    }

    private void setUpOnClickListener() {


        topSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        break;
                    case 1:
                        break;
                    case 2:
                        break;
                    case 3:
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        bottomSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        break;
                    case 1:
                        break;
                    case 2:
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }
}
