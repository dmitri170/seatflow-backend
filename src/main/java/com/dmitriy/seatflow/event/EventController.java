package com.dmitriy.seatflow.event;

import com.dmitriy.seatflow.event.dto.CreateEventRequest;
import com.dmitriy.seatflow.event.dto.EventResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * REST-контроллер для работы с событиями.
 */
@Tag(
        name = "Events",
        description = "Операции для управления событиями"
)
@RestController
@RequestMapping("/api/v1")
public class EventController {

    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    /**
     * Создает новое событие в указанном зале.
     *
     * @param hallId идентификатор зала
     * @param request данные для создания события
     * @return созданное событие
     */
    @Operation(
            summary = "Создать событие",
            description = "Создает новое событие в указанном зале"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Событие успешно создано"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Некорректные данные запроса"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Зал не найден"
            )
    })
    @PostMapping("/halls/{hallId}/events")
    public ResponseEntity<EventResponse> createEvent(
            @PathVariable UUID hallId,
            @Valid @RequestBody CreateEventRequest request
    ) {
        EventResponse response = eventService.createEvent(hallId, request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    /**
     * Возвращает все события указанного зала.
     *
     * @param hallId идентификатор зала
     * @return список событий
     */
    @Operation(
            summary = "Получить события зала",
            description = "Возвращает список событий указанного зала, отсортированный по времени начала"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Список событий успешно получен"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Зал не найден"
            )
    })
    @GetMapping("/halls/{hallId}/events")
    public List<EventResponse> getEventsByHallId(
            @PathVariable UUID hallId
    ) {
        return eventService.getEventsByHallId(hallId);
    }
}