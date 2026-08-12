package com.lisovskyi.jpa.autoconfigure.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

/**
 * Extends {@link BaseEntity} with an automatic last-update timestamp only.
 *
 * <p>The {@code updatedAt} field is refreshed by Hibernate on every
 * {@code UPDATE} statement. No creation timestamp is tracked.
 *
 * <p>Use this class when you only need to know <em>when</em> a record was
 * last modified. For both creation and update timestamps, use
 * {@link TimestampedEntity}. For full user-level auditing,
 * use {@link AuditableEntity}.
 *
 * @see TimestampedEntity
 * @see CreationTimestampedEntity
 * @see AuditableEntity
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@MappedSuperclass
public abstract class UpdateTimestampedEntity extends BaseEntity {

    /** Timestamp refreshed automatically each time the entity is updated. */
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
