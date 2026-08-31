package com.dmitriy.seatflow.hall;

import com.dmitriy.seatflow.common.error.ResourceConflictException;
import com.dmitriy.seatflow.common.error.ResourceNotFoundException;
import com.dmitriy.seatflow.hall.dto.CreateHallRequest;
import com.dmitriy.seatflow.hall.dto.HallResponse;
import com.dmitriy.seatflow.venue.Venue;
import com.dmitriy.seatflow.venue.VenueRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Unit-тесты бизнес-логики управления залами.
 *
 * <p>Spring и PostgreSQL здесь не запускаются: зависимости сервиса
 * заменяются Mockito-объектами.</p>
 */
@ExtendWith(MockitoExtension.class)
class HallServiceTest {

    @Mock
    private HallRepository hallRepository;

    @Mock
    private VenueRepository venueRepository;

    @InjectMocks
    private HallService hallService;

    /**
     * Проверяет успешное создание зала и mapping сохранённой entity.
     */
    @Test
    void shouldCreateHall() {
        UUID venueId = UUID.randomUUID();
        UUID hallId = UUID.randomUUID();
        Instant now = Instant.now();

        CreateHallRequest request = new CreateHallRequest();
        request.setName("Main Hall");
        request.setCapacity(500);

        Venue venue = mock(Venue.class);
        when(venue.getId()).thenReturn(venueId);

        Hall savedHall = createHallMock(
                hallId,
                venue,
                "Main Hall",
                500,
                now
        );

        when(venueRepository.findById(venueId))
                .thenReturn(Optional.of(venue));
        when(hallRepository.existsByVenue_IdAndName(
                venueId,
                "Main Hall"
        )).thenReturn(false);
        when(hallRepository.save(any(Hall.class)))
                .thenReturn(savedHall);

        HallResponse response = hallService.create(venueId, request);

        assertThat(response.getId()).isEqualTo(hallId);
        assertThat(response.getVenueId()).isEqualTo(venueId);
        assertThat(response.getName()).isEqualTo("Main Hall");
        assertThat(response.getCapacity()).isEqualTo(500);
        assertThat(response.getCreatedAt()).isEqualTo(now);
        assertThat(response.getUpdatedAt()).isEqualTo(now);

        // Проверяем entity, которую сервис отправил на сохранение.
        ArgumentCaptor<Hall> hallCaptor =
                ArgumentCaptor.forClass(Hall.class);

        verify(hallRepository).save(hallCaptor.capture());

        Hall hallToSave = hallCaptor.getValue();

        assertThat(hallToSave.getVenue()).isSameAs(venue);
        assertThat(hallToSave.getName()).isEqualTo("Main Hall");
        assertThat(hallToSave.getCapacity()).isEqualTo(500);
    }

