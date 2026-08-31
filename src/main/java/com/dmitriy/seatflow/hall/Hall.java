package com.dmitriy.seatflow.hall;

import com.dmitriy.seatflow.venue.Venue;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name="halls",schema = "seatflow")
public class Hall {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY,optional = false)
    @JoinColumn(name="venue_id", nullable = false)
    private Venue venue;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(nullable = false)
    private int capacity;

    @Column(nullable = false, updatable = false,name="created_at")
    private Instant createdAt;

    @Column(nullable = false, updatable = false,name="updated_at")
    private Instant updatedAt;

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

    protected Hall() {
    }

    public Hall( Venue venue, String name, int capacity) {
        this.venue = venue;
        this.name = name;
        this.capacity = capacity;
    }

    public UUID getId() {
        return id;
    }

    public Venue getVenue() {
        return venue;
    }

    public String getName() {
        return name;
    }

    public int getCapacity() {
        return capacity;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
