package edu.illinois.cs465.pandemicpass;

import android.content.Context;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

public class GuestListGuestNameAdapter extends ArrayAdapter<Guest> {
    private ArrayList<Guest> guestArrayList;
    private Context context;
    private int resource;

    public GuestListGuestNameAdapter(@NonNull Context context, int resource, @NonNull ArrayList<Guest> guestArrayList) {
        super(context, resource, guestArrayList);
        this.context = context;
        this.resource = resource;
        this.guestArrayList = guestArrayList;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        String name = getItem(position).name;
        String approvalStatus = getItem(position).approvalStatus;

        LayoutInflater inflater = LayoutInflater.from(context);
        convertView = inflater.inflate(resource, parent, false);

        TextView nameTextView = (TextView) convertView.findViewById(R.id.guestListName);
        nameTextView.setText(name);

        TextView statusTextView = (TextView) convertView.findViewById(R.id.guestListStatus);
        statusTextView.setText(approvalStatus);

        return convertView;
    }
}