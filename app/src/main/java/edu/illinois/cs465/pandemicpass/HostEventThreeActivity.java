package edu.illinois.cs465.pandemicpass;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class HostEventThreeActivity extends AppCompatActivity implements View.OnClickListener{

    private Button nextButton;
    private EditText eventDescriptionEditText;
    private String eventName;
    private String eventLocation;
    private boolean vaxAllowed;
    private boolean testAllowed;
    private int eventMonth;
    private int eventDay;
    private int eventYear;
    private int hour;
    private int minute;
    private int hour24;
    private int minute24;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_host_event_three);

        eventDescriptionEditText = (EditText) findViewById(R.id.eventDescriptionEditText);

        nextButton = (Button) findViewById(R.id.hostEventThreeNextButton);
        nextButton.setOnClickListener(this);

        initExtras();
    }

    private void initExtras() {
        eventName = getIntent().getExtras().getString("event_name");
        vaxAllowed = getIntent().getExtras().getBoolean("vax_allowed");
        testAllowed = getIntent().getExtras().getBoolean("test_allowed");

        eventLocation = getIntent().getExtras().getString("event_location");
        eventMonth = getIntent().getExtras().getInt("event_month");
        eventDay = getIntent().getExtras().getInt("event_day");
        eventYear = getIntent().getExtras().getInt("event_year");
        hour = getIntent().getExtras().getInt("event_hour");
        minute = getIntent().getExtras().getInt("event_minute");
        hour24 = getIntent().getExtras().getInt("hour24");
        minute24 = getIntent().getExtras().getInt("minute24");
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        if (id == R.id.hostEventThreeNextButton) {

            String eventDescription = eventDescriptionEditText.getText().toString().trim();
            if (eventDescription.isEmpty()) {
                eventDescriptionEditText.setError("Event description is required.");
                eventDescriptionEditText.requestFocus();
            } else {
                Intent intent = new Intent(this, HostEventFourActivity.class);

                intent.putExtra("event_name", eventName);
                intent.putExtra("vax_allowed", vaxAllowed);
                intent.putExtra("test_allowed", testAllowed);
                intent.putExtra("event_year", eventYear);
                intent.putExtra("event_month", eventMonth);
                intent.putExtra("event_day", eventDay);
                intent.putExtra("event_location", eventLocation);
                intent.putExtra("event_description", eventDescription);
                intent.putExtra("event_hour", hour);
                intent.putExtra("event_minute", minute);
                intent.putExtra("hour24", hour24);
                intent.putExtra("minute24", minute24);

                startActivity(intent);
            }
        }
    }
}