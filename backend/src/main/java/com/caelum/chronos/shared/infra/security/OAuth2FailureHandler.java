package com.caelum.chronos.shared.infra.security;

import java.io.IOException;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2FailureHandler implements AuthenticationFailureHandler {

    private final SecurityProperties securityProperties;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException exception) throws IOException, ServletException {

        log.error("OAuth2 authentication failure: {}", exception.getMessage());

        String targetUrl = securityProperties.cors().allowedOrigins().get(0);
        
        String redirectUrl = UriComponentsBuilder.fromUriString(targetUrl)
                .queryParam("error", "service_unavailable")
                .queryParam("message", "O serviço de autenticação externa está temporariamente indisponível.")
                .build().toUriString();

        response.sendRedirect(redirectUrl);
    }
}
