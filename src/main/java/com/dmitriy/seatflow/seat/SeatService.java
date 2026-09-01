package com.dmitriy.seatflow.seat;

import com.dmitriy.seatflow.common.error.ResourceNotFoundException;
import com.dmitriy.seatflow.seat.dto.SeatResponse;
import com.dmitriy.seatflow.sector.SectorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Сервис для работы с местами в секторах.
 *
 * <p>Отвечает за получение мест, относящихся
 * к конкретному сектору.</p>
 */
@Service
public class SeatService {

    private final SectorRepository sectorRepository;
    private final SeatRepository seatRepository;

    public SeatService(
            SectorRepository sectorRepository,
            SeatRepository seatRepository
    ) {
        this.sectorRepository = sectorRepository;
        this.seatRepository = seatRepository;
    }

    /**
     * Возвращает все места указанного сектора.
     *
     * <p>Места возвращаются в порядке номера ряда,
     * а внутри ряда — по номеру места.</p>
     *
     * @param sectorId идентификатор сектора
     * @return список мест сектора
     * @throws ResourceNotFoundException если сектор не найден
     */
    @Transactional(readOnly = true)
    public List<SeatResponse> getSeatsBySectorId(UUID sectorId) {

        if (!sectorRepository.existsById(sectorId)) {
            throw new ResourceNotFoundException(
                    "Sector not found: " + sectorId
            );
        }

        return seatRepository
                .findAllBySectorIdOrderByRowNumberAscSeatNumberAsc(sectorId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * Преобразует сущность Seat в DTO для ответа API.
     *
     * @param seat сущность места
     * @return DTO места
     */
    private SeatResponse toResponse(Seat seat) {
        return new SeatResponse(
                seat.getId(),
                seat.getSector().getId(),
                seat.getRowNumber(),
                seat.getSeatNumber(),
                seat.getCreatedAt(),
                seat.getUpdatedAt()
        );
    }
}