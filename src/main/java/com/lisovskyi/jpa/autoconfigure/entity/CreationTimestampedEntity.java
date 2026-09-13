package com.lisovskyi.jpa.autoconfigure.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;

import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@MappedSuperclass
public abstract class CreationTimestampedEntity extends BaseEntity {

    protected CreationTimestampedEntity() {}

    protected CreationTimestampedEntity(Instant createdAt) { this.createdAt = createdAt; }

    /** Timestamp set once when the entity is first persisted. Never modified on update. */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public Instant getCreatedAt() { return createdAt; }

    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
