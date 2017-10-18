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


public class ReminderAdapter extends ArrayAdapter<String> implements SpinnerAdapter {

    private int spinnerStyle;
    private LayoutInflater inflater;
    private String[] dropDownItems;
    private String[] listItems;

    public ReminderAdapter(Context context, int textViewResourceId, String[] listItems, String[] dropDownItems, int spinnerStyle) {
        super(context, textViewResourceId, listItems);
        this.inflater = LayoutInflater.from(context);
        this.dropDownItems = dropDownItems;
        this.spinnerStyle = spinnerStyle;
        this.listItems = listItems;
        listItems[4] = "";
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = super.getView(position, convertView, parent);
        if (position == 4) {
            TextView textView = view.findViewById(android.R.id.text1);
            textView.setText(listItems[4]);
        }

        return view;
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        convertView = inflater.inflate(R.layout.row, parent, false);
        TextView leftText = convertView.findViewById(R.id.leftSpinnerText);

        if (spinnerStyle == 0) {
            if (position != 4) {
                TextView rightText = convertView.findViewById(R.id.rightSpinnerText);
                rightText.setText(listItems[position]);
                rightText.setVisibility(View.VISIBLE);
            }
        }

        leftText.setText(dropDownItems[position]);

        return convertView;
    }
}
