package edu.illinois.cs465.pandemicpass;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.UUID;

public class DbTestActivity extends AppCompatActivity implements View.OnClickListener {

    private String userId;
    private Calendar calendar;
    private DateFormat dateFormatOnlyDate;
    private DateFormat dateFormatOnlyTime;

    private DatabaseReference dbReferenceEvent;
    private DatabaseReference dbReferenceUser;

    private Button createEvent;
    private Button uploadPicture;
    private Button getMembers;

    private EditText memberNameEditText;
    private Button addMember;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_db_test);

        dbReferenceEvent = FirebaseDatabase.getInstance()
                .getReference("Event");

        dbReferenceUser = FirebaseDatabase.getInstance()
                .getReference("User");

        createEvent = (Button) findViewById(R.id.createEvent);
        createEvent.setOnClickListener(this);

        uploadPicture = (Button) findViewById(R.id.uploadPicture);
        uploadPicture.setOnClickListener(this);

        memberNameEditText = (EditText) findViewById(R.id.memberName);

        addMember = (Button) findViewById(R.id.addMember);
        addMember.setOnClickListener(this);

        getMembers = (Button) findViewById(R.id.getMembers);
        getMembers.setOnClickListener(this);

        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        dateFormatOnlyDate = DateFormat.getDateInstance();
        dateFormatOnlyTime = DateFormat.getTimeInstance(DateFormat.SHORT);

        calendar = Calendar.getInstance();

        // testing out a bunch of events objects (Zhengyu)
        Event event1 = new Event();
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();

        if (id == R.id.createEvent) {
            createDummyEvent();
        }
        else if (id == R.id.uploadPicture) {
            upload();
        }
        else if (id == R.id.addMember) {
            add();
        }
        else if (id == R.id.getMembers) {
            getMemberNames();
        }
    }

    private void createDummyEvent() {
        Log.d("event creation", "CREATE DUMMY EVENT");

        String generated_code = UUID.randomUUID().toString().substring(0, 7);

        dbReferenceEvent.orderByChild("eventCode").equalTo(generated_code).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    String eventDate = dateFormatOnlyDate.format(calendar.getTime());
                    String eventTime = dateFormatOnlyTime.format(calendar.getTime());

                    Event event = new Event(userId, "tony", generated_code, "tony's event", eventDate, eventTime, "home", "11123", new HashMap<String, Guest>(), true, true);

                    dbReferenceEvent.push().setValue(event).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if (task.isSuccessful()) {
                                // will probably remove the toast for success and just redirect instead
                                Toast.makeText(DbTestActivity.this, "Success", Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(DbTestActivity.this, "Fail", Toast.LENGTH_LONG).show();
                            }

                        }
                    });
                }
                else {
                    Log.e("firebase", "duplicate key");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void upload() {

    }

    private void add() {
        String memberName = memberNameEditText.getText().toString();
        Member member = new Member(memberName, "", "");
        dbReferenceUser.child(userId).child("members").push().setValue(member).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if (task.isSuccessful()) {
                    // will probably remove the toast for success and just redirect instead
                    Toast.makeText(DbTestActivity.this, "Success", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(DbTestActivity.this, "Fail", Toast.LENGTH_LONG).show();
                }

            }
        });

    }

    // turn database stuff into objects
    // add listview
    // make listview dynamic when members added / deleted

    private void getMemberNames() {
        dbReferenceUser.child(userId).child("members").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot memberSnapshot: dataSnapshot.getChildren()) {
                    String id = memberSnapshot.getKey();
                    Member member = memberSnapshot.getValue(Member.class);
                    member.id = id;
                    Log.e("firebase", makeStringFromMember(member));
                }
                Log.e("firebase", "change");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private String makeStringFromMember(Member m) {
        return (m.id + " " + m.name + " " + m.testResultFileName + " " + m.vaccinationRecordFileName);
    }
}