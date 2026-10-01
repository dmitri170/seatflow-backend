CREATE TABLE seatflow.event_sector_prices
(
    id         UUID                     NOT NULL,
    event_id   UUID                     NOT NULL,
    sector_id  UUID                     NOT NULL,
    amount     NUMERIC(12, 2)           NOT NULL,
    currency   VARCHAR(3)               NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,

    CONSTRAINT pk_event_sector_prices
        PRIMARY KEY (id),

    CONSTRAINT fk_event_sector_prices_event
        FOREIGN KEY (event_id)
            REFERENCES seatflow.events (id)
            ON DELETE CASCADE,

    CONSTRAINT fk_event_sector_prices_sector
        FOREIGN KEY (sector_id)
            REFERENCES seatflow.sectors (id),

    CONSTRAINT uq_event_sector_prices_event_sector
        UNIQUE (event_id, sector_id),

    CONSTRAINT chk_event_sector_prices_amount_non_negative
        CHECK (amount >= 0),

    CONSTRAINT chk_event_sector_prices_currency_format
        CHECK (currency ~ '^[A-Z]{3}$')
    );

CREATE INDEX idx_event_sector_prices_sector_id
    ON seatflow.event_sector_prices (sector_id);

COMMENT ON TABLE seatflow.event_sector_prices
    IS 'Стоимость мест сектора для конкретного события';

COMMENT ON COLUMN seatflow.event_sector_prices.event_id
    IS 'Идентификатор события';

COMMENT ON COLUMN seatflow.event_sector_prices.sector_id
    IS 'Идентификатор сектора зала';

COMMENT ON COLUMN seatflow.event_sector_prices.amount
    IS 'Стоимость места в секторе';

COMMENT ON COLUMN seatflow.event_sector_prices.currency
    IS 'Трёхбуквенный код валюты ISO 4217';