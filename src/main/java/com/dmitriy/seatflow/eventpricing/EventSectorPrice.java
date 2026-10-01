package com.dmitriy.seatflow.eventpricing;

import com.dmitriy.seatflow.common.money.Money;
import com.dmitriy.seatflow.event.Event;
import com.dmitriy.seatflow.sector.Sector;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "event_sector_prices", schema = "seatflow")
public class EventSectorPrice {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sector_id", nullable = false)
    private Sector sector;

    @Embedded
    private Money price;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected EventSectorPrice() {
    }

    public EventSectorPrice(Event event, Sector sector, Money price) {
        requireNotNull(event, "Event");
        requireNotNull(sector, "Sector");
        requireNotNull(price, "Price");

        this.event = event;
        this.sector = sector;
        this.price = price;
    }

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public Event getEvent() {
        return event;
    }

    public Sector getSector() {
        return sector;
    }

    public Money getPrice() {
        return price;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void changePrice(Money price) {
        requireNotNull(price, "Price");
        this.price = price;
    }

    private static void requireNotNull(Object value, String fieldName) {
        if (value == null) {
            throw new IllegalArgumentException(
                    fieldName + " must not be null"
            );
        }
    }
}
