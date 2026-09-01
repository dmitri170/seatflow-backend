package com.dmitriy.seatflow.event;

import com.dmitriy.seatflow.common.error.RequestValidationException;
import com.dmitriy.seatflow.common.error.ResourceNotFoundException;
import com.dmitriy.seatflow.event.dto.CreateEventRequest;
import com.dmitriy.seatflow.event.dto.EventResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Тесты REST-контроллера {@link EventController}.
 */
@WebMvcTest(EventController.class)
class EventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EventService eventService;

    /**
     * Проверяет успешное создание события.
     */
    @Test
    void shouldCreateEvent() throws Exception {
        UUID hallId = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();

        Instant startsAt =
                Instant.parse("2026-10-10T16:00:00Z");

        Instant endsAt =
                Instant.parse("2026-10-10T19:00:00Z");

        Instant createdAt =
                Instant.parse("2026-09-01T10:00:00Z");

        EventResponse response = new EventResponse(
                eventId,
                hallId,
                "Football Match",
                "Championship match",
                startsAt,
                endsAt,
                createdAt,
                createdAt
        );

        when(eventService.createEvent(
                eq(hallId),
                any(CreateEventRequest.class)
        )).thenReturn(response);

        String json = """
                {
                  "title": "Football Match",
                  "description": "Championship match",
                  "startsAt": "2026-10-10T16:00:00Z",
                  "endsAt": "2026-10-10T19:00:00Z"
                }
                """;

        mockMvc.perform(
                        post("/api/v1/halls/{hallId}/events", hallId)
                                .contentType("application/json")
                                .content(json)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(eventId.toString()))
                .andExpect(jsonPath("$.hallId").value(hallId.toString()))
                .andExpect(jsonPath("$.title").value("Football Match"))
                .andExpect(jsonPath("$.description")
                        .value("Championship match"))
                .andExpect(jsonPath("$.startsAt")
                        .value(startsAt.toString()))
                .andExpect(jsonPath("$.endsAt")
                        .value(endsAt.toString()))
                .andExpect(jsonPath("$.createdAt")
                        .value(createdAt.toString()))
                .andExpect(jsonPath("$.updatedAt")
                        .value(createdAt.toString()));

        verify(eventService).createEvent(
                eq(hallId),
                any(CreateEventRequest.class)
        );
    }

    /**
     * Проверяет получение списка событий зала.
     */
    @Test
    void shouldReturnEventsByHallId() throws Exception {
        UUID hallId = UUID.randomUUID();

        UUID firstEventId = UUID.randomUUID();
        UUID secondEventId = UUID.randomUUID();

        Instant createdAt =
                Instant.parse("2026-09-01T10:00:00Z");

        EventResponse firstEvent = new EventResponse(
                firstEventId,
                hallId,
                "First Event",
                "First description",
                Instant.parse("2026-10-10T16:00:00Z"),
                Instant.parse("2026-10-10T19:00:00Z"),
                createdAt,
                createdAt
        );

        EventResponse secondEvent = new EventResponse(
                secondEventId,
                hallId,
                "Second Event",
                "Second description",
                Instant.parse("2026-11-10T16:00:00Z"),
                Instant.parse("2026-11-10T19:00:00Z"),
                createdAt,
                createdAt
        );

        when(eventService.getEventsByHallId(hallId))
                .thenReturn(List.of(firstEvent, secondEvent));

        mockMvc.perform(
                        get("/api/v1/halls/{hallId}/events", hallId)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id")
                        .value(firstEventId.toString()))
                .andExpect(jsonPath("$[0].hallId")
                        .value(hallId.toString()))
                .andExpect(jsonPath("$[0].title")
                        .value("First Event"))
                .andExpect(jsonPath("$[1].id")
                        .value(secondEventId.toString()))
                .andExpect(jsonPath("$[1].title")
                        .value("Second Event"));

        verify(eventService).getEventsByHallId(hallId);
    }

    /**
     * Проверяет возврат пустого списка,
     * если в зале нет событий.
     */
    @Test
    void shouldReturnEmptyListWhenHallHasNoEvents() throws Exception {
        UUID hallId = UUID.randomUUID();

        when(eventService.getEventsByHallId(hallId))
                .thenReturn(List.of());

        mockMvc.perform(
                        get("/api/v1/halls/{hallId}/events", hallId)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        verify(eventService).getEventsByHallId(hallId);
    }

    /**
     * Проверяет возврат 400 при невалидных
     * полях запроса.
     */
    @Test
    void shouldReturnBadRequestWhenRequestIsInvalid() throws Exception {
        UUID hallId = UUID.randomUUID();

        String json = """
                {
                  "title": "",
                  "description": "Test",
                  "startsAt": null,
                  "endsAt": null
                }
                """;

        mockMvc.perform(
                        post("/api/v1/halls/{hallId}/events", hallId)
                                .contentType("application/json")
                                .content(json)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code")
                        .value("VALIDATION_ERROR"));

        verify(eventService, never())
                .createEvent(
                        eq(hallId),
                        any(CreateEventRequest.class)
                );
    }

    /**
     * Проверяет возврат 400 при нарушении
     * бизнес-правила времени события.
     */
    @Test
    void shouldReturnBadRequestWhenEventTimeIsInvalid()
            throws Exception {

        UUID hallId = UUID.randomUUID();

        when(eventService.createEvent(
                eq(hallId),
                any(CreateEventRequest.class)
        )).thenThrow(
                new RequestValidationException(
                        "Event end time must be after start time"
                )
        );

        String json = """
                {
                  "title": "Football Match",
                  "description": "Championship match",
                  "startsAt": "2026-10-10T19:00:00Z",
                  "endsAt": "2026-10-10T16:00:00Z"
                }
                """;

        mockMvc.perform(
                        post("/api/v1/halls/{hallId}/events", hallId)
                                .contentType("application/json")
                                .content(json)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code")
                        .value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message")
                        .value(
                                "Event end time must be after start time"
                        ));
    }

    /**
     * Проверяет возврат 404,
     * если зал не существует.
     */
    @Test
    void shouldReturnNotFoundWhenHallDoesNotExist()
            throws Exception {

        UUID hallId = UUID.randomUUID();

        when(eventService.createEvent(
                eq(hallId),
                any(CreateEventRequest.class)
        )).thenThrow(
                new ResourceNotFoundException(
                        "Hall not found: " + hallId
                )
        );

        String json = """
                {
                  "title": "Football Match",
                  "description": "Championship match",
                  "startsAt": "2026-10-10T16:00:00Z",
                  "endsAt": "2026-10-10T19:00:00Z"
                }
                """;

        mockMvc.perform(
                        post("/api/v1/halls/{hallId}/events", hallId)
                                .contentType("application/json")
                                .content(json)
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.code")
                        .value("RESOURCE_NOT_FOUND"));
    }
}