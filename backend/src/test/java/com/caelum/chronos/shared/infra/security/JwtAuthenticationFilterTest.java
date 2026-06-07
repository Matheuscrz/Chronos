package com.caelum.chronos.shared.infra.security;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import com.caelum.chronos.modules.auth.application.service.TokenBlacklistService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtService jwtService;
    @Mock
    private TokenBlacklistService blacklistService;
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthenticationFilter filter;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void deveAutenticarQuandoTokenValidoENaoBlacklistado() throws ServletException, IOException {
        Cookie cookie = new Cookie(JwtCookieService.ACCESS_COOKIE, "valid-token");
        when(request.getCookies()).thenReturn(new Cookie[]{cookie});
        
        Jwt jwt = mock(Jwt.class);
        when(jwt.getId()).thenReturn("jti");
        when(jwt.getSubject()).thenReturn("user-id");
        when(jwt.getClaimAsString("role")).thenReturn("ROLE_USER");
        
        when(jwtService.decode("valid-token")).thenReturn(jwt);
        when(blacklistService.isBlacklisted("jti")).thenReturn(false);

        filter.doFilterInternal(request, response, filterChain);

        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void naoDeveAutenticarQuandoTokenBlacklistado() throws ServletException, IOException {
        Cookie cookie = new Cookie(JwtCookieService.ACCESS_COOKIE, "blacklisted-token");
        when(request.getCookies()).thenReturn(new Cookie[]{cookie});
        
        Jwt jwt = mock(Jwt.class);
        when(jwt.getId()).thenReturn("jti");
        
        when(jwtService.decode("blacklisted-token")).thenReturn(jwt);
        when(blacklistService.isBlacklisted("jti")).thenReturn(true);

        filter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void deveIgnorarEndpointsPublicos() {
        when(request.getRequestURI()).thenReturn("/auth/login");
        when(request.getMethod()).thenReturn("POST");

        assertTrue(filter.shouldNotFilter(request));
    }
}