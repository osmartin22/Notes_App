package com.ozmar.notes;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by ozmar on 9/22/2017.
 */

public class NotesAdapter extends ArrayAdapter<SingleNote> {

    private Context context;
    private List<SingleNote> nList;

    private class ViewHolder {
        TextView title;
        TextView content;
    }

    public NotesAdapter(Context context, int textViewResourceId, List<SingleNote> objects) {
        super (context, textViewResourceId, objects);
        this.context = context;
        nList = objects;
    }

//    // Set my onclick here
//    @Override
//    public void onClick(View view) {

//    }

    @Override @NonNull
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        ViewHolder holder;
        if(convertView == null) {
            LayoutInflater vi = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = vi.inflate(R.layout.note_preview, parent, false);

            holder = new ViewHolder();
            holder.title = convertView.findViewById(R.id.title);
            holder.content = convertView.findViewById(R.id.content);
            convertView.setTag(holder);
        }

        else {
            holder = (ViewHolder) convertView.getTag();
        }

        SingleNote singleNote = nList.get(position);
        holder.title.setText(singleNote.get_title());
        holder.content.setText(singleNote.get_content());

        return convertView;
    }

}
