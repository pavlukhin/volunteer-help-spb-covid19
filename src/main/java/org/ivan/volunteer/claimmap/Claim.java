package org.ivan.volunteer.claimmap;

import java.util.Objects;

public class Claim {
    private final String id;
    private final String address;
    private final String details;

    public Claim(String id, String address, String details) {
        this.id = Objects.requireNonNull(id);
        this.address = Objects.requireNonNull(address);
        this.details = Objects.requireNonNull(details);
    }

    public String getId() {
        return id;
    }

    public String getAddress() {
        return address;
    }

    public String getDetails() {
        return details;
    }
}
