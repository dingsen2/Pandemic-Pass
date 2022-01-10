package edu.illinois.cs465.pandemicpass;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MemberListAdapter extends ArrayAdapter<Member> {
    private ArrayList<Member> memberArrayList;
    private Context context;
    private int resource;
    private DatabaseReference dbReferenceUserWithUserIdMembers;
    private DatabaseReference dbReferenceUserWithUserId;
    private DatabaseReference dbReferenceEvent;
    private String userId;
    private StorageReference storageReference;

    public MemberListAdapter(@NonNull Context context, int resource, @NonNull ArrayList<Member> memberArrayList, DatabaseReference dbReferenceUserWithUserIdMembers, DatabaseReference dbReferenceUserWithUserId, String userId) {
        super(context, resource, memberArrayList);
        this.context = context;
        this.resource = resource;
        this.memberArrayList = memberArrayList;
        this.dbReferenceUserWithUserIdMembers = dbReferenceUserWithUserIdMembers;
        this.dbReferenceUserWithUserId = dbReferenceUserWithUserId;
        this.dbReferenceEvent = FirebaseDatabase.getInstance()
                .getReference("Event");
        this.userId = userId;
        this.storageReference = FirebaseStorage.getInstance().getReference();

    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        String name = getItem(position).name;

        LayoutInflater inflater = LayoutInflater.from(context);
        convertView = inflater.inflate(resource, parent, false);

        TextView nameTextView = (TextView) convertView.findViewById(R.id.name);
        nameTextView.setText(name);

        Button remove = (Button) convertView.findViewById(R.id.remove);

        remove.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                //do something
                Member member = memberArrayList.get(position);

                Query eventAttendance = dbReferenceUserWithUserId.child("eventAttendance").orderByKey();

                eventAttendance.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        for (DataSnapshot eventAttendanceChild: dataSnapshot.getChildren()) {
                            String eventAttendanceEventKey = eventAttendanceChild.getKey();
                            Log.e("firebase", "event attendance key " + eventAttendanceEventKey);

                            EventAttendanceEntry eventAttendanceEntry = eventAttendanceChild.getValue(EventAttendanceEntry.class);
                            String eventId = eventAttendanceEntry.eventId;

                            if (eventAttendanceEntry.attendingMemberList == null) {
                                dbReferenceUserWithUserId.child("eventAttendance").child(eventAttendanceEventKey).removeValue();
                                continue;
                            }


                            for (Map.Entry event : eventAttendanceEntry.attendingMemberList.entrySet()) {

                                HashMap<String, String> attendingMap = (HashMap<String, String>) event.getValue();
                                String keyToAttendingMemberId = (String) event.getKey();
                                String attendingMemberId = attendingMap.get("memberId");

                                if (attendingMemberId.equals(member.id)) {
                                    dbReferenceEvent.child(eventId).child("guestList").orderByChild("memberId").equalTo(member.id).addListenerForSingleValueEvent(
                                            new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                    if (dataSnapshot.exists()) {

                                                        for (DataSnapshot memberUUID : dataSnapshot.getChildren()) {
                                                            Log.e("firebase", "member UUID key " + memberUUID.getKey());
                                                            Log.e("firebase", "delete member " + member.id + " from guest list");
                                                            dbReferenceEvent.child(eventId).child("guestList").child(memberUUID.getKey()).removeValue();
                                                            dbReferenceUserWithUserId.child("eventAttendance").child(eventAttendanceEventKey).child("attendingMemberList").child(keyToAttendingMemberId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    if (task.isSuccessful()) {

                                                                        dbReferenceUserWithUserId.child("eventAttendance").child(eventAttendanceEventKey).orderByKey().equalTo("attendingMemberList").addValueEventListener(new ValueEventListener() {
                                                                            @Override
                                                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                                                if (!snapshot.exists()) {
                                                                                    // attendingMembersList is empty, dpes not exist
                                                                                    dbReferenceUserWithUserId.child("eventAttendance").child(eventAttendanceEventKey).removeValue();
                                                                                    Log.e("firebase", "attendingMemberList does not exist");
                                                                                }
                                                                                else {
                                                                                    Log.e("firebase", "attendingMemberList existd");
                                                                                }
                                                                            }

                                                                            @Override
                                                                            public void onCancelled(@NonNull DatabaseError error) {

                                                                            }
                                                                        });
                                                                    }
                                                                }
                                                            });


                                                            dbReferenceUserWithUserIdMembers.child(member.id).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    if (task.isSuccessful()) {
                                                                        memberArrayList.remove(member);
                                                                        notifyDataSetChanged();
                                                                    }
                                                                }
                                                            });

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

                        }

                        deleteUploadedImages(member.id);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }
        });

        return convertView;
    }

    private void deleteUploadedImages(String memberId) {
        String testResultUploadPath = "images/" + userId + "/" + memberId + "/testResult";
        StorageReference testResultReference = storageReference.child(testResultUploadPath);
        testResultReference.delete();

        String vaccinationRecordUploadPath = "images/" + userId + "/" + memberId + "/vaccinationRecord";
        StorageReference vaccinationRecordReference = storageReference.child(vaccinationRecordUploadPath);
        vaccinationRecordReference.delete();
    }
}
