package com.dmitriy.seatflow.eventseat;

import com.dmitriy.seatflow.common.error.ResourceNotFoundException;
import com.dmitriy.seatflow.event.Event;
import com.dmitriy.seatflow.event.EventRepository;
import com.dmitriy.seatflow.eventseat.dto.EventSeatResponse;
import com.dmitriy.seatflow.hall.Hall;
import com.dmitriy.seatflow.seat.Seat;
import com.dmitriy.seatflow.seat.SeatRepository;
import com.dmitriy.seatflow.sector.Sector;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EventSeatServiceTest {

    @Mock
    private EventSeatRepository eventSeatRepository;

    @Mock
    private SeatRepository seatRepository;

    @Mock
    private EventRepository eventRepository;

    @InjectMocks
    private EventSeatService eventSeatService;

    @Captor
    private ArgumentCaptor<List<EventSeat>> eventSeatsCaptor;

    @Test
    void shouldCreateEventSeatsForAllHallSeats() {
        UUID hallId = UUID.randomUUID();

        Hall hall = mock(Hall.class);
        Event event = mock(Event.class);
        Seat firstSeat = mock(Seat.class);
        Seat secondSeat = mock(Seat.class);

        when(event.getHall()).thenReturn(hall);
        when(hall.getId()).thenReturn(hallId);
        when(seatRepository
                .findAllBySectorHallIdOrderBySectorNameAscRowNumberAscSeatNumberAsc(hallId))
                .thenReturn(List.of(firstSeat, secondSeat));

        eventSeatService.createForEvent(event);

        verify(eventSeatRepository).saveAll(eventSeatsCaptor.capture());

        List<EventSeat> savedEventSeats = eventSeatsCaptor.getValue();

        assertThat(savedEventSeats).hasSize(2);
        assertThat(savedEventSeats.get(0).getEvent()).isSameAs(event);
        assertThat(savedEventSeats.get(0).getSeat()).isSameAs(firstSeat);
        assertThat(savedEventSeats.get(0).getStatus())
                .isEqualTo(EventSeatStatus.AVAILABLE);
        assertThat(savedEventSeats.get(1).getEvent()).isSameAs(event);
        assertThat(savedEventSeats.get(1).getSeat()).isSameAs(secondSeat);
        assertThat(savedEventSeats.get(1).getStatus())
                .isEqualTo(EventSeatStatus.AVAILABLE);
    }

    @Test
    void shouldReturnEventSeatsByEventId() {
        UUID eventId = UUID.randomUUID();
        UUID eventSeatId = UUID.randomUUID();
        UUID seatId = UUID.randomUUID();
        UUID sectorId = UUID.randomUUID();

        EventSeat eventSeat = mock(EventSeat.class);
        Seat seat = mock(Seat.class);
        Sector sector = mock(Sector.class);

        when(eventRepository.existsById(eventId)).thenReturn(true);
        when(eventSeatRepository
                .findAllByEventIdOrderBySeatSectorNameAscSeatRowNumberAscSeatSeatNumberAsc(eventId))
                .thenReturn(List.of(eventSeat));

        when(eventSeat.getId()).thenReturn(eventSeatId);
        when(eventSeat.getSeat()).thenReturn(seat);
        when(eventSeat.getStatus()).thenReturn(EventSeatStatus.AVAILABLE);
        when(seat.getId()).thenReturn(seatId);
        when(seat.getSector()).thenReturn(sector);
        when(seat.getRowNumber()).thenReturn(3);
        when(seat.getSeatNumber()).thenReturn(14);
        when(sector.getId()).thenReturn(sectorId);
        when(sector.getName()).thenReturn("Parterre");

        List<EventSeatResponse> result =
                eventSeatService.getByEventId(eventId);

        assertThat(result).hasSize(1);

        EventSeatResponse response = result.getFirst();

        assertThat(response.getId()).isEqualTo(eventSeatId);
        assertThat(response.getSeatId()).isEqualTo(seatId);
        assertThat(response.getSectorId()).isEqualTo(sectorId);
        assertThat(response.getSectorName()).isEqualTo("Parterre");
        assertThat(response.getRowNumber()).isEqualTo(3);
        assertThat(response.getSeatNumber()).isEqualTo(14);
        assertThat(response.getStatus()).isEqualTo(EventSeatStatus.AVAILABLE);
    }

    @Test
    void shouldThrowExceptionWhenEventNotFound() {
        UUID eventId = UUID.randomUUID();

        when(eventRepository.existsById(eventId)).thenReturn(false);

        assertThatThrownBy(() -> eventSeatService.getByEventId(eventId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Event not found: " + eventId);

        verify(eventSeatRepository, never())
                .findAllByEventIdOrderBySeatSectorNameAscSeatRowNumberAscSeatSeatNumberAsc(eventId);
    }
}
