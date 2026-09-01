package com.dmitriy.seatflow.sector;

import com.dmitriy.seatflow.common.error.ResourceConflictException;
import com.dmitriy.seatflow.common.error.ResourceNotFoundException;
import com.dmitriy.seatflow.hall.Hall;
import com.dmitriy.seatflow.hall.HallRepository;
import com.dmitriy.seatflow.seat.Seat;
import com.dmitriy.seatflow.seat.SeatRepository;
import com.dmitriy.seatflow.sector.dto.CreateSectorRequest;
import com.dmitriy.seatflow.sector.dto.SectorResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit-тесты для {@link SectorService}.
 */
@ExtendWith(MockitoExtension.class)
class SectorServiceTest {

    @Mock
    private HallRepository hallRepository;

    @Mock
    private SectorRepository sectorRepository;

    @Mock
    private SeatRepository seatRepository;

    @InjectMocks
    private SectorService sectorService;

    @Captor
    private ArgumentCaptor<Sector> sectorCaptor;

    @Captor
    private ArgumentCaptor<List<Seat>> seatsCaptor;

    /**
     * Проверяет создание сектора и автоматическую генерацию
     * всех мест согласно количеству рядов и мест в ряду.
     */
    @Test
    void shouldCreateSectorAndGenerateSeats() {
        UUID hallId = UUID.randomUUID();
        UUID sectorId = UUID.randomUUID();

        Instant createdAt = Instant.parse("2026-08-31T10:00:00Z");
        Instant updatedAt = Instant.parse("2026-08-31T10:00:00Z");

        CreateSectorRequest request = new CreateSectorRequest(
                "VIP",
                2,
                3
        );

        Hall hall = org.mockito.Mockito.mock(Hall.class);
        Sector savedSector = org.mockito.Mockito.mock(Sector.class);

        when(hall.getId()).thenReturn(hallId);

        when(hallRepository.findById(hallId))
                .thenReturn(Optional.of(hall));

        when(sectorRepository.existsByHallIdAndNameIgnoreCase(
                hallId,
                request.getName()
        )).thenReturn(false);

        when(sectorRepository.save(any(Sector.class)))
                .thenReturn(savedSector);

        when(savedSector.getId()).thenReturn(sectorId);
        when(savedSector.getHall()).thenReturn(hall);
        when(savedSector.getName()).thenReturn("VIP");
        when(savedSector.getRowCount()).thenReturn(2);
        when(savedSector.getSeatsPerRow()).thenReturn(3);
        when(savedSector.getCreatedAt()).thenReturn(createdAt);
        when(savedSector.getUpdatedAt()).thenReturn(updatedAt);

        SectorResponse response = sectorService.createSector(
                hallId,
                request
        );

        verify(sectorRepository).save(sectorCaptor.capture());

        Sector sectorToSave = sectorCaptor.getValue();

        assertThat(sectorToSave.getHall()).isSameAs(hall);
        assertThat(sectorToSave.getName()).isEqualTo("VIP");
        assertThat(sectorToSave.getRowCount()).isEqualTo(2);
        assertThat(sectorToSave.getSeatsPerRow()).isEqualTo(3);

        verify(seatRepository).saveAll(seatsCaptor.capture());

        List<Seat> seats = seatsCaptor.getValue();

        assertThat(seats).hasSize(6);

        assertThat(seats.get(0).getRowNumber()).isEqualTo(1);
        assertThat(seats.get(0).getSeatNumber()).isEqualTo(1);

        assertThat(seats.get(1).getRowNumber()).isEqualTo(1);
        assertThat(seats.get(1).getSeatNumber()).isEqualTo(2);

        assertThat(seats.get(2).getRowNumber()).isEqualTo(1);
        assertThat(seats.get(2).getSeatNumber()).isEqualTo(3);

        assertThat(seats.get(3).getRowNumber()).isEqualTo(2);
        assertThat(seats.get(3).getSeatNumber()).isEqualTo(1);

        assertThat(seats.get(4).getRowNumber()).isEqualTo(2);
        assertThat(seats.get(4).getSeatNumber()).isEqualTo(2);

        assertThat(seats.get(5).getRowNumber()).isEqualTo(2);
        assertThat(seats.get(5).getSeatNumber()).isEqualTo(3);

        assertThat(seats)
                .allSatisfy(seat ->
                        assertThat(seat.getSector())
                                .isSameAs(savedSector)
                );

        assertThat(response.getId()).isEqualTo(sectorId);
        assertThat(response.getHallId()).isEqualTo(hallId);
        assertThat(response.getName()).isEqualTo("VIP");
        assertThat(response.getRowCount()).isEqualTo(2);
        assertThat(response.getSeatsPerRow()).isEqualTo(3);
        assertThat(response.getCreatedAt()).isEqualTo(createdAt);
        assertThat(response.getUpdatedAt()).isEqualTo(updatedAt);
    }

