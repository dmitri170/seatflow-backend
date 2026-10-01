package com.dmitriy.seatflow.eventpricing;

import com.dmitriy.seatflow.common.error.ApiErrorResponse;
import com.dmitriy.seatflow.eventpricing.dto.EventSectorPriceResponse;
import com.dmitriy.seatflow.eventpricing.dto.SetEventPricesRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Event sector prices were set",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(
                                    schema = @Schema(
                                            implementation = EventSectorPriceResponse.class
                                    )
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Request validation failed",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Event or sector was not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Duplicate sector or sector does not belong to the event hall",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Unexpected server error",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorResponse.class)
                    )
            )
    })
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
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Event sector prices were found",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(
                                    schema = @Schema(
                                            implementation = EventSectorPriceResponse.class
                                    )
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Event was not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Unexpected server error",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorResponse.class)
                    )
            )
    })
    @GetMapping("/{eventId}/prices")
    public ResponseEntity<List<EventSectorPriceResponse>> getPrices(
            @PathVariable UUID eventId
    ) {
        return ResponseEntity.ok(
                eventSectorPriceService.getPrices(eventId)
        );
    }
}
