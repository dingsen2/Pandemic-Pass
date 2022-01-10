package edu.illinois.cs465.pandemicpass;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class EventGuestListForHostActivity extends AppCompatActivity {

    private ListView guestsListView;

    private ArrayList<Guest> guestsArrayList;

    private DatabaseReference dbReferenceEvent;

    private GuestListGuestNameAdapter guestListAdapter;

    private String eventId;
    private String guestKey;

    // TODO: Refresh list when back button is hit from HostApprovedAndDeny.java
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("HMMM", "test");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_guest_list_for_host);

        guestsListView = (ListView) findViewById(R.id.guestListView);

        guestsArrayList = new ArrayList<>();

        dbReferenceEvent = FirebaseDatabase.getInstance()
                .getReference("Event");

        guestListAdapter = new GuestListGuestNameAdapter(this, R.layout.guest_list_adapter, guestsArrayList);
        guestsListView.setAdapter(guestListAdapter);

        eventId = getIntent().getExtras().getString("event_id");

        guestsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                Guest g = (Guest) adapterView.getItemAtPosition(i);

                Intent intent = new Intent(EventGuestListForHostActivity.this, HostApproveAndDeny.class);
                intent.putExtra("guest_id", g.userId);
                intent.putExtra("member_id", g.memberId);
                intent.putExtra("event_id", eventId);
                intent.putExtra("guest_key", g.guestKey);
                intent.putExtra("approval_status", g.approvalStatus);
                intent.putExtra("guest_name", g.name);

                startActivity(intent);
            }
        });

        guestsArrayList.clear();
        guestListAdapter.clear();
        guestListAdapter.notifyDataSetChanged();
//        getData();
    }

    public void getData() {
        guestsArrayList.clear();
        guestListAdapter.clear();
        guestListAdapter.notifyDataSetChanged();
        dbReferenceEvent.child(eventId).child("guestList").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Guest guest = snapshot.getValue(Guest.class);
                guest.guestKey = snapshot.getKey();
                guestsArrayList.add(guest);
                guestListAdapter.notifyDataSetChanged();
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
    public void onResume() {
        super.onResume();
        guestsArrayList.clear();
        guestListAdapter.clear();
        guestListAdapter.notifyDataSetChanged();

        guestsListView.setAdapter(null);
        guestListAdapter = null;

        guestListAdapter = new GuestListGuestNameAdapter(this, R.layout.guest_list_adapter, guestsArrayList);
        guestsListView.setAdapter(guestListAdapter);

        getData();
    }
}