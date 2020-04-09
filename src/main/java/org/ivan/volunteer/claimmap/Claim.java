package org.ivan.volunteer.claimmap;

import java.util.Objects;

public class Claim {
    public enum Status {
        OPEN, IN_PROGRESS
    }

    private final String id;
    private final String address;
    private final String details;
    private final Status status;

    public Claim(String id, String address, String details, Status status) {
        this.id = Objects.requireNonNull(id);
        this.address = Objects.requireNonNull(address);
        this.details = Objects.requireNonNull(details);
        this.status = Objects.requireNonNull(status);
    }

    // By default getters are needed to include fields to JSON

    public String getId() {
        return id;
    }

    public String getAddress() {
        return address;
    }

    public String getDetails() {
        return details;
    }

    public Status getStatus() {
        return status;
    }
}
