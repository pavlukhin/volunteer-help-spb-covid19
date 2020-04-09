package org.ivan.volunteer.claimmap.geocoder;

import java.util.Objects;

public class GeoObject {
    private final String latitude;
    private final String longitude;

    public GeoObject(String latitude, String longitude) {
        this.latitude = Objects.requireNonNull(latitude);
        this.longitude = Objects.requireNonNull(longitude);
    }

    public String getLatitude() {
        return latitude;
    }

    public String getLongitude() {
        return longitude;
    }
}
