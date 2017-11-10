package com.ozmar.notes.reminderDialog;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import com.ozmar.notes.R;


public class ReminderFrequencyAdapter extends ArrayAdapter<String> implements SpinnerAdapter {

    private final LayoutInflater inflater;
    private final String[] listItems;
    private final String[] dropDownItems;
    private TextView mTextView;

    public ReminderFrequencyAdapter(Context context, int resource, String[] listItems) {
        super(context, resource, listItems);
        this.inflater = LayoutInflater.from(context);
        this.listItems = listItems;
        this.dropDownItems = context.getResources().getStringArray(R.array.frequencyXMLArrayDropDown);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = super.getView(position, convertView, parent);
        if (position == 5) {
            if (mTextView == null) {
                mTextView = view.findViewById(R.id.frequencySpinnerItem);
            }
            mTextView.setText(listItems[5]);
        }

        return view;
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        convertView = inflater.inflate(R.layout.row, parent, false);
        TextView leftText = convertView.findViewById(R.id.leftSpinnerText);
        leftText.setText(dropDownItems[position]);

        return convertView;
    }
}
