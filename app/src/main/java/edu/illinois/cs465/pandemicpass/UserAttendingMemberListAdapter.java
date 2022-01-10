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

public class UserAttendingMemberListAdapter extends ArrayAdapter<Guest> {
    private ArrayList<Guest> attendingMemberArrayList;
    private Context context;
    private int resource;

    public UserAttendingMemberListAdapter(@NonNull Context context, int resource, @NonNull ArrayList<Guest> attendingMemberArrayList) {
        super(context, resource, attendingMemberArrayList);
        this.context = context;
        this.resource = resource;
        this.attendingMemberArrayList = attendingMemberArrayList;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        String name = getItem(position).name;
        String approvalStatus = getItem(position).approvalStatus;

        LayoutInflater inflater = LayoutInflater.from(context);
        convertView = inflater.inflate(resource, parent, false);

        TextView nameTextView = (TextView) convertView.findViewById(R.id.name);
        nameTextView.setText(name);

        TextView approvalStatusTextView = (TextView) convertView.findViewById(R.id.approvalStatus);
        approvalStatusTextView.setText(approvalStatus);

        return convertView;
    }

}
