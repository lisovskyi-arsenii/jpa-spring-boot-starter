package com.lisovskyi.jpa.autoconfigure.audit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class SecurityAuditorAwareTest {

    private final SecurityAuditorAware auditorAware = new SecurityAuditorAware();

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void returnsSystemWhenNoAuthenticationIsPresent() {
        assertThat(auditorAware.getCurrentAuditor()).contains("SYSTEM");
    }

    @Test
    void returnsSystemWhenAuthenticationIsNotAuthenticated() {
        Authentication authentication = new TestingAuthenticationToken("someone", "n/a");
        authentication.setAuthenticated(false);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        assertThat(auditorAware.getCurrentAuditor()).contains("SYSTEM");
    }

    @Test
    void returnsSystemForAnonymousPrincipal() {
        Authentication authentication = new TestingAuthenticationToken("anonymousUser", "n/a", "ROLE_ANONYMOUS");
        SecurityContextHolder.getContext().setAuthentication(authentication);

        assertThat(auditorAware.getCurrentAuditor()).contains("SYSTEM");
    }

    @Test
    void returnsUsernameForUserDetailsPrincipal() {
        User principal = new User("jane.doe", "n/a", java.util.List.of());
        Authentication authentication =
                new UsernamePasswordAuthenticationToken(principal, "n/a", principal.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        assertThat(auditorAware.getCurrentAuditor()).contains("jane.doe");
    }

    @Test
    void returnsPrincipalDirectlyWhenItIsAPlainString() {
        Authentication authentication = new TestingAuthenticationToken("john.smith", "n/a", "ROLE_USER");
        SecurityContextHolder.getContext().setAuthentication(authentication);

        assertThat(auditorAware.getCurrentAuditor()).contains("john.smith");
    }

    @Test
    void fallsBackToAuthenticationNameForOtherPrincipalTypes() {
        Authentication authentication = new TestingAuthenticationToken(42, "n/a", "ROLE_USER");
        SecurityContextHolder.getContext().setAuthentication(authentication);

        Optional<String> auditor = auditorAware.getCurrentAuditor();

        assertThat(auditor).isPresent();
        assertThat(auditor.get()).isEqualTo(authentication.getName());
    }
}