    /**
     * Зал нельзя создать внутри несуществующей площадки.
     */
    @Test
    void shouldThrowWhenVenueDoesNotExistDuringCreation() {
        UUID venueId = UUID.randomUUID();

        CreateHallRequest request = new CreateHallRequest();
        request.setName("Main Hall");
        request.setCapacity(500);

        when(venueRepository.findById(venueId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> hallService.create(venueId, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Venue not found: " + venueId);

        // До HallRepository выполнение доходить не должно.
        verifyNoInteractions(hallRepository);
    }

    /**
     * Название зала должно быть уникальным внутри одной площадки.
     */
    @Test
    void shouldThrowWhenHallNameAlreadyExists() {
        UUID venueId = UUID.randomUUID();
        Venue venue = mock(Venue.class);

        CreateHallRequest request = new CreateHallRequest();
        request.setName("Main Hall");
        request.setCapacity(500);

        when(venueRepository.findById(venueId))
                .thenReturn(Optional.of(venue));
        when(hallRepository.existsByVenue_IdAndName(
                venueId,
                "Main Hall"
        )).thenReturn(true);

        assertThatThrownBy(() -> hallService.create(venueId, request))
                .isInstanceOf(ResourceConflictException.class)
                .hasMessage("Hall already exists in venue: Main Hall");

        verify(hallRepository, never()).save(any(Hall.class));
    }

    /**
     * Проверяет получение существующего зала по UUID.
     */
    @Test
    void shouldReturnHallById() {
        UUID venueId = UUID.randomUUID();
        UUID hallId = UUID.randomUUID();
        Instant now = Instant.now();

        Venue venue = mock(Venue.class);
        when(venue.getId()).thenReturn(venueId);

        Hall hall = createHallMock(
                hallId,
                venue,
                "Main Hall",
                500,
                now
        );

        when(hallRepository.findById(hallId))
                .thenReturn(Optional.of(hall));

        HallResponse response = hallService.getById(hallId);

        assertThat(response.getId()).isEqualTo(hallId);
        assertThat(response.getVenueId()).isEqualTo(venueId);
        assertThat(response.getName()).isEqualTo("Main Hall");
        assertThat(response.getCapacity()).isEqualTo(500);

        verify(hallRepository).findById(hallId);
    }

    /**
     * Для неизвестного UUID сервис должен вернуть доменную ошибку 404.
     */
    @Test
    void shouldThrowWhenHallDoesNotExist() {
        UUID hallId = UUID.randomUUID();

        when(hallRepository.findById(hallId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> hallService.getById(hallId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Hall not found: " + hallId);

        verify(hallRepository).findById(hallId);
    }

    /**
     * Проверяет получение отсортированного списка залов площадки.
     */
    @Test
    void shouldReturnHallsByVenueId() {
        UUID venueId = UUID.randomUUID();
        Instant now = Instant.now();

        Venue venue = mock(Venue.class);
        when(venue.getId()).thenReturn(venueId);

        Hall firstHall = createHallMock(
                UUID.randomUUID(),
                venue,
                "Main Hall",
                500,
                now
        );

        Hall secondHall = createHallMock(
                UUID.randomUUID(),
                venue,
                "Small Hall",
                100,
                now
        );

        when(venueRepository.existsById(venueId)).thenReturn(true);
        when(hallRepository.findAllByVenue_IdOrderByNameAsc(venueId))
                .thenReturn(List.of(firstHall, secondHall));

        List<HallResponse> responses =
                hallService.getAllByVenueId(venueId);

        assertThat(responses)
                .extracting(HallResponse::getName)
                .containsExactly("Main Hall", "Small Hall");

        assertThat(responses)
                .extracting(HallResponse::getVenueId)
                .containsOnly(venueId);

        verify(hallRepository)
                .findAllByVenue_IdOrderByNameAsc(venueId);
    }

    /**
     * Запрос вложенного ресурса должен отличать пустой список
     * от несуществующей площадки.
     */
    @Test
    void shouldThrowWhenVenueDoesNotExistDuringListRequest() {
        UUID venueId = UUID.randomUUID();

        when(venueRepository.existsById(venueId)).thenReturn(false);

        assertThatThrownBy(() ->
                hallService.getAllByVenueId(venueId)
        )
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Venue not found: " + venueId);

        verifyNoInteractions(hallRepository);
    }

    /**
     * Создаёт mock сохранённого Hall с заполненными JPA-полями.
     */
    private Hall createHallMock(
            UUID hallId,
            Venue venue,
            String name,
            int capacity,
            Instant timestamp
    ) {
        Hall hall = mock(Hall.class);

        when(hall.getId()).thenReturn(hallId);
        when(hall.getVenue()).thenReturn(venue);
        when(hall.getName()).thenReturn(name);
        when(hall.getCapacity()).thenReturn(capacity);
        when(hall.getCreatedAt()).thenReturn(timestamp);
        when(hall.getUpdatedAt()).thenReturn(timestamp);

        return hall;
    }
}