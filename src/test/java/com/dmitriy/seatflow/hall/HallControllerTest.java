package com.dmitriy.seatflow.hall;

import com.dmitriy.seatflow.common.error.GlobalExceptionHandler;
import com.dmitriy.seatflow.common.error.ResourceConflictException;
import com.dmitriy.seatflow.common.error.ResourceNotFoundException;
import com.dmitriy.seatflow.hall.dto.CreateHallRequest;
import com.dmitriy.seatflow.hall.dto.HallResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * MVC-тесты HTTP-контракта Hall API.
 *
 * <p>Поднимается только web-слой, а HallService заменяется Mockito-объектом.
 * Благодаря этому тесты проверяют маршруты, JSON, валидацию и обработку ошибок
 * без запуска PostgreSQL.</p>
 */
@WebMvcTest(HallController.class)
@Import(GlobalExceptionHandler.class)
class HallControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private HallService hallService;

    @Test
    void shouldCreateHall() throws Exception {
        UUID venueId = UUID.randomUUID();
        UUID hallId = UUID.randomUUID();
        Instant now = Instant.now();

        HallResponse response = new HallResponse(
                hallId,
                venueId,
                "Main Hall",
                500,
                now,
                now
        );

        when(hallService.create(
                eq(venueId),
                any(CreateHallRequest.class)
        )).thenReturn(response);

        mockMvc.perform(post(
                        "/api/v1/venues/{venueId}/halls",
                        venueId
                )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Main Hall",
                                  "capacity": 500
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string(
                        "Location",
                        "http://localhost/api/v1/halls/" + hallId
                ))
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON
                ))
                .andExpect(jsonPath("$.id").value(hallId.toString()))
                .andExpect(jsonPath("$.venueId").value(venueId.toString()))
                .andExpect(jsonPath("$.name").value("Main Hall"))
                .andExpect(jsonPath("$.capacity").value(500));

        verify(hallService).create(
                eq(venueId),
                any(CreateHallRequest.class)
        );
    }

    @Test
    void shouldReturnValidationErrorForInvalidCapacity() throws Exception {
        UUID venueId = UUID.randomUUID();

        mockMvc.perform(post(
                        "/api/v1/venues/{venueId}/halls",
                        venueId
                )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Main Hall",
                                  "capacity": 0
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.path").value(
                        "/api/v1/venues/" + venueId + "/halls"
                ))
                .andExpect(jsonPath("$.fieldErrors[0].field")
                        .value("capacity"));

        // Невалидный request отклоняется до вызова контроллера и сервиса.
        verifyNoInteractions(hallService);
    }

    @Test
    void shouldReturnConflictWhenHallAlreadyExists() throws Exception {
        UUID venueId = UUID.randomUUID();

        when(hallService.create(
                eq(venueId),
                any(CreateHallRequest.class)
        )).thenThrow(new ResourceConflictException(
                "Hall already exists in venue: Main Hall"
        ));

        mockMvc.perform(post(
                        "/api/v1/venues/{venueId}/halls",
                        venueId
                )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Main Hall",
                                  "capacity": 500
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.code")
                        .value("RESOURCE_CONFLICT"))
                .andExpect(jsonPath("$.message").value(
                        "Hall already exists in venue: Main Hall"
                ))
                .andExpect(jsonPath("$.path").value(
                        "/api/v1/venues/" + venueId + "/halls"
                ));

        verify(hallService).create(
                eq(venueId),
                any(CreateHallRequest.class)
        );
    }

    @Test
    void shouldReturnHallsByVenueId() throws Exception {
        UUID venueId = UUID.randomUUID();
        Instant now = Instant.now();

        HallResponse firstHall = new HallResponse(
                UUID.randomUUID(),
                venueId,
                "Main Hall",
                500,
                now,
                now
        );

        HallResponse secondHall = new HallResponse(
                UUID.randomUUID(),
                venueId,
                "Small Hall",
                100,
                now,
                now
        );

        when(hallService.getAllByVenueId(venueId))
                .thenReturn(List.of(firstHall, secondHall));

        mockMvc.perform(get(
                        "/api/v1/venues/{venueId}/halls",
                        venueId
                ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name").value("Main Hall"))
                .andExpect(jsonPath("$[1].name").value("Small Hall"));

        verify(hallService).getAllByVenueId(venueId);
    }

    @Test
    void shouldReturnHallById() throws Exception {
        UUID venueId = UUID.randomUUID();
        UUID hallId = UUID.randomUUID();
        Instant now = Instant.now();

        HallResponse response = new HallResponse(
                hallId,
                venueId,
                "Main Hall",
                500,
                now,
                now
        );

        when(hallService.getById(hallId)).thenReturn(response);

        mockMvc.perform(get("/api/v1/halls/{hallId}", hallId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(hallId.toString()))
                .andExpect(jsonPath("$.venueId")
                        .value(venueId.toString()))
                .andExpect(jsonPath("$.name").value("Main Hall"))
                .andExpect(jsonPath("$.capacity").value(500));

        verify(hallService).getById(hallId);
    }

    @Test
    void shouldReturnNotFoundWhenHallDoesNotExist() throws Exception {
        UUID hallId = UUID.randomUUID();

        when(hallService.getById(hallId))
                .thenThrow(new ResourceNotFoundException(
                        "Hall not found: " + hallId
                ));

        mockMvc.perform(get("/api/v1/halls/{hallId}", hallId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code")
                        .value("RESOURCE_NOT_FOUND"))
                .andExpect(jsonPath("$.message")
                        .value("Hall not found: " + hallId))
                .andExpect(jsonPath("$.path")
                        .value("/api/v1/halls/" + hallId));

        verify(hallService).getById(hallId);
    }

    @Test
    void shouldReturnNotFoundWhenVenueDoesNotExist() throws Exception {
        UUID venueId = UUID.randomUUID();

        when(hallService.getAllByVenueId(venueId))
                .thenThrow(new ResourceNotFoundException(
                        "Venue not found: " + venueId
                ));

        mockMvc.perform(get(
                        "/api/v1/venues/{venueId}/halls",
                        venueId
                ))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code")
                        .value("RESOURCE_NOT_FOUND"))
                .andExpect(jsonPath("$.message")
                        .value("Venue not found: " + venueId))
                .andExpect(jsonPath("$.path").value(
                        "/api/v1/venues/" + venueId + "/halls"
                ));

        verify(hallService).getAllByVenueId(venueId);
    }
}
