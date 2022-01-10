package edu.illinois.cs465.pandemicpass;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class MemberListActivity extends AppCompatActivity implements View.OnClickListener {

    private String userId;

    private DatabaseReference dbReferenceUserWithUserId;
    private DatabaseReference dbReferenceUserWithUserIdMembers;

    private EditText memberNameEditText;
    private Button addMember;
    private ListView memberListView;
    private ArrayList<Member> memberArrayList;

    AlertDialog.Builder builder;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_member_list);

//        InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
//        imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);

        if (getIntent().getExtras() != null) {

            TextView title = new TextView(this);
            title.setText("New User Requirements");
            title.setPadding(10, 10, 10, 10);
            title.setGravity(Gravity.CENTER);
            title.setTextColor(Color.BLACK);
            title.setTextSize(23);

            TextView msg = new TextView(this);
            msg.setText("Add at least one member to your profile so you can join and host events! Clicking on members allow you to upload vaccine and test images!");
            msg.setPadding(10, 10, 10, 10);
            msg.setTextColor(Color.BLACK);
            msg.setGravity(Gravity.CENTER);
            msg.setTextSize(18);

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setCustomTitle(title);
            builder.setView(msg);
            builder.setCancelable(false);

            DialogInterface.OnClickListener onClick = new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int which) {
                    if (which == DialogInterface.BUTTON_POSITIVE) {
                        dialog.dismiss();
                    }
                }

            };

            builder.setPositiveButton("Okay", onClick);
            AlertDialog dialog = builder.create();
            dialog.show();
        }

        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        dbReferenceUserWithUserId = FirebaseDatabase.getInstance()
                .getReference("User").child(userId);
        dbReferenceUserWithUserIdMembers = dbReferenceUserWithUserId.child("members");

        memberNameEditText = (EditText) findViewById(R.id.memberName);

        addMember = (Button) findViewById(R.id.addMember);
        addMember.setOnClickListener(this);

        memberArrayList = new ArrayList<Member>();

        memberListView = (ListView) findViewById(R.id.memberList);

        MemberListAdapter memberListAdapter = new MemberListAdapter(this, R.layout.member_list_adapter, memberArrayList, dbReferenceUserWithUserIdMembers, dbReferenceUserWithUserId, userId);

        memberListView.setAdapter(memberListAdapter);

        memberListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Member member = (Member) adapterView.getItemAtPosition(i);
                Intent profileIntent = new Intent(MemberListActivity.this, MemberActivity.class);
                profileIntent.putExtra("member", member);
                startActivity(profileIntent);
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

        if (id == R.id.addMember) {
            addMemberToMemberList();
        }
    }

    private void addMemberToMemberList() {
        String memberName = memberNameEditText.getText().toString().trim();
        if (!memberName.isEmpty()) {
            Member member = new Member(memberName, "", "");
            dbReferenceUserWithUserIdMembers.push().setValue(member);
        }
    }
}