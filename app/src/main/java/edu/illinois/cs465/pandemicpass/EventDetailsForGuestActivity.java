// Go here when event clicked and host id != current id

package edu.illinois.cs465.pandemicpass;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class EventDetailsForGuestActivity extends AppCompatActivity implements View.OnClickListener {

    private String userId;
    private String eventId;

    private String eventName;
    private String eventDate;
    private String eventTime;

    private TextView eventNameTextView;
    private TextView hostTextView;
    private TextView dateTextView;
    private TextView timeTextView;
    private TextView locationTextView;
    private TextView requirementsTextView;
    private TextView descriptionTextView;
    private TextView proportionOfGuestsApprovedTextView;
    private ListView attendingMemberListView;
    private Button editPartyMembers;
    private Button updateStatuses;
    private Button viewGuestList;
    private int totalGuests;
    private int guestsApproved;

    private DatabaseReference dbReferenceEventWithEventId;
    private DatabaseReference dbReferenceEventWithEventIdGuestList;
    private ArrayList<Guest> guestsInUserGroupAttending;
    private String eventCode;
    private DatabaseReference dbReferenceEvent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_details_for_guest);

        if (getIntent() != null && getIntent().getExtras() != null
                && getIntent().hasExtra("eventId")) {
            eventId = getIntent().getExtras().getString("eventId");
        }
        else {
            return;
        }

        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        dbReferenceEvent = FirebaseDatabase.getInstance().getReference("Event");
        eventNameTextView = (TextView) findViewById(R.id.eventName);
        hostTextView = (TextView) findViewById(R.id.host);
        dateTextView = (TextView) findViewById(R.id.date);
        timeTextView = (TextView) findViewById(R.id.time);
        locationTextView = (TextView) findViewById(R.id.location);
        requirementsTextView = (TextView) findViewById(R.id.requirements);
        descriptionTextView = (TextView) findViewById(R.id.description);

        proportionOfGuestsApprovedTextView = (TextView) findViewById(R.id.proportionOfGuestsApproved);

        attendingMemberListView = (ListView) findViewById(R.id.attendingMemberList);

        editPartyMembers = (Button) findViewById(R.id.editPartyMembers);
        editPartyMembers.setOnClickListener(this);

        updateStatuses = (Button) findViewById(R.id.updateStatuses);
        updateStatuses.setOnClickListener(this);

        viewGuestList = (Button) findViewById(R.id.guestList);
        viewGuestList.setOnClickListener(this);

        dbReferenceEventWithEventId = FirebaseDatabase.getInstance().getReference("Event").child(eventId);
        dbReferenceEventWithEventIdGuestList = dbReferenceEventWithEventId.child("guestList");

        dbReferenceEventWithEventId.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Event e = snapshot.getValue(Event.class);
                    loadEventDetails(e);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        totalGuests = 0;
        guestsApproved = 0;

        guestsInUserGroupAttending = new ArrayList<Guest>();

        UserAttendingMemberListAdapter attendingMemberListAdapter = new UserAttendingMemberListAdapter(this, R.layout.attending_member_list_adapter, guestsInUserGroupAttending);
        attendingMemberListView.setAdapter(attendingMemberListAdapter);

        dbReferenceEventWithEventIdGuestList.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Guest guest = snapshot.getValue(Guest.class);

                if (guest.userId.equals(userId)) {
                    guestsInUserGroupAttending.add(guest);
                }
                totalGuests += 1;
                if (guest.approvalStatus.equals("Approved")) {
                    guestsApproved += 1;
                }
                updateProportionOfGuestsApprovedTextView();
                attendingMemberListAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Guest guest = snapshot.getValue(Guest.class);
                int index = guestsInUserGroupAttending.indexOf(guest);

                if (index != -1) {
                    String previousApprovalStatus = guestsInUserGroupAttending.get(index).approvalStatus;
                    String newApprovalStatus = guest.approvalStatus;

                    if (!previousApprovalStatus.equals(newApprovalStatus)) {
                        if (newApprovalStatus.equals("Approved")) {
                            guestsApproved += 1;
                        }
                        else if (previousApprovalStatus.equals("Approved")) {
                            guestsApproved -= 1;
                        }
                    }

                    guestsInUserGroupAttending.set(index, guest);
                    updateProportionOfGuestsApprovedTextView();
                    attendingMemberListAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                Guest guest = snapshot.getValue(Guest.class);
                int index = guestsInUserGroupAttending.indexOf(guest);

                if (index != -1) {
                    String previousApprovalStatus = guestsInUserGroupAttending.get(index).approvalStatus;

                    if (previousApprovalStatus.equals("Approved")) {
                        guestsApproved -= 1;
                    }

                    guestsInUserGroupAttending.remove(guest);
                    totalGuests -= 1;
                    updateProportionOfGuestsApprovedTextView();
                    attendingMemberListAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadEventDetails(Event e) {
        eventName = e.eventName;
        eventDate = e.date;
        eventTime = e.time;
        eventCode = e.eventCode;

        eventNameTextView.setText(e.eventName);
        hostTextView.setText(e.hostName);
        dateTextView.setText(e.date);
        timeTextView.setText(e.time);
        locationTextView.setText(e.location);
        ArrayList<String> requirements = new ArrayList<String>();

        if (e.acceptVaccinationRecord) {
            requirements.add("Vaccination Record");
        }
        if (e.acceptTestResult) {
            requirements.add("Test Result");
        }

        if (requirements.size() == 0) {
            requirementsTextView.setText("None");
        }
        else if (requirements.size() == 1) {
            requirementsTextView.setText(requirements.get(0));
        }
        else {
            String reqStr = requirements.get(0);
            for (int i = 1; i < requirements.size(); i += 1) {
                reqStr += ", " + requirements.get(i);
            }
            requirementsTextView.setText(reqStr);
        }

        descriptionTextView.setText(e.description);

        updateProportionOfGuestsApprovedTextView();
    }

    private void updateProportionOfGuestsApprovedTextView() {
        proportionOfGuestsApprovedTextView = (TextView) findViewById(R.id.proportionOfGuestsApproved);
        proportionOfGuestsApprovedTextView.setText(String.valueOf(guestsApproved) + " of " + String.valueOf(totalGuests) + " guests approved.");

    }

    @Override
    public void onClick(View view) {
        int id = view.getId();

        if (id == R.id.editPartyMembers) {
            //TODO
            verifyEventCodeAndRedirect();
        }
        else if (id == R.id.updateStatuses) {
            //TODO
            Intent updateStatusesIntent = new Intent(this, UploadStatusesActivity.class);
            updateStatusesIntent.putExtra("eventId", eventId);
            startActivity(updateStatusesIntent);
        }
        else if (id == R.id.guestList) {
            goToGuestList();
        }
    }

    private void goToGuestList() {
        Intent guestListForGuestIntent = new Intent(EventDetailsForGuestActivity.this, GuestListForGuestsActivity.class);
        guestListForGuestIntent.putExtra("eventId", eventId);
        guestListForGuestIntent.putExtra("eventName", eventName);
        guestListForGuestIntent.putExtra("eventDate", eventDate);
        guestListForGuestIntent.putExtra("eventTime", eventTime);
        startActivity(guestListForGuestIntent);

    }

    public void verifyEventCodeAndRedirect() {
        if (eventCode.isEmpty()) {
            return;
        }

        dbReferenceEvent.orderByChild("eventCode").equalTo(eventCode).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    Toast.makeText(EventDetailsForGuestActivity.this, "Invalid event code", Toast.LENGTH_LONG).show();
                }
                else {
                    Intent selectMembersIntent = new Intent(EventDetailsForGuestActivity.this, JoinSelectMembersActivity.class);
                    for(DataSnapshot snap: snapshot.getChildren()) {
                        String eventId = snap.getKey();
                        Event e = (Event) snap.getValue(Event.class);
                        String eventName = e.eventName;
                        String eventDate = e.date;
                        selectMembersIntent.putExtra("eventId", eventId);
                        selectMembersIntent.putExtra("eventName", eventName);
                        selectMembersIntent.putExtra("eventDate", eventDate);
                        break;
                    }
                    selectMembersIntent.putExtra("eventCode", eventCode);
                    startActivity(selectMembersIntent);
                    EventDetailsForGuestActivity.this.finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}