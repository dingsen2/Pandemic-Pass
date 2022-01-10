package edu.illinois.cs465.pandemicpass;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class JoinSelectMembersActivity extends AppCompatActivity implements View.OnClickListener {
    private String userId;

    private String eventCode;
    private String eventName;
    private String eventId;
    private String eventDate;

    private TextView eventNameTextView;
    private Button join;
    private ListView memberListView;

    private DatabaseReference dbReferenceEventWithEventId;
    private DatabaseReference dbReferenceUserWithUserId;
    private DatabaseReference dbReferenceUserWithUserIdMembers;

    private ArrayList<Member> memberArrayList;
    private ArrayList<Member> attendingMemberList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_select_members);

        if (getIntent() != null && getIntent().getExtras() != null
                && getIntent().hasExtra("eventCode")
                && getIntent().hasExtra("eventName")
                && getIntent().hasExtra("eventId")
                && getIntent().hasExtra("eventDate")
        ) {
            eventCode = getIntent().getExtras().getString("eventCode");
            eventName = getIntent().getExtras().getString("eventName");
            eventId = getIntent().getExtras().getString("eventId");
            eventDate = getIntent().getExtras().getString("eventDate");
        }
        else {
            return;
        }

        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        dbReferenceUserWithUserId = FirebaseDatabase.getInstance()
                .getReference("User").child(userId);
        dbReferenceUserWithUserIdMembers = dbReferenceUserWithUserId.child("members");
        dbReferenceEventWithEventId = FirebaseDatabase.getInstance().getReference("Event").child(eventId);


        eventNameTextView = (TextView) findViewById(R.id.eventName);
        eventNameTextView.setText(eventName);

        join = (Button) findViewById(R.id.join);
        join.setOnClickListener(this);

        attendingMemberList = new ArrayList<Member>();

        memberListView = (ListView) findViewById(R.id.memberList);
        memberArrayList = new ArrayList<Member>();

        MemberListMemberNameAdapter memberListAdapter = new MemberListMemberNameAdapter(this, R.layout.member_list_member_name_adapter, memberArrayList);

        memberListView.setAdapter(memberListAdapter);

        memberListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Member member = (Member) adapterView.getItemAtPosition(i);

                int index = attendingMemberList.indexOf(member);
                if (index == -1) {
                    attendingMemberList.add(member);
                }
                else {
                    attendingMemberList.remove(member);
                }

                TextView name = adapterView.getChildAt(i).findViewById(R.id.name);
                ColorDrawable viewColor = (ColorDrawable) name.getBackground();
                int colorId = viewColor.getColor();

                if (colorId == Color.WHITE) {
                    name.setBackgroundColor(Color.CYAN);
                }
                else {
                    name.setBackgroundColor(Color.WHITE);
                }

            }
        });

        dbReferenceUserWithUserIdMembers.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Member member = snapshot.getValue(Member.class);
                member.id = snapshot.getKey();
                memberArrayList.add(member);
                memberListAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Member member = snapshot.getValue(Member.class);
                member.id = snapshot.getKey();
                int index = memberArrayList.indexOf(member);

                if (index != -1) {
                    memberArrayList.set(index, member);
                    memberListAdapter.notifyDataSetChanged();
                }

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                Member member = snapshot.getValue(Member.class);
                member.id = snapshot.getKey();
                memberArrayList.remove(member);
                memberListAdapter.notifyDataSetChanged();
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
    public void onClick(View view) {
        int id = view.getId();
        
        if (id == R.id.join) {
            joinEvent();
        }
    }

    private void joinEvent() {
        if (attendingMemberList.isEmpty()) {
            Toast.makeText(this, "You cannot join an event with no members selected.", Toast.LENGTH_LONG).show();
            return;
        }

        String ea_key = dbReferenceUserWithUserId.child("eventAttendance").push().getKey();
        DatabaseReference ea_ref = dbReferenceUserWithUserId.child("eventAttendance").child(ea_key);
        ea_ref.child("eventId").setValue(eventId);
        ea_ref.child("eventName").setValue(eventName);
        ea_ref.child("eventDate").setValue(eventDate);
        
        for (Member m : attendingMemberList) {
            AttendingMemberInfo aInfo = new AttendingMemberInfo(m.id);
            ea_ref.child("attendingMemberList").push().setValue(aInfo);
            Guest guest = new Guest(userId, m.id, m.name,"No decision");
            dbReferenceEventWithEventId.child("guestList").push().setValue(guest);
        }

        Intent eventDetailsForGuestIntent = new Intent(JoinSelectMembersActivity.this, EventDetailsForGuestActivity.class);
        eventDetailsForGuestIntent.putExtra("eventId", eventId);

        startActivity(eventDetailsForGuestIntent);
        JoinSelectMembersActivity.this.finish();
    }
}