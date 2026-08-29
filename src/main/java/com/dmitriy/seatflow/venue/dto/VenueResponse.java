package com.dmitriy.seatflow.venue.dto;

import java.time.Instant;
import java.util.UUID;

public class VenueResponse {

    private final UUID id;
    private final String name;
    private final String city;
    private final String address;
    private final String timezone;
    private final Instant createdAt;
    private final Instant updatedAt;

    public VenueResponse(
            UUID id,
            String name,
            String city,
            String address,
            String timezone,
            Instant createdAt,
            Instant updatedAt
    ) {
        this.id = id;
        this.name = name;
        this.city = city;
        this.address = address;
        this.timezone = timezone;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCity() {
        return city;
    }

    public String getAddress() {
        return address;
    }

    public String getTimezone() {
        return timezone;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}