package com.dmitriy.seatflow.eventseat.dto;

import com.dmitriy.seatflow.eventseat.EventSeatStatus;

import java.util.UUID;

public class EventSeatResponse {

    private UUID id;
    private UUID seatId;
    private UUID sectorId;
    private String sectorName;
    private int rowNumber;
    private int seatNumber;
    private EventSeatStatus status;

    public EventSeatResponse(UUID id, UUID seatId, UUID sectorId, String sectorName, int rowNumber, int seatNumber, EventSeatStatus status) {
        this.id = id;
        this.seatId = seatId;
        this.sectorId = sectorId;
        this.sectorName = sectorName;
        this.rowNumber = rowNumber;
        this.seatNumber = seatNumber;
        this.status = status;
    }

    public UUID getId() {
        return id;
    }

    public UUID getSeatId() {
        return seatId;
    }

    public UUID getSectorId() {
        return sectorId;
    }

    public String getSectorName() {
        return sectorName;
    }

    public int getRowNumber() {
        return rowNumber;
    }

    public int getSeatNumber() {
        return seatNumber;
    }

    public EventSeatStatus getStatus() {
        return status;
    }


}
