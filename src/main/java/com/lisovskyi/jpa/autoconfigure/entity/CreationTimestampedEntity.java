package com.lisovskyi.jpa.autoconfigure.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

/**
 * Extends {@link BaseEntity} with an automatic creation timestamp only.
 *
 * <p>The {@code createdAt} field is set once by Hibernate on insert and is
 * never modified afterwards. No update timestamp is tracked.
 *
 * <p>Use this class when you need to record <em>when</em> a record was created
 * but have no requirement to track last-update time. For both timestamps,
 * use {@link TimestampedEntity}. For full user-level auditing,
 * use {@link AuditableEntity}.
 *
 * @see TimestampedEntity
 * @see UpdateTimestampedEntity
 * @see AuditableEntity
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@MappedSuperclass
public abstract class CreationTimestampedEntity extends BaseEntity {

    /** Timestamp set once when the entity is first persisted. Never modified on update. */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
