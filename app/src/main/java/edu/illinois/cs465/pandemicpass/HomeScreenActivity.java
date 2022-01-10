package edu.illinois.cs465.pandemicpass;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

public class HomeScreenActivity extends AppCompatActivity implements View.OnClickListener {

    private Button joinEvent;
    private Button hostEvent;
    private Button viewEvents;
    private ImageButton userProfile;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen);

        joinEvent = (Button) findViewById(R.id.joinEvent);
        joinEvent.setOnClickListener(this);

        hostEvent = (Button) findViewById(R.id.hostEvent);
        hostEvent.setOnClickListener(this);

        viewEvents = (Button) findViewById(R.id.viewEvents);
        viewEvents.setOnClickListener(this);

        userProfile = (ImageButton) findViewById(R.id.userProfile);
        userProfile.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();

        if (id == R.id.joinEvent) {
            startActivity(new Intent(HomeScreenActivity.this, JoinEventCodeActivity.class));
        }
        else if (id == R.id.hostEvent) {
            startActivity(new Intent(HomeScreenActivity.this, HostEventActivity.class));
        }
        else if (id == R.id.viewEvents) {
            startActivity(new Intent(HomeScreenActivity.this, EventListActivity.class));
        }
        else if (id == R.id.userProfile) {
            // TODO for tony
            startActivity(new Intent(HomeScreenActivity.this, MemberListActivity.class));
        }
    }
}