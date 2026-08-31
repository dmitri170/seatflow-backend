package com.dmitriy.seatflow.hall;

import com.dmitriy.seatflow.common.error.ApiErrorResponse;
import com.dmitriy.seatflow.hall.dto.CreateHallRequest;
import com.dmitriy.seatflow.hall.dto.HallResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

/**
 * REST API для управления залами внутри площадок.
 */
@Tag(
        name = "Halls",
        description = "Operations for managing venue halls"
)
@RestController
@RequestMapping("/api/v1")
public class HallController {

    private final HallService hallService;

    public HallController(HallService hallService) {
        this.hallService = hallService;
    }

    /**
     * Создаёт зал внутри указанной площадки.
     *
     * @param venueId идентификатор площадки
     * @param request параметры создаваемого зала
     * @return созданный зал и Location его отдельного endpoint
     */
    @Operation(summary = "Create a hall for a venue")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Hall created",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = HallResponse.class)
                    ),
                    headers = @Header(
                            name = "Location",
                            description = "URI of the created hall",
                            schema = @Schema(type = "string", format = "uri")
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Venue not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Hall already exists in the venue",
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
    @PostMapping("/venues/{venueId}/halls")
    public ResponseEntity<HallResponse> createHall(
            @PathVariable UUID venueId,
            @Valid @RequestBody CreateHallRequest request
    ) {
        HallResponse response = hallService.create(venueId, request);

        // Получать отдельный зал будем через GET /api/v1/halls/{hallId}.
        URI location = ServletUriComponentsBuilder
                .fromCurrentContextPath()
                .path("/api/v1/halls/{hallId}")
                .buildAndExpand(response.getId())
                .toUri();

        return ResponseEntity.created(location).body(response);
    }

    /**
     * Возвращает все залы площадки, отсортированные по названию.
     *
     * @param venueId идентификатор площадки
     * @return список залов или пустой список
     */
    @Operation(summary = "Get halls for a venue")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Halls returned",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(
                                    schema = @Schema(implementation = HallResponse.class)
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid venue ID",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Venue not found",
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
    @GetMapping("/venues/{venueId}/halls")
    public List<HallResponse> getHallsByVenue(
            @PathVariable UUID venueId
    ) {
        return hallService.getAllByVenueId(venueId);
    }

    /**
     * Возвращает отдельный зал независимо от площадки.
     *
     * @param hallId идентификатор зала
     * @return найденный зал
     */
    @Operation(summary = "Get a hall by ID")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Hall found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = HallResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid hall ID",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Hall not found",
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
    @GetMapping("/halls/{hallId}")
    public HallResponse getHallById(
            @PathVariable UUID hallId
    ) {
        return hallService.getById(hallId);
    }
}
