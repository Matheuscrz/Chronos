package com.caelum.chronos.modules.billing.api;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.caelum.chronos.modules.billing.application.dto.request.AccountCreateRequest;
import com.caelum.chronos.modules.billing.application.dto.request.MoneyOperationRequest;
import com.caelum.chronos.modules.billing.application.dto.response.AccountResponse;
import com.caelum.chronos.modules.billing.application.service.BillingService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Controlador responsável por gerenciar as operações relacionadas às contas de
 * cobrança, incluindo criação de contas, depósitos, saques e consulta de saldo.
 * Ele utiliza o serviço de cobrança para realizar as operações necessárias e
 * expõe endpoints REST para cada uma dessas operações, garantindo que apenas
 * usuários autenticados possam acessar os recursos protegidos.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/billing/accounts")
@PreAuthorize("isAuthenticated()")
public class BillingController {

    private final BillingService billingService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','TECNICO','CLIENTE')")
    public ResponseEntity<AccountResponse> create(@RequestBody @Valid AccountCreateRequest request) {
        return ResponseEntity.status(201).body(billingService.createAccount(request));
    }

    @PostMapping("/{id}/deposit")
    @PreAuthorize("hasAnyRole('ADMIN','TECNICO','CLIENTE')")
    public ResponseEntity<AccountResponse> deposit(@PathVariable UUID id,
            @RequestBody @Valid MoneyOperationRequest request) {
        return ResponseEntity.ok(billingService.deposit(id, request));
    }

    @PostMapping("/{id}/withdraw")
    @PreAuthorize("hasAnyRole('ADMIN','TECNICO','CLIENTE')")
    public ResponseEntity<AccountResponse> withdraw(@PathVariable UUID id,
            @RequestBody @Valid MoneyOperationRequest request) {
        return ResponseEntity.ok(billingService.withdraw(id, request));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','TECNICO','CLIENTE')")
    public ResponseEntity<AccountResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(billingService.findById(id));
    }
}