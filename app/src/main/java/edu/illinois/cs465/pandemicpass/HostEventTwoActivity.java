// Reference Code: https://www.youtube.com/watch?v=qCoidM98zNk

package edu.illinois.cs465.pandemicpass;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;

import java.time.LocalTime;
import java.util.Calendar;
import java.util.Locale;

public class HostEventTwoActivity extends AppCompatActivity implements View.OnClickListener {

    private DatePickerDialog datePickerDialog;
    private Button eventDateButton;
    private Button timeButton;
    private EditText eventLocationEditText;
    private Button nextButton;
    private String eventName;
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
        setContentView(R.layout.activity_host_event_two);

        initDatePicker();
        eventDateButton = findViewById(R.id.eventDatePickerButton);
        eventDateButton.setText(getTodaysDate());

        nextButton = (Button) findViewById(R.id.hostEventTwoNextButton);
        nextButton.setOnClickListener(this);

        timeButton = (Button) findViewById(R.id.timeButton);

        eventLocationEditText = (EditText) findViewById(R.id.eventLocationEditText);

        initExtras();
    }

    public void popTimePicker(View view) {
        TimePickerDialog.OnTimeSetListener onTimeSetListener = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int selectHour, int selectMinute) {
                hour = selectHour;
                minute = selectMinute;

                hour24 = selectHour;
                minute24 = selectMinute;

//                timeButton.setText(String.format(Locale.getDefault(), "%02d:%02d"));
                String timeSet = "";
                if (hour > 12) {
                    hour -= 12;
                    timeSet = "PM";
                } else if (hour == 0) {
                    hour += 12;
                    timeSet = "AM";
                } else if (hour == 12){
                    timeSet = "PM";
                }else{
                    timeSet = "AM";
                }

                String min = "";
                if (minute < 10)
                    min = "0" + minute ;
                else
                    min = String.valueOf(minute);

                // Append in a StringBuilder
                String aTime = new StringBuilder().append(hour).append(':')
                        .append(min ).append(" ").append(timeSet).toString();
                timeButton.setText(aTime);
            }
        };

        TimePickerDialog timePickerDialog = new TimePickerDialog(this, onTimeSetListener, hour, minute, true);

        timePickerDialog.setTitle("Select Time");
        timePickerDialog.show();
    }

    private void initExtras() {
        eventName = getIntent().getExtras().getString("event_name");
        vaxAllowed = getIntent().getExtras().getBoolean("vax_allowed");
        testAllowed = getIntent().getExtras().getBoolean("test_allowed");
    }

    private String getTodaysDate() {
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        month = month + 1;
        int day = cal.get(Calendar.DAY_OF_MONTH);

        eventMonth = month;
        eventDay = day;
        eventYear = year;

        return makeDateString(month, day, year);
    }

    private void initDatePicker() {
        DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int day) {
                month = month + 1;
                String date = makeDateString(month, day, year);
                eventDateButton.setText(date);

                eventYear = year;
                eventMonth = month;
                eventDay = day;
            }
        };

        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);

        int style = AlertDialog.THEME_HOLO_LIGHT;

        datePickerDialog = new DatePickerDialog(this, style, dateSetListener, year, month, day);
    }

    private String makeDateString(int month, int day, int year) {
        return day + " " + getMonthFormat(month) + " " + year;
    }

    private String getMonthFormat(int month) {
        if (month == 1)
            return "JAN";
        if (month == 2)
            return "FEB";
        if (month == 3)
            return "MAR";
        if (month == 4)
            return "APR";
        if (month == 5)
            return "MAY";
        if (month == 6)
            return "JUN";
        if (month == 7)
            return "JUL";
        if (month == 8)
            return "AUG";
        if (month == 9)
            return "SEP";
        if (month == 10)
            return "OCT";
        if (month == 11)
            return "NOV";
        if (month == 12)
            return "DEC";
        return "JAN";
    }

    public void openEventDatePicker(View view) {
        datePickerDialog.show();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        if (id == R.id.hostEventTwoNextButton) {

            String eventLocation = eventLocationEditText.getText().toString().trim();
            if (eventLocation.isEmpty()) {
                eventLocationEditText.setError("Event location is required.");
                eventLocationEditText.requestFocus();
            } else if (timeButton.getText().equals("Select Time")) {
                timeButton.setError("Event Time is required.");
                timeButton.requestFocus();
            } else {
                Intent intent = new Intent(this, HostEventThreeActivity.class);

                intent.putExtra("event_name", eventName);
                intent.putExtra("vax_allowed", vaxAllowed);
                intent.putExtra("test_allowed", testAllowed);
                intent.putExtra("event_year", eventYear);
                intent.putExtra("event_month", eventMonth);
                intent.putExtra("event_day", eventDay);
                intent.putExtra("event_location", eventLocation);
                intent.putExtra("event_hour", hour);
                intent.putExtra("event_minute", minute);
                intent.putExtra("hour24", hour24);
                intent.putExtra("minute24", minute24);

                startActivity(intent);
            }
        }
    }
}