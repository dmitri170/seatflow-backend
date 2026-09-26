package com.dmitriy.seatflow.eventseat;

import com.dmitriy.seatflow.common.error.ResourceNotFoundException;
import com.dmitriy.seatflow.event.Event;
import com.dmitriy.seatflow.event.EventRepository;
import com.dmitriy.seatflow.eventseat.dto.EventSeatResponse;
import com.dmitriy.seatflow.seat.Seat;
import com.dmitriy.seatflow.seat.SeatRepository;
import com.dmitriy.seatflow.sector.Sector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class EventSeatService {

    private final EventSeatRepository eventSeatRepository;
    private final SeatRepository seatRepository;
    private final EventRepository eventRepository;

    @Autowired
    public EventSeatService(EventSeatRepository eventSeatRepository, SeatRepository seatRepository, EventRepository eventRepository) {
        this.eventSeatRepository = eventSeatRepository;
        this.seatRepository = seatRepository;
        this.eventRepository = eventRepository;
    }

    @Transactional
    public void createForEvent(Event event){
        List<Seat> seats=
                seatRepository.findAllBySectorHallIdOrderBySectorNameAscRowNumberAscSeatNumberAsc(event.getHall().getId());
        List<EventSeat> eventSeat=new ArrayList<>();
        for(Seat seat:seats){
            eventSeat.add(new EventSeat(event,seat));
        }
        eventSeatRepository.saveAll(eventSeat);
    }
    @Transactional(readOnly = true)
    public List<EventSeatResponse> getByEventId(UUID eventId){
        if (!eventRepository.existsById(eventId)) {
            throw new ResourceNotFoundException(
                    "Event not found: " + eventId
            );
        }

        return eventSeatRepository
                .findAllByEventIdOrderBySeatSectorNameAscSeatRowNumberAscSeatSeatNumberAsc(eventId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private EventSeatResponse toResponse(EventSeat eventSeat){
        Seat seat=eventSeat.getSeat();
        Sector sector=seat.getSector();
        return new EventSeatResponse(
                eventSeat.getId(),
        seat.getId(),
        sector.getId(),
        sector.getName(),
        seat.getRowNumber(),
        seat.getSeatNumber(),
        eventSeat.getStatus()
        );
    }
}
