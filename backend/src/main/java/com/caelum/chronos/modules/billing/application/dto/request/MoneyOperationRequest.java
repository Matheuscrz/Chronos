package com.caelum.chronos.modules.billing.application.dto.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * DTO para operações de dinheiro, contendo o valor da operação. O campo amount
 * é obrigatório e deve ser um valor positivo. Este DTO é utilizado para receber
 * os dados necessários para realizar operações
 * de depósito ou saque em uma conta no sistema de billing.
 */
public record MoneyOperationRequest(
        @NotNull @Positive BigDecimal amount) {
}