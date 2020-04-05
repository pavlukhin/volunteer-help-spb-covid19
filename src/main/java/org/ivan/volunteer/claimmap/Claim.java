package org.ivan.volunteer.claimmap;

import java.util.Objects;

public class Claim {
    private final String id;
    private final String address;

    public Claim(String id, String address) {
        this.id = Objects.requireNonNull(id);
        this.address = Objects.requireNonNull(address);
    }

    public String getId() {
        return id;
    }

    public String getAddress() {
        return address;
    }
}
