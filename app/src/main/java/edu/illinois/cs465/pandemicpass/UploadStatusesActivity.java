package edu.illinois.cs465.pandemicpass;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class UploadStatusesActivity extends AppCompatActivity {
    private String eventId;
    private String userId;
    private FirebaseAuth mAuth;
    private ArrayList<Guest> guestsInUserGroupAttending;

    private DatabaseReference dbReferenceEventWithEventIdGuestList;
    private DatabaseReference dbReferenceEventWithEventId;
    private DatabaseReference dbReferenceCurrentUserMember;
    private ListView attendingMemberListView;

//    private TextView acceptTestResult;
//    private TextView acceptVaccinationRecord;

    private ImageView acceptTestResult;
    private ImageView acceptVaccinationRecord;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_statuses);
        mAuth = FirebaseAuth.getInstance();

        if (getIntent() != null && getIntent().getExtras() != null
                && getIntent().hasExtra("eventId")) {
            eventId = getIntent().getExtras().getString("eventId");
        }
        else {
            return;
        }


        acceptTestResult = (ImageView) findViewById(R.id.accept_test_result_res);
        acceptVaccinationRecord = (ImageView) findViewById(R.id.accept_vaccine_record_res);
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        attendingMemberListView = (ListView) findViewById(R.id.nameStatusListView);
        guestsInUserGroupAttending = new ArrayList<>();
        UserAttendingMemberListAdapter attendingMemberListAdapter = new UserAttendingMemberListAdapter(this, R.layout.adapter_view_layout_name_status, guestsInUserGroupAttending);
        attendingMemberListView.setAdapter(attendingMemberListAdapter);

        dbReferenceCurrentUserMember = FirebaseDatabase.getInstance().getReference("User").child(userId).child("members");
        HashMap<String, Member> currentUserMemberMap = new HashMap<>();
        dbReferenceCurrentUserMember.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                currentUserMemberMap.clear();
                for(DataSnapshot snapshot:dataSnapshot.getChildren()){
                    Member member = snapshot.getValue(Member.class);
                    member.id = snapshot.getKey();
                    currentUserMemberMap.put(member.id, member);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });



        attendingMemberListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                Guest guest = (Guest) adapterView.getItemAtPosition(i);
                Intent profileIntent = new Intent(UploadStatusesActivity.this, UploadStatusAfterPickingMemberActivity.class);
                Member currentMember = currentUserMemberMap.get(guest.memberId);
                profileIntent.putExtra("member", currentMember);
                profileIntent.putExtra("status", guest.approvalStatus);
                startActivity(profileIntent);
            }
        });

        dbReferenceEventWithEventId = FirebaseDatabase.getInstance().getReference("Event").child(eventId);
        dbReferenceEventWithEventId.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean testAllowed = (boolean) snapshot.child("acceptTestResult").getValue();
                boolean vaxAllowed = (boolean) snapshot.child("acceptVaccinationRecord").getValue();

                if (testAllowed) {
                    acceptTestResult.setImageResource(R.drawable.icons8_check_circle);
                } else {
                    acceptTestResult.setImageResource(R.drawable.icons8_cancel);
                }

                if (vaxAllowed) {
                    acceptVaccinationRecord.setImageResource(R.drawable.icons8_check_circle);
                } else {
                    acceptVaccinationRecord.setImageResource(R.drawable.icons8_cancel);
                }
//                acceptTestResult.setText(snapshot.child("acceptTestResult").getValue().toString());
//                acceptVaccinationRecord.setText(snapshot.child("acceptVaccinationRecord").getValue().toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        dbReferenceEventWithEventIdGuestList = FirebaseDatabase.getInstance().getReference("Event").child(eventId).child("guestList");
        dbReferenceEventWithEventIdGuestList.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Guest guest = snapshot.getValue(Guest.class);
                if(guest.userId.equals(userId)){
                    guestsInUserGroupAttending.add(guest);
                }
                attendingMemberListAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Guest guest = snapshot.getValue(Guest.class);
                int index = guestsInUserGroupAttending.indexOf(guest);
                System.out.println("index is: 131");
                System.out.println(index);
                if (index != -1) {
                    guestsInUserGroupAttending.set(index, guest);
                    attendingMemberListAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                Guest guest = snapshot.getValue(Guest.class);
                int index = guestsInUserGroupAttending.indexOf(guest);
                if (index != -1) {
                    guestsInUserGroupAttending.remove(guest);
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
}




