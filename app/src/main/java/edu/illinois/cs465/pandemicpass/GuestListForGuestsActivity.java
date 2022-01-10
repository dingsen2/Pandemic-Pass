package edu.illinois.cs465.pandemicpass;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class GuestListForGuestsActivity extends AppCompatActivity {

    private String eventId;
    private String eventName;
    private String eventDate;
    private String eventTime;

    private ArrayList<Guest> guestArrayList;

    private DatabaseReference dbReferenceEventWithEventIdGuestList;

    private ListView guestListView;
    private TextView eventNameTextView;
    private TextView eventDateTextView;
    private TextView eventTimeTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guest_list_for_guests);

        if (getIntent() != null && getIntent().getExtras() != null
                && getIntent().hasExtra("eventId")
                && getIntent().hasExtra("eventName")
                && getIntent().hasExtra("eventDate")
                && getIntent().hasExtra("eventTime")) {
            eventId = getIntent().getExtras().getString("eventId");
            eventName = getIntent().getExtras().getString("eventName");
            eventDate = getIntent().getExtras().getString("eventDate");
            eventTime = getIntent().getExtras().getString("eventTime");
        }
        else {
            return;
        }

        eventNameTextView = (TextView) findViewById(R.id.eventName);
        eventNameTextView.setText(eventName);

        eventDateTextView = (TextView) findViewById(R.id.eventDate);
        eventDateTextView.setText(eventDate);

        eventTimeTextView = (TextView) findViewById(R.id.eventTime);
        eventTimeTextView.setText(eventTime);

        dbReferenceEventWithEventIdGuestList = FirebaseDatabase.getInstance().getReference("Event").child(eventId).child("guestList");

        guestListView = (ListView) findViewById(R.id.guestList);
        guestArrayList = new ArrayList<Guest>();

        GuestListNameAdapter guestListAdapter = new GuestListNameAdapter(this, R.layout.member_list_member_name_adapter, guestArrayList);

        guestListView.setAdapter(guestListAdapter);

        dbReferenceEventWithEventIdGuestList.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Guest g = snapshot.getValue(Guest.class);
                guestArrayList.add(g);
                guestListAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Guest g = snapshot.getValue(Guest.class);

                int index = guestArrayList.indexOf(g);

                if (index != -1) {
                    guestArrayList.set(index, g);
                    guestListAdapter.notifyDataSetChanged();
                }

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                Guest g = snapshot.getValue(Guest.class);
                guestArrayList.remove(g);
                guestListAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}