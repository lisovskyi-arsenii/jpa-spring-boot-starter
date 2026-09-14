package com.lisovskyi.jpa.autoconfigure.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@MappedSuperclass
@SuperBuilder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public abstract class CreationTimestampedEntity extends BaseEntity {

    /** Timestamp set once when the entity is first persisted. Never modified on update. */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
