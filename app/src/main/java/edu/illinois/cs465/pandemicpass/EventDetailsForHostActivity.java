package edu.illinois.cs465.pandemicpass;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class EventDetailsForHostActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView EventNameTextView;
    private TextView EventDateTextView;
    private TextView EventCodeTextView;
    private TextView EventLocationTextView;
    private TextView EventDescriptionTextView;
    private TextView VaxTextView;
    private TextView TestTextView;
    private TextView timeTextView;
    private Button ReturnHomeButton;
    private Button ViewGuestsButton;
    private Button EditDetailsButton;
    private Button ShareCodeButton;
    private DatabaseReference dbReferenceEvent;
    private String hostId;
    private String eventId;
    private String eventCode;
    private Event event;
    private String eventTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_details_for_host);

        // This is for when the host comes from HostEventCodeActivity.java
        if (getIntent() != null && getIntent().getExtras() != null
                && getIntent().hasExtra("event_code")) {
            Log.d("DEBUG", "Got Event Code");
            eventCode = getIntent().getExtras().getString("event_code");
        } else if (getIntent() != null && getIntent().getExtras() != null
                && getIntent().hasExtra("event_id")) {
            eventId = getIntent().getExtras().getString("event_id");
            Log.d("DEBUG", "we here!");
            Log.d("DEBUG", eventId);
        }

        EventNameTextView = (TextView) findViewById(R.id.HostViewEventName);
        EventDateTextView = (TextView) findViewById(R.id.HostViewEventDate);
        EventCodeTextView = (TextView) findViewById(R.id.HostViewEventCode);
        EventLocationTextView = (TextView) findViewById(R.id.HostViewEventLocation);
        EventDescriptionTextView = (TextView) findViewById(R.id.HostViewEventDescription);
        VaxTextView = (TextView) findViewById(R.id.HostViewEventVax);
        TestTextView = (TextView) findViewById(R.id.HostViewEventTest);
        timeTextView = (TextView) findViewById(R.id.HostViewEventTime);

        ReturnHomeButton = (Button) findViewById(R.id.HostViewHomeButton);
        ReturnHomeButton.setOnClickListener(this);

        ViewGuestsButton = (Button) findViewById(R.id.HostViewGuestsButton);
        ViewGuestsButton.setOnClickListener(this);

        EditDetailsButton = (Button) findViewById(R.id.HostViewEditEventButton);
        EditDetailsButton.setOnClickListener(this);

        ShareCodeButton = (Button) findViewById(R.id.HostViewShareCode);
        ShareCodeButton.setOnClickListener(this);

        dbReferenceEvent = FirebaseDatabase.getInstance().getReference("Event");
        dbReferenceEvent.keepSynced(true);

        hostId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        getEventDetails();
    }

    private void getEventDetails() {
        if (eventCode != null) {
            Log.d("DEBUG", "Getting event details from event code");
            dbReferenceEvent.orderByChild("eventCode").equalTo(eventCode).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot snap : snapshot.getChildren()) {
                        eventId = snap.getKey();
                        event = (Event) snap.getValue(Event.class);
                        break;
                    }
                    renderScreen();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        } else if (eventId != null) {
            DatabaseReference dbReferenceEventWithEventId = dbReferenceEvent.child(eventId);
            dbReferenceEventWithEventId = FirebaseDatabase.getInstance().getReference("Event").child(eventId);

            dbReferenceEventWithEventId.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        event = snapshot.getValue(Event.class);
                        renderScreen();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }

    private void renderScreen() {
        Log.d("DEBUG", "Rendering");

        if (eventCode == null) {
            eventCode = event.eventCode;
        }

        EventNameTextView.setText(event.eventName);
        EventDateTextView.setText(event.date);
        EventCodeTextView.setText(eventCode);
        EventLocationTextView.setText(event.location);
        EventDescriptionTextView.setText(event.description);
        timeTextView.setText(event.time);


        if (event.acceptVaccinationRecord) {
            VaxTextView.setText("Vaccine Card Is Allowed");
        } else {
            VaxTextView.setText("Vaccine Card Is Not Allowed");
        }

        if (event.acceptTestResult) {
            TestTextView.setText("COVID Test Is Allowed");
        } else {
            TestTextView.setText("COVID Test Is Not Allowed");
        }
    }

    private void EmailAndTextIntent() {
        /*Create an ACTION_SEND Intent*/
        Intent intent = new Intent(android.content.Intent.ACTION_SEND);
        /*This will be the actual content you wish you share.*/
        String shareBody = eventCode;
        /*The type of the content is text, obviously.*/
        intent.setType("text/plain");
        /*Applying information Subject and Body.*/
        intent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
        /*Fire!*/
        startActivity(Intent.createChooser(intent, "Share With:"));
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        if (id == R.id.HostViewHomeButton) {
            startActivity(new Intent(EventDetailsForHostActivity.this, HomeScreenActivity.class));
        } else if (id == R.id.HostViewShareCode) {
            EmailAndTextIntent();
        } else if (id == R.id.HostViewEditEventButton) {
            Intent intent = new Intent(this, HostEditEventDetails.class);

            intent.putExtra("event_name", event.eventName);
            intent.putExtra("vax_allowed", event.acceptVaccinationRecord);
            intent.putExtra("test_allowed", event.acceptTestResult);
            intent.putExtra("event_year", event.getYear());
            intent.putExtra("event_month", event.getMonth());
            intent.putExtra("event_day", event.getDay());
            intent.putExtra("event_location", event.location);
            intent.putExtra("event_description", event.description);
            intent.putExtra("event_code", eventCode);
            intent.putExtra("event_time", event.time);

            startActivity(intent);
        } else if (id == R.id.HostViewGuestsButton) {
            Intent intent = new Intent(this, EventGuestListForHostActivity.class);

            intent.putExtra("event_id", eventId);

            startActivity(intent);
        }
    }
}