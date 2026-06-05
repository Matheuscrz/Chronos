package com.caelum.chronos.modules.billing.application.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.Objects;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import com.caelum.chronos.backend.BackendApplication;
import com.caelum.chronos.modules.billing.application.dto.request.AccountCreateRequest;
import com.caelum.chronos.modules.billing.application.dto.request.MoneyOperationRequest;
import com.caelum.chronos.modules.billing.application.dto.response.AccountResponse;
import com.caelum.chronos.modules.billing.application.service.BillingService;
import com.caelum.chronos.modules.billing.infra.BillingAccountRepository;
import com.caelum.chronos.modules.users.domain.enums.UserRole;
import com.caelum.chronos.modules.users.domain.model.User;
import com.caelum.chronos.modules.users.infra.UserRepository;

@SpringBootTest(classes = BackendApplication.class)
@ActiveProfiles("test")
class BillingServiceConcurrencyIT {

    @Autowired
    BillingService billingService;

    @Autowired
    BillingAccountRepository billingAccountRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @BeforeEach
    void limparDados() {
        jdbcTemplate.execute("""
                    TRUNCATE TABLE chronos_app_test.billing_accounts,
                                   chronos_app_test.users
                    RESTART IDENTITY CASCADE
                """);
    }

    @Test
    void naoDevePermitirGastarMaisDoQueSaldoComDuasOperacoesSimultaneas() throws Exception {
        User owner = Objects.requireNonNull(userRepository.save(User.builder()
            .username("owner")
            .fullName("Owner")
            .email("owner@test.com")
            .passwordHash("hash")
            .role(UserRole.CLIENTE)
            .build()));

        AccountResponse account = billingService.createAccount(new AccountCreateRequest(
                owner.getId(),
                new BigDecimal("1000.00")));

        UUID accountId = account.id();

        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);

        Callable<Object> task = () -> {
            ready.countDown();
            start.await(5, TimeUnit.SECONDS);
            try {
                return billingService.withdraw(accountId,
                        new MoneyOperationRequest(new BigDecimal("1000.00")));
            } catch (Throwable ex) {
                return ex;
            }
        };

        Future<Object> f1 = executor.submit(task);
        Future<Object> f2 = executor.submit(task);

        ready.await(5, TimeUnit.SECONDS);
        start.countDown();

        Object r1 = f1.get(10, TimeUnit.SECONDS);
        Object r2 = f2.get(10, TimeUnit.SECONDS);

        int successCount = 0;
        int failureCount = 0;

        if (r1 instanceof AccountResponse)
            successCount++;
        else
            failureCount++;
        if (r2 instanceof AccountResponse)
            successCount++;
        else
            failureCount++;

        AccountResponse finalState = billingService.findById(accountId);

        assertThat(successCount).isEqualTo(1);
        assertThat(failureCount).isEqualTo(1);
        assertThat(finalState.balance()).isEqualByComparingTo("0.00");

        executor.shutdownNow();
    }

    @Test
    void naoDevePermitirSaqueMaiorQueSaldo() {
        User owner = Objects.requireNonNull(userRepository.save(User.builder()
            .username("owner2")
            .fullName("Owner 2")
            .email("owner2@test.com")
            .passwordHash("hash")
            .role(UserRole.CLIENTE)
            .build()));

        AccountResponse account = billingService.createAccount(new AccountCreateRequest(
                owner.getId(),
                new BigDecimal("1000.00")));

        UUID accountId = account.id();
        MoneyOperationRequest withdrawRequest = new MoneyOperationRequest(new BigDecimal("2000.00"));

        assertThatThrownBy(
                () -> billingService.withdraw(accountId, withdrawRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("insufficient funds");
    }
}