package com.caelum.chronos.modules.billing.application.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

import com.caelum.chronos.modules.billing.domain.enums.BillingAccountStatus;

import lombok.Builder;

/**
 * DTO de resposta para informações de conta, contendo o ID da conta, ID do
 * proprietário, saldo e status da conta. Este DTO é utilizado para retornar os
 * dados de uma conta no sistema de billing, permitindo que os clientes
 * visualizem as informações relevantes sobre suas contas, como o saldo atual e
 * o status (ativo, inativo, etc.). O campo id é um UUID
 */
@Builder
public record AccountResponse(
        UUID id,
        UUID ownerId,
        BigDecimal balance,
        BillingAccountStatus status) {
}