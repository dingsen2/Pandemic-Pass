package edu.illinois.cs465.pandemicpass;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class EventListActivity extends AppCompatActivity implements View.OnClickListener {

    private ListView hostingEventsListView;
    private ListView attendingEventsListView;
    private TextView attendingTextView;
    private TextView hostingTextView;

    private ArrayList<Event> hostingEvents;
    private ArrayList<Event> attendingEvents;

    private String userId;

    private DatabaseReference dbReferenceEvent;
    private DatabaseReference dbReferenceUserWithUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_list);

        hostingEventsListView = (ListView) findViewById(R.id.listViewHosting);
        hostingEventsListView.setVisibility(View.GONE);
        attendingEventsListView = (ListView) findViewById(R.id.listViewAttending);

        attendingTextView = (TextView) findViewById(R.id.AttendingTextView);
        attendingTextView.setBackgroundColor(Color.CYAN);
        hostingTextView = (TextView) findViewById(R.id.HostingTextView);
        hostingTextView.setBackgroundColor(Color.WHITE);

        hostingEvents = new ArrayList<>();
        attendingEvents = new ArrayList<>();

        EventListNameAndDateAdapter hostEventListAdapter = new EventListNameAndDateAdapter(this, R.layout.event_list_event_name_and_date_adapter, hostingEvents);
        hostingEventsListView.setAdapter(hostEventListAdapter);

        EventListNameAndDateAdapter attendingEventListAdapter = new EventListNameAndDateAdapter(this, R.layout.event_list_event_name_and_date_adapter, attendingEvents);
        attendingEventsListView.setAdapter(attendingEventListAdapter);

        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        dbReferenceEvent = FirebaseDatabase.getInstance()
                .getReference("Event");

        dbReferenceUserWithUserId = FirebaseDatabase.getInstance()
                .getReference("User").child(userId);

        attendingEventsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Event e = (Event) adapterView.getItemAtPosition(i);

                Intent intent = new Intent(EventListActivity.this, EventDetailsForGuestActivity.class);
                Log.d("DEBUG", e.id);
                intent.putExtra("eventId", e.id);

                startActivity(intent);
            }
        });

        hostingEventsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Event e = (Event) adapterView.getItemAtPosition(i);

                Intent intent = new Intent(EventListActivity.this, EventDetailsForHostActivity.class);
                // lmao these are different
                Log.d("DEBUG", e.id);
                intent.putExtra("event_id", e.id);

                startActivity(intent);
            }
        });

        dbReferenceEvent.orderByChild("hostId").equalTo(userId).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Event e = snapshot.getValue(Event.class);
                e.id = snapshot.getKey();
                hostingEvents.add(e);
                hostEventListAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        dbReferenceUserWithUserId.child("eventAttendance").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Event e = snapshot.getValue(Event.class);
                String date = snapshot.child("eventDate").getValue(String.class);
                String id_pls = snapshot.child("eventId").getValue(String.class);
                e.date = date;
                e.id = id_pls;
                attendingEvents.add(e);
                attendingEventListAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        if (id == R.id.AttendingTextView) {
            attendingTextView.setBackgroundColor(Color.CYAN);
            hostingTextView.setBackgroundColor(Color.WHITE);
            hostingEventsListView.setVisibility(View.GONE);
            attendingEventsListView.setVisibility(View.VISIBLE);
        } else if (id == R.id.HostingTextView) {
            hostingTextView.setBackgroundColor(Color.CYAN);
            attendingTextView.setBackgroundColor(Color.WHITE);
            attendingEventsListView.setVisibility(View.GONE);
            hostingEventsListView.setVisibility(View.VISIBLE);
        }
    }
}