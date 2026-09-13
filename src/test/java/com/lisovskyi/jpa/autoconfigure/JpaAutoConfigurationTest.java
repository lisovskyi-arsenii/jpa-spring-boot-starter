package com.lisovskyi.jpa.autoconfigure;

import com.lisovskyi.jpa.autoconfigure.audit.SecurityAuditorAware;
import com.lisovskyi.jpa.autoconfigure.entity.AuditableEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.hibernate.autoconfigure.HibernateJpaAutoConfiguration;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class JpaAutoConfigurationTest {

    // A real EntityManagerFactory is required: @EnableJpaAuditing eagerly builds a
    // JpaMetamodelMappingContext from it, which fails to start with an empty metamodel.
    // An in-memory H2 database plus one @Entity is enough to satisfy that requirement.
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                    DataSourceAutoConfiguration.class,
                    HibernateJpaAutoConfiguration.class,
                    JpaAutoConfiguration.class))
            .withUserConfiguration(TestEntityScanConfig.class)
            .withPropertyValues("spring.jpa.hibernate.ddl-auto=create-drop");

    @Test
    void registersCoreBeansByDefault() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(JpaProperties.class);
            assertThat(context).hasSingleBean(AuditorAware.class);
            assertThat(context.getBean(AuditorAware.class)).isInstanceOf(SecurityAuditorAware.class);
        });
    }

    @Test
    void doesNotRegisterAuditorAwareWhenSpringSecurityIsAbsent() {
        contextRunner
                .withClassLoader(new FilteredClassLoader(SecurityContextHolder.class))
                .run(context -> assertThat(context).doesNotHaveBean(AuditorAware.class));
    }

    @Test
    void backsOffCompletelyWhenDisabledByProperty() {
        contextRunner
                .withPropertyValues("app.jpa.auditing-enabled=false")
                .run(context -> assertThat(context).doesNotHaveBean(AuditorAware.class));
    }

    @Test
    void respectsUserDefinedAuditorAwareBean() {
        contextRunner.withUserConfiguration(CustomAuditorConfig.class).run(context -> {
            assertThat(context).hasSingleBean(AuditorAware.class);
            assertThat(context.getBean(AuditorAware.class)).isSameAs(CustomAuditorConfig.CUSTOM_AUDITOR);
        });
    }

    @Entity
    @Table(name = "test_orders")
    static class TestOrderEntity extends AuditableEntity {
    }

    @Configuration(proxyBeanMethods = false)
    @EntityScan(basePackageClasses = TestOrderEntity.class)
    static class TestEntityScanConfig {
    }

    @Configuration(proxyBeanMethods = false)
    static class CustomAuditorConfig {

        static final AuditorAware<String> CUSTOM_AUDITOR = () -> Optional.of("custom-user");

        @Bean
        AuditorAware<String> auditorAware() {
            return CUSTOM_AUDITOR;
        }
    }
}
