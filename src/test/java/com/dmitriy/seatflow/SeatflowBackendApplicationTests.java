package com.dmitriy.seatflow;

import com.dmitriy.seatflow.hall.Hall;
import com.dmitriy.seatflow.hall.HallRepository;
import com.dmitriy.seatflow.venue.Venue;
import com.dmitriy.seatflow.venue.VenueRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest
class SeatflowBackendApplicationTests {

	@Autowired
	private VenueRepository venueRepository;

	@Autowired
	private HallRepository hallRepository;

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
		assertThat(appliedVersions).containsExactly("1", "2", "3");
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
}
