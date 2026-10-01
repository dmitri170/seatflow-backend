package com.dmitriy.seatflow.eventpricing;

import com.dmitriy.seatflow.common.error.ResourceConflictException;
import com.dmitriy.seatflow.common.error.ResourceNotFoundException;
import com.dmitriy.seatflow.common.error.RequestValidationException;
import com.dmitriy.seatflow.common.money.Money;
import com.dmitriy.seatflow.event.Event;
import com.dmitriy.seatflow.event.EventRepository;
import com.dmitriy.seatflow.eventpricing.dto.EventSectorPriceResponse;
import com.dmitriy.seatflow.eventpricing.dto.SetEventPricesRequest;
import com.dmitriy.seatflow.eventpricing.dto.SetEventSectorPriceRequest;
import com.dmitriy.seatflow.sector.Sector;
import com.dmitriy.seatflow.sector.SectorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
public class EventSectorPriceService {

    private final EventSectorPriceRepository eventSectorPriceRepository;
    private final EventRepository eventRepository;
    private final SectorRepository sectorRepository;

    public EventSectorPriceService(
            EventSectorPriceRepository eventSectorPriceRepository,
            EventRepository eventRepository,
            SectorRepository sectorRepository
    ) {
        this.eventSectorPriceRepository = eventSectorPriceRepository;
        this.eventRepository = eventRepository;
        this.sectorRepository = sectorRepository;
    }

    @Transactional
    public List<EventSectorPriceResponse> setPrices(
            UUID eventId,
            SetEventPricesRequest request
    ) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Event not found: " + eventId
                        )
                );

        Set<UUID> sectorIds = new HashSet<>();

        for (SetEventSectorPriceRequest requestedPrice : request.prices()) {
            boolean added = sectorIds.add(requestedPrice.sectorId());

            if (!added) {
                throw new ResourceConflictException(
                        "Duplicate sector ID: "
                                + requestedPrice.sectorId()
                );
            }
        }

        List<Sector> sectors = sectorRepository.findAllById(sectorIds);

        if (sectors.size() != sectorIds.size()) {
            Set<UUID> foundSectorIds = new HashSet<>();

            for (Sector sector : sectors) {
                foundSectorIds.add(sector.getId());
            }

            UUID missingSectorId = sectorIds.stream()
                    .filter(id -> !foundSectorIds.contains(id))
                    .findFirst()
                    .orElseThrow();

            throw new ResourceNotFoundException(
                    "Sector not found: " + missingSectorId
            );
        }

        UUID eventHallId = event.getHall().getId();

        for (Sector sector : sectors) {
            if (!eventHallId.equals(sector.getHall().getId())) {
                throw new ResourceConflictException(
                        "Sector does not belong to the event hall: "
                                + sector.getId()
                );
            }
        }

        Map<UUID, Sector> sectorsById = new HashMap<>();

        for (Sector sector : sectors) {
            sectorsById.put(sector.getId(), sector);
        }

        Map<UUID, Money> requestedMoneyBySectorId = new HashMap<>();

        for (SetEventSectorPriceRequest requestedPrice : request.prices()) {
            requestedMoneyBySectorId.put(
                    requestedPrice.sectorId(),
                    createMoney(requestedPrice)
            );
        }

        List<EventSectorPrice> existingPrices =
                eventSectorPriceRepository
                        .findAllByEvent_IdOrderBySector_NameAsc(eventId);

        Map<UUID, EventSectorPrice> existingPricesBySectorId =
                new HashMap<>();

        for (EventSectorPrice existingPrice : existingPrices) {
            existingPricesBySectorId.put(
                    existingPrice.getSector().getId(),
                    existingPrice
            );
        }

        List<EventSectorPrice> pricesToSave = new ArrayList<>();

        for (SetEventSectorPriceRequest requestedPrice : request.prices()) {
            UUID sectorId = requestedPrice.sectorId();
            Money money = requestedMoneyBySectorId.get(sectorId);

            EventSectorPrice eventSectorPrice =
                    existingPricesBySectorId.get(sectorId);

            if (eventSectorPrice == null) {
                eventSectorPrice = new EventSectorPrice(
                        event,
                        sectorsById.get(sectorId),
                        money
                );
            } else {
                eventSectorPrice.changePrice(money);
            }

            pricesToSave.add(eventSectorPrice);
        }

        eventSectorPriceRepository.saveAll(pricesToSave);

        return eventSectorPriceRepository
                .findAllByEvent_IdOrderBySector_NameAsc(eventId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<EventSectorPriceResponse> getPrices(UUID eventId) {
        if (!eventRepository.existsById(eventId)) {
            throw new ResourceNotFoundException("Event not found: " + eventId);
        }

        return eventSectorPriceRepository
                .findAllByEvent_IdOrderBySector_NameAsc(eventId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private Money createMoney(SetEventSectorPriceRequest requestedPrice) {
        try {
            return new Money(
                    requestedPrice.amount(),
                    requestedPrice.currency()
            );
        } catch (IllegalArgumentException exception) {
            throw new RequestValidationException(exception.getMessage());
        }
    }

    private EventSectorPriceResponse toResponse(
            EventSectorPrice eventSectorPrice
    ) {
        return new EventSectorPriceResponse(
                eventSectorPrice.getId(),
                eventSectorPrice.getEvent().getId(),
                eventSectorPrice.getSector().getId(),
                eventSectorPrice.getSector().getName(),
                eventSectorPrice.getPrice().getAmount(),
                eventSectorPrice.getPrice().getCurrency(),
                eventSectorPrice.getCreatedAt(),
                eventSectorPrice.getUpdatedAt()
        );
    }
}
