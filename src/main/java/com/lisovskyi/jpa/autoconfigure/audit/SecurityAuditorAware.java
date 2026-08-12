package com.lisovskyi.jpa.autoconfigure.audit;

import lombok.NonNull;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;

/**
 * Default {@link AuditorAware} implementation that resolves the current
 * auditor name from the Spring Security context.
 *
 * <p>This bean is registered automatically by the JPA starter when:
 * <ul>
 *   <li>Spring Security ({@code spring-security-core}) is present on the classpath, and</li>
 *   <li>No custom {@code AuditorAware<String>} bean is already defined in the application context.</li>
 * </ul>
 *
 * <h2>Resolution strategy</h2>
 * <ol>
 *   <li>If there is no active authentication, or the principal is {@code "anonymousUser"},
 *       returns {@code "SYSTEM"}.</li>
 *   <li>If the principal is a {@link UserDetails} instance, returns
 *       {@link UserDetails#getUsername()}.</li>
 *   <li>If the principal is a plain {@link String}, returns it directly.</li>
 *   <li>Otherwise, falls back to {@link Authentication#getName()}.</li>
 * </ol>
 *
 * <p>To override this behaviour, declare a custom {@code AuditorAware<String>} bean
 * in your application context — the starter will not register this default bean
 * when a custom one is already present.
 *
 * @see AuditorAware
 * @see com.lisovskyi.jpa.autoconfigure.entity.AuditableEntity
 */
public class SecurityAuditorAware implements AuditorAware<String> {

    private static final String SYSTEM_USER = "SYSTEM";

    /**
     * Returns the name of the currently authenticated user, or {@code "SYSTEM"}
     * if no authenticated user is present in the security context.
     *
     * @return a non-empty {@link Optional} containing the auditor name;
     *         never {@link Optional#empty()}
     */
    @Override
    public @NonNull Optional<String> getCurrentAuditor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            return Optional.of(SYSTEM_USER);
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof UserDetails userDetails) {
            return Optional.of(userDetails.getUsername());
        } else if (principal instanceof String principalStr) {
            return Optional.of(principalStr);
        }

        return Optional.of(authentication.getName());
    }
}
