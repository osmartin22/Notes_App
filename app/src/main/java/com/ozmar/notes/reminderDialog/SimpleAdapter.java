package com.ozmar.notes.reminderDialog;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.ozmar.notes.R;

/**
 * Created by ozmar on 10/18/2017.
 */

public class SimpleAdapter extends ArrayAdapter<String> {

    private String[] dropDownItems;
    private LayoutInflater inflater;

    public SimpleAdapter(Context context, int textViewResourceId, String[] listItems) {
        super(context, textViewResourceId, listItems);
        this.dropDownItems = context.getResources().getStringArray(R.array.bottomArrayDropDown);
        this.inflater = LayoutInflater.from(context);
    }
    
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return super.getView(position, convertView, parent);
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        convertView = inflater.inflate(android.R.layout.simple_spinner_dropdown_item, parent, false);
        TextView textView = convertView.findViewById(android.R.id.text1);
        textView.setText(dropDownItems[position]);
        return convertView;
    }
}
