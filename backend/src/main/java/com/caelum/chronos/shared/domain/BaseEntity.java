package com.caelum.chronos.shared.domain;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Id;
import jakarta.persistence.Version;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;

/**
 * Classe base para entidades, fornecendo campos comuns como id, timestamps de
 * criação e atualização, e controle de versão.
 * Utiliza JPA para mapeamento e Spring Data JPA para auditoria automática.
 * <ul>
 * <li><strong>id</strong>: Identificador único da entidade, gerado
 * automaticamente como UUID.</li>
 * <li><strong>createdAt</strong>: Timestamp de criação da entidade, preenchido
 * automaticamente.</li>
 * <li><strong>updatedAt</strong>: Timestamp da última atualização da entidade,
 * preenchido automaticamente.</li>
 * <li><strong>createdBy</strong>: Identificador do usuário que criou a
 * entidade, preenchido automaticamente.</li>
 * <li><strong>updatedBy</strong>: Identificador do usuário que fez a última
 * modificação na entidade, preenchido automaticamente.</li>
 * <li><strong>version</strong>: Campo de controle de versão para otimistic
 * locking.</li>
 * </ul>
 */
@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @CreatedBy
    @Column(name = "created_by")
    private String createdBy;

    @LastModifiedBy
    @Column(name = "updated_by")
    private String updatedBy;

    @Version
    @Column(name = "version")
    private Long version;
}