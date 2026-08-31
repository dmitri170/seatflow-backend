CREATE TABLE seatflow.halls
(
    id         UUID                     NOT NULL,
    venue_id   UUID                     NOT NULL,
    name       VARCHAR(150)             NOT NULL,
    capacity   INTEGER                  NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,

    CONSTRAINT pk_halls PRIMARY KEY (id),

    CONSTRAINT fk_halls_venue
        FOREIGN KEY (venue_id)
            REFERENCES seatflow.venues (id)
            ON DELETE RESTRICT,

    CONSTRAINT uq_halls_venue_name
        UNIQUE (venue_id, name),

    CONSTRAINT chk_halls_capacity_positive
        CHECK (capacity > 0)
);

COMMENT ON TABLE seatflow.halls IS 'Halls located inside event venues';