package com.caelum.chronos.modules.billing.api;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.caelum.chronos.modules.billing.application.dto.request.AccountCreateRequest;
import com.caelum.chronos.modules.billing.application.dto.request.MoneyOperationRequest;
import com.caelum.chronos.modules.billing.application.dto.response.AccountResponse;
import com.caelum.chronos.modules.billing.application.service.BillingService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Billing", description = "Endpoints relacionados à gestão de contas de cobrança, incluindo criação, depósitos, saques e consulta de saldo")
public class BillingController {

    private final BillingService billingService;

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ADMIN','TECNICO','CLIENTE')")
    @Operation(description = "Endpoint para criar uma nova conta de cobrança. Requer um objeto AccountCreateRequest contendo os dados necessários para a criação da conta, como o ID do usuário associado. Retorna um AccountResponse com os detalhes da conta criada.")
    @ApiResponse(responseCode = "201", description = "Conta criada com sucesso")
    @ApiResponse(responseCode = "400", description = "Requisição inválida, como dados de criação incompletos ou usuário não encontrado")
    public ResponseEntity<AccountResponse> create(@RequestBody @Valid AccountCreateRequest request) {
        return ResponseEntity.status(201).body(billingService.createAccount(request));
    }

    @PostMapping("/{id}/deposit")
    @PreAuthorize("hasAnyAuthority('ADMIN','TECNICO','CLIENTE')")
    @Operation(description = "Endpoint para realizar um depósito em uma conta de cobrança. Requer o ID da conta e um objeto MoneyOperationRequest contendo os dados do depósito. Retorna um AccountResponse com os detalhes da conta após o depósito.")
    @ApiResponse(responseCode = "200", description = "Depósito realizado com sucesso")
    @ApiResponse(responseCode = "400", description = "Requisição inválida, como valores inválidos ou conta não encontrada")
    public ResponseEntity<AccountResponse> deposit(@PathVariable UUID id,
            @RequestBody @Valid MoneyOperationRequest request) {
        return ResponseEntity.ok(billingService.deposit(id, request));
    }

    @PostMapping("/{id}/withdraw")
    @PreAuthorize("hasAnyAuthority('ADMIN','TECNICO','CLIENTE')")
    @Operation(description = "Endpoint para realizar um saque em uma conta de cobrança. Requer o ID da conta e um objeto MoneyOperationRequest contendo os dados do saque. Retorna um AccountResponse com os detalhes da conta após o saque.")
    @ApiResponse(responseCode = "200", description = "Saque realizado com sucesso")
    @ApiResponse(responseCode = "400", description = "Requisição inválida, como valores inválidos, saldo insuficiente ou conta não encontrada")
    public ResponseEntity<AccountResponse> withdraw(@PathVariable UUID id,
            @RequestBody @Valid MoneyOperationRequest request) {
        return ResponseEntity.ok(billingService.withdraw(id, request));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN','TECNICO','CLIENTE')")
    @Operation(description = "Endpoint para consultar os detalhes de uma conta de cobrança, incluindo o saldo atual. Requer o ID da conta e retorna um AccountResponse com os detalhes da conta encontrada.")
    @ApiResponse(responseCode = "200", description = "Conta encontrada com sucesso")
    @ApiResponse(responseCode = "404", description = "Conta não encontrada com o ID fornecido")
    public ResponseEntity<AccountResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(billingService.findById(id));
    }
}