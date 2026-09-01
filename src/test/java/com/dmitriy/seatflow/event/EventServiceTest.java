package com.dmitriy.seatflow.event;

import com.dmitriy.seatflow.common.error.RequestValidationException;
import com.dmitriy.seatflow.common.error.ResourceNotFoundException;
import com.dmitriy.seatflow.event.dto.CreateEventRequest;
import com.dmitriy.seatflow.event.dto.EventResponse;
import com.dmitriy.seatflow.hall.Hall;
import com.dmitriy.seatflow.hall.HallRepository;
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
 * Unit-тесты для {@link EventService}.
 */
@ExtendWith(MockitoExtension.class)
class EventServiceTest {

    @Mock
    private HallRepository hallRepository;

    @Mock
    private EventRepository eventRepository;

    @InjectMocks
    private EventService eventService;

    @Captor
    private ArgumentCaptor<Event> eventCaptor;

    /**
     * Проверяет успешное создание события.
     */
    @Test
    void shouldCreateEvent() {
        UUID hallId = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();

        Instant startsAt = Instant.parse("2026-10-10T16:00:00Z");
        Instant endsAt = Instant.parse("2026-10-10T19:00:00Z");
        Instant createdAt = Instant.parse("2026-09-01T10:00:00Z");
        Instant updatedAt = Instant.parse("2026-09-01T10:00:00Z");

        CreateEventRequest request = new CreateEventRequest(
                "Football Match",
                "Championship match",
                startsAt,
                endsAt
        );

        Hall hall = org.mockito.Mockito.mock(Hall.class);
        Event savedEvent = org.mockito.Mockito.mock(Event.class);

        when(hall.getId()).thenReturn(hallId);

        when(hallRepository.findById(hallId))
                .thenReturn(Optional.of(hall));

        when(eventRepository.save(any(Event.class)))
                .thenReturn(savedEvent);

        when(savedEvent.getId()).thenReturn(eventId);
        when(savedEvent.getHall()).thenReturn(hall);
        when(savedEvent.getTitle()).thenReturn("Football Match");
        when(savedEvent.getDescription()).thenReturn("Championship match");
        when(savedEvent.getStartsAt()).thenReturn(startsAt);
        when(savedEvent.getEndsAt()).thenReturn(endsAt);
        when(savedEvent.getCreatedAt()).thenReturn(createdAt);
        when(savedEvent.getUpdatedAt()).thenReturn(updatedAt);

        EventResponse response = eventService.createEvent(
                hallId,
                request
        );

        verify(eventRepository).save(eventCaptor.capture());

        Event eventToSave = eventCaptor.getValue();

        assertThat(eventToSave.getHall()).isSameAs(hall);
        assertThat(eventToSave.getTitle()).isEqualTo("Football Match");
        assertThat(eventToSave.getDescription()).isEqualTo("Championship match");
        assertThat(eventToSave.getStartsAt()).isEqualTo(startsAt);
        assertThat(eventToSave.getEndsAt()).isEqualTo(endsAt);

        assertThat(response.getId()).isEqualTo(eventId);
        assertThat(response.getHallId()).isEqualTo(hallId);
        assertThat(response.getTitle()).isEqualTo("Football Match");
        assertThat(response.getDescription()).isEqualTo("Championship match");
        assertThat(response.getStartsAt()).isEqualTo(startsAt);
        assertThat(response.getEndsAt()).isEqualTo(endsAt);
        assertThat(response.getCreatedAt()).isEqualTo(createdAt);
        assertThat(response.getUpdatedAt()).isEqualTo(updatedAt);
    }

