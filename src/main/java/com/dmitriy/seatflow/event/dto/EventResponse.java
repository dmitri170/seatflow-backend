package com.dmitriy.seatflow.event.dto;

import java.time.Instant;
import java.util.UUID;

public class EventResponse {

    private UUID id;
    private UUID hallId;
    private String title;
    private String description;
    private Instant startsAt;
    private Instant endsAt;
    private Instant createdAt;
    private Instant updatedAt;

    public EventResponse(
            UUID id,
            UUID hallId,
            String title,
            String description,
            Instant startsAt,
            Instant endsAt,
            Instant createdAt,
            Instant updatedAt
    ) {
        this.id = id;
        this.hallId = hallId;
        this.title = title;
        this.description = description;
        this.startsAt = startsAt;
        this.endsAt = endsAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getHallId() {
        return hallId;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public Instant getStartsAt() {
        return startsAt;
    }

    public Instant getEndsAt() {
        return endsAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
