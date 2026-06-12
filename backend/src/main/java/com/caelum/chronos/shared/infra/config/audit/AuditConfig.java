package com.caelum.chronos.shared.infra.config.audit;

import java.util.Optional;
import org.springframework.lang.NonNull;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.AnonymousAuthenticationToken;

/**
 * Configuração para auditoria automática de entidades JPA. Esta classe habilita
 * a auditoria e define um bean AuditorAware que fornece o identificador do
 * usuário atualmente autenticado para ser utilizado nos campos de auditoria das
 * entidades. Se não houver um usuário autenticado, o bean retorna "system" como
 * valor padrão. Isso permite que as entidades que estendem BaseEntity sejam
 * automaticamente preenchidas com informações de criação e atualização,
 * incluindo o usuário responsável por cada ação, melhorando a rastreabilidade e
 * a manutenção dos dados na aplicação. A anotação @EnableJpaAuditing é
 * utilizada para ativar a auditoria JPA, e o método auditorAware() define a
 * lógica para obter o nome do usuário autenticado a partir do contexto de
 * segurança do Spring Security.
 */
@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorAware")
public class AuditConfig {

    @Bean
    @SuppressWarnings("null")
    AuditorAware<String> auditorAware() {
        return new AuditorAware<String>() {
            @Override
            @NonNull
            public Optional<String> getCurrentAuditor() {
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                if (auth == null || auth instanceof AnonymousAuthenticationToken
                        || !auth.isAuthenticated() || auth.getPrincipal() == null) {
                    return Optional.of("system");
                }
                return Optional.ofNullable(auth.getName());
            }
        };
    }
}