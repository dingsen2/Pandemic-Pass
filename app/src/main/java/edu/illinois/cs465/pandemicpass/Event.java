package edu.illinois.cs465.pandemicpass;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class Event {

    public String hostId;
    public String hostName;
    public String eventCode;
    public String eventName;
    public String date;
    public String time;
    public String location;
    public String description;
    public HashMap<String, Guest> guestList;
    public boolean acceptVaccinationRecord;
    public boolean acceptTestResult;
    public String id;

    public Event() {}

    public Event(String hostId, String hostName, String eventCode, String eventName,
                 String date, String time, String location, String description, HashMap<String, Guest> guestList,
                 boolean acceptVaccinationRecord, boolean acceptTestResult) {
        this.hostId = hostId;
        this.hostName = hostName;
        this.eventCode = eventCode;
        this.eventName = eventName;
        this.date = date;
        this.time = time;
        this.location = location;
        this.description = description;
        this.guestList = guestList;
        this.acceptVaccinationRecord = acceptVaccinationRecord;
        this.acceptTestResult = acceptTestResult;
    }

    private int getMonthFormat(String month) {
        if (month.equals("Jan"))
            return 1;
        if (month.equals("Feb"))
            return 2;
        if (month.equals("Mar"))
            return 3;
        if (month.equals("Apr"))
            return 4;
        if (month.equals("May"))
            return 5;
        if (month.equals("Jun"))
            return 6;
        if (month.equals("Jul"))
            return 7;
        if (month.equals("Aug"))
            return 8;
        if (month.equals("Sep"))
            return 9;
        if (month.equals("Oct"))
            return 10;
        if (month.equals("Nov"))
            return 11;
        if (month.equals("Dec"))
            return 12;
        return 1;
    }

    public int getMonth() {
        String [] splitted = date.split(" ");
        return getMonthFormat(splitted[0]);
    }

    public int getDay() {
        String [] splitted = date.split(" ");
        String month = splitted[1];
        int sepPos = month.indexOf(",");
        month = month.substring(0, sepPos);

        return Integer.parseInt(month);
    }

    public int getYear() {
        String [] splitted = date.split(" ");
        return Integer.parseInt(splitted[2]);
    }
}
