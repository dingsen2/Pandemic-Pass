package edu.illinois.cs465.pandemicpass;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;

public class HostEventActivity extends AppCompatActivity implements View.OnClickListener {

    private Button nextButton;
    private EditText eventName;
    private Switch vaxSwitch;
    private Switch testSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_host_event_one);

        nextButton = (Button) findViewById(R.id.hostEventOneNextButton);
        nextButton.setOnClickListener(this);

        eventName = (EditText) findViewById(R.id.eventName);

        vaxSwitch = (Switch) findViewById(R.id.vaxSwitch);
        testSwitch = (Switch) findViewById(R.id.testSwitch);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        if (id == R.id.hostEventOneNextButton) {
            String eventStr = eventName.getText().toString().trim();
            if (eventStr.isEmpty()) {
                eventName.setError("Event name is required.");
                eventName.requestFocus();
            } else {
                Intent intent = new Intent(this, HostEventTwoActivity.class);

                intent.putExtra("event_name", eventStr);

                boolean vaxAllowed = vaxSwitch.isChecked();
                intent.putExtra("vax_allowed", vaxAllowed);

                boolean testAllowed = testSwitch.isChecked();
                intent.putExtra("test_allowed", testAllowed);

                startActivity(intent);
            }
        }
    }
}