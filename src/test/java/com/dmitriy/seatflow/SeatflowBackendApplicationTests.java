package com.dmitriy.seatflow;

import com.dmitriy.seatflow.event.EventService;
import com.dmitriy.seatflow.event.dto.CreateEventRequest;
import com.dmitriy.seatflow.event.dto.EventResponse;
import com.dmitriy.seatflow.eventseat.EventSeatRepository;
import com.dmitriy.seatflow.hall.Hall;
import com.dmitriy.seatflow.hall.HallRepository;
import com.dmitriy.seatflow.venue.Venue;
import com.dmitriy.seatflow.venue.VenueRepository;
import com.dmitriy.seatflow.eventpricing.EventSectorPriceRepository;
import com.dmitriy.seatflow.eventpricing.EventSectorPriceService;
import com.dmitriy.seatflow.eventpricing.dto.EventSectorPriceResponse;
import com.dmitriy.seatflow.eventpricing.dto.SetEventPricesRequest;
import com.dmitriy.seatflow.eventpricing.dto.SetEventSectorPriceRequest;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest
class SeatflowBackendApplicationTests {

	@Autowired
	private VenueRepository venueRepository;

	@Autowired
	private HallRepository hallRepository;

	@Autowired
	private EventService eventService;

	@Autowired
	private EventSeatRepository eventSeatRepository;

	@Autowired
	private EventSectorPriceService eventSectorPriceService;

	@Autowired
	private EventSectorPriceRepository eventSectorPriceRepository;

	@Container
	@ServiceConnection
	static final PostgreSQLContainer POSTGRES =
			new PostgreSQLContainer("postgres:17-alpine")
					.withDatabaseName("seatflow")
					.withUsername("seatflow")
					.withPassword("seatflow_test_password");

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Test
	void shouldApplyFlywayMigration() {
		Boolean schemaExists = jdbcTemplate.queryForObject("""
                SELECT EXISTS (
                    SELECT 1
                    FROM information_schema.schemata
                    WHERE schema_name = 'seatflow'
                )
                """, Boolean.class);

		List<String> appliedVersions = jdbcTemplate.queryForList("""
				SELECT version
				FROM public.flyway_schema_history
				WHERE success = TRUE
				ORDER BY installed_rank
				""", String.class);

		assertThat(schemaExists).isTrue();
		// Проверяем не только количество, но и порядок применённых миграций.
		assertThat(appliedVersions).containsExactly("1", "2", "3", "4", "5", "6","7");
	}

	@Test
	void shouldPersistVenue() {
		Venue venue = new Venue(
				"Luzhniki Stadium",
				"Moscow",
				"Luzhnetskaya Naberezhnaya, 24",
				"Europe/Moscow"
		);

		Venue savedVenue = venueRepository.saveAndFlush(venue);

		Venue foundVenue = venueRepository.findById(savedVenue.getId())
				.orElseThrow();

		assertThat(foundVenue.getId()).isNotNull();
		assertThat(foundVenue.getName()).isEqualTo("Luzhniki Stadium");
		assertThat(foundVenue.getCity()).isEqualTo("Moscow");
		assertThat(foundVenue.getAddress())
				.isEqualTo("Luzhnetskaya Naberezhnaya, 24");
		assertThat(foundVenue.getTimezone()).isEqualTo("Europe/Moscow");
		assertThat(foundVenue.getCreatedAt()).isNotNull();
		assertThat(foundVenue.getUpdatedAt()).isNotNull();
	}

	@Test
	void shouldPersistHallForVenue() {
		Venue venue = new Venue(
				"Crocus City Hall",
				"Krasnogorsk",
				"Mezhdunarodnaya Street, 20",
				"Europe/Moscow"
		);
		Venue savedVenue = venueRepository.saveAndFlush(venue);

		Hall hall = new Hall(savedVenue, "Concert Hall", 6200);
		Hall savedHall = hallRepository.saveAndFlush(hall);

		Hall foundHall = hallRepository.findById(savedHall.getId())
				.orElseThrow();

		assertThat(foundHall.getId()).isNotNull();
		assertThat(foundHall.getVenue().getId()).isEqualTo(savedVenue.getId());
		assertThat(foundHall.getName()).isEqualTo("Concert Hall");
		assertThat(foundHall.getCapacity()).isEqualTo(6200);
		assertThat(foundHall.getCreatedAt()).isNotNull();
		assertThat(foundHall.getUpdatedAt()).isNotNull();

		// Дополнительно проверяем repository-метод, который использует HallService.
		List<Hall> venueHalls = hallRepository
				.findAllByVenue_IdOrderByNameAsc(savedVenue.getId());

		assertThat(venueHalls)
				.extracting(Hall::getId)
				.contains(savedHall.getId());
	}

