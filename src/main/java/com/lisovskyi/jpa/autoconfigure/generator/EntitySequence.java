package com.lisovskyi.jpa.autoconfigure.generator;

import org.hibernate.annotations.IdGeneratorType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a primary key field to be populated via a database sequence whose
 * name is automatically derived from the entity class name.
 *
 * <p>The sequence name follows the pattern {@code {class_name}_seq_gen},
 * where the simple class name is converted from CamelCase to snake_case:
 * <ul>
 *   <li>{@code UserEntity}   → {@code user_entity_seq_gen}</li>
 *   <li>{@code ProductOrder} → {@code product_order_seq_gen}</li>
 *   <li>{@code Invoice}      → {@code invoice_seq_gen}</li>
 * </ul>
 *
 * <p>Usage — place on the {@code @Id} field of any entity that extends
 * {@link com.lisovskyi.jpa.autoconfigure.entity.BaseEntity}
 * (it is already applied there; no action is needed in subclasses):
 * <pre>{@code
 * @Id
 * @EntitySequence
 * @Column(name = "id", updatable = false)
 * private Long id;
 * }</pre>
 *
 * <p>The corresponding sequence must exist in the database before the
 * application starts, or be created automatically via
 * {@code spring.jpa.hibernate.ddl-auto=create} / {@code update}:
 * <pre>{@code
 * CREATE SEQUENCE IF NOT EXISTS user_entity_seq_gen START WITH 1 INCREMENT BY 50;
 * }</pre>
 *
 * <p>The default allocation size (IDs pre-fetched per database round-trip) is
 * {@code 50}. Override it per entity with {@link SequenceSize}:
 * <pre>{@code
 * @SequenceSize(size = 10)
 * @Entity
 * public class RareEntity extends BaseEntity { ... }
 * }</pre>
 *
 * @see EntitySequenceGenerator
 * @see SequenceSize
 */
@IdGeneratorType(EntitySequenceGenerator.class)
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD})
public @interface EntitySequence {
}