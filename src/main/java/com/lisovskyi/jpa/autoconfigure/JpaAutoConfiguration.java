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

@AutoConfiguration
@EnableConfigurationProperties(JpaProperties.class)
@ConditionalOnProperty(prefix = "app.jpa", name = "auditing-enabled", havingValue = "true", matchIfMissing = true)
@Import(JpaAutoConfiguration.JpaAuditingConfiguration.class)
public class JpaAutoConfiguration {

    /**
     * Registers the default AuditorAware bean that reads the current user
     * from the Spring Security context. Only activated when Spring Security
     * is present on the classpath and no custom AuditorAware bean exists.
     */
    @Bean
    @ConditionalOnMissingBean(AuditorAware.class)
    @ConditionalOnClass(name = "org.springframework.security.core.context.SecurityContextHolder")
    public AuditorAware<String> auditorAware() {
        return new SecurityAuditorAware();
    }

    /**
     * Separate configuration class for @EnableJpaAuditing to avoid
     * IllegalStateException when multiple configurations attempt to enable
     * JPA auditing (e.g., if the consumer app also declares @EnableJpaAuditing).
     * The {@code jpaAuditingHandler} bean name is registered by @EnableJpaAuditing
     * so @ConditionalOnMissingBean prevents double registration.
     */
    @Configuration
    @ConditionalOnMissingBean(name = "jpaAuditingHandler")
    @EnableJpaAuditing
    static class JpaAuditingConfiguration {
    }
}
