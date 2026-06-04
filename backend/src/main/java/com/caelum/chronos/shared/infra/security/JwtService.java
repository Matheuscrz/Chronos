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