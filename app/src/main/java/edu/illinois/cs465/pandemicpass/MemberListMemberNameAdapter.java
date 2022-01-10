
package edu.illinois.cs465.pandemicpass;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.database.DatabaseReference;

import org.w3c.dom.Text;

import java.util.ArrayList;

public class MemberListMemberNameAdapter extends ArrayAdapter<Member> {
    private ArrayList<Member> memberArrayList;
    private Context context;
    private int resource;

    public MemberListMemberNameAdapter(@NonNull Context context, int resource, @NonNull ArrayList<Member> memberArrayList) {
        super(context, resource, memberArrayList);
        this.context = context;
        this.resource = resource;
        this.memberArrayList = memberArrayList;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        String name = getItem(position).name;

        LayoutInflater inflater = LayoutInflater.from(context);
        convertView = inflater.inflate(resource, parent, false);

        TextView nameTextView = (TextView) convertView.findViewById(R.id.name);
        nameTextView.setText(name);


        return convertView;
    }
//
//    @Override
//    public void onClick(View view) {
//        ColorDrawable viewColor = (ColorDrawable) view.getBackground();
//        int colorId = viewColor.getColor();
//
////        if (colorId == Color.WHITE) {
////            view.setBackgroundColor(Color.CYAN);
////        }
////        else {
////            view.setBackgroundColor(Color.WHITE);
////        }
//    }
}
