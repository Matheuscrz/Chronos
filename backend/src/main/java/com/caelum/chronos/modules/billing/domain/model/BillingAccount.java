package com.caelum.chronos.modules.billing.domain.model;

import java.math.BigDecimal;

import com.caelum.chronos.modules.billing.domain.enums.BillingAccountStatus;
import com.caelum.chronos.modules.users.domain.model.User;
import com.caelum.chronos.shared.domain.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Builder.Default;

/**
 * Representa uma conta de billing no sistema. Cada conta está associada a um
 * usuário (proprietário) e possui um saldo e um status. A classe inclui métodos
 * para realizar operações de depósito e saque, garantindo que os valores sejam
 * positivos e que o saldo seja suficiente para saques. O status da conta é
 * gerenciado por meio do enum BillingAccountStatus, permitindo que o sistema
 * controle as operações com base no estado da conta (ativo, congelado, fechado,
 * etc.).
 * <ul>
 * <li><strong>owner</strong>: Referência para o usuário proprietário da
 * conta.</li>
 * <li><strong>balance</strong>: Saldo atual da conta, representado por um
 * BigDecimal para precisão financeira.</li>
 * <li><strong>status</strong>: Status atual da conta, definido pelo enum
 * BillingAccountStatus.</li>
 * </ul>
 */
@Getter
@Entity
@Table(name = "billing_accounts")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class BillingAccount extends BaseEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Column(name = "balance", precision = 19, scale = 2, nullable = false)
    @Default
    private BigDecimal balance = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 50, nullable = false)
    @Default
    private BillingAccountStatus status = BillingAccountStatus.ACTIVE;

    public void deposit(BigDecimal amount) {
        validatePositiveAmount(amount);
        this.balance = this.balance.add(amount);
    }

    public void withdraw(BigDecimal amount) {
        validatePositiveAmount(amount);
        if (this.balance.compareTo(amount) < 0) {
            throw new IllegalArgumentException("insufficient funds");
        }
        this.balance = this.balance.subtract(amount);
    }

    private void validatePositiveAmount(BigDecimal amount) {
        if (amount == null || amount.signum() <= 0) {
            throw new IllegalArgumentException("amount must be greater than zero");
        }
    }
}