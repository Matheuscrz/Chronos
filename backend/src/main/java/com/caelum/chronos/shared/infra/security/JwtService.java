package com.caelum.chronos.shared.infra.security;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import com.caelum.chronos.modules.users.domain.model.User;
import com.caelum.chronos.shared.infra.security.dto.TokenPair;

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
        return encode(user, properties.jwt().accessTtlMinutes(), "access", UUID.randomUUID().toString()).token();
    }

    public String generateRefreshToken(User user) {
        return encode(user, properties.jwt().refreshTtlDays() * 24 * 60, "refresh", UUID.randomUUID().toString()).token();
    }

    public TokenPair generateTokenPair(User user) {
        String accessJti = UUID.randomUUID().toString();
        String refreshJti = UUID.randomUUID().toString();

        var access = encode(user, properties.jwt().accessTtlMinutes(), "access", accessJti);
        var refresh = encode(user, properties.jwt().refreshTtlDays() * 24 * 60, "refresh", refreshJti);

        return new TokenPair(
            access.token(),
            refresh.token(),
            accessJti,
            refreshJti,
            access.expiresAt(),
            refresh.expiresAt()
        );
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

    private EncodedToken encode(User user, long ttlMinutes, String tokenType, String jti) {
        Instant now = Instant.now();
        Instant expiresAt = now.plus(ttlMinutes, ChronoUnit.MINUTES);
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(properties.jwt().issuer())
                .issuedAt(now)
                .expiresAt(expiresAt)
                .subject(user.getId().toString())
                .id(jti)
                .claim("username", user.getUsername())
                .claim("role", user.getRole().name())
                .claim("token_type", tokenType)
                .build();

        JwsHeader jwsHeader = JwsHeader.with(MacAlgorithm.HS256).build();
        String token = jwtEncoder.encode(JwtEncoderParameters.from(jwsHeader, claims)).getTokenValue();
        return new EncodedToken(token, expiresAt);
    }

    private record EncodedToken(String token, Instant expiresAt) {}
}