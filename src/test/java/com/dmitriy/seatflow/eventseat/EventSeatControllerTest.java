package com.dmitriy.seatflow.eventseat;

import com.dmitriy.seatflow.common.error.GlobalExceptionHandler;
import com.dmitriy.seatflow.common.error.ResourceNotFoundException;
import com.dmitriy.seatflow.eventseat.dto.EventSeatResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EventSeatController.class)
@Import(GlobalExceptionHandler.class)
class EventSeatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EventSeatService eventSeatService;

    @Test
    void shouldReturnEventSeats() throws Exception {
        UUID eventId = UUID.randomUUID();
        UUID firstEventSeatId = UUID.randomUUID();
        UUID firstSeatId = UUID.randomUUID();
        UUID firstSectorId = UUID.randomUUID();
        UUID secondEventSeatId = UUID.randomUUID();
        UUID secondSeatId = UUID.randomUUID();
        UUID secondSectorId = UUID.randomUUID();

        EventSeatResponse firstResponse = new EventSeatResponse(
                firstEventSeatId,
                firstSeatId,
                firstSectorId,
                "Parterre",
                1,
                1,
                EventSeatStatus.AVAILABLE
        );

        EventSeatResponse secondResponse = new EventSeatResponse(
                secondEventSeatId,
                secondSeatId,
                secondSectorId,
                "Balcony",
                2,
                15,
                EventSeatStatus.BLOCKED
        );

        when(eventSeatService.getByEventId(eventId))
                .thenReturn(List.of(firstResponse, secondResponse));

        mockMvc.perform(get(
                        "/api/v1/events/{eventId}/seats",
                        eventId
                ))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON
                ))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id")
                        .value(firstEventSeatId.toString()))
                .andExpect(jsonPath("$[0].seatId")
                        .value(firstSeatId.toString()))
                .andExpect(jsonPath("$[0].sectorId")
                        .value(firstSectorId.toString()))
                .andExpect(jsonPath("$[0].sectorName")
                        .value("Parterre"))
                .andExpect(jsonPath("$[0].rowNumber").value(1))
                .andExpect(jsonPath("$[0].seatNumber").value(1))
                .andExpect(jsonPath("$[0].status")
                        .value("AVAILABLE"))
                .andExpect(jsonPath("$[1].id")
                        .value(secondEventSeatId.toString()))
                .andExpect(jsonPath("$[1].sectorName")
                        .value("Balcony"))
                .andExpect(jsonPath("$[1].status")
                        .value("BLOCKED"));

        verify(eventSeatService).getByEventId(eventId);
    }

    @Test
    void shouldReturnNotFoundWhenEventDoesNotExist() throws Exception {
        UUID eventId = UUID.randomUUID();

        when(eventSeatService.getByEventId(eventId))
                .thenThrow(new ResourceNotFoundException(
                        "Event not found: " + eventId
                ));

        mockMvc.perform(get(
                        "/api/v1/events/{eventId}/seats",
                        eventId
                ))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON
                ))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.code")
                        .value("RESOURCE_NOT_FOUND"))
                .andExpect(jsonPath("$.message")
                        .value("Event not found: " + eventId))
                .andExpect(jsonPath("$.path")
                        .value("/api/v1/events/" + eventId + "/seats"));

        verify(eventSeatService).getByEventId(eventId);
    }
}
