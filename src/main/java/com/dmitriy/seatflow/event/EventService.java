package com.dmitriy.seatflow.event;

import com.dmitriy.seatflow.common.error.RequestValidationException;
import com.dmitriy.seatflow.common.error.ResourceNotFoundException;
import com.dmitriy.seatflow.event.dto.CreateEventRequest;
import com.dmitriy.seatflow.event.dto.EventResponse;
import com.dmitriy.seatflow.hall.Hall;
import com.dmitriy.seatflow.hall.HallRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Сервис для работы с событиями.
 */
@Service
public class EventService {

    private final HallRepository hallRepository;
    private final EventRepository eventRepository;

    public EventService(
            HallRepository hallRepository,
            EventRepository eventRepository
    ) {
        this.hallRepository = hallRepository;
        this.eventRepository = eventRepository;
    }

    /**
     * Создает новое событие в указанном зале.
     *
     * @param hallId идентификатор зала
     * @param request данные для создания события
     * @return созданное событие
     * @throws ResourceNotFoundException если зал не найден
     * @throws RequestValidationException если время окончания
     *                                    не позже времени начала
     */
    @Transactional
    public EventResponse createEvent(
            UUID hallId,
            CreateEventRequest request
    ) {
        Hall hall = hallRepository.findById(hallId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Hall not found: " + hallId
                        )
                );

        if (!request.getEndsAt().isAfter(request.getStartsAt())) {
            throw new RequestValidationException(
                    "Event end time must be after start time"
            );
        }

        Event event = new Event(
                hall,
                request.getTitle(),
                request.getDescription(),
                request.getStartsAt(),
                request.getEndsAt()
        );

        Event savedEvent = eventRepository.save(event);

        return toResponse(savedEvent);
    }

    /**
     * Возвращает все события указанного зала.
     *
     * @param hallId идентификатор зала
     * @return список событий, отсортированный по времени начала
     * @throws ResourceNotFoundException если зал не найден
     */
    @Transactional(readOnly = true)
    public List<EventResponse> getEventsByHallId(UUID hallId) {
        if (!hallRepository.existsById(hallId)) {
            throw new ResourceNotFoundException(
                    "Hall not found: " + hallId
            );
        }

        return eventRepository
                .findAllByHallIdOrderByStartsAtAsc(hallId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * Преобразует сущность Event в DTO для ответа API.
     *
     * @param event сущность события
     * @return DTO события
     */
    private EventResponse toResponse(Event event) {
        return new EventResponse(
                event.getId(),
                event.getHall().getId(),
                event.getTitle(),
                event.getDescription(),
                event.getStartsAt(),
                event.getEndsAt(),
                event.getCreatedAt(),
                event.getUpdatedAt()
        );
    }
}