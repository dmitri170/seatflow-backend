package com.dmitriy.seatflow.eventpricing;

import com.dmitriy.seatflow.eventpricing.dto.EventSectorPriceResponse;
import com.dmitriy.seatflow.eventpricing.dto.SetEventPricesRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = EventSectorPriceController.class)
class EventSectorPriceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EventSectorPriceService eventSectorPriceService;

    @Test
    void shouldSetEventSectorPrices() throws Exception {
        UUID priceId = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();
        UUID sectorId = UUID.randomUUID();

        EventSectorPriceResponse response =
                new EventSectorPriceResponse(
                        priceId,
                        eventId,
                        sectorId,
                        "Parterre",
                        new BigDecimal("1500.00"),
                        "RUB",
                        Instant.parse("2026-10-01T12:00:00Z"),
                        Instant.parse("2026-10-01T12:00:00Z")
                );

        when(eventSectorPriceService.setPrices(
                eq(eventId),
                any(SetEventPricesRequest.class)
        )).thenReturn(List.of(response));

        String requestBody = """
                {
                  "prices": [
                    {
                      "sectorId": "%s",
                      "amount": 1500.00,
                      "currency": "RUB"
                    }
                  ]
                }
                """.formatted(sectorId);

        mockMvc.perform(
                        put("/api/v1/events/{eventId}/prices", eventId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON
                ))
                .andExpect(jsonPath("$[0].id")
                        .value(priceId.toString()))
                .andExpect(jsonPath("$[0].eventId")
                        .value(eventId.toString()))
                .andExpect(jsonPath("$[0].sectorId")
                        .value(sectorId.toString()))
                .andExpect(jsonPath("$[0].sectorName")
                        .value("Parterre"))
                .andExpect(jsonPath("$[0].amount")
                        .value(1500.00))
                .andExpect(jsonPath("$[0].currency")
                        .value("RUB"));

        verify(eventSectorPriceService).setPrices(
                eq(eventId),
                any(SetEventPricesRequest.class)
        );
    }

    @Test
    void shouldGetEventSectorPrices() throws Exception {
        UUID priceId = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();
        UUID sectorId = UUID.randomUUID();

        EventSectorPriceResponse response =
                new EventSectorPriceResponse(
                        priceId,
                        eventId,
                        sectorId,
                        "Balcony",
                        new BigDecimal("2500.00"),
                        "RUB",
                        Instant.parse("2026-10-01T12:00:00Z"),
                        Instant.parse("2026-10-01T12:00:00Z")
                );

        when(eventSectorPriceService.getPrices(eventId))
                .thenReturn(List.of(response));

        mockMvc.perform(
                        get("/api/v1/events/{eventId}/prices", eventId)
                )
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON
                ))
                .andExpect(jsonPath("$[0].id")
                        .value(priceId.toString()))
                .andExpect(jsonPath("$[0].eventId")
                        .value(eventId.toString()))
                .andExpect(jsonPath("$[0].sectorId")
                        .value(sectorId.toString()))
                .andExpect(jsonPath("$[0].sectorName")
                        .value("Balcony"))
                .andExpect(jsonPath("$[0].amount")
                        .value(2500.00))
                .andExpect(jsonPath("$[0].currency")
                        .value("RUB"));

        verify(eventSectorPriceService).getPrices(eventId);
    }

    @Test
    void shouldRejectEmptyPrices() throws Exception {
        UUID eventId = UUID.randomUUID();

        String requestBody = """
                {
                  "prices": []
                }
                """;

        mockMvc.perform(
                        put("/api/v1/events/{eventId}/prices", eventId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON
                ))
                .andExpect(jsonPath("$.code")
                        .value("VALIDATION_ERROR"));

        verifyNoInteractions(eventSectorPriceService);
    }

    @Test
    void shouldRejectInvalidSectorPrice() throws Exception {
        UUID eventId = UUID.randomUUID();

        String requestBody = """
                {
                  "prices": [
                    {
                      "sectorId": null,
                      "amount": -1.001,
                      "currency": "rub"
                    }
                  ]
                }
                """;

        mockMvc.perform(
                        put("/api/v1/events/{eventId}/prices", eventId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON
                ))
                .andExpect(jsonPath("$.code")
                        .value("VALIDATION_ERROR"));

        verifyNoInteractions(eventSectorPriceService);
    }
}