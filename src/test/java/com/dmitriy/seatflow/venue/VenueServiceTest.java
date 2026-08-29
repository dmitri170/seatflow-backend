package com.dmitriy.seatflow.venue;

import com.dmitriy.seatflow.common.error.ResourceNotFoundException;
import com.dmitriy.seatflow.venue.dto.CreateVenueRequest;
import com.dmitriy.seatflow.venue.dto.VenueResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VenueServiceTest {

    @Mock
    private VenueRepository venueRepository;

    @InjectMocks
    private VenueService venueService;

    @Test
    void shouldCreateVenue() {
        CreateVenueRequest request = new CreateVenueRequest();
        request.setName("Luzhniki Stadium");
        request.setCity("Moscow");
        request.setAddress("Luzhnetskaya Naberezhnaya, 24");
        request.setTimezone("Europe/Moscow");

        when(venueRepository.save(any(Venue.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        VenueResponse response = venueService.create(request);

        assertThat(response.getName()).isEqualTo("Luzhniki Stadium");
        assertThat(response.getCity()).isEqualTo("Moscow");
        assertThat(response.getAddress())
                .isEqualTo("Luzhnetskaya Naberezhnaya, 24");
        assertThat(response.getTimezone()).isEqualTo("Europe/Moscow");

        ArgumentCaptor<Venue> venueCaptor =
                ArgumentCaptor.forClass(Venue.class);

        verify(venueRepository).save(venueCaptor.capture());

        Venue capturedVenue = venueCaptor.getValue();

        assertThat(capturedVenue.getName()).isEqualTo("Luzhniki Stadium");
        assertThat(capturedVenue.getCity()).isEqualTo("Moscow");
        assertThat(capturedVenue.getAddress())
                .isEqualTo("Luzhnetskaya Naberezhnaya, 24");
        assertThat(capturedVenue.getTimezone()).isEqualTo("Europe/Moscow");
    }

    @Test
    void shouldReturnVenueById() {
        UUID venueId = UUID.randomUUID();

        Venue venue = new Venue(
                "Bolshoi Theatre",
                "Moscow",
                "Theatre Square, 1",
                "Europe/Moscow"
        );

        when(venueRepository.findById(venueId))
                .thenReturn(Optional.of(venue));

        VenueResponse response = venueService.getById(venueId);

        assertThat(response.getName()).isEqualTo("Bolshoi Theatre");
        assertThat(response.getCity()).isEqualTo("Moscow");
        assertThat(response.getAddress()).isEqualTo("Theatre Square, 1");
        assertThat(response.getTimezone()).isEqualTo("Europe/Moscow");

        verify(venueRepository).findById(venueId);
    }

    @Test
    void shouldThrowWhenVenueDoesNotExist() {
        UUID venueId = UUID.randomUUID();

        when(venueRepository.findById(venueId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> venueService.getById(venueId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Venue not found: " + venueId);

        verify(venueRepository).findById(venueId);
        verify(venueRepository, never()).save(any(Venue.class));
    }

    @Test
    void shouldReturnAllVenuesSortedByName() {
        Venue firstVenue = new Venue(
                "Bolshoi Theatre",
                "Moscow",
                "Theatre Square, 1",
                "Europe/Moscow"
        );

        Venue secondVenue = new Venue(
                "Luzhniki Stadium",
                "Moscow",
                "Luzhnetskaya Naberezhnaya, 24",
                "Europe/Moscow"
        );

        Sort sort = Sort.by(Sort.Direction.ASC, "name");

        when(venueRepository.findAll(sort))
                .thenReturn(List.of(firstVenue, secondVenue));

        List<VenueResponse> responses = venueService.getAll();

        assertThat(responses)
                .extracting(VenueResponse::getName)
                .containsExactly("Bolshoi Theatre", "Luzhniki Stadium");

        verify(venueRepository).findAll(sort);
    }
}