package com.lisovskyi.jpa.autoconfigure;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.jpa")
public class JpaProperties {
    /**
     * Whether JPA auditing is enabled.
     */
    private boolean auditingEnabled = true;
}
