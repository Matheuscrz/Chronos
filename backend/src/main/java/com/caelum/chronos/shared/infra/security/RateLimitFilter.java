package com.caelum.chronos.shared.infra.security;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.time.Instant;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.caelum.chronos.modules.auth.application.service.RateLimitService;
import com.caelum.chronos.shared.infra.logging.LogContext;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {

    private final RateLimitService rateLimitService;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();
        
        // Aplica rate limit apenas para endpoints de autenticação sensíveis
        if (path.startsWith("/auth/login") || path.startsWith("/auth/register") || path.startsWith("/auth/refresh")) {
            String clientIp = request.getRemoteAddr();
            String key = "rl:" + path + ":" + clientIp;

            if (!rateLimitService.tryConsume(key)) {
                sendErrorResponse(response, request);
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private void sendErrorResponse(HttpServletResponse response, HttpServletRequest request) throws IOException {
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        Map<String, Object> error = new LinkedHashMap<>();
        error.put("timestamp", Instant.now().toString());
        error.put("status", HttpStatus.TOO_MANY_REQUESTS.value());
        error.put("error", "Too Many Requests");
        error.put("message", "Muitas tentativas. Por favor, tente novamente mais tarde.");
        error.put("path", request.getRequestURI());
        error.put("correlationId", request.getAttribute(LogContext.CORRELATION_ID));

        response.getWriter().write(objectMapper.writeValueAsString(error));
    }
}
