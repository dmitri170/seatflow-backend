CREATE TABLE seatflow.events
(
    id          UUID                     NOT NULL,
    hall_id     UUID                     NOT NULL,
    title       VARCHAR(200)             NOT NULL,
    description TEXT,
    starts_at   TIMESTAMP WITH TIME ZONE NOT NULL,
    ends_at     TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at  TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at  TIMESTAMP WITH TIME ZONE NOT NULL,

    CONSTRAINT pk_events
        PRIMARY KEY (id),

    CONSTRAINT fk_events_hall
        FOREIGN KEY (hall_id)
            REFERENCES seatflow.halls (id),

    CONSTRAINT chk_events_end_after_start
        CHECK (ends_at > starts_at)
);

CREATE INDEX idx_events_hall_id_starts_at
    ON seatflow.events (hall_id, starts_at);

COMMENT ON TABLE seatflow.events
    IS 'События, проводимые в залах';

COMMENT ON COLUMN seatflow.events.hall_id
    IS 'Идентификатор зала, в котором проводится событие';

COMMENT ON COLUMN seatflow.events.starts_at
    IS 'Дата и время начала события';

COMMENT ON COLUMN seatflow.events.ends_at
    IS 'Дата и время окончания события';