package com.caelum.chronos.shared.infra.security;

import java.nio.charset.StandardCharsets;
import java.util.List;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.http.MediaType;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.caelum.chronos.shared.infra.logging.LogContext;

/**
 * Configuração de segurança para a aplicação, definindo as regras de
 * autenticação
 * e autorização, bem como os beans necessários para o funcionamento do sistema
 * de
 * segurança. Esta classe habilita a segurança baseada em JWT, configura os
 * filtros de autenticação, define as políticas de CORS e gerencia a criação e
 * validação dos tokens JWT. Ela também inclui a configuração do PasswordEncoder
 * para garantir que as senhas dos usuários sejam armazenadas de forma segura
 * utilizando o algoritmo Argon2. A anotação @EnableMethodSecurity é utilizada
 * para habilita a segurança em nível de método, permitindo o uso de anotações
 * como @PreAuthorize e @PostAuthorize para controlar o acesso aos métodos com
 * base nas autoridades do usuário. A anotação @EnableConfigurationProperties é
 * utilizada para habilitar a configuração baseada em propriedades, permitindo
 * que as configurações de segurança sejam definidas no arquivo application.yml
 * e injetadas nos beans de segurança conforme necessário. O método
 * securityFilterChain() define as regras de segurança para as requisições HTTP,
 * enquanto os métodos passwordEncoder(), jwtEncoder() e jwtDecoder() definem os
 * beans necessários para a codificação de senhas e a geração/validação de
 * tokens JWT. O método corsConfigurationSource() configura as políticas de CORS
 * para permitir que aplicação seja acessada de diferentes origens, conforme
 * definido nas propriedades de segurança. O método secretKey() é um utilitário
 * para criar a chave secreta utilizada na assinatura dos tokens JWT a partir de
 * uma string definida nas propriedades de segurança.
 */
@Configuration
@EnableMethodSecurity
@EnableConfigurationProperties(SecurityProperties.class)
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http,
            SecurityProperties properties,
            JwtAuthenticationFilter jwtAuthenticationFilter,
            RateLimitFilter rateLimitFilter,
            OAuth2SuccessHandler oAuth2SuccessHandler,
            OAuth2FailureHandler oAuth2FailureHandler,
            HttpCookieOAuth2AuthorizationRequestRepository cookieAuthorizationRequestRepository,
            ObjectMapper objectMapper) throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource(properties)))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())
                .addFilterBefore(rateLimitFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .oauth2Login(oauth2 -> oauth2
                        .authorizationEndpoint(auth -> auth
                                .baseUri("/oauth2/authorization")
                                .authorizationRequestRepository(cookieAuthorizationRequestRepository))
                        .redirectionEndpoint(redirection -> redirection
                                .baseUri("/auth/oauth2/callback"))
                        .successHandler(oAuth2SuccessHandler)
                        .failureHandler(oAuth2FailureHandler))
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(unauthorizedEntryPoint(objectMapper))
                        .accessDeniedHandler(forbiddenHandler(objectMapper)))
                .authorizeHttpRequests(auth -> {
                    auth.requestMatchers(
                            "/auth/**",
                            "/login/**",
                            "/oauth2/**",
                            "/swagger-ui/**",
                            "/swagger-ui.html",
                            "/v3/api-docs",
                            "/v3/api-docs/**",
                            "/v3/api-docs.yaml",
                            "/webjars/**",
                            "/actuator/health/**",
                            "/actuator/info")
                            .permitAll();
                    auth.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll();

                    if (properties.permitAll()) {
                        auth.anyRequest().permitAll();
                    } else {
                        auth.anyRequest().authenticated();
                    }
                });

        return http.build();
    }

    @Bean
    PasswordEncoder passwordEncoder(SecurityProperties properties) {
        var cfg = properties.passwordEncoder();
        return new Argon2PasswordEncoder(
                cfg.saltLength(),
                cfg.hashLength(),
                cfg.parallelism(),
                cfg.memory(),
                cfg.iterations());
    }

    @Bean
    JwtEncoder jwtEncoder(SecurityProperties properties) {
        SecretKey key = secretKey(properties.jwt().secret());
        return new NimbusJwtEncoder(new ImmutableSecret<>(key));
    }

    @Bean
    JwtDecoder jwtDecoder(SecurityProperties properties) {
        SecretKey key = secretKey(properties.jwt().secret());
        return NimbusJwtDecoder.withSecretKey(key).macAlgorithm(MacAlgorithm.HS256).build();
    }

    private CorsConfigurationSource corsConfigurationSource(SecurityProperties properties) {
        CorsConfiguration cors = new CorsConfiguration();
        cors.setAllowedOrigins(normalize(properties.cors().allowedOrigins()));
        cors.setAllowedMethods(normalize(properties.cors().allowedMethods()));
        cors.setAllowedHeaders(normalize(properties.cors().allowedHeaders()));
        cors.setExposedHeaders(normalize(properties.cors().exposedHeaders()));
        cors.setAllowCredentials(properties.cors().allowCredentials());

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cors);
        return source;
    }

    private List<String> normalize(List<String> values) {
        if (values == null) {
            return List.of();
        }
        return values.stream()
                .filter(v -> v != null && !v.isBlank())
                .map(String::trim)
                .toList();
    }

    private SecretKey secretKey(String secret) {
        return new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
    }

    private AuthenticationEntryPoint unauthorizedEntryPoint(ObjectMapper objectMapper) {
        return (request, response, authException) -> {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);

            String correlationId = (String) request.getAttribute(LogContext.CORRELATION_ID);
            Map<String, Object> error = new LinkedHashMap<>();
            error.put("timestamp", Instant.now().toString());
            error.put("status", HttpServletResponse.SC_UNAUTHORIZED);
            error.put("error", "Unauthorized");
            error.put("message", "Não autenticado");
            error.put("path", request.getRequestURI());
            error.put("correlationId", correlationId);
            error.put("fieldErrors", List.of());

            response.getWriter().write(objectMapper.writeValueAsString(error));
        };
    }

    private AccessDeniedHandler forbiddenHandler(ObjectMapper objectMapper) {
        return (request, response, accessDeniedException) -> {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || auth instanceof AnonymousAuthenticationToken) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);

                String correlationId = (String) request.getAttribute(LogContext.CORRELATION_ID);
                Map<String, Object> error = new LinkedHashMap<>();
                error.put("timestamp", Instant.now().toString());
                error.put("status", HttpServletResponse.SC_UNAUTHORIZED);
                error.put("error", "Unauthorized");
                error.put("message", "Não autenticado");
                error.put("path", request.getRequestURI());
                error.put("correlationId", correlationId);
                error.put("fieldErrors", List.of());

                response.getWriter().write(objectMapper.writeValueAsString(error));
            } else {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);

                String correlationId = (String) request.getAttribute(LogContext.CORRELATION_ID);
                Map<String, Object> error = new LinkedHashMap<>();
                error.put("timestamp", Instant.now().toString());
                error.put("status", HttpServletResponse.SC_FORBIDDEN);
                error.put("error", "Forbidden");
                error.put("message", "Acesso negado");
                error.put("path", request.getRequestURI());
                error.put("correlationId", correlationId);
                error.put("fieldErrors", List.of());

                response.getWriter().write(objectMapper.writeValueAsString(error));
            }
        };
    }
}