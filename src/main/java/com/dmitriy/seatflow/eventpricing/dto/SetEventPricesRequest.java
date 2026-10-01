package com.dmitriy.seatflow.eventpricing.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record SetEventPricesRequest(

        @NotEmpty(message = "At least one sector price is required")
        List<
                @NotNull(message = "Sector price must not be null")
                @Valid SetEventSectorPriceRequest
                > prices
) {
}
