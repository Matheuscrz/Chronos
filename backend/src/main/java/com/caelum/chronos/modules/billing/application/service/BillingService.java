package com.caelum.chronos.modules.billing.application.service;

import java.util.UUID;

import com.caelum.chronos.modules.billing.application.dto.request.AccountCreateRequest;
import com.caelum.chronos.modules.billing.application.dto.request.MoneyOperationRequest;
import com.caelum.chronos.modules.billing.application.dto.response.AccountResponse;

/**
 * Interface de serviço para operações de billing, incluindo criação de conta,
 * depósito, saque e consulta de conta por ID. Esta interface define os métodos
 * que serão implementados para gerenciar as contas de billing, permitindo que
 * os clientes criem novas contas, realizem operações de depósito e saque, e
 * consultem o status e saldo de suas contas. Cada método recebe os dados
 * necessários para a operação e retorna um AccountResponse com as informações
 * atualizadas da conta.
 * <ul>
 * <li><strong>createAccount</strong>: Método para criar uma nova conta,
 * recebendo um AccountCreateRequest e retornando um AccountResponse com os
 * detalhes da conta criada.</li>
 * <li><strong>deposit</strong>: Método para realizar um depósito em uma conta
 * existente, recebendo o ID da conta e um MoneyOperationRequest, e retornando
 * um AccountResponse com os detalhes da conta atualizada.</li>
 * <li><strong>withdraw</strong>: Método para realizar um saque em uma conta
 * existente, recebendo o ID da conta e um MoneyOperationRequest, e retornando
 * um AccountResponse com os detalhes da conta atualizada.</li>
 * <li><strong>findById</strong>: Método para consultar uma conta por seu ID,
 * recebendo o ID da conta e retornando um AccountResponse com os detalhes da
 * conta encontrada.</li>
 * </ul>
 */
public interface BillingService {
    /**
     * Cria uma nova conta com base na requisição de criação fornecida.
     * 
     * @param req A requisição contendo o ID do proprietário e o saldo inicial para
     *            a nova conta.
     * @return Um AccountResponse contendo os detalhes da conta criada, incluindo o
     *         ID da conta, ID do proprietário, saldo e status.
     */
    AccountResponse createAccount(AccountCreateRequest req);

    /**
     * Realiza um depósito em uma conta existente, atualizando o saldo da conta
     * 
     * @param accountId O ID da conta onde o depósito será realizado
     * @param req       A requisição contendo o valor do depósito a ser realizado
     * @return Um AccountResponse contendo os detalhes da conta atualizada após o
     *         depósito, incluindo o ID da conta, ID do proprietário, saldo e
     *         status.
     */
    AccountResponse deposit(UUID accountId, MoneyOperationRequest req);

    /**
     * Realiza um saque em uma conta existente, atualizando o saldo da conta.
     * 
     * @param accountId O ID da conta onde o saque será realizado
     * @param req       A requisição contendo o valor do saque a ser realizado
     * @return Um AccountResponse contendo os detalhes da conta atualizada após o
     *         saque, incluindo o ID da conta, ID do proprietário, saldo e status.
     */
    AccountResponse withdraw(UUID accountId, MoneyOperationRequest req);

    /**
     * Consulta uma conta por seu ID, retornando os detalhes da conta encontrada.
     * 
     * @param accountId O ID da conta a ser consultada.
     * @return Um AccountResponse contendo os detalhes da conta encontrada,
     *         incluindo
     *         o ID da conta, ID do proprietário, saldo e status.
     */
    AccountResponse findById(UUID accountId);
}