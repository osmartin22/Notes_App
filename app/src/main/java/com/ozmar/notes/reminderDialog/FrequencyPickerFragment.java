package com.ozmar.notes.reminderDialog;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.ViewSwitcher;

import com.ozmar.notes.R;


public class FrequencyPickerFragment extends DialogFragment {

    private static final float TRANSPARENCY_ON = 0.5f;
    private static final float TRANSPARENCY_OFF = 1f;

    private Switch mySwitch;
    private Spinner topSpinner;
    private Spinner bottomSpinner;
    private Button doneButton;
    private EditText numberEditText;

    private View mainView;
    private View contentView;
    private ViewSwitcher viewSwitcher;
    private View weeklyView;
    private View monthlyView;

    public static FrequencyPickerFragment newInstance() {
        FrequencyPickerFragment fragment = new FrequencyPickerFragment();
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        mainView = inflater.inflate(R.layout.reminder_frequency_picker, container, false);
        mySwitch = mainView.findViewById(R.id.reminderSwitch);
        topSpinner = mainView.findViewById(R.id.topSpinner);
        bottomSpinner = mainView.findViewById(R.id.bottomSpinner);
        doneButton = mainView.findViewById(R.id.reminderDoneButton);
        mySwitch = mainView.findViewById(R.id.reminderSwitch);

        contentView = mainView.findViewById(R.id.reminderDialogContent);
        numberEditText = contentView.findViewById(R.id.everyNumberEditText);
        viewSwitcher = contentView.findViewById(R.id.viewSwitcher);
        weeklyView = viewSwitcher.findViewById(R.id.repeatWeeklyLayout);
        monthlyView = viewSwitcher.findViewById(R.id.repeatMonthlyLayout);


        String[] listItems = getResources().getStringArray(R.array.bottomArrayListItem);
        SimpleAdapter adapter = new SimpleAdapter(getContext(), android.R.layout.simple_spinner_item,
                listItems);
        bottomSpinner.setAdapter(adapter);

        setUpOnClickListener();
        setupSwitchListener();
        setDoneListener();

        return mainView;
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
                        viewSwitcher.setVisibility(View.GONE);
                        break;
                    case 1:
                        // Set Weekly View
                        viewSwitcher.setVisibility(View.VISIBLE);
                        if (viewSwitcher.getNextView() == weeklyView) {
                            viewSwitcher.showNext();
                        }
                        break;
                    case 2:
                        if (viewSwitcher.getNextView() == monthlyView) {
                            viewSwitcher.showNext();
                        }
                        // Set Monthly View
                        break;
                    case 3:
                        viewSwitcher.setVisibility(View.VISIBLE);
                        viewSwitcher.setVisibility(View.GONE);
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

    private void setupSwitchListener() {
        mySwitch.setChecked(true);
        mySwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    contentView.setAlpha(TRANSPARENCY_OFF);
                    topSpinner.setAlpha(TRANSPARENCY_OFF);
                } else {
                    disableView(numberEditText);
                    disableView(contentView);
                    disableView(topSpinner);
                    disableView(bottomSpinner);
                    contentView.setAlpha(TRANSPARENCY_ON);
                    topSpinner.setAlpha(TRANSPARENCY_ON);
                }
            }
        });
    }

    private void disableView(View view) {
        view.setEnabled(false);
        view.setClickable(false);
    }

    private void setDoneListener() {
        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: Implement callback, send back here
                dismiss();
            }
        });
    }
}
