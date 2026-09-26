package com.dmitriy.seatflow.eventseat;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface EventSeatRepository extends JpaRepository<EventSeat, UUID> {

    @EntityGraph(attributePaths = {"seat", "seat.sector"})
    List<EventSeat> findAllByEventIdOrderBySeatSectorNameAscSeatRowNumberAscSeatSeatNumberAsc(
            UUID eventId
    );

}
