package com.dmitriy.seatflow.sector;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SectorRepository extends JpaRepository<Sector, UUID> {

    List<Sector> findAllByHallId(UUID hallId);

    boolean existsByHallIdAndNameIgnoreCase(UUID hallId, String name);
}