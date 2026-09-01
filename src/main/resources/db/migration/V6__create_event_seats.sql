CREATE TABLE seatflow.event_seats
(
    id         UUID                     NOT NULL,
    event_id   UUID                     NOT NULL,
    seat_id    UUID                     NOT NULL,
    status     VARCHAR(20)              NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,

    CONSTRAINT pk_event_seats
        PRIMARY KEY (id),

    CONSTRAINT fk_event_seats_event
        FOREIGN KEY (event_id)
            REFERENCES seatflow.events (id)
            ON DELETE CASCADE,

    CONSTRAINT fk_event_seats_seat
        FOREIGN KEY (seat_id)
            REFERENCES seatflow.seats (id),

    CONSTRAINT uq_event_seats_event_seat
        UNIQUE (event_id, seat_id),

    CONSTRAINT chk_event_seats_status
        CHECK (status IN (
                          'AVAILABLE',
                          'RESERVED',
                          'SOLD',
                          'BLOCKED'
            ))
);

CREATE INDEX idx_event_seats_event_status
    ON seatflow.event_seats (event_id, status);

COMMENT ON TABLE seatflow.event_seats
    IS 'Состояние физических мест для конкретного события';

COMMENT ON COLUMN seatflow.event_seats.event_id
    IS 'Идентификатор события';

COMMENT ON COLUMN seatflow.event_seats.seat_id
    IS 'Идентификатор физического места в зале';

COMMENT ON COLUMN seatflow.event_seats.status
    IS 'Состояние места на конкретном событии';