package com.dmitriy.seatflow.hall;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface HallRepository extends JpaRepository<Hall, UUID> {

    public List<Hall> findAllByVenue_IdOrderByNameAsc(UUID venueId);

    public boolean existsByVenue_IdAndName(UUID venueId,String name);
}
