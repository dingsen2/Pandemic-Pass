package edu.illinois.cs465.pandemicpass;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

public class HostApproveAndDeny extends AppCompatActivity implements View.OnClickListener {

    private TextView guestNameTextView;
    private TextView guestApprovalStatusTextView;
    private TextView guestVaccineStatus;
    private TextView guestTestingStatus;
    private TextView testingStatusTextTextView;
    private TextView vaccineStatusTextTextView;

    private Button viewVaccineButton;
    private Button viewTestButton;
    private Button acceptButton;
    private Button denyButton;

    FrameLayout mainFrame;

    private DatabaseReference dbReferenceEvent;
    private DatabaseReference dbReferenceUser;
    private DatabaseReference dbReferenceUserWithUserIdMembersMemberWithMemberId;

    private ActivityResultLauncher<String> activityResultLauncherForTestResult;
    private ActivityResultLauncher<String> activityResultLauncherForVaccinationRecord;
    private StorageReference storageReference;

    private String guestId;
    private String eventId;
    private String memberId;
    private String userId;
    private String guestKey;
    private String guestName;
    private String guestApprovalStatus;

    private Event event;
    private Guest guest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_host_approve_and_deny);

        userId = getIntent().getExtras().getString("guest_id");
        eventId = getIntent().getExtras().getString("event_id");
        memberId = getIntent().getExtras().getString("member_id");
        guestKey = getIntent().getExtras().getString("guest_key");
        guestApprovalStatus = getIntent().getExtras().getString("approval_status");
        guestName = getIntent().getExtras().getString("guest_name");

        guestNameTextView = (TextView) findViewById(R.id.hostViewGuestName);
        guestApprovalStatusTextView = (TextView) findViewById(R.id.hostViewGuestapproval_status);
        guestVaccineStatus = (TextView) findViewById(R.id.hostViewGuestvaccine_status);
        guestTestingStatus = (TextView) findViewById(R.id.hostViewGuesttesting_status);

        testingStatusTextTextView = (TextView) findViewById(R.id.hostViewTestingStatusText);
        vaccineStatusTextTextView = (TextView) findViewById(R.id.HostViewVaccineStatusText);

        viewVaccineButton = (Button) findViewById(R.id.hostViewGuestVaccine);
        viewVaccineButton.setOnClickListener(this);

        viewTestButton = (Button) findViewById(R.id.hostViewGuestTest);
        viewTestButton.setOnClickListener(this);

        acceptButton = (Button) findViewById(R.id.acceptButton);
        acceptButton.setOnClickListener(this);

        denyButton = (Button) findViewById(R.id.denyButton);
        denyButton.setOnClickListener(this);

        mainFrame = (FrameLayout) findViewById(R.id.MainFrame);

        dbReferenceEvent = FirebaseDatabase.getInstance()
                .getReference("Event");

        dbReferenceUser = FirebaseDatabase.getInstance()
                .getReference("User");

        storageReference = FirebaseStorage.getInstance().getReference();

        Log.d("TESTINGUGH", "userId: " + userId);
        Log.d("TESTINGUGH", "memberId: " + memberId);
        Log.d("TESTINGUGH", "guestKey: " + guestKey);

        getEventDetails();
    }

    private void getEventDetails() {
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

    private void renderScreen() {
        // need to get the member here
        dbReferenceUser.child(userId).child("members").child(memberId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Member member = snapshot.getValue(Member.class);

                if (!event.acceptTestResult) {
                    testingStatusTextTextView.setVisibility(View.GONE);
                    guestTestingStatus.setVisibility(View.GONE);
                    viewTestButton.setVisibility(View.GONE);

                } else {
                    // check and see if they have something uploaded
                    if (member.vaccinationRecordFileName == null || member.vaccinationRecordFileName.equals("")) {
                        guestTestingStatus.setText("No Submission");
                        viewTestButton.setVisibility(View.GONE);
                    } else {
                        guestTestingStatus.setText("Submitted");
                    }
                }

                if (!event.acceptVaccinationRecord) {
                    vaccineStatusTextTextView.setVisibility(View.GONE);
                    guestVaccineStatus.setVisibility(View.GONE);
                    viewVaccineButton.setVisibility(View.GONE);
                } else {
                    // check and see if they have something uploaded
                    if (member.testResultFileName == null || member.testResultFileName.equals("")) {
                        guestVaccineStatus.setText("No Submission");
                        viewVaccineButton.setVisibility(View.GONE);
                    } else {
                        guestVaccineStatus.setText("Submitted");
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        guestNameTextView.setText(guestName);
        guestApprovalStatusTextView.setText(guestApprovalStatus);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        if (id == R.id.hostViewGuestVaccine) {
            dbReferenceUser.child(userId).child("members").child(memberId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Member member = snapshot.getValue(Member.class);
                    if (!member.vaccinationRecordFileName.isEmpty()) {
                        storageReference.child(member.vaccinationRecordFileName).getDownloadUrl()
                                .addOnCompleteListener(new OnCompleteListener<Uri>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Uri> task) {
                                        if (task.isSuccessful()) {
                                            Uri vaccinationRecord = task.getResult();
                                            Log.d("HMMM", vaccinationRecord.toString());

                                            ImageView overlay = new ImageView(HostApproveAndDeny.this);
                                            Glide.with(HostApproveAndDeny.this).load(vaccinationRecord).into(overlay);
                                            mainFrame.addView(overlay);

                                            overlay.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    mainFrame.removeView(overlay);
                                                }
                                            });
                                        } else {
                                            // TODO: something
                                        }
                                    }
                                });
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        } else if (id == R.id.hostViewGuestTest) {
            dbReferenceUser.child(userId).child("members").child(memberId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Member member = snapshot.getValue(Member.class);
                    if (!member.testResultFileName.isEmpty()) {
                        storageReference.child(member.testResultFileName).getDownloadUrl()
                                .addOnCompleteListener(new OnCompleteListener<Uri>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Uri> task) {
                                        if (task.isSuccessful()) {
                                            Uri vaccinationRecord = task.getResult();
                                            Log.d("HMMM", vaccinationRecord.toString());

                                            ImageView overlay = new ImageView(HostApproveAndDeny.this);
                                            Glide.with(HostApproveAndDeny.this).load(vaccinationRecord).into(overlay);
                                            mainFrame.addView(overlay);

                                            overlay.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    mainFrame.removeView(overlay);
                                                }
                                            });
                                        } else {
                                            // TODO: something
                                        }
                                    }
                                });
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        } else if (id == R.id.acceptButton) {
            dbReferenceEvent.child(eventId).child("guestList").child(guestKey).child("approvalStatus").setValue("Approved");
            guestApprovalStatusTextView.setText("Approved");

        } else if (id == R.id.denyButton) {
            dbReferenceEvent.child(eventId).child("guestList").child(guestKey).child("approvalStatus").setValue("Denied");
            guestApprovalStatusTextView.setText("Denied");
        }
    }
}