    /**
     * Проверяет ошибку при создании события
     * в несуществующем зале.
     */
    @Test
    void shouldThrowExceptionWhenHallNotFoundOnCreate() {
        UUID hallId = UUID.randomUUID();

        CreateEventRequest request = new CreateEventRequest(
                "Football Match",
                "Championship match",
                Instant.parse("2026-10-10T16:00:00Z"),
                Instant.parse("2026-10-10T19:00:00Z")
        );

        when(hallRepository.findById(hallId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                eventService.createEvent(hallId, request)
        )
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Hall not found: " + hallId);

        verify(eventRepository, never())
                .save(any(Event.class));
    }

    /**
     * Проверяет ошибку, если время окончания события
     * раньше времени начала.
     */
    @Test
    void shouldThrowExceptionWhenEndTimeIsBeforeStartTime() {
        UUID hallId = UUID.randomUUID();

        Hall hall = org.mockito.Mockito.mock(Hall.class);

        Instant startsAt = Instant.parse("2026-10-10T19:00:00Z");
        Instant endsAt = Instant.parse("2026-10-10T16:00:00Z");

        CreateEventRequest request = new CreateEventRequest(
                "Football Match",
                null,
                startsAt,
                endsAt
        );

        when(hallRepository.findById(hallId))
                .thenReturn(Optional.of(hall));

        assertThatThrownBy(() ->
                eventService.createEvent(hallId, request)
        )
                .isInstanceOf(RequestValidationException.class)
                .hasMessage("Event end time must be after start time");

        verify(eventRepository, never())
                .save(any(Event.class));
    }

    /**
     * Проверяет ошибку, если время начала и окончания
     * события совпадают.
     */
    @Test
    void shouldThrowExceptionWhenStartAndEndTimeAreEqual() {
        UUID hallId = UUID.randomUUID();

        Hall hall = org.mockito.Mockito.mock(Hall.class);

        Instant eventTime = Instant.parse("2026-10-10T19:00:00Z");

        CreateEventRequest request = new CreateEventRequest(
                "Football Match",
                null,
                eventTime,
                eventTime
        );

        when(hallRepository.findById(hallId))
                .thenReturn(Optional.of(hall));

        assertThatThrownBy(() ->
                eventService.createEvent(hallId, request)
        )
                .isInstanceOf(RequestValidationException.class)
                .hasMessage("Event end time must be after start time");

        verify(eventRepository, never())
                .save(any(Event.class));
    }

    /**
     * Проверяет получение событий указанного зала.
     */
    @Test
    void shouldReturnEventsByHallId() {
        UUID hallId = UUID.randomUUID();

        UUID firstEventId = UUID.randomUUID();
        UUID secondEventId = UUID.randomUUID();

        Hall hall = org.mockito.Mockito.mock(Hall.class);

        Event firstEvent = org.mockito.Mockito.mock(Event.class);
        Event secondEvent = org.mockito.Mockito.mock(Event.class);

        Instant firstStartsAt =
                Instant.parse("2026-10-10T16:00:00Z");
        Instant firstEndsAt =
                Instant.parse("2026-10-10T19:00:00Z");

        Instant secondStartsAt =
                Instant.parse("2026-11-10T16:00:00Z");
        Instant secondEndsAt =
                Instant.parse("2026-11-10T19:00:00Z");

        Instant createdAt =
                Instant.parse("2026-09-01T10:00:00Z");

        when(hall.getId()).thenReturn(hallId);

        when(firstEvent.getId()).thenReturn(firstEventId);
        when(firstEvent.getHall()).thenReturn(hall);
        when(firstEvent.getTitle()).thenReturn("First Event");
        when(firstEvent.getDescription()).thenReturn("First description");
        when(firstEvent.getStartsAt()).thenReturn(firstStartsAt);
        when(firstEvent.getEndsAt()).thenReturn(firstEndsAt);
        when(firstEvent.getCreatedAt()).thenReturn(createdAt);
        when(firstEvent.getUpdatedAt()).thenReturn(createdAt);

        when(secondEvent.getId()).thenReturn(secondEventId);
        when(secondEvent.getHall()).thenReturn(hall);
        when(secondEvent.getTitle()).thenReturn("Second Event");
        when(secondEvent.getDescription()).thenReturn("Second description");
        when(secondEvent.getStartsAt()).thenReturn(secondStartsAt);
        when(secondEvent.getEndsAt()).thenReturn(secondEndsAt);
        when(secondEvent.getCreatedAt()).thenReturn(createdAt);
        when(secondEvent.getUpdatedAt()).thenReturn(createdAt);

        when(hallRepository.existsById(hallId))
                .thenReturn(true);

        when(eventRepository.findAllByHallIdOrderByStartsAtAsc(hallId))
                .thenReturn(List.of(firstEvent, secondEvent));

        List<EventResponse> result =
                eventService.getEventsByHallId(hallId);

        assertThat(result).hasSize(2);

        assertThat(result.get(0).getId()).isEqualTo(firstEventId);
        assertThat(result.get(0).getHallId()).isEqualTo(hallId);
        assertThat(result.get(0).getTitle()).isEqualTo("First Event");
        assertThat(result.get(0).getStartsAt()).isEqualTo(firstStartsAt);
        assertThat(result.get(0).getEndsAt()).isEqualTo(firstEndsAt);

        assertThat(result.get(1).getId()).isEqualTo(secondEventId);
        assertThat(result.get(1).getHallId()).isEqualTo(hallId);
        assertThat(result.get(1).getTitle()).isEqualTo("Second Event");
        assertThat(result.get(1).getStartsAt()).isEqualTo(secondStartsAt);
        assertThat(result.get(1).getEndsAt()).isEqualTo(secondEndsAt);

        verify(eventRepository)
                .findAllByHallIdOrderByStartsAtAsc(hallId);
    }

    /**
     * Проверяет возврат пустого списка,
     * если в зале пока нет событий.
     */
    @Test
    void shouldReturnEmptyListWhenHallHasNoEvents() {
        UUID hallId = UUID.randomUUID();

        when(hallRepository.existsById(hallId))
                .thenReturn(true);

        when(eventRepository.findAllByHallIdOrderByStartsAtAsc(hallId))
                .thenReturn(List.of());

        List<EventResponse> result =
                eventService.getEventsByHallId(hallId);

        assertThat(result).isEmpty();
    }

    /**
     * Проверяет ошибку при получении событий
     * несуществующего зала.
     */
    @Test
    void shouldThrowExceptionWhenHallNotFoundOnGetEvents() {
        UUID hallId = UUID.randomUUID();

        when(hallRepository.existsById(hallId))
                .thenReturn(false);

        assertThatThrownBy(() ->
                eventService.getEventsByHallId(hallId)
        )
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Hall not found: " + hallId);

        verify(eventRepository, never())
                .findAllByHallIdOrderByStartsAtAsc(hallId);
    }
}