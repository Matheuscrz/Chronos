package com.caelum.chronos.modules.billing.application.service.impl;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.caelum.chronos.modules.billing.application.dto.request.AccountCreateRequest;
import com.caelum.chronos.modules.billing.application.dto.request.MoneyOperationRequest;
import com.caelum.chronos.modules.billing.application.dto.response.AccountResponse;
import com.caelum.chronos.modules.billing.application.service.BillingService;
import com.caelum.chronos.modules.billing.domain.enums.BillingAccountStatus;
import com.caelum.chronos.modules.billing.domain.model.BillingAccount;
import com.caelum.chronos.modules.billing.infra.BillingAccountRepository;
import com.caelum.chronos.modules.users.infra.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BillingServiceImpl implements BillingService {

    private static final String ACCOUNT_NOT_FOUND = "account not found";

    private final BillingAccountRepository billingAccountRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public AccountResponse createAccount(AccountCreateRequest req) {
        var ownerId = Objects.requireNonNull(req.ownerId(), "owner id cannot be null");
        var owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new IllegalArgumentException("owner not found"));

        if (billingAccountRepository.existsByOwner_Id(owner.getId())) {
            throw new IllegalArgumentException("owner already has an account");
        }

        BigDecimal initialBalance = req.initialBalance() == null ? BigDecimal.ZERO : req.initialBalance();

        BillingAccount account = BillingAccount.builder()
                .owner(owner)
                .balance(initialBalance)
                .status(BillingAccountStatus.ACTIVE)
                .build();

        account = billingAccountRepository.save(account);

        return toResponse(account);
    }

    @Override
    @Transactional
    public AccountResponse deposit(UUID accountId, MoneyOperationRequest req) {
        BillingAccount account = billingAccountRepository.findByIdForUpdate(accountId)
                .orElseThrow(() -> new IllegalArgumentException(ACCOUNT_NOT_FOUND));

        account.deposit(req.amount());

        return toResponse(account);
    }

    @Override
    @Transactional
    public AccountResponse withdraw(UUID accountId, MoneyOperationRequest req) {
        BillingAccount account = billingAccountRepository.findByIdForUpdate(accountId)
                .orElseThrow(() -> new IllegalArgumentException(ACCOUNT_NOT_FOUND));

        account.withdraw(req.amount());

        return toResponse(account);
    }

    @Override
    @Transactional(readOnly = true)
    public AccountResponse findById(@NonNull UUID accountId) {
        BillingAccount account = billingAccountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException(ACCOUNT_NOT_FOUND));

        return toResponse(account);
    }

    private AccountResponse toResponse(BillingAccount account) {
        return AccountResponse.builder()
                .id(account.getId())
                .ownerId(account.getOwner().getId())
                .balance(account.getBalance())
                .status(account.getStatus())
                .build();
    }
}