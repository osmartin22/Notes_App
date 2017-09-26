package com.ozmar.notes;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class NotesAdapter extends ArrayAdapter<SingleNote> implements View.OnClickListener {

    private Context context;
    private List<SingleNote> nList;

    private class ViewHolder {
        TextView title;
        TextView content;
    }

    public NotesAdapter(Context context, int textViewResourceId, List<SingleNote> objects) {
        super(context, textViewResourceId, objects);
        this.context = context;
        nList = objects;
    }

    public void updateAdapter(List<SingleNote> newList) {
        nList.clear();
        nList.addAll(newList);
        this.notifyDataSetChanged();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.note:
                Log.i("Adapter ", "OnClick called");
                break;
        }
    } // onClick() end

    @Override
    @NonNull
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            LayoutInflater vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = vi.inflate(R.layout.note_preview, parent, false);

            holder = new ViewHolder();
            holder.title = convertView.findViewById(R.id.title);
            holder.content = convertView.findViewById(R.id.content);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        SingleNote singleNote = nList.get(position);

        // Limit content display in preview
        int limit = 80;
        if (singleNote.get_content().length() > limit) {
            String temp = singleNote.get_content();
            temp = temp.substring(0, limit - 3);
            temp += "...";
            holder.content.setText(temp);
        } else {
            holder.content.setText(singleNote.get_content());
        }

        holder.title.setText(singleNote.get_title());

        return convertView;
    } // getView() end

} // NotesAdapter end
