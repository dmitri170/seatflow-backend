CREATE TABLE seatflow.sectors
(
    id            UUID                     NOT NULL,
    hall_id       UUID                     NOT NULL,
    name          VARCHAR(100)             NOT NULL,
    row_count     INTEGER                  NOT NULL,
    seats_per_row INTEGER                  NOT NULL,
    created_at    TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at    TIMESTAMP WITH TIME ZONE NOT NULL,

    CONSTRAINT pk_sectors
        PRIMARY KEY (id),

    CONSTRAINT fk_sectors_hall
        FOREIGN KEY (hall_id)
            REFERENCES seatflow.halls (id)
            ON DELETE CASCADE,

    CONSTRAINT chk_sectors_row_count_positive
        CHECK (row_count > 0),

    CONSTRAINT chk_sectors_seats_per_row_positive
        CHECK (seats_per_row > 0)
);

CREATE UNIQUE INDEX uq_sectors_hall_name
    ON seatflow.sectors (hall_id, LOWER(name));

COMMENT ON TABLE seatflow.sectors
    IS 'Seating sectors located inside halls';

CREATE TABLE seatflow.seats
(
    id          UUID                     NOT NULL,
    sector_id   UUID                     NOT NULL,
    row_number  INTEGER                  NOT NULL,
    seat_number INTEGER                  NOT NULL,
    created_at  TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at  TIMESTAMP WITH TIME ZONE NOT NULL,

    CONSTRAINT pk_seats
        PRIMARY KEY (id),

    CONSTRAINT fk_seats_sector
        FOREIGN KEY (sector_id)
            REFERENCES seatflow.sectors (id)
            ON DELETE CASCADE,

    CONSTRAINT uq_seats_sector_row_seat
        UNIQUE (sector_id, row_number, seat_number),

    CONSTRAINT chk_seats_row_number_positive
        CHECK (row_number > 0),

    CONSTRAINT chk_seats_seat_number_positive
        CHECK (seat_number > 0)
);