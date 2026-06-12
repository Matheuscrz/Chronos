package com.caelum.chronos.modules.workorders.application.dto.request;

import java.util.UUID;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record WorkOrderCreateRequest(
    @NotNull(message = "ID do cliente é obrigatório")
    UUID clientId,
    
    @NotBlank(message = "Descrição é obrigatória")
    String description
) {}
