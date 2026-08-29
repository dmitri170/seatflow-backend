CREATE TABLE seatflow.venues
(
    id         UUID                     NOT NULL,
    name       VARCHAR(200)             NOT NULL,
    city       VARCHAR(100)             NOT NULL,
    address    VARCHAR(255)             NOT NULL,
    timezone   VARCHAR(50)              NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,

    CONSTRAINT pk_venues PRIMARY KEY (id)
);

COMMENT ON TABLE seatflow.venues IS 'Event venues';