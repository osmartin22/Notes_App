package com.ozmar.notes.reminderDialog;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.ozmar.notes.R;

import java.util.List;


public class FrequencyPickerFragment extends DialogFragment {

    private static final float TRANSPARENCY_ON = 0.5f;
    private static final float TRANSPARENCY_OFF = 1;

    private int topSpinnerPosition;
    private boolean timeUnitPlural;
    private boolean emptyTextView;

    private Switch mySwitch;
    private Spinner topSpinner;
    private Spinner bottomSpinner;
    private Button doneButton;
    private EditText numberEditText;
    private TextView timeUnitTextView;

    private View mainView;
    private View contentView;
    private ViewSwitcher viewSwitcher;

    private MonthlyLayoutHelper monthlyHelper;
    private WeeklyLayoutHelper weeklyHelper;

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
        timeUnitTextView = contentView.findViewById(R.id.timeUnitTextView);
        viewSwitcher = contentView.findViewById(R.id.viewSwitcher);

        weeklyHelper = new WeeklyLayoutHelper(viewSwitcher.findViewById(R.id.repeatWeeklyLayout));
        monthlyHelper = new MonthlyLayoutHelper(viewSwitcher.findViewById(R.id.repeatMonthlyLayout));

        SimpleAdapter adapter = new SimpleAdapter(getContext(), android.R.layout.simple_spinner_item,
                getResources().getStringArray(R.array.bottomArrayListItem));
        bottomSpinner.setAdapter(adapter);

        setUpOnClickListener();
        setUpSwitchListener();
        setUpDoneListener();
        setUpTextWatcher();

        return mainView;
    }

    private void setUpOnClickListener() {

        topSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:     // Daily View
                        viewSwitcher.setVisibility(View.GONE);
                        break;

                    case 1:     // Weekly View
                        viewSwitcher.setVisibility(View.VISIBLE);
                        if (viewSwitcher.getNextView() == weeklyHelper.getMainView()) {
                            viewSwitcher.showNext();
                        }
                        break;

                    case 2:     // Monthly View
                        viewSwitcher.setVisibility(View.VISIBLE);
                        if (viewSwitcher.getNextView() == monthlyHelper.getMainView()) {
                            viewSwitcher.showNext();
                        }
                        break;

                    case 3:     // Yearly View
                        viewSwitcher.setVisibility(View.GONE);
                        break;
                }

                topSpinnerPosition = position;
                setTimeUnitString();
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
                        // No view next to spinner
                        break;
                    case 1:
                        // View to press for calendarDialog
                        break;
                    case 2:
                        // EditText with TextView
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void setUpSwitchListener() {
        mySwitch.setChecked(true);
        mySwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    contentView.setAlpha(TRANSPARENCY_OFF);
                    topSpinner.setAlpha(TRANSPARENCY_OFF);
                    if (!emptyTextView) {
                        doneButton.setEnabled(true);
                    }

                    monthlyHelper.setViewEnabled(true);
                    weeklyHelper.setViewEnabled(true);

                    numberEditText.setEnabled(true);
                    contentView.setEnabled(true);
                    topSpinner.setEnabled(true);
                    bottomSpinner.setEnabled(true);
                } else {
                    contentView.setAlpha(TRANSPARENCY_ON);
                    topSpinner.setAlpha(TRANSPARENCY_ON);

                    monthlyHelper.setViewEnabled(false);
                    weeklyHelper.setViewEnabled(false);

                    numberEditText.setEnabled(false);
                    contentView.setEnabled(false);
                    topSpinner.setEnabled(false);
                    bottomSpinner.setEnabled(false);
                }
            }
        });
    }

    private void setUpDoneListener() {
        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // TODO: Implement callback, send data back here
                int checkedButton = monthlyHelper.getCheckedButton();
                List<Boolean> list = weeklyHelper.getCheckedButtons();
                dismiss();
            }
        });
    }

    private void setUpTextWatcher() {
        numberEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: Mess around with cursor
                // Create general onclick to use with the other EditText that was added
                // TODO: Generalize TextWatcher as well
                numberEditText.selectAll();
            }
        });

        numberEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (count == 0) {
                    emptyTextView = true;
                    doneButton.setEnabled(false);
                } else {
                    emptyTextView = false;
                    doneButton.setEnabled(true);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().equals("0")) {
                    s.replace(0, 1, "1");
                } else if (!s.toString().isEmpty()) {
                    timeUnitPlural = Integer.parseInt(s.toString()) > 1;
                    setTimeUnitString();
                }
            }
        });
    }

    private void setTimeUnitString() {
        switch (topSpinnerPosition) {
            case 0:
                if (timeUnitPlural) {
                    timeUnitTextView.setText(getResources().getString(R.string.dayPlural));
                } else {
                    timeUnitTextView.setText(getResources().getString(R.string.daySingular));
                }
                break;
            case 1:
                if (timeUnitPlural) {
                    timeUnitTextView.setText(getResources().getString(R.string.weekPlural));
                } else {
                    timeUnitTextView.setText(getResources().getString(R.string.weekSingular));
                }
                break;
            case 2:
                if (timeUnitPlural) {
                    timeUnitTextView.setText(getResources().getString(R.string.monthPlural));
                } else {
                    timeUnitTextView.setText(getResources().getString(R.string.monthSingular));
                }
                break;
            case 3:
                if (timeUnitPlural) {
                    timeUnitTextView.setText(getResources().getString(R.string.yearPlural));
                } else {
                    timeUnitTextView.setText(getResources().getString(R.string.yearSingular));
                }
                break;
        }
    }
}
