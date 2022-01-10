package edu.illinois.cs465.pandemicpass;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class UploadStatusAfterPickingMemberActivity extends AppCompatActivity implements View.OnClickListener{

    private Member member;
    private String currMemberStatus;

    private String userId;
    private DatabaseReference dbReferenceUserWithUserId;
    private DatabaseReference dbReferenceUserWithUserIdMembersMemberWithMemberId;

    private ActivityResultLauncher<String> activityResultLauncherForTestResult;
    private ActivityResultLauncher<String> activityResultLauncherForVaccinationRecord;

    private ImageView testResultImage;
    private Button uploadTestResult;

    private ImageView vaccinationRecordImage;
    private Button uploadVaccinationRecord;

    private TextView nameTextView;

    private StorageReference storageReference;
    private ProgressBar progressBar;

    private TextView approvalStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_status_afterpickingmem);

        if (getIntent() != null && getIntent().getExtras() != null
                && getIntent().hasExtra("member")
                && getIntent().hasExtra("status")) {
            member = (Member) getIntent().getSerializableExtra("member");
            currMemberStatus = (String) getIntent().getSerializableExtra("status");
        }
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        dbReferenceUserWithUserId = FirebaseDatabase.getInstance().getReference("User").child(userId);

        if (member != null) {
            dbReferenceUserWithUserIdMembersMemberWithMemberId = dbReferenceUserWithUserId.child("members").child(member.id);
        }

        nameTextView = (TextView) findViewById(R.id.name);
        approvalStatus = (TextView) findViewById(R.id.approval_status_result);

        if (member != null && currMemberStatus != null) {
            nameTextView.setText(member.name);
            approvalStatus.setText(currMemberStatus);
        }

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
                                    Glide.with(UploadStatusAfterPickingMemberActivity.this).load(vaccinationRecord).into(vaccinationRecordImage);
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
                                    Glide.with(UploadStatusAfterPickingMemberActivity.this).load(testResult).into(testResultImage);
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
        else if (id == R.id.uploadTestResult) {
            uploadMemberTestResult();
        }
        else if (id == R.id.uploadVaccinationRecord) {
            uploadMemberVaccinationRecord();
        }
    }

    private void uploadMemberTestResult() {
        activityResultLauncherForTestResult.launch("image/*");
    }


    private void uploadAndSetTestResult(Uri result) {
        String uploadPath = "images/" + userId + "/" + member.id + "/testResult";
        StorageReference testResultReference = storageReference.child(uploadPath);

        progressBar.setVisibility(View.VISIBLE);

        if(result == null){
            return;
        }

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

        if(result == null){
            System.out.println("result is none");
            return;
        }
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