package com.caelum.chronos.modules.workorders.application.dto.response;

import java.math.BigDecimal;
import java.util.UUID;
import lombok.Builder;

@Builder
public record WorkOrderItemResponse(
    UUID id,
    UUID inventoryItemId,
    Integer quantity,
    BigDecimal unitPrice,
    BigDecimal totalPrice
) {}
