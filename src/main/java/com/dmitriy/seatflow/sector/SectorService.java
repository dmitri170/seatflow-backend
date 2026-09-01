package com.dmitriy.seatflow.sector;

import com.dmitriy.seatflow.common.error.ResourceConflictException;
import com.dmitriy.seatflow.common.error.ResourceNotFoundException;
import com.dmitriy.seatflow.hall.Hall;
import com.dmitriy.seatflow.hall.HallRepository;
import com.dmitriy.seatflow.seat.Seat;
import com.dmitriy.seatflow.seat.SeatRepository;
import com.dmitriy.seatflow.sector.dto.CreateSectorRequest;
import com.dmitriy.seatflow.sector.dto.SectorResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Сервис для управления секторами внутри залов.
 *
 * <p>Отвечает за создание секторов, автоматическую генерацию мест
 * и получение списка секторов конкретного зала.</p>
 */
@Service
public class SectorService {

    private final HallRepository hallRepository;
    private final SectorRepository sectorRepository;
    private final SeatRepository seatRepository;

    public SectorService(
            HallRepository hallRepository,
            SectorRepository sectorRepository,
            SeatRepository seatRepository
    ) {
        this.hallRepository = hallRepository;
        this.sectorRepository = sectorRepository;
        this.seatRepository = seatRepository;
    }

    /**
     * Создает новый сектор в указанном зале и автоматически
     * генерирует все места в соответствии с количеством рядов
     * и количеством мест в каждом ряду.
     *
     * @param hallId идентификатор зала, в котором создается сектор
     * @param request данные для создания сектора
     * @return созданный сектор
     * @throws ResourceNotFoundException если зал не найден
     * @throws ResourceConflictException если сектор с таким именем
     *                                   уже существует в этом зале
     */
    @Transactional
    public SectorResponse createSector(UUID hallId, CreateSectorRequest request) {

        // Проверяем, что родительский зал существует.
        Hall hall = hallRepository.findById(hallId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Hall not found: " + hallId)
                );

        // В пределах одного зала названия секторов должны быть уникальными.
        if (sectorRepository.existsByHallIdAndNameIgnoreCase(
                hallId,
                request.getName()
        )) {
            throw new ResourceConflictException(
                    "Sector already exists: " + request.getName()
            );
        }

        // Сначала сохраняем сектор, так как каждое место
        // должно ссылаться на существующий сектор.
        Sector sector = new Sector(
                hall,
                request.getName(),
                request.getRowCount(),
                request.getSeatsPerRow()
        );

        Sector savedSector = sectorRepository.save(sector);

        // Генерируем все места для нового сектора.
        List<Seat> seats = new ArrayList<>();

        for (int rowNumber = 1;
             rowNumber <= request.getRowCount();
             rowNumber++) {

            for (int seatNumber = 1;
                 seatNumber <= request.getSeatsPerRow();
                 seatNumber++) {

                seats.add(new Seat(
                        savedSector,
                        rowNumber,
                        seatNumber
                ));
            }
        }

        // Сохраняем все созданные места одним вызовом.
        seatRepository.saveAll(seats);

        return toResponse(savedSector);
    }

    /**
     * Возвращает все сектора, относящиеся к указанному залу.
     *
     * @param hallId идентификатор зала
     * @return список секторов зала
     * @throws ResourceNotFoundException если зал не найден
     */
    @Transactional(readOnly = true)
    public List<SectorResponse> getSectorsByHallId(UUID hallId) {

        if (!hallRepository.existsById(hallId)) {
            throw new ResourceNotFoundException(
                    "Hall not found: " + hallId
            );
        }

        return sectorRepository.findAllByHallId(hallId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * Преобразует сущность Sector в DTO для ответа API.
     *
     * @param sector сущность сектора
     * @return DTO сектора
     */
    private SectorResponse toResponse(Sector sector) {
        return new SectorResponse(
                sector.getId(),
                sector.getHall().getId(),
                sector.getName(),
                sector.getRowCount(),
                sector.getSeatsPerRow(),
                sector.getCreatedAt(),
                sector.getUpdatedAt()
        );
    }
}