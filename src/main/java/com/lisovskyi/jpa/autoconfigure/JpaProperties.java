package com.lisovskyi.jpa.autoconfigure;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for the JPA starter, bound to the {@code app.jpa} prefix.
 *
 * <p>Available properties:
 * <pre>
 * app.jpa.auditing-enabled=true   # default: true
 * </pre>
 *
 * @see JpaAutoConfiguration
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "app.jpa")
public class JpaProperties {

    /**
     * Whether Spring Data JPA auditing is enabled.
     *
     * <p>When {@code true} (default), the starter registers
     * {@code @EnableJpaAuditing} and, if Spring Security is on the classpath,
     * a default {@link com.lisovskyi.jpa.autoconfigure.audit.SecurityAuditorAware}
     * bean that populates the {@code createdBy} / {@code updatedBy} fields
     * of {@link com.lisovskyi.jpa.autoconfigure.entity.AuditableEntity}.
     *
     * <p>Set to {@code false} to disable this auto-configuration entirely,
     * for example in modules that manage their own auditing setup.
     */
    private boolean auditingEnabled = true;
}
