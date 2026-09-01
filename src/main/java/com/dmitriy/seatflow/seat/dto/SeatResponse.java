package com.dmitriy.seatflow.seat.dto;

import java.time.Instant;
import java.util.UUID;

public class SeatResponse {

    private UUID id;
    private UUID sectorId;
    private int rowNumber;
    private int seatNumber;
    private Instant createdAt;
    private Instant updatedAt;

    public SeatResponse(
            UUID id,
            UUID sectorId,
            int rowNumber,
            int seatNumber,
            Instant createdAt,
            Instant updatedAt
    ) {
        this.id = id;
        this.sectorId = sectorId;
        this.rowNumber = rowNumber;
        this.seatNumber = seatNumber;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getSectorId() {
        return sectorId;
    }

    public int getRowNumber() {
        return rowNumber;
    }

    public int getSeatNumber() {
        return seatNumber;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
