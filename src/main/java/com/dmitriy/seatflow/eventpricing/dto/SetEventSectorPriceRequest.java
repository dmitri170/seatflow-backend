package com.dmitriy.seatflow.eventpricing.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;
import java.util.UUID;

public record SetEventSectorPriceRequest(

        @NotNull(message = "Sector ID must not be null")
        UUID sectorId,

        @NotNull(message = "Amount must not be null")
        @DecimalMin(
                value = "0.00",
                message = "Amount must not be negative"
        )
        @Digits(
                integer = 10,
                fraction = 2,
                message = "Amount must contain up to 10 integer and 2 fraction digits"
        )
        BigDecimal amount,

        @NotBlank(message = "Currency must not be blank")
        @Pattern(
                regexp = "^[A-Z]{3}$",
                message = "Currency must contain exactly three uppercase letters"
        )
        String currency
) {
}