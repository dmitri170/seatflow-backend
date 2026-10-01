package com.dmitriy.seatflow.eventpricing.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record EventSectorPriceResponse(
        UUID id,
        UUID eventId,
        UUID sectorId,
        String sectorName,
        BigDecimal amount,
        String currency,
        Instant createdAt,
        Instant updatedAt
) {
}