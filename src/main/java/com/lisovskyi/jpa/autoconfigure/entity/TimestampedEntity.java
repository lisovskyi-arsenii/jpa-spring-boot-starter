package com.lisovskyi.jpa.autoconfigure.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

/**
 * Extends {@link BaseEntity} with automatic creation and last-update timestamps.
 *
 * <p>Both timestamps are populated and maintained by Hibernate directly,
 * without requiring Spring Data JPA auditing to be enabled. This makes the
 * class suitable for projects that do not use {@code @EnableJpaAuditing}.
 *
 * <ul>
 *   <li>{@code createdAt} — set once on insert via {@link CreationTimestamp};
 *       never updated afterwards ({@code updatable = false}).</li>
 *   <li>{@code updatedAt} — refreshed on every update via {@link UpdateTimestamp}.</li>
 * </ul>
 *
 * <p>Use this class when you need both timestamps but <em>do not</em> need to
 * track who created or modified the record. For auditing with user information,
 * use {@link AuditableEntity} instead.
 *
 * @see CreationTimestampedEntity
 * @see UpdateTimestampedEntity
 * @see AuditableEntity
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@MappedSuperclass
public abstract class TimestampedEntity extends BaseEntity {

    /** Timestamp set once when the entity is first persisted. Never modified on update. */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    /** Timestamp refreshed automatically each time the entity is updated. */
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