	@Test
	@Transactional
	void shouldCreateAvailableEventSeatsForEveryHallSeat() {
		Venue venue = venueRepository.saveAndFlush(new Venue(
				"Event Seat Test Venue",
				"Moscow",
				"Test Address, 1",
				"Europe/Moscow"
		));

		Hall hall = hallRepository.saveAndFlush(
				new Hall(venue, "Event Seat Test Hall", 2)
		);

		UUID sectorId = UUID.randomUUID();
		UUID firstSeatId = UUID.randomUUID();
		UUID secondSeatId = UUID.randomUUID();
		Timestamp now = Timestamp.from(Instant.now());

		jdbcTemplate.update("""
				INSERT INTO seatflow.sectors (
				    id, hall_id, name, row_count, seats_per_row,
				    created_at, updated_at
				)
				VALUES (?, ?, ?, ?, ?, ?, ?)
				""",
				sectorId,
				hall.getId(),
				"Parterre",
				1,
				2,
				now,
				now
		);

		jdbcTemplate.update("""
        INSERT INTO seatflow.seats (
            id, sector_id, row_number, seat_number,
            created_at, updated_at
        )
        VALUES
            (?, ?, ?, ?, ?, ?),
            (?, ?, ?, ?, ?, ?)
        """,
				firstSeatId, sectorId, 1, 1, now, now,
				secondSeatId, sectorId, 1, 2, now, now
		);

		CreateEventRequest request = new CreateEventRequest(
				"Event Seat Integration Test",
				"Checks automatic event-seat generation",
				Instant.parse("2027-10-10T16:00:00Z"),
				Instant.parse("2027-10-10T19:00:00Z")
		);

		EventResponse createdEvent = eventService.createEvent(
				hall.getId(),
				request
		);
		// saveAll() может отложить INSERT до конца транзакции.
		// Принудительно отправляем данные в PostgreSQL перед JDBC-проверкой.
		eventSeatRepository.flush();

		List<UUID> generatedSeatIds = jdbcTemplate.query("""
				SELECT seat_id
				FROM seatflow.event_seats
				WHERE event_id = ?
				""",
				(rs, rowNum) -> rs.getObject("seat_id", UUID.class),
				createdEvent.getId()
		);

		List<String> statuses = jdbcTemplate.queryForList("""
				SELECT status
				FROM seatflow.event_seats
				WHERE event_id = ?
				""",
				String.class,
				createdEvent.getId()
		);

		assertThat(generatedSeatIds)
				.containsExactlyInAnyOrder(firstSeatId, secondSeatId);
		assertThat(statuses)
				.hasSize(2)
				.containsOnly("AVAILABLE");
	}

	@Test
	@Transactional
	void shouldPersistAndUpdateEventSectorPrice() {
		Venue venue = venueRepository.saveAndFlush(new Venue(
				"Pricing Test Venue",
				"Moscow",
				"Pricing Test Address, 1",
				"Europe/Moscow"
		));

		Hall hall = hallRepository.saveAndFlush(
				new Hall(venue, "Pricing Test Hall", 100)
		);

		UUID sectorId = UUID.randomUUID();
		Timestamp now = Timestamp.from(Instant.now());

		jdbcTemplate.update("""
            INSERT INTO seatflow.sectors (
                id, hall_id, name, row_count, seats_per_row,
                created_at, updated_at
            )
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """,
				sectorId,
				hall.getId(),
				"Parterre",
				10,
				10,
				now,
				now
		);

		CreateEventRequest eventRequest = new CreateEventRequest(
				"Pricing Integration Test",
				"Checks event sector pricing",
				Instant.parse("2027-11-10T16:00:00Z"),
				Instant.parse("2027-11-10T19:00:00Z")
		);

		EventResponse createdEvent = eventService.createEvent(
				hall.getId(),
				eventRequest
		);

		SetEventPricesRequest initialPrices =
				new SetEventPricesRequest(
						List.of(
								new SetEventSectorPriceRequest(
										sectorId,
										new BigDecimal("1500.00"),
										"RUB"
								)
						)
				);

		List<EventSectorPriceResponse> createdPrices =
				eventSectorPriceService.setPrices(
						createdEvent.getId(),
						initialPrices
				);

		eventSectorPriceRepository.flush();

		assertThat(createdPrices).hasSize(1);
		assertThat(createdPrices.getFirst().eventId())
				.isEqualTo(createdEvent.getId());
		assertThat(createdPrices.getFirst().sectorId())
				.isEqualTo(sectorId);
		assertThat(createdPrices.getFirst().amount())
				.isEqualByComparingTo("1500.00");
		assertThat(createdPrices.getFirst().currency())
				.isEqualTo("RUB");

		SetEventPricesRequest updatedPrices =
				new SetEventPricesRequest(
						List.of(
								new SetEventSectorPriceRequest(
										sectorId,
										new BigDecimal("1800.00"),
										"RUB"
								)
						)
				);

		eventSectorPriceService.setPrices(
				createdEvent.getId(),
				updatedPrices
		);

		eventSectorPriceRepository.flush();

		Integer priceCount = jdbcTemplate.queryForObject("""
            SELECT COUNT(*)
            FROM seatflow.event_sector_prices
            WHERE event_id = ?
              AND sector_id = ?
            """,
				Integer.class,
				createdEvent.getId(),
				sectorId
		);

		BigDecimal storedAmount = jdbcTemplate.queryForObject("""
            SELECT amount
            FROM seatflow.event_sector_prices
            WHERE event_id = ?
              AND sector_id = ?
            """,
				BigDecimal.class,
				createdEvent.getId(),
				sectorId
		);

		String storedCurrency = jdbcTemplate.queryForObject("""
            SELECT currency
            FROM seatflow.event_sector_prices
            WHERE event_id = ?
              AND sector_id = ?
            """,
				String.class,
				createdEvent.getId(),
				sectorId
		);

		assertThat(priceCount).isEqualTo(1);
		assertThat(storedAmount).isEqualByComparingTo("1800.00");
		assertThat(storedCurrency).isEqualTo("RUB");
	}
}
