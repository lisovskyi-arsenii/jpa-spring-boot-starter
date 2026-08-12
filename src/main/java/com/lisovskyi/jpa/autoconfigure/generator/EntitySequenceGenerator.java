package com.lisovskyi.jpa.autoconfigure.generator;

import org.hibernate.MappingException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.generator.EventType;
import org.hibernate.generator.GeneratorCreationContext;
import org.hibernate.id.enhanced.SequenceStyleGenerator;

import java.util.Locale;
import java.util.Properties;
import java.util.regex.Pattern;

/**
 * Hibernate sequence generator that automatically derives the sequence name
 * from the entity class name.
 *
 * <p>The sequence name is built by converting the simple class name from
 * CamelCase to snake_case and appending the {@code _seq_gen} suffix.
 *
 * <p>Naming examples:
 * <ul>
 *   <li>{@code UserEntity}    → {@code user_entity_seq_gen}</li>
 *   <li>{@code ProductOrder}  → {@code product_order_seq_gen}</li>
 *   <li>{@code Invoice}       → {@code invoice_seq_gen}</li>
 * </ul>
 *
 * <p>This class extends {@link SequenceStyleGenerator} to leverage Hibernate's
 * built-in sequence handling, including allocation-size batching, DDL generation
 * ({@code hibernate.hbm2ddl.auto}), and multi-database compatibility (PostgreSQL, H2, Oracle).
 *
 * <p>The allocation size (how many IDs are pre-fetched per round-trip) defaults to
 * {@code 50} and can be overridden per entity via {@link SequenceSize}:
 * <pre>{@code
 * @SequenceSize(size = 10)
 * @Entity
 * public class RareEntity extends BaseEntity { ... }
 * }</pre>
 *
 * <p>The required database sequence must exist before the application starts
 * (or be created by {@code hbm2ddl.auto = create/update}):
 * <pre>{@code
 * CREATE SEQUENCE IF NOT EXISTS user_entity_seq_gen START WITH 1 INCREMENT BY 50;
 * }</pre>
 *
 * <p>This generator is not meant to be used directly — annotate the {@code @Id}
 * field with {@link EntitySequence} instead.
 *
 * @see EntitySequence
 * @see SequenceSize
 * @see SequenceStyleGenerator
 */
public class EntitySequenceGenerator extends SequenceStyleGenerator {
    private static final Pattern CAMEL_TO_SNAKE = Pattern.compile("([a-z])([A-Z])");

    /**
     * Configures the sequence name and allocation size before delegating to
     * the standard {@link SequenceStyleGenerator} configuration.
     *
     * <p>The sequence name is derived from {@code ENTITY_NAME} parameter
     * (the fully-qualified class name) using {@link #toSequenceName(String)}.
     *
     * <p>If the entity class is annotated with {@link SequenceSize}, the
     * specified {@link SequenceSize#size()} overrides Hibernate's default
     * allocation size of {@code 50}.
     *
     * @param creationContext context provided by Hibernate during generator setup
     * @param parameters      generator configuration parameters populated by Hibernate
     * @throws MappingException if the underlying {@link SequenceStyleGenerator} fails to configure
     */
    @Override
    public void configure(GeneratorCreationContext creationContext, Properties parameters) throws MappingException {
        String entityName = parameters.getProperty(ENTITY_NAME);

        if (entityName != null) {
            String simpleName = extractSimpleName(entityName);
            String sequenceName = toSequenceName(simpleName);

            parameters.setProperty(SEQUENCE_PARAM, sequenceName);
        }

        String allocationSize = null;

        try {
            Class<?> entityClass = Class.forName(entityName);
            SequenceSize customSize = entityClass.getAnnotation(SequenceSize.class);
            if (customSize != null) {
                allocationSize = String.valueOf(customSize.size());
            }
        } catch (ClassNotFoundException ignored) {}

        if (allocationSize != null) {
            parameters.setProperty(INCREMENT_PARAM, allocationSize);
        }

        super.configure(creationContext, parameters);
    }

    /**
     * Generates the next ID value from the sequence, or returns the existing
     * value if the entity already has a non-zero ID assigned.
     *
     * <p>This allows callers to control the ID explicitly when needed
     * (e.g. during data migration or programmatic inserts with pre-set keys).
     * A value of {@code 0} is treated as "not set" and triggers a sequence call,
     * since {@code 0} is the default for uninitialized {@code long} primitives
     * and is not a valid database primary key.
     *
     * @param session      the current Hibernate session
     * @param owner        the entity instance being persisted
     * @param currentValue the current value of the identifier field, or {@code null}
     * @param eventType    the event that triggered ID generation (INSERT)
     * @return the existing non-zero ID, or the next value from the database sequence
     */
    @Override
    public Object generate(SharedSessionContractImplementor session, Object owner, Object currentValue, EventType eventType) {
        if (currentValue != null) {
            if (!(currentValue instanceof Number value) || value.longValue() != 0L) {
                return currentValue;
            }
        }

        return super.generate(session, owner, currentValue, eventType);
    }

    /**
     * Extracts the simple class name from a fully-qualified entity name.
     *
     * <p>For example: {@code com.example.UserEntity} → {@code UserEntity}.
     *
     * @param entityName the fully-qualified class name provided by Hibernate
     * @return the simple class name (part after the last {@code .})
     */
    private String extractSimpleName(String entityName) {
        int lastDotIndex = entityName.lastIndexOf('.');
        return lastDotIndex >= 0 ? entityName.substring(lastDotIndex + 1) : entityName;
    }

    /**
     * Converts a CamelCase class name to a snake_case sequence name with
     * the {@code _seq_gen} suffix.
     *
     * <p>Conversion examples:
     * <ul>
     *   <li>{@code UserEntity}   → {@code user_entity_seq_gen}</li>
     *   <li>{@code ProductOrder} → {@code product_order_seq_gen}</li>
     *   <li>{@code Invoice}      → {@code invoice_seq_gen}</li>
     * </ul>
     *
     * @param simpleClassName the simple class name (without package)
     * @return the derived sequence name
     */
    private String toSequenceName(String simpleClassName) {
        return CAMEL_TO_SNAKE
                .matcher(simpleClassName)
                .replaceAll("$1_$2")
                .toLowerCase(Locale.ROOT)
                .concat("_seq_gen");
    }
}