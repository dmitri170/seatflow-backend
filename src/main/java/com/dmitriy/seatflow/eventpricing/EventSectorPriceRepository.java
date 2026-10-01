package com.dmitriy.seatflow.eventpricing;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EventSectorPriceRepository extends JpaRepository<EventSectorPrice, UUID> {

    @EntityGraph(attributePaths = "sector")
    List<EventSectorPrice> findAllByEvent_IdOrderBySector_NameAsc(
            UUID eventId
    );

    @EntityGraph(attributePaths = "sector")
    Optional<EventSectorPrice> findByEvent_IdAndSector_Id(
            UUID eventId,
            UUID sectorId
    );
}
