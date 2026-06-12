package com.caelum.chronos.modules.workorders.api;

import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.caelum.chronos.modules.workorders.application.dto.request.WorkOrderCreateRequest;
import com.caelum.chronos.modules.workorders.application.dto.request.WorkOrderItemRequest;
import com.caelum.chronos.modules.workorders.application.dto.response.WorkOrderResponse;
import com.caelum.chronos.modules.workorders.application.service.WorkOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/work-orders")
@RequiredArgsConstructor
@Tag(name = "Work Orders", description = "Endpoints para gestão do ciclo de vida de ordens de serviço")
@PreAuthorize("isAuthenticated()")
public class WorkOrderController {

    private final WorkOrderService workOrderService;

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ADMIN', 'TECNICO', 'CLIENTE')")
    @Operation(summary = "Cria uma nova ordem de serviço")
    public ResponseEntity<WorkOrderResponse> create(@RequestBody @Valid WorkOrderCreateRequest request) {
        return ResponseEntity.status(201).body(workOrderService.create(request));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'TECNICO', 'CLIENTE')")
    @Operation(summary = "Busca uma ordem de serviço pelo ID")
    public ResponseEntity<WorkOrderResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(workOrderService.findById(id));
    }

    @PostMapping("/{id}/assign")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'TECNICO')")
    @Operation(summary = "Atribui uma ordem de serviço a um técnico")
    public ResponseEntity<WorkOrderResponse> assign(@PathVariable UUID id, @RequestParam UUID technicianId) {
        return ResponseEntity.ok(workOrderService.assign(id, technicianId));
    }

    @PostMapping("/{id}/start")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'TECNICO')")
    @Operation(summary = "Inicia a execução de uma ordem de serviço")
    public ResponseEntity<WorkOrderResponse> start(@PathVariable UUID id) {
        return ResponseEntity.ok(workOrderService.start(id));
    }

    @PostMapping("/{id}/complete")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'TECNICO')")
    @Operation(summary = "Finaliza uma ordem de serviço")
    public ResponseEntity<WorkOrderResponse> complete(@PathVariable UUID id) {
        return ResponseEntity.ok(workOrderService.complete(id));
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'TECNICO', 'CLIENTE')")
    @Operation(summary = "Cancela uma ordem de serviço")
    public ResponseEntity<WorkOrderResponse> cancel(@PathVariable UUID id, @RequestParam(required = false) String reason) {
        return ResponseEntity.ok(workOrderService.cancel(id, reason));
    }

    @PostMapping("/{id}/items")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'TECNICO')")
    @Operation(summary = "Adiciona um item (peça ou serviço) à ordem de serviço")
    public ResponseEntity<WorkOrderResponse> addItem(@PathVariable UUID id, @RequestBody @Valid WorkOrderItemRequest request) {
        return ResponseEntity.ok(workOrderService.addItem(id, request));
    }
}
