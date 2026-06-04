package com.caelum.chronos.modules.billing.domain.enums;

/**
 * Enum representando os possíveis status de uma conta de billing. Os status
 * incluem ACTIVE (ativo), FROZEN (congelado) e CLOSED (fechado). Este enum é
 * utilizado para indicar o estado atual de uma conta de billing, permitindo que
 * o sistema gerencie as operações de acordo com o status da conta, como
 * permitir ou bloquear transações, ou indicar que a conta foi encerrada.
 */
public enum BillingAccountStatus {
    ACTIVE,
    FROZEN,
    CLOSED
}