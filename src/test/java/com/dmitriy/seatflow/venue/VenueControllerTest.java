package com.dmitriy.seatflow.venue;

import com.dmitriy.seatflow.common.error.GlobalExceptionHandler;
import com.dmitriy.seatflow.common.error.ResourceNotFoundException;
import com.dmitriy.seatflow.venue.dto.CreateVenueRequest;
import com.dmitriy.seatflow.venue.dto.VenueResponse;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(VenueController.class)
@Import(GlobalExceptionHandler.class)
class VenueControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private VenueService venueService;

    @Test
    void shouldCreateVenue() throws Exception {
        UUID venueId = UUID.randomUUID();
        Instant now = Instant.now();

        VenueResponse response = new VenueResponse(
                venueId,
                "Luzhniki Stadium",
                "Moscow",
                "Luzhnetskaya Naberezhnaya, 24",
                "Europe/Moscow",
                now,
                now
        );

        when(venueService.create(any(CreateVenueRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/venues")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Luzhniki Stadium",
                                  "city": "Moscow",
                                  "address": "Luzhnetskaya Naberezhnaya, 24",
                                  "timezone": "Europe/Moscow"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string(
                        "Location",
                        "http://localhost/api/v1/venues/" + venueId
                ))
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON
                ))
                .andExpect(jsonPath("$.id").value(venueId.toString()))
                .andExpect(jsonPath("$.name").value("Luzhniki Stadium"))
                .andExpect(jsonPath("$.city").value("Moscow"))
                .andExpect(jsonPath("$.timezone").value("Europe/Moscow"));

        verify(venueService).create(any(CreateVenueRequest.class));
    }

    @Test
    void shouldReturnValidationErrorForBlankName() throws Exception {
        mockMvc.perform(post("/api/v1/venues")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "",
                                  "city": "Moscow",
                                  "address": "Luzhnetskaya Naberezhnaya, 24",
                                  "timezone": "Europe/Moscow"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.fieldErrors[0].field").value("name"))
                .andExpect(jsonPath("$.fieldErrors[0].message")
                        .value("must not be blank"));

        verifyNoInteractions(venueService);
    }

    @Test
    void shouldReturnVenueById() throws Exception {
        UUID venueId = UUID.randomUUID();
        Instant now = Instant.now();

        VenueResponse response = new VenueResponse(
                venueId,
                "Bolshoi Theatre",
                "Moscow",
                "Theatre Square, 1",
                "Europe/Moscow",
                now,
                now
        );

        when(venueService.getById(venueId)).thenReturn(response);

        mockMvc.perform(get("/api/v1/venues/{id}", venueId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(venueId.toString()))
                .andExpect(jsonPath("$.name").value("Bolshoi Theatre"))
                .andExpect(jsonPath("$.address").value("Theatre Square, 1"));

        verify(venueService).getById(venueId);
    }

    @Test
    void shouldReturnNotFoundWhenVenueDoesNotExist() throws Exception {
        UUID venueId = UUID.randomUUID();

        when(venueService.getById(venueId))
                .thenThrow(new ResourceNotFoundException(
                        "Venue not found: " + venueId
                ));

        mockMvc.perform(get("/api/v1/venues/{id}", venueId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"))
                .andExpect(jsonPath("$.path")
                        .value("/api/v1/venues/" + venueId));

        verify(venueService).getById(venueId);
    }

    @Test
    void shouldReturnAllVenues() throws Exception {
        Instant now = Instant.now();

        VenueResponse firstVenue = new VenueResponse(
                UUID.randomUUID(),
                "Bolshoi Theatre",
                "Moscow",
                "Theatre Square, 1",
                "Europe/Moscow",
                now,
                now
        );

        VenueResponse secondVenue = new VenueResponse(
                UUID.randomUUID(),
                "Luzhniki Stadium",
                "Moscow",
                "Luzhnetskaya Naberezhnaya, 24",
                "Europe/Moscow",
                now,
                now
        );

        when(venueService.getAll())
                .thenReturn(List.of(firstVenue, secondVenue));

        mockMvc.perform(get("/api/v1/venues"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name").value("Bolshoi Theatre"))
                .andExpect(jsonPath("$[1].name").value("Luzhniki Stadium"));

        verify(venueService).getAll();
    }

    @Test
    void shouldReturnValidationErrorForInvalidTimezone() throws Exception {
        mockMvc.perform(post("/api/v1/venues")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "name": "Luzhniki Stadium",
                              "city": "Moscow",
                              "address": "Luzhnetskaya Naberezhnaya, 24",
                              "timezone": "Wrong/Timezone"
                            }
                            """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.fieldErrors[0].field")
                        .value("timezone"))
                .andExpect(jsonPath("$.fieldErrors[0].message")
                        .value("must be a valid timezone"));

        verifyNoInteractions(venueService);
    }
}