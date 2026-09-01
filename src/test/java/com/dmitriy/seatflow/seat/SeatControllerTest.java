package com.dmitriy.seatflow.seat;

import com.dmitriy.seatflow.seat.dto.SeatResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Тесты REST-контроллера {@link SeatController}.
 */
@WebMvcTest(SeatController.class)
class SeatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SeatService seatService;

    /**
     * Проверяет получение списка мест указанного сектора.
     */
    @Test
    void shouldReturnSeatsBySectorId() throws Exception {
        UUID sectorId = UUID.randomUUID();

        UUID firstSeatId = UUID.randomUUID();
        UUID secondSeatId = UUID.randomUUID();

        Instant createdAt = Instant.parse("2026-08-31T10:00:00Z");
        Instant updatedAt = Instant.parse("2026-08-31T10:00:00Z");

        SeatResponse firstSeat = new SeatResponse(
                firstSeatId,
                sectorId,
                1,
                1,
                createdAt,
                updatedAt
        );

        SeatResponse secondSeat = new SeatResponse(
                secondSeatId,
                sectorId,
                1,
                2,
                createdAt,
                updatedAt
        );

        when(seatService.getSeatsBySectorId(sectorId))
                .thenReturn(List.of(firstSeat, secondSeat));

        mockMvc.perform(
                        get("/api/v1/sectors/{sectorId}/seats", sectorId)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(firstSeatId.toString()))
                .andExpect(jsonPath("$[0].sectorId").value(sectorId.toString()))
                .andExpect(jsonPath("$[0].rowNumber").value(1))
                .andExpect(jsonPath("$[0].seatNumber").value(1))
                .andExpect(jsonPath("$[0].createdAt").value(createdAt.toString()))
                .andExpect(jsonPath("$[0].updatedAt").value(updatedAt.toString()))
                .andExpect(jsonPath("$[1].id").value(secondSeatId.toString()))
                .andExpect(jsonPath("$[1].sectorId").value(sectorId.toString()))
                .andExpect(jsonPath("$[1].rowNumber").value(1))
                .andExpect(jsonPath("$[1].seatNumber").value(2));

        verify(seatService).getSeatsBySectorId(sectorId);
    }

    /**
     * Проверяет возврат пустого списка,
     * если в секторе нет мест.
     */
    @Test
    void shouldReturnEmptyListWhenSectorHasNoSeats() throws Exception {
        UUID sectorId = UUID.randomUUID();

        when(seatService.getSeatsBySectorId(sectorId))
                .thenReturn(List.of());

        mockMvc.perform(
                        get("/api/v1/sectors/{sectorId}/seats", sectorId)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        verify(seatService).getSeatsBySectorId(sectorId);
    }
}