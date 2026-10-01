package com.dmitriy.seatflow.eventpricing.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record SetEventPricesRequest(

        @NotEmpty(message = "At least one sector price is required")
        List<@Valid SetEventSectorPriceRequest> prices
) {
}