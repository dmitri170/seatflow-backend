package com.dmitriy.seatflow.hall;

import com.dmitriy.seatflow.common.error.ResourceConflictException;
import com.dmitriy.seatflow.common.error.ResourceNotFoundException;
import com.dmitriy.seatflow.hall.dto.CreateHallRequest;
import com.dmitriy.seatflow.hall.dto.HallResponse;
import com.dmitriy.seatflow.venue.Venue;
import com.dmitriy.seatflow.venue.VenueRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Сервис управления залами внутри площадок.
 *
 * <p>Все операции чтения выполняются в read-only транзакциях. Для методов,
 * изменяющих данные, режим транзакции переопределяется отдельно.</p>
 */
@Service
@Transactional(readOnly = true)
public class HallService {

    private final HallRepository hallRepository;
    private final VenueRepository venueRepository;

    public HallService(
            HallRepository hallRepository,
            VenueRepository venueRepository
    ) {
        this.hallRepository = hallRepository;
        this.venueRepository = venueRepository;
    }

    /**
     * Создаёт зал внутри существующей площадки.
     * @param venueId идентификатор площадки
     * @param request параметры создаваемого зала
     * @return созданный зал
     */
    @Transactional
    public HallResponse create(
            UUID venueId,
            CreateHallRequest request
    ) {
        // Зал нельзя создать без существующей площадки.
        Venue venue = venueRepository.findById(venueId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Venue not found: " + venueId
                        )
                );

        // Названия залов должны быть уникальными внутри одной площадки.
        if (hallRepository.existsByVenue_IdAndName(
                venueId,
                request.getName()
        )) {
            throw new ResourceConflictException(
                    "Hall already exists in venue: " + request.getName()
            );
        }

        // UUID и timestamps заполнят JPA lifecycle callbacks.
        Hall hall = new Hall(
                venue,
                request.getName(),
                request.getCapacity()
        );

        return toResponse(hallRepository.save(hall));
    }

    /**
     * Возвращает зал по идентификатору.
     * @param hallId идентификатор зала
     * @return найденный зал
     * @throws ResourceNotFoundException если зал не существует
     */
    public HallResponse getById(UUID hallId) {
        Hall hall = hallRepository.findById(hallId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Hall not found: " + hallId
                        )
                );

        return toResponse(hall);
    }

    /**
     * Возвращает залы указанной площадки, отсортированные по названию.
     *
     * @param venueId идентификатор площадки
     * @return список залов; пустой список, если у площадки ещё нет залов
     * @throws ResourceNotFoundException если площадка не существует
     */
    public List<HallResponse> getAllByVenueId(UUID venueId) {
        // Отличаем существующую площадку без залов от несуществующей площадки.
        if (!venueRepository.existsById(venueId)) {
            throw new ResourceNotFoundException(
                    "Venue not found: " + venueId
            );
        }

        return hallRepository.findAllByVenue_IdOrderByNameAsc(venueId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private HallResponse toResponse(Hall hall) {
        return new HallResponse(
                hall.getId(),
                hall.getVenue().getId(),
                hall.getName(),
                hall.getCapacity(),
                hall.getCreatedAt(),
                hall.getUpdatedAt()
        );
    }
}
