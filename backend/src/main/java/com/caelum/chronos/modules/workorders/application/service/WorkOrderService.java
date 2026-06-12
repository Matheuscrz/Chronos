package com.caelum.chronos.modules.workorders.application.service;

import java.util.UUID;
import com.caelum.chronos.modules.workorders.application.dto.request.WorkOrderCreateRequest;
import com.caelum.chronos.modules.workorders.application.dto.request.WorkOrderItemRequest;
import com.caelum.chronos.modules.workorders.application.dto.response.WorkOrderResponse;

public interface WorkOrderService {
    WorkOrderResponse create(WorkOrderCreateRequest request);
    WorkOrderResponse findById(UUID id);
    WorkOrderResponse assign(UUID id, UUID technicianId);
    WorkOrderResponse start(UUID id);
    WorkOrderResponse complete(UUID id);
    WorkOrderResponse cancel(UUID id, String reason);
    WorkOrderResponse addItem(UUID id, WorkOrderItemRequest request);
}
