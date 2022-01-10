package edu.illinois.cs465.pandemicpass;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

public class EventListNameAndDateAdapter extends ArrayAdapter<Event> {

    private ArrayList<Event> eventArrayList;
    private Context context;
    private int resource;


    public EventListNameAndDateAdapter(@NonNull Context context, int resource, @NonNull ArrayList<Event> eventArrayList) {
        super(context, resource, eventArrayList);
        this.context = context;
        this.resource = resource;
        this.eventArrayList = eventArrayList;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        String name = getItem(position).eventName;
        String date = getItem(position).date;

        LayoutInflater inflater = LayoutInflater.from(context);
        convertView = inflater.inflate(resource, parent, false);

        TextView eventNameTextView = (TextView) convertView.findViewById(R.id.ViewEventsEventName);
        eventNameTextView.setText(name);

        TextView eventDateTextView = (TextView) convertView.findViewById(R.id.ViewEventsEventDate);
        eventDateTextView.setText(date);

        return convertView;
    }
}
