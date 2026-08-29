package com.dmitriy.seatflow.venue;

import com.dmitriy.seatflow.common.error.ResourceNotFoundException;
import com.dmitriy.seatflow.venue.dto.CreateVenueRequest;
import com.dmitriy.seatflow.venue.dto.VenueResponse;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class VenueService {

    private final VenueRepository venueRepository;

    public VenueService(VenueRepository venueRepository) {
        this.venueRepository = venueRepository;
    }

    @Transactional
    public VenueResponse create(CreateVenueRequest request) {
        Venue venue = new Venue(
                request.getName(),
                request.getCity(),
                request.getAddress(),
                request.getTimezone()
        );

        Venue savedVenue = venueRepository.save(venue);

        return toResponse(savedVenue);
    }

    public VenueResponse getById(UUID id) {
        Venue venue = venueRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Venue not found: " + id)
                );

        return toResponse(venue);
    }

    public List<VenueResponse> getAll() {
        return venueRepository.findAll(Sort.by(Sort.Direction.ASC, "name"))
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private VenueResponse toResponse(Venue venue) {
        return new VenueResponse(
                venue.getId(),
                venue.getName(),
                venue.getCity(),
                venue.getAddress(),
                venue.getTimezone(),
                venue.getCreatedAt(),
                venue.getUpdatedAt()
        );
    }
}