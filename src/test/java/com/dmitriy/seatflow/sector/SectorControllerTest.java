package com.dmitriy.seatflow.sector;

import com.dmitriy.seatflow.sector.dto.CreateSectorRequest;
import com.dmitriy.seatflow.sector.dto.SectorResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
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
 * Тесты REST-контроллера {@link SectorController}.
 */
@WebMvcTest(SectorController.class)
class SectorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SectorService sectorService;

    /**
     * Проверяет успешное создание сектора.
     */
    @Test
    void shouldCreateSector() throws Exception {
        UUID hallId = UUID.randomUUID();
        UUID sectorId = UUID.randomUUID();

        Instant createdAt = Instant.parse("2026-08-31T10:00:00Z");
        Instant updatedAt = Instant.parse("2026-08-31T10:00:00Z");

        SectorResponse response = new SectorResponse(
                sectorId,
                hallId,
                "VIP",
                2,
                3,
                createdAt,
                updatedAt
        );

        when(sectorService.createSector(
                eq(hallId),
                any(CreateSectorRequest.class)
        )).thenReturn(response);

        String json = """
                {
                  "name": "VIP",
                  "rowCount": 2,
                  "seatsPerRow": 3
                }
                """;

        mockMvc.perform(post("/api/v1/halls/{hallId}/sectors", hallId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(sectorId.toString()))
                .andExpect(jsonPath("$.hallId").value(hallId.toString()))
                .andExpect(jsonPath("$.name").value("VIP"))
                .andExpect(jsonPath("$.rowCount").value(2))
                .andExpect(jsonPath("$.seatsPerRow").value(3))
                .andExpect(jsonPath("$.createdAt").value(createdAt.toString()))
                .andExpect(jsonPath("$.updatedAt").value(updatedAt.toString()));

        verify(sectorService).createSector(
                eq(hallId),
                any(CreateSectorRequest.class)
        );
    }

    /**
     * Проверяет получение списка секторов указанного зала.
     */
    @Test
    void shouldReturnSectorsByHallId() throws Exception {
        UUID hallId = UUID.randomUUID();

        UUID firstSectorId = UUID.randomUUID();
        UUID secondSectorId = UUID.randomUUID();

        Instant createdAt = Instant.parse("2026-08-31T10:00:00Z");

        SectorResponse firstSector = new SectorResponse(
                firstSectorId,
                hallId,
                "VIP",
                2,
                3,
                createdAt,
                createdAt
        );

        SectorResponse secondSector = new SectorResponse(
                secondSectorId,
                hallId,
                "Standard",
                5,
                10,
                createdAt,
                createdAt
        );

        when(sectorService.getSectorsByHallId(hallId))
                .thenReturn(List.of(firstSector, secondSector));

        mockMvc.perform(get("/api/v1/halls/{hallId}/sectors", hallId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(firstSectorId.toString()))
                .andExpect(jsonPath("$[0].name").value("VIP"))
                .andExpect(jsonPath("$[0].rowCount").value(2))
                .andExpect(jsonPath("$[0].seatsPerRow").value(3))
                .andExpect(jsonPath("$[1].id").value(secondSectorId.toString()))
                .andExpect(jsonPath("$[1].name").value("Standard"))
                .andExpect(jsonPath("$[1].rowCount").value(5))
                .andExpect(jsonPath("$[1].seatsPerRow").value(10));

        verify(sectorService).getSectorsByHallId(hallId);
    }

    /**
     * Проверяет возврат ошибки 400 при невалидных данных
     * для создания сектора.
     */
    @Test
    void shouldReturnBadRequestWhenCreateSectorRequestIsInvalid()
            throws Exception {

        UUID hallId = UUID.randomUUID();

        String json = """
                {
                  "name": "",
                  "rowCount": 0,
                  "seatsPerRow": -1
                }
                """;

        mockMvc.perform(post("/api/v1/halls/{hallId}/sectors", hallId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());

        verify(sectorService, never())
                .createSector(
                        eq(hallId),
                        any(CreateSectorRequest.class)
                );
    }
}