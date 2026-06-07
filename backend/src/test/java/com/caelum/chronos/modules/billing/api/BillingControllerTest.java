package com.caelum.chronos.modules.billing.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.math.BigDecimal;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.caelum.chronos.modules.billing.application.dto.request.AccountCreateRequest;
import com.caelum.chronos.modules.billing.application.dto.request.MoneyOperationRequest;
import com.caelum.chronos.modules.billing.application.dto.response.AccountResponse;
import com.caelum.chronos.modules.billing.application.service.BillingService;
import com.caelum.chronos.modules.billing.domain.enums.BillingAccountStatus;
import com.caelum.chronos.shared.api.error.GlobalExceptionHandler;

class BillingControllerTest {

    private MockMvc mockMvc;
    private BillingService billingService;

    @BeforeEach
    void setUp() {
        billingService = mock(BillingService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new BillingController(billingService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void createDeveCriarConta() throws Exception {
        UUID userId = UUID.randomUUID();
        AccountResponse response = AccountResponse.builder()
                .id(UUID.randomUUID())
                .ownerId(userId)
                .balance(new BigDecimal("100.00"))
                .status(BillingAccountStatus.ACTIVE)
                .build();

        when(billingService.createAccount(any(AccountCreateRequest.class))).thenReturn(response);

        mockMvc.perform(post("/billing/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(String.format("{\"ownerId\":\"%s\", \"initialBalance\": 100}", userId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.ownerId").value(userId.toString()));
    }

    @Test
    void depositDeveRealizarDeposito() throws Exception {
        UUID accountId = UUID.randomUUID();
        AccountResponse response = AccountResponse.builder()
                .id(accountId)
                .ownerId(UUID.randomUUID())
                .balance(new BigDecimal("150.00"))
                .status(BillingAccountStatus.ACTIVE)
                .build();

        when(billingService.deposit(eq(accountId), any(MoneyOperationRequest.class))).thenReturn(response);

        mockMvc.perform(post("/billing/accounts/{id}/deposit", accountId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"amount\": 50.00}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(150.00));
    }

    @Test
    void withdrawDeveRealizarSaque() throws Exception {
        UUID accountId = UUID.randomUUID();
        AccountResponse response = AccountResponse.builder()
                .id(accountId)
                .ownerId(UUID.randomUUID())
                .balance(new BigDecimal("50.00"))
                .status(BillingAccountStatus.ACTIVE)
                .build();

        when(billingService.withdraw(eq(accountId), any(MoneyOperationRequest.class))).thenReturn(response);

        mockMvc.perform(post("/billing/accounts/{id}/withdraw", accountId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"amount\": 50.00}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(50.00));
    }

    @Test
    void findByIdDeveRetornarConta() throws Exception {
        UUID accountId = UUID.randomUUID();
        AccountResponse response = AccountResponse.builder()
                .id(accountId)
                .ownerId(UUID.randomUUID())
                .balance(new BigDecimal("100.00"))
                .status(BillingAccountStatus.ACTIVE)
                .build();

        when(billingService.findById(accountId)).thenReturn(response);

        mockMvc.perform(get("/billing/accounts/{id}", accountId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(accountId.toString()));
    }
}