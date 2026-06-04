package com.caelum.chronos.shared.infra.security;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import com.caelum.chronos.modules.users.domain.model.User;

import lombok.RequiredArgsConstructor;

/**
 * Serviço responsável por gerenciar a geração e validação de tokens JWT (JSON
 * Web Tokens) para autenticação e autorização. Ele utiliza o JwtEncoder para
 * criar tokens JWT contendo as informações do usuário e as reivindicações
 * necessárias, e o JwtDecoder para decodificar e validar os tokens recebidos
 * nas requisições. O serviço também inclui métodos auxiliares para verificar o
 * tipo do token (access ou refresh) e para extrair o ID do usuário a partir do
 * token, facilitando a autenticação e autorização em toda a aplicação. Este
 * serviço é utilizado pelo controlador de autenticação para gerar e validar os
 * tokens JWT durante as operações de login, refresh e acesso a recursos
 * protegidos.
 */
@Service
@RequiredArgsConstructor
public class JwtService {

    private final SecurityProperties properties;
    private final JwtEncoder jwtEncoder;
    private final JwtDecoder jwtDecoder;

    public String generateAccessToken(User user) {
        return encode(user, properties.jwt().accessTtlMinutes(), "access");
    }

    public String generateRefreshToken(User user) {
        return encode(user, properties.jwt().refreshTtlDays() * 24 * 60, "refresh");
    }

    public Jwt decode(String token) {
        return jwtDecoder.decode(token);
    }

    public boolean isRefreshToken(Jwt jwt) {
        return "refresh".equals(jwt.getClaimAsString("token_type"));
    }

    public UUID getUserId(Jwt jwt) {
        return UUID.fromString(jwt.getSubject());
    }

    private String encode(User user, long ttlMinutes, String tokenType) {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(properties.jwt().issuer())
                .issuedAt(now)
                .expiresAt(now.plus(ttlMinutes, ChronoUnit.MINUTES))
                .subject(user.getId().toString())
                .claim("username", user.getUsername())
                .claim("role", user.getRole().name())
                .claim("token_type", tokenType)
                .build();

        return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }
}