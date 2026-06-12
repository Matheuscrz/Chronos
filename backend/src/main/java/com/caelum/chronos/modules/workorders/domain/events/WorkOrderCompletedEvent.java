package com.caelum.chronos.modules.workorders.domain.events;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record WorkOrderCompletedEvent(
    UUID workOrderId,
    UUID clientId,
    BigDecimal totalAmount,
    Instant completedAt,
    List<Item> items
) {
    public record Item(
        UUID inventoryItemId,
        Integer quantity,
        BigDecimal unitPrice
    ) {}
}
