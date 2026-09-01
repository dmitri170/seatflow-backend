package com.dmitriy.seatflow.seat;

import com.dmitriy.seatflow.common.error.ResourceNotFoundException;
import com.dmitriy.seatflow.seat.dto.SeatResponse;
import com.dmitriy.seatflow.sector.Sector;
import com.dmitriy.seatflow.sector.SectorRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit-тесты для {@link SeatService}.
 */
@ExtendWith(MockitoExtension.class)
class SeatServiceTest {

    @Mock
    private SectorRepository sectorRepository;

    @Mock
    private SeatRepository seatRepository;

    @InjectMocks
    private SeatService seatService;

    /**
     * Проверяет получение всех мест сектора
     * и корректное преобразование сущностей в DTO.
     */
    @Test
    void shouldReturnSeatsBySectorId() {
        UUID sectorId = UUID.randomUUID();

        UUID firstSeatId = UUID.randomUUID();
        UUID secondSeatId = UUID.randomUUID();
        UUID thirdSeatId = UUID.randomUUID();

        Instant createdAt = Instant.parse("2026-08-31T10:00:00Z");
        Instant updatedAt = Instant.parse("2026-08-31T10:00:00Z");

        Sector sector = org.mockito.Mockito.mock(Sector.class);

        Seat firstSeat = org.mockito.Mockito.mock(Seat.class);
        Seat secondSeat = org.mockito.Mockito.mock(Seat.class);
        Seat thirdSeat = org.mockito.Mockito.mock(Seat.class);

        when(sector.getId()).thenReturn(sectorId);

        when(firstSeat.getId()).thenReturn(firstSeatId);
        when(firstSeat.getSector()).thenReturn(sector);
        when(firstSeat.getRowNumber()).thenReturn(1);
        when(firstSeat.getSeatNumber()).thenReturn(1);
        when(firstSeat.getCreatedAt()).thenReturn(createdAt);
        when(firstSeat.getUpdatedAt()).thenReturn(updatedAt);

        when(secondSeat.getId()).thenReturn(secondSeatId);
        when(secondSeat.getSector()).thenReturn(sector);
        when(secondSeat.getRowNumber()).thenReturn(1);
        when(secondSeat.getSeatNumber()).thenReturn(2);
        when(secondSeat.getCreatedAt()).thenReturn(createdAt);
        when(secondSeat.getUpdatedAt()).thenReturn(updatedAt);

        when(thirdSeat.getId()).thenReturn(thirdSeatId);
        when(thirdSeat.getSector()).thenReturn(sector);
        when(thirdSeat.getRowNumber()).thenReturn(2);
        when(thirdSeat.getSeatNumber()).thenReturn(1);
        when(thirdSeat.getCreatedAt()).thenReturn(createdAt);
        when(thirdSeat.getUpdatedAt()).thenReturn(updatedAt);

        when(sectorRepository.existsById(sectorId))
                .thenReturn(true);

        when(seatRepository
                .findAllBySectorIdOrderByRowNumberAscSeatNumberAsc(sectorId))
                .thenReturn(List.of(
                        firstSeat,
                        secondSeat,
                        thirdSeat
                ));

        List<SeatResponse> result =
                seatService.getSeatsBySectorId(sectorId);

        assertThat(result).hasSize(3);

        assertThat(result.get(0).getId()).isEqualTo(firstSeatId);
        assertThat(result.get(0).getSectorId()).isEqualTo(sectorId);
        assertThat(result.get(0).getRowNumber()).isEqualTo(1);
        assertThat(result.get(0).getSeatNumber()).isEqualTo(1);
        assertThat(result.get(0).getCreatedAt()).isEqualTo(createdAt);
        assertThat(result.get(0).getUpdatedAt()).isEqualTo(updatedAt);

        assertThat(result.get(1).getId()).isEqualTo(secondSeatId);
        assertThat(result.get(1).getSectorId()).isEqualTo(sectorId);
        assertThat(result.get(1).getRowNumber()).isEqualTo(1);
        assertThat(result.get(1).getSeatNumber()).isEqualTo(2);

        assertThat(result.get(2).getId()).isEqualTo(thirdSeatId);
        assertThat(result.get(2).getSectorId()).isEqualTo(sectorId);
        assertThat(result.get(2).getRowNumber()).isEqualTo(2);
        assertThat(result.get(2).getSeatNumber()).isEqualTo(1);

        verify(sectorRepository).existsById(sectorId);

        verify(seatRepository)
                .findAllBySectorIdOrderByRowNumberAscSeatNumberAsc(sectorId);
    }

    /**
     * Проверяет возврат пустого списка,
     * если сектор существует, но мест в нем нет.
     */
    @Test
    void shouldReturnEmptyListWhenSectorHasNoSeats() {
        UUID sectorId = UUID.randomUUID();

        when(sectorRepository.existsById(sectorId))
                .thenReturn(true);

        when(seatRepository
                .findAllBySectorIdOrderByRowNumberAscSeatNumberAsc(sectorId))
                .thenReturn(List.of());

        List<SeatResponse> result =
                seatService.getSeatsBySectorId(sectorId);

        assertThat(result).isEmpty();

        verify(seatRepository)
                .findAllBySectorIdOrderByRowNumberAscSeatNumberAsc(sectorId);
    }

    /**
     * Проверяет ошибку при попытке получить места
     * несуществующего сектора.
     */
    @Test
    void shouldThrowExceptionWhenSectorNotFound() {
        UUID sectorId = UUID.randomUUID();

        when(sectorRepository.existsById(sectorId))
                .thenReturn(false);

        assertThatThrownBy(() ->
                seatService.getSeatsBySectorId(sectorId)
        )
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Sector not found: " + sectorId);

        verify(seatRepository, never())
                .findAllBySectorIdOrderByRowNumberAscSeatNumberAsc(sectorId);
    }
}