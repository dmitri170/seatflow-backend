package com.dmitriy.seatflow.sector.dto;

import java.time.Instant;
import java.util.UUID;

public class SectorResponse {

    private UUID id;
    private UUID hallId;
    private String name;
    private int rowCount;
    private int seatsPerRow;
    private Instant createdAt;
    private Instant updatedAt;

    public SectorResponse(
            UUID id,
            UUID hallId,
            String name,
            int rowCount,
            int seatsPerRow,
            Instant createdAt,
            Instant updatedAt
    ) {
        this.id = id;
        this.hallId = hallId;
        this.name = name;
        this.rowCount = rowCount;
        this.seatsPerRow = seatsPerRow;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getHallId() {
        return hallId;
    }

    public String getName() {
        return name;
    }

    public int getRowCount() {
        return rowCount;
    }

    public int getSeatsPerRow() {
        return seatsPerRow;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
