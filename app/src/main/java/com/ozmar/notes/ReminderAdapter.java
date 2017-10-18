package com.ozmar.notes;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.SpinnerAdapter;
import android.widget.TextView;


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
        if(position == 4) {
            Log.d("DropDown", "Selected Custom choice");
        }

        return super.getView(position, convertView, parent);
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

//        Log.d("DropDown", "Position Chosen -> " + position);

        ViewHolder viewHolder;

        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = inflater.inflate(R.layout.row, parent, false);
            viewHolder.leftText = convertView.findViewById(R.id.leftSpinnerText);

            if (spinnerStyle == 1) {
                viewHolder.rightText = convertView.findViewById(R.id.rightSpinnerText);
                viewHolder.rightText.setText(listItems[position]);
                viewHolder.rightText.setVisibility(View.VISIBLE);
            }

            Log.d("DropDown", "Inside " + position + ": " + dropDownItems[position]);
            viewHolder.leftText.setText(dropDownItems[position]);
        }

        return convertView;
    }



    public static class ViewHolder {
        TextView leftText   ;
        TextView rightText;
    }
}
