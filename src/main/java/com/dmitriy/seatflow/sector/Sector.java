package com.dmitriy.seatflow.sector;

import com.dmitriy.seatflow.hall.Hall;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "sectors", schema = "seatflow")
public class Sector {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "hall_id", nullable = false)
    private Hall hall;

    @Column(name = "name", nullable = false, length = 100)
    private String name;


    @Column(name = "row_count", nullable = false)
    private int rowCount;

    @Column(name = "seats_per_row", nullable = false)
    private int seatsPerRow;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    void prePersist() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = Instant.now();
    }

    protected Sector() {
    }

    public Sector(Hall hall, String name, int rowCount, int seatsPerRow) {
        this.hall = hall;
        this.name = name;
        this.rowCount = rowCount;
        this.seatsPerRow = seatsPerRow;
    }

    public UUID getId() {
        return id;
    }

    public Hall getHall() {
        return hall;
    }

    public String getName() {
        return name;
    }

    public int getRowCount() {
        return rowCount;
    }

    public int getSeatsPerRow() {
        return seatsPerRow;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
