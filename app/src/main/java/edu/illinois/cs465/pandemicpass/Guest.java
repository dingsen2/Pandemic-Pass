package edu.illinois.cs465.pandemicpass;

public class Guest {
    public String name;
    public String userId;
    public String memberId;
    public String approvalStatus;
    public String guestKey; // might remove later

    public Guest() {}

    public Guest(String userId, String memberId, String name, String approvalStatus) {
        this.name = name;
        this.userId = userId;
        this.memberId = memberId;
        this.approvalStatus = approvalStatus;
    }

    @Override
    public String toString() {
        return this.name;
    }

    @Override
    public boolean equals(Object o) {
        return (o instanceof Guest)
                && (this.userId.equals(((Guest) o).userId))
                && (this.memberId.equals(((Guest) o).memberId));
    }
}
