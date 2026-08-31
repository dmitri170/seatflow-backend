package com.dmitriy.seatflow.hall.dto;

import java.time.Instant;
import java.util.UUID;

public class HallResponse {

    private final UUID id;
    private final UUID venueId;
    private final String name;
    private final int capacity;
    private final Instant createdAt;
    private final Instant updatedAt;

    public HallResponse(
            UUID id,
            UUID venueId,
            String name,
            int capacity,
            Instant createdAt,
            Instant updatedAt
    ) {
        this.id = id;
        this.venueId = venueId;
        this.name = name;
        this.capacity = capacity;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getVenueId() {
        return venueId;
    }

    public String getName() {
        return name;
    }

    public int getCapacity() {
        return capacity;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}