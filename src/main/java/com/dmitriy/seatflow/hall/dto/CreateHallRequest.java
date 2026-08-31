package com.dmitriy.seatflow.hall.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.jspecify.annotations.NonNull;

public class CreateHallRequest {

    @NotBlank
    @Size(min = 1, max = 150)
    private String name;

    @NonNull
    @Positive
    private Integer capacity;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }
}
