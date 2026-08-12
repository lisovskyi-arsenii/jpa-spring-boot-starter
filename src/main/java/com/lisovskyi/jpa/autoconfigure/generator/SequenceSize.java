package com.lisovskyi.jpa.autoconfigure.generator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Overrides the default allocation size used by {@link EntitySequenceGenerator}
 * for a specific entity class.
 *
 * <p>The allocation size controls how many sequence values Hibernate pre-fetches
 * from the database in a single round-trip. A larger value reduces database
 * round-trips at the cost of leaving gaps in the sequence if the application
 * restarts before all pre-fetched values are consumed.
 *
 * <p>This annotation must be placed on the <strong>entity class</strong> itself
 * (not on the field), and only takes effect when the entity's {@code @Id} field
 * is annotated with {@link EntitySequence}.
 *
 * <p>Default value is {@code 50}, which matches Hibernate's own default.
 *
 * <p>Usage example — set allocation size to {@code 10} for a low-traffic entity:
 * <pre>{@code
 * @SequenceSize(size = 10)
 * @Entity
 * @Table(name = "audit_logs")
 * public class AuditLogEntity extends BaseEntity { ... }
 * }</pre>
 *
 * @see EntitySequenceGenerator
 * @see EntitySequence
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface SequenceSize {

    /**
     * The number of sequence values to pre-fetch per database round-trip.
     * Must be a positive integer. Defaults to {@code 50}.
     */
    int size() default 50;
}
