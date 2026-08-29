package com.dmitriy.seatflow;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import com.dmitriy.seatflow.venue.Venue;
import com.dmitriy.seatflow.venue.VenueRepository;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest
class SeatflowBackendApplicationTests {

	@Autowired
	private VenueRepository venueRepository;

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

		Integer appliedMigrations = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM public.flyway_schema_history
                WHERE version = '1'
                  AND success = TRUE
                """, Integer.class);

		assertThat(schemaExists).isTrue();
		assertThat(appliedMigrations).isEqualTo(1);
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
}
