package com.caelum.chronos.shared.infra.security;

import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class CircuitBreakerOidcUserService implements OAuth2UserService<OidcUserRequest, OidcUser> {

    private final OidcUserService delegate = new OidcUserService();

    @Override
    @CircuitBreaker(name = "keycloak", fallbackMethod = "fallbackOidcUser")
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        log.debug("Calling Keycloak to load OIDC user info...");
        return delegate.loadUser(userRequest);
    }

    public OidcUser fallbackOidcUser(OidcUserRequest userRequest, Throwable t) {
        log.error("Circuit Breaker OPEN or Keycloak failure during OIDC user info fetch. Reason: {}", t.getMessage());
        throw new OAuth2AuthenticationException("Serviço de autenticação temporariamente indisponível. Tente novamente mais tarde.");
    }
}
