package com.dmitriy.seatflow.venue;

import com.dmitriy.seatflow.common.error.ResourceNotFoundException;
import com.dmitriy.seatflow.venue.dto.CreateVenueRequest;
import com.dmitriy.seatflow.venue.dto.VenueResponse;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Сервис управления площадками проведения событий.
 *
 * <p>По умолчанию методы выполняются в read-only транзакциях. Операции записи
 * явно переопределяют этот режим.</p>
 */
@Service
@Transactional(readOnly = true)
public class VenueService {

    private final VenueRepository venueRepository;

    public VenueService(VenueRepository venueRepository) {
        this.venueRepository = venueRepository;
    }

    /**
     * Создаёт новую площадку.
     *
     * @param request параметры создаваемой площадки
     * @return созданная площадка с UUID и timestamps
     */
    @Transactional
    public VenueResponse create(CreateVenueRequest request) {
        // UUID и timestamps заполняются JPA при сохранении entity.
        Venue venue = new Venue(
                request.getName(),
                request.getCity(),
                request.getAddress(),
                request.getTimezone()
        );

        Venue savedVenue = venueRepository.save(venue);

        return toResponse(savedVenue);
    }

    /**
     * Возвращает площадку по идентификатору.
     *
     * @param id идентификатор площадки
     * @return найденная площадка
     * @throws ResourceNotFoundException если площадка не существует
     */
    public VenueResponse getById(UUID id) {
        Venue venue = venueRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Venue not found: " + id)
                );

        return toResponse(venue);
    }

    /**
     * Возвращает все площадки, отсортированные по названию.
     *
     * @return список площадок или пустой список
     */
    public List<VenueResponse> getAll() {
        return venueRepository.findAll(Sort.by(Sort.Direction.ASC, "name"))
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private VenueResponse toResponse(Venue venue) {
        return new VenueResponse(
                venue.getId(),
                venue.getName(),
                venue.getCity(),
                venue.getAddress(),
                venue.getTimezone(),
                venue.getCreatedAt(),
                venue.getUpdatedAt()
        );
    }
}
