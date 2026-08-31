package com.dmitriy.seatflow.hall;

import com.dmitriy.seatflow.hall.dto.CreateHallRequest;
import com.dmitriy.seatflow.hall.dto.HallResponse;
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
    @GetMapping("/halls/{hallId}")
    public HallResponse getHallById(
            @PathVariable UUID hallId
    ) {
        return hallService.getById(hallId);
    }
}