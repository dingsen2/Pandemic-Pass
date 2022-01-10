package edu.illinois.cs465.pandemicpass;

import com.google.firebase.database.Exclude;

import java.io.Serializable;

public class Member implements Serializable {
    @Exclude
    public String id;

    public String name;
    public String vaccinationRecordFileName;
    public String testResultFileName;

    public Member() {}

    public Member(String name, String vaccinationRecordFileName, String testResultFileName) {
        this.name = name;
        this.vaccinationRecordFileName = vaccinationRecordFileName;
        this.testResultFileName = testResultFileName;
    }

    @Override
    public String toString() {
        return this.name;
    }

    @Override
    public boolean equals(Object o) {
        return (o instanceof Member)
                && (this.id.equals(((Member) o).id));
    }
}