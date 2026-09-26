package com.dmitriy.seatflow.seat;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SeatRepository extends JpaRepository<Seat, UUID> {

    List<Seat> findAllBySectorIdOrderByRowNumberAscSeatNumberAsc(UUID sectorId);

    @EntityGraph(attributePaths = "sector")
    List<Seat> findAllBySectorHallIdOrderBySectorNameAscRowNumberAscSeatNumberAsc(
            UUID hallId
    );

}