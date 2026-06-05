package com.caelum.chronos.shared.infra.security;

import java.nio.charset.StandardCharsets;
import java.util.List;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.nimbusds.jose.jwk.source.ImmutableSecret;

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
 * para habilitar a segurança em nível de método, permitindo o uso de anotações
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
            JwtAuthenticationFilter jwtAuthenticationFilter) throws Exception {

        http
                .csrf(csrf -> csrf
                        .csrfTokenRepository(cookieCsrfTokenRepository())
                        .ignoringRequestMatchers("/auth/**"))
                .cors(cors -> cors.configurationSource(corsConfigurationSource(properties)))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(auth -> {
                    auth.requestMatchers(
                            "/auth/**",
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

    private CookieCsrfTokenRepository cookieCsrfTokenRepository() {
        return new CookieCsrfTokenRepository();
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
}