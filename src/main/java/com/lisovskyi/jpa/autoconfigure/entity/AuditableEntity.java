package com.lisovskyi.jpa.autoconfigure.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

/**
 * Extends {@link BaseEntity} with full Spring Data JPA auditing:
 * creation/update timestamps and the username of who performed each operation.
 *
 * <p>All four audit fields are populated automatically by
 * {@link AuditingEntityListener}, which requires:
 * <ol>
 *   <li>JPA auditing to be enabled — either by setting
 *       {@code app.jpa.auditing-enabled=true} (default) in the starter
 *       configuration, or via {@code @EnableJpaAuditing} in the consumer app.</li>
 *   <li>An {@code AuditorAware<String>} bean to be present — provided
 *       automatically by the starter when Spring Security is on the classpath
 *       (reads the username from the {@code SecurityContext}). Override by
 *       declaring a custom {@code AuditorAware} bean in the consumer app.</li>
 * </ol>
 *
 * <p>Unlike {@link TimestampedEntity} (which uses Hibernate's own
 * {@code @CreationTimestamp} / {@code @UpdateTimestamp}), this class
 * delegates entirely to Spring Data's auditing infrastructure.
 *
 * <p>Use this class when you need to track <em>who</em> created and last
 * modified a record in addition to <em>when</em>. For timestamp-only tracking
 * without Spring Security, prefer {@link TimestampedEntity}.
 *
 * @see TimestampedEntity
 * @see com.lisovskyi.jpa.autoconfigure.audit.SecurityAuditorAware
 * @see AuditingEntityListener
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class AuditableEntity extends BaseEntity {

    /** Timestamp set once when the entity is first persisted. Never modified on update. */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    /** Timestamp refreshed automatically each time the entity is updated. */
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    /** Username (or principal name) of the user who created this record. Never modified on update. */
    @CreatedBy
    @Column(name = "created_by", updatable = false)
    private String createdBy;

    /** Username (or principal name) of the user who last modified this record. */
    @LastModifiedBy
    @Column(name = "updated_by")
    private String updatedBy;
}
