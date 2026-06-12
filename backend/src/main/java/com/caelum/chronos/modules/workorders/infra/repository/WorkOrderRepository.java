package com.caelum.chronos.modules.workorders.infra.repository;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import com.caelum.chronos.modules.workorders.domain.model.WorkOrder;

public interface WorkOrderRepository extends JpaRepository<WorkOrder, UUID> {
}
