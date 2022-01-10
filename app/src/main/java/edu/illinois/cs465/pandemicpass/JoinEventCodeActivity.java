package edu.illinois.cs465.pandemicpass;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class JoinEventCodeActivity extends AppCompatActivity implements View.OnClickListener {

    private Button next;
    private TextView eventCodeTextView;
    private String userId;
    private DatabaseReference dbReferenceEvent;
    private DatabaseReference dbReferenceUserWithUserIdEventAttendance;
    private Event e;
    private String eventId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_event_code);

        dbReferenceEvent = FirebaseDatabase.getInstance().getReference("Event");
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        dbReferenceUserWithUserIdEventAttendance = FirebaseDatabase.getInstance().getReference("User").child(userId).child("eventAttendance");

        eventCodeTextView = (TextView) findViewById(R.id.eventCode);

        next = (Button) findViewById(R.id.next);
        next.setOnClickListener(this);

        e = null;
        eventId = "";
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        if (id == R.id.next) {
            verifyEventCodeAndRedirect();
        }
    }

    public void verifyEventCodeAndRedirect() {
        String eventCode = eventCodeTextView.getText().toString().trim();
        if (eventCode.isEmpty()) {
            return;
        }

        dbReferenceEvent.orderByChild("eventCode").equalTo(eventCode).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    Toast.makeText(JoinEventCodeActivity.this, "Invalid event code", Toast.LENGTH_LONG).show();
                }
                else {
                    // can't join an event you're already attending
                    // can still join event you're hosting (maybe because you want everyone included in event details)



                    Intent selectMembersIntent = new Intent(JoinEventCodeActivity.this, JoinSelectMembersActivity.class);

                    for(DataSnapshot snap: snapshot.getChildren()) {
                        eventId = snap.getKey();
                        e = (Event) snap.getValue(Event.class);
                        break;
                    }

                    dbReferenceUserWithUserIdEventAttendance.orderByChild("eventId").equalTo(eventId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (!snapshot.exists()) {
                                String eventName = e.eventName;
                                String eventDate = e.date;
                                selectMembersIntent.putExtra("eventId", eventId);
                                selectMembersIntent.putExtra("eventName", eventName);
                                selectMembersIntent.putExtra("eventDate", eventDate);
                                selectMembersIntent.putExtra("eventCode", eventCode);
                                startActivity(selectMembersIntent);
                                JoinEventCodeActivity.this.finish();
                            }
                            else {
                                Toast.makeText(JoinEventCodeActivity.this, "You've already joined this event.", Toast.LENGTH_LONG).show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });



                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

}