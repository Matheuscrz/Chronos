package com.caelum.chronos.shared.infra.security;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.security")
public record SecurityProperties(
        boolean permitAll,
        Cors cors,
        PasswordEncoder passwordEncoder) {

    public record Cors(
            List<String> allowedOrigins,
            List<String> allowedMethods,
            List<String> allowedHeaders,
            List<String> exposedHeaders,
            boolean allowCredentials) {
    }

    public record PasswordEncoder(
            int saltLength,
            int hashLength,
            int parallelism,
            int memory,
            int iterations) {
    }
}