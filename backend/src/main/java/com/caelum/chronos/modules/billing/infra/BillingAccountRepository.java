package com.caelum.chronos.modules.billing.infra;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;

import com.caelum.chronos.modules.billing.domain.model.BillingAccount;

import jakarta.persistence.LockModeType;

/**
 * Repositório para a entidade BillingAccount, responsável por realizar
 * operações de
 * persistência e consulta no banco de dados.
 * Estende JpaRepository, fornecendo métodos CRUD básicos e a capacidade de
 * definir consultas personalizadas.
 * <ul>
 * <li><strong>existsByOwner_Id</strong>: Método para verificar se uma conta de
 * billing existe para um determinado ID de proprietário, retornando um
 * boolean.</li>
 * <li><strong>findByOwner_Id</strong>: Método para encontrar uma conta de
 * billing pelo ID do proprietário, retornando um Optional<BillingAccount>.</li>
 * <li><strong>findByIdForUpdate</strong>: Método para encontrar uma conta de
 * billing pelo ID com bloqueio pessimista para atualização, garantindo que a
 * conta seja bloqueada para outras transações enquanto estiver sendo
 * atualizada,
 * retornando um Optional<BillingAccount>.</li>
 * </ul>
 */
public interface BillingAccountRepository extends JpaRepository<BillingAccount, UUID> {
    /**
     * Verifica se uma conta de billing existe para um determinado ID de
     * proprietário.
     * 
     * @param ownerId O ID do proprietário a ser verificado.
     * @return true se uma conta de billing existir para o ID do proprietário, false
     *         caso contrário.
     */
    boolean existsByOwner_Id(UUID ownerId);

    /**
     * Encontra uma conta de billing pelo ID do proprietário.
     * 
     * @param ownerId O ID do proprietário a ser pesquisado.
     * @return Um Optional contendo a conta de billing encontrada, ou vazio se
     *         nenhuma conta for encontrada para o ID do proprietário.
     */
    Optional<BillingAccount> findByOwner_Id(UUID ownerId);

    /**
     * Encontra uma conta de billing pelo ID com bloqueio pessimista para
     * atualização,
     * garantindo que a conta seja bloqueada para outras transações enquanto estiver
     * sendo atualizada.
     *
     * @param id O ID da conta de billing a ser pesquisada.
     * @return Um Optional contendo a conta de billing encontrada, ou vazio se
     *         nenhuma conta for encontrada para o ID especificado.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select b from BillingAccount b where b.id = :id")
    @NonNull
    Optional<BillingAccount> findByIdForUpdate(@Param("id") UUID id);
}