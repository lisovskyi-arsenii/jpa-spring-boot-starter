package com.lisovskyi.jpa.autoconfigure;

import com.lisovskyi.jpa.autoconfigure.audit.SecurityAuditorAware;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Spring Boot auto-configuration for JPA auditing support.
 *
 * <p>Activated when {@code app.jpa.auditing-enabled=true} (default).
 * Disable by setting {@code app.jpa.auditing-enabled=false} in
 * {@code application.properties} / {@code application.yml}.
 *
 * <p>This configuration:
 * <ul>
 *   <li>Enables JPA auditing via a nested {@link JpaAuditingConfiguration}
 *       class to avoid conflicts when the consumer application also
 *       declares {@code @EnableJpaAuditing}.</li>
 *   <li>Registers a default {@link SecurityAuditorAware} bean that reads
 *       the current username from the Spring Security context — but only
 *       when Spring Security is on the classpath and no custom
 *       {@code AuditorAware<String>} bean is already defined.</li>
 * </ul>
 *
 * @see JpaProperties
 * @see SecurityAuditorAware
 */
@AutoConfiguration
@EnableConfigurationProperties(JpaProperties.class)
@ConditionalOnProperty(prefix = "app.jpa", name = "auditing-enabled", havingValue = "true", matchIfMissing = true)
@Import(JpaAutoConfiguration.JpaAuditingConfiguration.class)
public class JpaAutoConfiguration {

    /**
     * Registers the default {@link SecurityAuditorAware} bean.
     *
     * <p>Only created when both conditions are met:
     * <ul>
     *   <li>Spring Security's {@code SecurityContextHolder} is present on the classpath.</li>
     *   <li>No custom {@code AuditorAware} bean is already defined in the context.</li>
     * </ul>
     *
     * <p>Override by declaring your own {@code AuditorAware<String>} bean:
     * <pre>{@code
     * @Bean
     * public AuditorAware<String> auditorAware() {
     *     return () -> Optional.of("custom-user");
     * }
     * }</pre>
     *
     * @return a {@link SecurityAuditorAware} that resolves the auditor from
     *         the active Spring Security {@code Authentication}
     */
    @Bean
    @ConditionalOnMissingBean(AuditorAware.class)
    @ConditionalOnClass(name = "org.springframework.security.core.context.SecurityContextHolder")
    public AuditorAware<String> auditorAware() {
        return new SecurityAuditorAware();
    }

    /**
     * Isolated configuration class that activates {@code @EnableJpaAuditing}.
     *
     * <p>Placed in a separate nested class to avoid an
     * {@code IllegalStateException} that occurs when two configuration classes
     * both declare {@code @EnableJpaAuditing} in the same application context
     * (e.g. when the consumer app also annotates its own config class).
     * The {@code jpaAuditingHandler} bean guard ensures that auditing is
     * enabled at most once.
     */
    @Configuration
    @ConditionalOnMissingBean(name = "jpaAuditingHandler")
    @EnableJpaAuditing
    static class JpaAuditingConfiguration {
    }
}