    /**
     * Проверяет, что сектор нельзя создать
     * для несуществующего зала.
     */
    @Test
    void shouldThrowExceptionWhenHallNotFoundOnCreate() {
        UUID hallId = UUID.randomUUID();

        CreateSectorRequest request = new CreateSectorRequest(
                "VIP",
                2,
                3
        );

        when(hallRepository.findById(hallId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                sectorService.createSector(hallId, request)
        )
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Hall not found: " + hallId);

        verify(sectorRepository, never())
                .save(any(Sector.class));

        verify(seatRepository, never())
                .saveAll(any());
    }

    /**
     * Проверяет, что нельзя создать сектор с именем,
     * которое уже используется внутри того же зала.
     */
    @Test
    void shouldThrowExceptionWhenSectorNameAlreadyExists() {
        UUID hallId = UUID.randomUUID();

        CreateSectorRequest request = new CreateSectorRequest(
                "VIP",
                2,
                3
        );

        Hall hall = org.mockito.Mockito.mock(Hall.class);

        when(hallRepository.findById(hallId))
                .thenReturn(Optional.of(hall));

        when(sectorRepository.existsByHallIdAndNameIgnoreCase(
                hallId,
                request.getName()
        )).thenReturn(true);

        assertThatThrownBy(() ->
                sectorService.createSector(hallId, request)
        )
                .isInstanceOf(ResourceConflictException.class)
                .hasMessage("Sector already exists: VIP");

        verify(sectorRepository, never())
                .save(any(Sector.class));

        verify(seatRepository, never())
                .saveAll(any());
    }

    /**
     * Проверяет получение всех секторов указанного зала.
     */
    @Test
    void shouldReturnSectorsByHallId() {
        UUID hallId = UUID.randomUUID();

        Hall hall = org.mockito.Mockito.mock(Hall.class);

        Sector firstSector = org.mockito.Mockito.mock(Sector.class);
        Sector secondSector = org.mockito.Mockito.mock(Sector.class);

        UUID firstSectorId = UUID.randomUUID();
        UUID secondSectorId = UUID.randomUUID();

        when(hall.getId()).thenReturn(hallId);

        when(firstSector.getId()).thenReturn(firstSectorId);
        when(firstSector.getHall()).thenReturn(hall);
        when(firstSector.getName()).thenReturn("VIP");
        when(firstSector.getRowCount()).thenReturn(2);
        when(firstSector.getSeatsPerRow()).thenReturn(3);

        when(secondSector.getId()).thenReturn(secondSectorId);
        when(secondSector.getHall()).thenReturn(hall);
        when(secondSector.getName()).thenReturn("Standard");
        when(secondSector.getRowCount()).thenReturn(5);
        when(secondSector.getSeatsPerRow()).thenReturn(10);

        when(hallRepository.existsById(hallId))
                .thenReturn(true);

        when(sectorRepository.findAllByHallId(hallId))
                .thenReturn(List.of(firstSector, secondSector));

        List<SectorResponse> result =
                sectorService.getSectorsByHallId(hallId);

        assertThat(result).hasSize(2);

        assertThat(result.get(0).getId()).isEqualTo(firstSectorId);
        assertThat(result.get(0).getHallId()).isEqualTo(hallId);
        assertThat(result.get(0).getName()).isEqualTo("VIP");
        assertThat(result.get(0).getRowCount()).isEqualTo(2);
        assertThat(result.get(0).getSeatsPerRow()).isEqualTo(3);

        assertThat(result.get(1).getId()).isEqualTo(secondSectorId);
        assertThat(result.get(1).getHallId()).isEqualTo(hallId);
        assertThat(result.get(1).getName()).isEqualTo("Standard");
        assertThat(result.get(1).getRowCount()).isEqualTo(5);
        assertThat(result.get(1).getSeatsPerRow()).isEqualTo(10);

        verify(sectorRepository).findAllByHallId(hallId);
    }

    /**
     * Проверяет ошибку при запросе секторов
     * несуществующего зала.
     */
    @Test
    void shouldThrowExceptionWhenHallNotFoundOnGetSectors() {
        UUID hallId = UUID.randomUUID();

        when(hallRepository.existsById(hallId))
                .thenReturn(false);

        assertThatThrownBy(() ->
                sectorService.getSectorsByHallId(hallId)
        )
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Hall not found: " + hallId);

        verify(sectorRepository, never())
                .findAllByHallId(hallId);
    }
}