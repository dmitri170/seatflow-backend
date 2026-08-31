package com.dmitriy.seatflow.sector.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public class CreateSectorRequest {

    @NotBlank
    @Size(max = 100)
    private String name;

    @Positive
    private int rowCount;

    @Positive
    private int seatsPerRow;

    public CreateSectorRequest() {
    }

    public CreateSectorRequest(String name, int rowCount, int seatsPerRow) {
        this.name = name;
        this.rowCount = rowCount;
        this.seatsPerRow = seatsPerRow;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getRowCount() {
        return rowCount;
    }

    public void setRowCount(int rowCount) {
        this.rowCount = rowCount;
    }

    public int getSeatsPerRow() {
        return seatsPerRow;
    }

    public void setSeatsPerRow(int seatsPerRow) {
        this.seatsPerRow = seatsPerRow;
    }
}
