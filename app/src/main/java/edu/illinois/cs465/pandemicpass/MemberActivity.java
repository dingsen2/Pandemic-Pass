package edu.illinois.cs465.pandemicpass;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;

public class MemberActivity extends AppCompatActivity implements View.OnClickListener {

    private Member member;

    private String userId;
    private DatabaseReference dbReferenceUserWithUserId;
    private DatabaseReference dbReferenceUserWithUserIdMembersMemberWithMemberId;

    private ActivityResultLauncher<String> activityResultLauncherForTestResult;
    private ActivityResultLauncher<String> activityResultLauncherForVaccinationRecord;
    private StorageReference storageReference;

    private ImageView testResultImage;
    private Button uploadTestResult;

    private ImageView vaccinationRecordImage;
    private Button uploadVaccinationRecord;

    private EditText nameEditText;
    private Button saveName;

    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_member);

        if (getIntent() != null && getIntent().getExtras() != null
                && getIntent().hasExtra("member")) {
            member = (Member) getIntent().getSerializableExtra("member");
        }

        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        dbReferenceUserWithUserId = FirebaseDatabase.getInstance().getReference("User").child(userId);

        if (member != null) {
            dbReferenceUserWithUserIdMembersMemberWithMemberId = dbReferenceUserWithUserId.child("members").child(member.id);
        }

        nameEditText = (EditText) findViewById(R.id.name);

        if (member != null) {
            nameEditText.setText(member.name);
        }

        saveName = (Button) findViewById(R.id.saveName);
        saveName.setOnClickListener(this);

        testResultImage = (ImageView) findViewById(R.id.testResultImage);
        uploadTestResult = (Button) findViewById(R.id.uploadTestResult);
        uploadTestResult.setOnClickListener(this);

        vaccinationRecordImage = (ImageView) findViewById(R.id.vaccinationRecordImage);
        uploadVaccinationRecord = (Button) findViewById(R.id.uploadVaccinationRecord);
        uploadVaccinationRecord.setOnClickListener(this);


        storageReference = FirebaseStorage.getInstance().getReference();
        activityResultLauncherForTestResult = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                new ActivityResultCallback<Uri>() {
                    @Override
                    public void onActivityResult(Uri result) {
                        uploadAndSetTestResult(result);
                    }
                }
        );

        activityResultLauncherForVaccinationRecord = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                new ActivityResultCallback<Uri>() {
                    @Override
                    public void onActivityResult(Uri result) {
                        uploadAndSetVaccinationRecord(result);
                    }
                }
        );

        if (member != null) {
            if (!member.vaccinationRecordFileName.isEmpty()) {
                storageReference.child(member.vaccinationRecordFileName).getDownloadUrl()
                        .addOnCompleteListener(new OnCompleteListener<Uri>() {
                            @Override
                            public void onComplete(@NonNull Task<Uri> task) {
                                if (task.isSuccessful()) {
                                    Uri vaccinationRecord = task.getResult();
                                    Glide.with(MemberActivity.this).load(vaccinationRecord).into(vaccinationRecordImage);
                                } else {
                                    // TODO: change image to be blank
                                    vaccinationRecordImage.setImageResource(android.R.color.darker_gray);
                                }
                            }
                        });
            }

            if (!member.testResultFileName.isEmpty()) {
                storageReference.child(member.testResultFileName).getDownloadUrl()
                        .addOnCompleteListener(new OnCompleteListener<Uri>() {
                            @Override
                            public void onComplete(@NonNull Task<Uri> task) {
                                if (task.isSuccessful()) {
                                    Uri testResult = task.getResult();
                                    Glide.with(MemberActivity.this).load(testResult).into(testResultImage);
                                } else {
                                    // TODO: change image to be blank
                                    testResultImage.setImageResource(android.R.color.darker_gray);
                                }
                            }
                        });
            }
        }


        progressBar = (ProgressBar) findViewById(R.id.progressBar);
    }


    @Override
    public void onClick(View view) {
        int id = view.getId();

        if (member == null) {
            return;
        }

        if (id == R.id.saveName) {
            saveMemberName();
        }
        else if (id == R.id.uploadTestResult) {
            uploadMemberTestResult();
        }
        else if (id == R.id.uploadVaccinationRecord) {
            uploadMemberVaccinationRecord();
        }
    }

    private void saveMemberName() {
        String newMemberName = nameEditText.getText().toString().trim();

        if (newMemberName.isEmpty()) {
            Toast.makeText(this, "New name cannot be empty.", Toast.LENGTH_LONG).show();
            return;
        }

        dbReferenceUserWithUserIdMembersMemberWithMemberId.child("name").setValue(newMemberName).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Query eventAttendance = dbReferenceUserWithUserId.child("eventAttendance").orderByKey();

                    eventAttendance.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            for (DataSnapshot eventAttendanceChild: dataSnapshot.getChildren()) {
                                String eventAttendanceEventKey = eventAttendanceChild.getKey();
                                Log.e("firebase", "event attendance key " + eventAttendanceEventKey);

                                EventAttendanceEntry eventAttendanceEntry = eventAttendanceChild.getValue(EventAttendanceEntry.class);
                                String eventId = eventAttendanceEntry.eventId;


                                FirebaseDatabase.getInstance().getReference("Event").child(eventId).child("guestList").orderByChild("memberId").equalTo(member.id).addListenerForSingleValueEvent(
                                            new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                                        if (dataSnapshot.exists()) {

                                                            for (DataSnapshot d : dataSnapshot.getChildren()) {
                                                                String guestListEntryKey = d.getKey();
                                                                FirebaseDatabase.getInstance().getReference("Event").child(eventId).child("guestList").child(guestListEntryKey).child("name").setValue(newMemberName);
                                                            }
                                                        }
                                                    }

                                                    @Override
                                                    public void onCancelled(DatabaseError databaseError) {

                                                    }
                                                }
                                        );
                                    }
                                }

                               @Override
                               public void onCancelled(DatabaseError databaseError) {

                               }

                    });
                    Toast.makeText(MemberActivity.this, "Updated name.", Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(MemberActivity.this, "Failed to update name.", Toast.LENGTH_SHORT).show();
                }
            }
        });


    }

    private void uploadMemberTestResult() {
        activityResultLauncherForTestResult.launch("image/*");
    }

    private void uploadAndSetTestResult(Uri result) {
        String uploadPath = "images/" + userId + "/" + member.id + "/testResult";
        StorageReference testResultReference = storageReference.child(uploadPath);

        progressBar.setVisibility(View.VISIBLE);

        testResultReference.putFile(result)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        String memberPath = userId + "/members/" + member.id + "/testResult";
                        dbReferenceUserWithUserIdMembersMemberWithMemberId.child("testResultFileName").setValue(uploadPath).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    testResultImage.setImageURI(result);
                                    progressBar.setVisibility(View.INVISIBLE);
                                }
                                else {
                                    Toast.makeText(getApplicationContext(), "Failed to upload test result.", Toast.LENGTH_LONG).show();
                                    progressBar.setVisibility(View.INVISIBLE);
                                }
                            }
                        });

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(), "Failed to upload test result.", Toast.LENGTH_LONG).show();
                        progressBar.setVisibility(View.INVISIBLE);
                    }
                });

    }

    private void uploadMemberVaccinationRecord() {
        activityResultLauncherForVaccinationRecord.launch("image/*");
    }

    private void uploadAndSetVaccinationRecord(Uri result) {
        String uploadPath = "images/" + userId + "/" + member.id + "/vaccinationRecord";
        StorageReference testResultReference = storageReference.child(uploadPath);

        progressBar.setVisibility(View.VISIBLE);

        testResultReference.putFile(result)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        String memberPath = userId + "/members/" + member.id + "/vaccinationRecord";
                        dbReferenceUserWithUserIdMembersMemberWithMemberId.child("vaccinationRecordFileName").setValue(uploadPath).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    vaccinationRecordImage.setImageURI(result);
                                    progressBar.setVisibility(View.INVISIBLE);
                                }
                                else {
                                    Toast.makeText(getApplicationContext(), "Failed to upload vaccination record.", Toast.LENGTH_LONG).show();
                                    progressBar.setVisibility(View.INVISIBLE);
                                }
                            }
                        });

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(), "Failed to upload vaccination record.", Toast.LENGTH_LONG).show();
                        progressBar.setVisibility(View.INVISIBLE);
                    }
                });

    }
}