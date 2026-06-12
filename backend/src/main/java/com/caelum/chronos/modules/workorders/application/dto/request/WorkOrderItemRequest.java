package com.caelum.chronos.modules.workorders.application.dto.request;

import java.math.BigDecimal;
import java.util.UUID;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record WorkOrderItemRequest(
    @NotNull(message = "ID do item de inventário é obrigatório")
    UUID inventoryItemId,
    
    @NotNull(message = "Quantidade é obrigatória")
    @Min(value = 1, message = "Quantidade deve ser pelo menos 1")
    Integer quantity,
    
    @NotNull(message = "Preço unitário é obrigatório")
    @DecimalMin(value = "0.01", message = "Preço unitário deve ser maior que zero")
    BigDecimal unitPrice
) {}
