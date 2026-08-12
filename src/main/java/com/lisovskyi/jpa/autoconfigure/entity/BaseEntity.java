package com.lisovskyi.jpa.autoconfigure.entity;

import com.lisovskyi.jpa.autoconfigure.generator.EntitySequence;
import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.util.Objects;

/**
 * Root superclass for all JPA entities in this project.
 *
 * <p>Provides a {@code Long} primary key generated via a database sequence
 * whose name is automatically derived from the concrete entity class name
 * (see {@link EntitySequence} for naming details).
 *
 * <p>Subclasses should not declare their own {@code @Id} field. Instead,
 * they inherit the {@code id} directly:
 * <pre>{@code
 * @Entity
 * @Table(name = "users")
 * public class UserEntity extends BaseEntity {
 *     // id is inherited — backed by sequence "user_entity_seq_gen"
 * }
 * }</pre>
 *
 * <h2>Identity semantics</h2>
 * <p>{@link #equals(Object)} and {@link #hashCode()} are implemented following
 * the JPA best-practice pattern recommended by Vlad Mihalcea:
 * <ul>
 *   <li>{@code hashCode} always returns a stable constant ({@code getClass().hashCode()})
 *       so that entities can safely live in {@link java.util.HashSet} or
 *       {@link java.util.HashMap} before and after being persisted.</li>
 *   <li>{@code equals} compares by non-null {@code id} via getters (not fields)
 *       to work correctly through Hibernate proxy objects.</li>
 *   <li>Two transient entities (both with {@code id == null}) are never equal
 *       to each other, which prevents accidental de-duplication before save.</li>
 * </ul>
 *
 * <p>This class implements {@link Serializable} to satisfy the JPA specification
 * requirement for entity identifier types and to support detached-entity use cases.
 *
 * @see EntitySequence
 * @see TimestampedEntity
 * @see CreationTimestampedEntity
 * @see UpdateTimestampedEntity
 * @see AuditableEntity
 */
@MappedSuperclass
@SuperBuilder
@Getter @Setter
@AllArgsConstructor @NoArgsConstructor
public abstract class BaseEntity implements Serializable {

    /**
     * The surrogate primary key, populated by the database sequence
     * {@code {entity_class_name}_seq_gen} on first persist.
     *
     * <p>{@code updatable = false} ensures that Hibernate never includes
     * the {@code id} column in {@code UPDATE} statements.
     */
    @Id
    @EntitySequence
    @Column(name = "id", updatable = false)
    private Long id;

    /**
     * Two entities are equal if and only if they belong to the same concrete
     * class hierarchy and share the same non-null {@code id}.
     *
     * <p>Getters are used instead of direct field access to ensure correct
     * behavior through Hibernate proxy objects (CGLIB / ByteBuddy subclasses).
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        // instanceof check handles Hibernate proxies transparently
        if (!(o instanceof BaseEntity that)) {
            return false;
        }
        // Two transient entities (id == null) are never considered equal
        return getId() != null && Objects.equals(getId(), that.getId());
    }

    /**
     * Returns a stable hash code based solely on the concrete class.
     *
     * <p>Using {@code getClass().hashCode()} guarantees that the hash code
     * never changes between the transient and persistent lifecycle states,
     * making it safe to store entities in hash-based collections at any point.
     */
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
