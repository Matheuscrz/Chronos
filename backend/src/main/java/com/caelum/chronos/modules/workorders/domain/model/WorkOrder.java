package com.caelum.chronos.modules.workorders.domain.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import com.caelum.chronos.modules.workorders.domain.enums.WorkOrderStatus;
import com.caelum.chronos.shared.domain.BaseEntity;
import com.caelum.chronos.shared.exception.BusinessException;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "work_orders")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class WorkOrder extends BaseEntity {

    @Column(name = "client_id", nullable = false)
    private UUID clientId;

    @Column(name = "technician_id")
    private UUID technicianId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    @Builder.Default
    private WorkOrderStatus status = WorkOrderStatus.OPEN;

    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "cancel_reason")
    private String cancelReason;

    @OneToMany(mappedBy = "workOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<WorkOrderItem> items = new ArrayList<>();

    public void assign(UUID technicianId) {
        if (this.status != WorkOrderStatus.OPEN) {
            throw new BusinessException("Apenas ordens no status OPEN podem ser atribuídas.");
        }
        if (technicianId == null) {
            throw new BusinessException("ID do técnico é obrigatório para atribuição.");
        }
        this.technicianId = technicianId;
        this.status = WorkOrderStatus.ASSIGNED;
    }

    public void start() {
        if (this.status != WorkOrderStatus.ASSIGNED) {
            throw new BusinessException("Apenas ordens no status ASSIGNED podem ser iniciadas.");
        }
        this.status = WorkOrderStatus.IN_PROGRESS;
    }

    public void complete() {
        if (this.status != WorkOrderStatus.IN_PROGRESS) {
            throw new BusinessException("Apenas ordens no status IN_PROGRESS podem ser concluídas.");
        }
        this.status = WorkOrderStatus.COMPLETED;
        this.completedAt = Instant.now();
    }

    public void cancel(String reason) {
        if (this.status == WorkOrderStatus.COMPLETED) {
            throw new BusinessException("Ordens concluídas não podem ser canceladas.");
        }
        this.status = WorkOrderStatus.CANCELED;
        this.cancelReason = reason;
    }

    public void addItem(WorkOrderItem item) {
        if (this.status == WorkOrderStatus.COMPLETED || this.status == WorkOrderStatus.CANCELED) {
            throw new BusinessException("Não é possível adicionar itens a uma ordem finalizada.");
        }
        this.items.add(item);
        item.setWorkOrder(this);
    }

    public List<WorkOrderItem> getItems() {
        return Collections.unmodifiableList(items);
    }
}
