package com.dmitriy.seatflow;

import com.dmitriy.seatflow.event.EventService;
import com.dmitriy.seatflow.event.dto.CreateEventRequest;
import com.dmitriy.seatflow.event.dto.EventResponse;
import com.dmitriy.seatflow.eventseat.EventSeatRepository;
import com.dmitriy.seatflow.hall.Hall;
import com.dmitriy.seatflow.hall.HallRepository;
import com.dmitriy.seatflow.venue.Venue;
import com.dmitriy.seatflow.venue.VenueRepository;
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
		assertThat(appliedVersions).containsExactly("1", "2", "3", "4", "5", "6");
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
}
