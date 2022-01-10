package edu.illinois.cs465.pandemicpass;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class HostEventCodeActivity extends AppCompatActivity implements View.OnClickListener {

    private Button homeButton;
    private Button ShareCodeButton;
    private Button ViewEventButton;
    private TextView eventCodeTextView;
    private String eventCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_code_host);

        homeButton = (Button) findViewById(R.id.hostEventHomeButton);
        homeButton.setOnClickListener(this);

        ShareCodeButton = (Button) findViewById(R.id.ShareCodeButton);
        ShareCodeButton.setOnClickListener(this);

        ViewEventButton = (Button) findViewById(R.id.ViewEventButton);
        ViewEventButton.setOnClickListener(this);

        eventCodeTextView = (TextView) findViewById(R.id.eventCode);

        eventCode = getIntent().getExtras().getString("event_code");
        eventCodeTextView.setText(eventCode);
    }

    private void EmailAndTextIntent() {
        /*Create an ACTION_SEND Intent*/
        Intent intent = new Intent(android.content.Intent.ACTION_SEND);
        /*This will be the actual content you wish you share.*/
        String shareBody = eventCode;
        /*The type of the content is text, obviously.*/
        intent.setType("text/plain");
        /*Applying information Subject and Body.*/
        intent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
        /*Fire!*/
        startActivity(Intent.createChooser(intent, "Share With:"));
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        if (id == R.id.hostEventHomeButton) {
            startActivity(new Intent(HostEventCodeActivity.this, HomeScreenActivity.class));
        } else if (id == R.id.ShareCodeButton) {
            EmailAndTextIntent();
        } else if (id == R.id.ViewEventButton) {
            Intent intent = new Intent(HostEventCodeActivity.this, EventDetailsForHostActivity.class);
            intent.putExtra("event_code", eventCode);
            startActivity(intent);
        }
    }
}