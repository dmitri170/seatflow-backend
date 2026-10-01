package com.dmitriy.seatflow.eventpricing;

import com.dmitriy.seatflow.common.error.ResourceConflictException;
import com.dmitriy.seatflow.common.error.ResourceNotFoundException;
import com.dmitriy.seatflow.common.money.Money;
import com.dmitriy.seatflow.event.Event;
import com.dmitriy.seatflow.event.EventRepository;
import com.dmitriy.seatflow.eventpricing.dto.EventSectorPriceResponse;
import com.dmitriy.seatflow.eventpricing.dto.SetEventPricesRequest;
import com.dmitriy.seatflow.eventpricing.dto.SetEventSectorPriceRequest;
import com.dmitriy.seatflow.hall.Hall;
import com.dmitriy.seatflow.sector.Sector;
import com.dmitriy.seatflow.sector.SectorRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EventSectorPriceServiceTest {

    @Mock
    private EventSectorPriceRepository eventSectorPriceRepository;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private SectorRepository sectorRepository;

    @InjectMocks
    private EventSectorPriceService eventSectorPriceService;

    @Test
    @SuppressWarnings("unchecked")
    void shouldCreateNewSectorPrice() {
        UUID eventId = UUID.randomUUID();
        UUID hallId = UUID.randomUUID();
        UUID sectorId = UUID.randomUUID();

        Event event = org.mockito.Mockito.mock(Event.class);
        Hall hall = org.mockito.Mockito.mock(Hall.class);
        Sector sector = org.mockito.Mockito.mock(Sector.class);

        stubValidEventAndSector(
                eventId,
                hallId,
                sectorId,
                event,
                hall,
                sector
        );

        SetEventPricesRequest request = request(
                sectorId,
                "1500.00",
                "RUB"
        );

        EventSectorPrice returnedPrice = new EventSectorPrice(
                event,
                sector,
                new Money(new BigDecimal("1500.00"), "RUB")
        );

        when(eventSectorPriceRepository
                .findAllByEvent_IdOrderBySector_NameAsc(eventId))
                .thenReturn(
                        List.of(),
                        List.of(returnedPrice)
                );

        List<EventSectorPriceResponse> result =
                eventSectorPriceService.setPrices(eventId, request);

        assertEquals(1, result.size());
        assertEquals(eventId, result.getFirst().eventId());
        assertEquals(sectorId, result.getFirst().sectorId());
        assertEquals("Parterre", result.getFirst().sectorName());
        assertEquals(
                new BigDecimal("1500.00"),
                result.getFirst().amount()
        );
        assertEquals("RUB", result.getFirst().currency());

        ArgumentCaptor<Iterable<EventSectorPrice>> captor =
                ArgumentCaptor.forClass(Iterable.class);

        verify(eventSectorPriceRepository).saveAll(captor.capture());

        EventSectorPrice savedPrice =
                captor.getValue().iterator().next();

        assertSame(event, savedPrice.getEvent());
        assertSame(sector, savedPrice.getSector());
        assertEquals(
                new BigDecimal("1500.00"),
                savedPrice.getPrice().getAmount()
        );
        assertEquals("RUB", savedPrice.getPrice().getCurrency());
    }

    @Test
    void shouldUpdateExistingSectorPrice() {
        UUID eventId = UUID.randomUUID();
        UUID hallId = UUID.randomUUID();
        UUID sectorId = UUID.randomUUID();

        Event event = org.mockito.Mockito.mock(Event.class);
        Hall hall = org.mockito.Mockito.mock(Hall.class);
        Sector sector = org.mockito.Mockito.mock(Sector.class);

        stubValidEventAndSector(
                eventId,
                hallId,
                sectorId,
                event,
                hall,
                sector
        );

        EventSectorPrice existingPrice = new EventSectorPrice(
                event,
                sector,
                new Money(new BigDecimal("1000.00"), "RUB")
        );

        when(eventSectorPriceRepository
                .findAllByEvent_IdOrderBySector_NameAsc(eventId))
                .thenReturn(
                        List.of(existingPrice),
                        List.of(existingPrice)
                );

        SetEventPricesRequest request = request(
                sectorId,
                "1800.00",
                "RUB"
        );

        List<EventSectorPriceResponse> result =
                eventSectorPriceService.setPrices(eventId, request);

        assertEquals(1, result.size());
        assertEquals(
                new BigDecimal("1800.00"),
                existingPrice.getPrice().getAmount()
        );
        assertEquals(
                new BigDecimal("1800.00"),
                result.getFirst().amount()
        );

        verify(eventSectorPriceRepository)
                .saveAll(List.of(existingPrice));
    }

    @Test
    void shouldThrowWhenSettingPricesForMissingEvent() {
        UUID eventId = UUID.randomUUID();

        when(eventRepository.findById(eventId))
                .thenReturn(Optional.empty());

        SetEventPricesRequest request = request(
                UUID.randomUUID(),
                "1500.00",
                "RUB"
        );

        assertThrows(
                ResourceNotFoundException.class,
                () -> eventSectorPriceService.setPrices(
                        eventId,
                        request
                )
        );

        verifyNoInteractions(
                sectorRepository,
                eventSectorPriceRepository
        );
    }

    @Test
    void shouldThrowWhenRequestContainsDuplicateSectorIds() {
        UUID eventId = UUID.randomUUID();
        UUID sectorId = UUID.randomUUID();

        Event event = org.mockito.Mockito.mock(Event.class);

        when(eventRepository.findById(eventId))
                .thenReturn(Optional.of(event));

        SetEventPricesRequest request = new SetEventPricesRequest(
                List.of(
                        new SetEventSectorPriceRequest(
                                sectorId,
                                new BigDecimal("1000.00"),
                                "RUB"
                        ),
                        new SetEventSectorPriceRequest(
                                sectorId,
                                new BigDecimal("1500.00"),
                                "RUB"
                        )
                )
        );

        assertThrows(
                ResourceConflictException.class,
                () -> eventSectorPriceService.setPrices(
                        eventId,
                        request
                )
        );

        verifyNoInteractions(
                sectorRepository,
                eventSectorPriceRepository
        );
    }

    @Test
    void shouldThrowWhenSectorDoesNotExist() {
        UUID eventId = UUID.randomUUID();
        UUID sectorId = UUID.randomUUID();

        Event event = org.mockito.Mockito.mock(Event.class);

        when(eventRepository.findById(eventId))
                .thenReturn(Optional.of(event));

        when(sectorRepository.findAllById(Set.of(sectorId)))
                .thenReturn(List.of());

        SetEventPricesRequest request = request(
                sectorId,
                "1500.00",
                "RUB"
        );

        assertThrows(
                ResourceNotFoundException.class,
                () -> eventSectorPriceService.setPrices(
                        eventId,
                        request
                )
        );

        verifyNoInteractions(eventSectorPriceRepository);
    }

    @Test
    void shouldThrowWhenSectorBelongsToAnotherHall() {
        UUID eventId = UUID.randomUUID();
        UUID eventHallId = UUID.randomUUID();
        UUID anotherHallId = UUID.randomUUID();
        UUID sectorId = UUID.randomUUID();

        Event event = org.mockito.Mockito.mock(Event.class);
        Hall eventHall = org.mockito.Mockito.mock(Hall.class);
        Hall anotherHall = org.mockito.Mockito.mock(Hall.class);
        Sector sector = org.mockito.Mockito.mock(Sector.class);

        when(eventRepository.findById(eventId))
                .thenReturn(Optional.of(event));

        when(event.getHall()).thenReturn(eventHall);
        when(eventHall.getId()).thenReturn(eventHallId);

        when(sectorRepository.findAllById(Set.of(sectorId)))
                .thenReturn(List.of(sector));

        when(sector.getId()).thenReturn(sectorId);
        when(sector.getHall()).thenReturn(anotherHall);
        when(anotherHall.getId()).thenReturn(anotherHallId);

        SetEventPricesRequest request = request(
                sectorId,
                "1500.00",
                "RUB"
        );

        assertThrows(
                ResourceConflictException.class,
                () -> eventSectorPriceService.setPrices(
                        eventId,
                        request
                )
        );

        verifyNoInteractions(eventSectorPriceRepository);
    }

    @Test
    void shouldReturnEventPrices() {
        UUID eventId = UUID.randomUUID();
        UUID sectorId = UUID.randomUUID();

        Event event = org.mockito.Mockito.mock(Event.class);
        Sector sector = org.mockito.Mockito.mock(Sector.class);

        when(eventRepository.existsById(eventId))
                .thenReturn(true);

        when(event.getId()).thenReturn(eventId);
        when(sector.getId()).thenReturn(sectorId);
        when(sector.getName()).thenReturn("Balcony");

        EventSectorPrice price = new EventSectorPrice(
                event,
                sector,
                new Money(new BigDecimal("2500.00"), "RUB")
        );

        when(eventSectorPriceRepository
                .findAllByEvent_IdOrderBySector_NameAsc(eventId))
                .thenReturn(List.of(price));

        List<EventSectorPriceResponse> result =
                eventSectorPriceService.getPrices(eventId);

        assertEquals(1, result.size());
        assertEquals(eventId, result.getFirst().eventId());
        assertEquals(sectorId, result.getFirst().sectorId());
        assertEquals("Balcony", result.getFirst().sectorName());
        assertEquals(
                new BigDecimal("2500.00"),
                result.getFirst().amount()
        );
        assertEquals("RUB", result.getFirst().currency());
    }

    @Test
    void shouldThrowWhenGettingPricesForMissingEvent() {
        UUID eventId = UUID.randomUUID();

        when(eventRepository.existsById(eventId))
                .thenReturn(false);

        assertThrows(
                ResourceNotFoundException.class,
                () -> eventSectorPriceService.getPrices(eventId)
        );

        verifyNoInteractions(eventSectorPriceRepository);
    }

    private void stubValidEventAndSector(
            UUID eventId,
            UUID hallId,
            UUID sectorId,
            Event event,
            Hall hall,
            Sector sector
    ) {
        when(eventRepository.findById(eventId))
                .thenReturn(Optional.of(event));

        when(event.getId()).thenReturn(eventId);
        when(event.getHall()).thenReturn(hall);
        when(hall.getId()).thenReturn(hallId);

        when(sectorRepository.findAllById(Set.of(sectorId)))
                .thenReturn(List.of(sector));

        when(sector.getId()).thenReturn(sectorId);
        when(sector.getName()).thenReturn("Parterre");
        when(sector.getHall()).thenReturn(hall);
    }

    private SetEventPricesRequest request(
            UUID sectorId,
            String amount,
            String currency
    ) {
        return new SetEventPricesRequest(
                List.of(
                        new SetEventSectorPriceRequest(
                                sectorId,
                                new BigDecimal(amount),
                                currency
                        )
                )
        );
    }
}