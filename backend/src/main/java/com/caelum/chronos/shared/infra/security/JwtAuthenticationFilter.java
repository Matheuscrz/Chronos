package com.caelum.chronos.shared.infra.security;

import java.io.IOException;
import java.util.List;

import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.caelum.chronos.shared.infra.logging.LogContext;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

/**
 * Filtro de autenticação JWT que intercepta as requisições HTTP e valida o
 * token JWT presente nos cookies. Se o token for válido, o filtro extrai as
 * informações do usuário e as autoridades (roles) do token e as adiciona ao
 * contexto de segurança do Spring Security, permitindo que os endpoints
 * protegidos possam autorizar o acesso com base nessas informações. O filtro
 * também define uma lista de endpoints que devem ser ignorados, como os
 * relacionados à autenticação, documentação e saúde do sistema, garantindo que
 * esses recursos possam ser acessados sem a necessidade de um token JWT válido.
 * Em caso de falha na validação do token, o filtro limpa o contexto de
 * segurança e permite que a requisição prossiga, o que resultará em uma
 * resposta de acesso negado para os endpoints protegidos.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        String method = request.getMethod();

        return HttpMethod.OPTIONS.matches(method)
                || path.equals("/auth")
                || path.startsWith("/auth/")
                || path.startsWith("/swagger-ui/")
                || path.startsWith("/v3/api-docs/")
                || path.startsWith("/webjars/")
                || path.startsWith("/actuator/health")
                || path.startsWith("/actuator/info");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        try {
            String token = resolveCookie(request, JwtCookieService.ACCESS_COOKIE);
            if (token != null && !token.isBlank()) {
                var jwt = jwtService.decode(token);
                String role = jwt.getClaimAsString("role");

                var auth = new UsernamePasswordAuthenticationToken(
                        jwt.getSubject(),
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_" + role)));

                SecurityContextHolder.getContext().setAuthentication(auth);
                LogContext.setUserId(jwt.getSubject());
            }

            filterChain.doFilter(request, response);
        } catch (JwtException ex) {
            SecurityContextHolder.clearContext();
            filterChain.doFilter(request, response);
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    private String resolveCookie(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }

        for (Cookie cookie : cookies) {
            if (name.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }
}