package com.caelum.chronos.modules.users.domain.model;

import com.caelum.chronos.modules.users.domain.enums.UserRole;
import com.caelum.chronos.shared.domain.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Entidade que representa um usuário do sistema, contendo informações como nome
 * de usuário, nome completo, email, senha e papel do usuário.
 * <ul>
 * <li><strong>username</strong>: Nome de usuário único, utilizado para
 * autenticação.</li>
 * <li><strong>fullName</strong>: Nome completo do usuário, utilizado para
 * exibição e identificação.</li>
 * <li><strong>email</strong>: Endereço de email do usuário, utilizado para
 * comunicação e recuperação de senha.</li>
 * <li><strong>passwordHash</strong>: Hash da senha do usuário, utilizado para
 * autenticação segura.</li>
 * <li><strong>role</strong>: Papel do usuário no sistema, definido pelo enum
 * UserRole (ADMIN, TECNICO, CLIENTE).</li>
 * </ul>
 */
@Getter
@Entity
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {

    @Column(name = "username", nullable = false, unique = true, length = 100)
    private String username;

    @Column(name = "full_name", length = 255)
    private String fullName;

    @Column(name = "email", unique = true, length = 255)
    private String email;

    @Column(name = "password_hash", length = 255)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", length = 50, nullable = false)
    private UserRole role;

    @Builder
    public User(String username, String fullName, String email, String passwordHash, UserRole role) {
        this.username = username;
        this.fullName = fullName;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
    }
}
