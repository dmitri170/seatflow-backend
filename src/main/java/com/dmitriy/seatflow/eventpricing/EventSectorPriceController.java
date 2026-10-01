package com.dmitriy.seatflow.eventpricing;

import com.dmitriy.seatflow.eventpricing.dto.EventSectorPriceResponse;
import com.dmitriy.seatflow.eventpricing.dto.SetEventPricesRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(
        name = "Event pricing",
        description = "Operations for managing event sector prices"
)
@RestController
@RequestMapping("/api/v1/events")
public class EventSectorPriceController {

    private final EventSectorPriceService eventSectorPriceService;

    public EventSectorPriceController(EventSectorPriceService eventSectorPriceService) {
        this.eventSectorPriceService = eventSectorPriceService;
    }

    @Operation(summary = "Set event sector prices")
    @PutMapping("/{eventId}/prices")
    public ResponseEntity<List<EventSectorPriceResponse>> setPrices(
            @PathVariable UUID eventId,
            @Valid @RequestBody SetEventPricesRequest request
    ) {
        return ResponseEntity.ok(
                eventSectorPriceService.setPrices(eventId, request)
        );
    }

    @Operation(summary = "Get event sector prices")
    @GetMapping("/{eventId}/prices")
    public ResponseEntity<List<EventSectorPriceResponse>> getPrices(
            @PathVariable UUID eventId
    ) {
        return ResponseEntity.ok(
                eventSectorPriceService.getPrices(eventId));
    }
}
