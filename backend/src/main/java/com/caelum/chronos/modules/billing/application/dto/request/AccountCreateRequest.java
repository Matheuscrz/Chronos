package com.caelum.chronos.modules.billing.application.dto.request;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

/**
 * DTO para criação de conta, contendo o ID do proprietário e o saldo inicial.
 * O campo ownerId é obrigatório e deve ser um UUID válido, enquanto o
 * initialBalance é opcional e deve ser um valor positivo ou zero.
 * Este DTO é utilizado para receber os dados necessários para criar uma nova
 * conta no sistema de billing.
 */
public record AccountCreateRequest(
        @NotNull UUID ownerId,
        @PositiveOrZero BigDecimal initialBalance) {
}