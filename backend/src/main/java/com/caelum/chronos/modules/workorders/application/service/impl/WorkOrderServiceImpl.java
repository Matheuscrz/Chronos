package com.caelum.chronos.modules.workorders.application.service.impl;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.caelum.chronos.modules.workorders.application.dto.request.WorkOrderCreateRequest;
import com.caelum.chronos.modules.workorders.application.dto.request.WorkOrderItemRequest;
import com.caelum.chronos.modules.workorders.application.dto.response.WorkOrderItemResponse;
import com.caelum.chronos.modules.workorders.application.dto.response.WorkOrderResponse;
import com.caelum.chronos.modules.workorders.application.service.WorkOrderService;
import com.caelum.chronos.modules.workorders.domain.events.WorkOrderCompletedEvent;
import com.caelum.chronos.modules.workorders.domain.model.WorkOrder;
import com.caelum.chronos.modules.workorders.domain.model.WorkOrderItem;
import com.caelum.chronos.modules.workorders.infra.repository.WorkOrderRepository;
import com.caelum.chronos.shared.exception.NotFoundException;
import com.caelum.chronos.shared.infra.messaging.outbox.OutboxService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class WorkOrderServiceImpl implements WorkOrderService {

    private final WorkOrderRepository workOrderRepository;
    private final OutboxService outboxService;

    @Override
    @Transactional
    public WorkOrderResponse create(WorkOrderCreateRequest request) {
        WorkOrder workOrder = WorkOrder.builder()
                .clientId(request.clientId())
                .description(request.description())
                .build();
        
        workOrder = workOrderRepository.save(workOrder);
        return toResponse(workOrder);
    }

    @Override
    @Transactional(readOnly = true)
    public WorkOrderResponse findById(UUID id) {
        return workOrderRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new NotFoundException("Ordem de serviço não encontrada"));
    }

    @Override
    @Transactional
    public WorkOrderResponse assign(UUID id, UUID technicianId) {
        WorkOrder workOrder = workOrderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Ordem de serviço não encontrada"));
        
        workOrder.assign(technicianId);
        return toResponse(workOrderRepository.save(workOrder));
    }

    @Override
    @Transactional
    public WorkOrderResponse start(UUID id) {
        WorkOrder workOrder = workOrderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Ordem de serviço não encontrada"));
        
        workOrder.start();
        return toResponse(workOrderRepository.save(workOrder));
    }

    @Override
    @Transactional
    public WorkOrderResponse complete(UUID id) {
        WorkOrder workOrder = workOrderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Ordem de serviço não encontrada"));
        
        workOrder.complete();
        workOrder = workOrderRepository.save(workOrder);

        publishCompletedEvent(workOrder);

        return toResponse(workOrder);
    }

    private void publishCompletedEvent(WorkOrder workOrder) {
        BigDecimal totalAmount = workOrder.getItems().stream()
                .map(WorkOrderItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        var eventItems = workOrder.getItems().stream()
                .map(item -> new WorkOrderCompletedEvent.Item(
                        item.getInventoryItemId(),
                        item.getQuantity(),
                        item.getUnitPrice()))
                .collect(Collectors.toList());

        WorkOrderCompletedEvent event = new WorkOrderCompletedEvent(
                workOrder.getId(),
                workOrder.getClientId(),
                totalAmount,
                workOrder.getCompletedAt(),
                eventItems);

        outboxService.saveEvent(
                workOrder.getId(),
                "WorkOrder",
                "WorkOrderCompleted",
                event);
    }

    @Override
    @Transactional
    public WorkOrderResponse cancel(UUID id, String reason) {
        WorkOrder workOrder = workOrderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Ordem de serviço não encontrada"));
        
        workOrder.cancel(reason);
        return toResponse(workOrderRepository.save(workOrder));
    }

    @Override
    @Transactional
    public WorkOrderResponse addItem(UUID id, WorkOrderItemRequest request) {
        WorkOrder workOrder = workOrderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Ordem de serviço não encontrada"));
        
        WorkOrderItem item = WorkOrderItem.builder()
                .inventoryItemId(request.inventoryItemId())
                .quantity(request.quantity())
                .unitPrice(request.unitPrice())
                .build();
        
        workOrder.addItem(item);
        return toResponse(workOrderRepository.save(workOrder));
    }

    private WorkOrderResponse toResponse(WorkOrder workOrder) {
        var items = workOrder.getItems().stream()
                .map(item -> WorkOrderItemResponse.builder()
                        .id(item.getId())
                        .inventoryItemId(item.getInventoryItemId())
                        .quantity(item.getQuantity())
                        .unitPrice(item.getUnitPrice())
                        .totalPrice(item.getTotalPrice())
                        .build())
                .collect(Collectors.toList());

        BigDecimal totalItemsPrice = workOrder.getItems().stream()
                .map(item -> item.getTotalPrice())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return WorkOrderResponse.builder()
                .id(workOrder.getId())
                .clientId(workOrder.getClientId())
                .technicianId(workOrder.getTechnicianId())
                .status(workOrder.getStatus())
                .description(workOrder.getDescription())
                .completedAt(workOrder.getCompletedAt())
                .cancelReason(workOrder.getCancelReason())
                .createdAt(workOrder.getCreatedAt())
                .items(items)
                .totalPrice(totalItemsPrice)
                .build();
    }
}
