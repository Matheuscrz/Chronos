package com.caelum.chronos.modules.workorders.application.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import com.caelum.chronos.modules.workorders.domain.enums.WorkOrderStatus;
import lombok.Builder;

@Builder
public record WorkOrderResponse(
    UUID id,
    UUID clientId,
    UUID technicianId,
    WorkOrderStatus status,
    String description,
    Instant completedAt,
    String cancelReason,
    List<WorkOrderItemResponse> items,
    BigDecimal totalPrice,
    Instant createdAt
) {}
