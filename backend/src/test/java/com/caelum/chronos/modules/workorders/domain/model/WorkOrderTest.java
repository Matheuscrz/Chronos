package com.caelum.chronos.modules.workorders.domain.model;

import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.caelum.chronos.modules.workorders.domain.enums.WorkOrderStatus;
import com.caelum.chronos.shared.exception.BusinessException;

class WorkOrderTest {

    @Test
    @DisplayName("Deve iniciar com status OPEN")
    void deveIniciarComStatusOpen() {
        WorkOrder wo = WorkOrder.builder()
                .clientId(UUID.randomUUID())
                .description("Teste")
                .build();

        assertEquals(WorkOrderStatus.OPEN, wo.getStatus());
        assertNull(wo.getTechnicianId());
    }

    @Test
    @DisplayName("Deve atribuir técnico e mudar status para ASSIGNED")
    void deveAtribuirTecnico() {
        WorkOrder wo = WorkOrder.builder()
                .clientId(UUID.randomUUID())
                .description("Teste")
                .build();

        UUID techId = UUID.randomUUID();
        wo.assign(techId);

        assertEquals(WorkOrderStatus.ASSIGNED, wo.getStatus());
        assertEquals(techId, wo.getTechnicianId());
    }

    @Test
    @DisplayName("Deve falhar ao atribuir se não estiver OPEN")
    void deveFalharAtribuicaoSeNaoEstiverOpen() {
        WorkOrder wo = WorkOrder.builder()
                .clientId(UUID.randomUUID())
                .description("Teste")
                .status(WorkOrderStatus.IN_PROGRESS)
                .build();

        assertThrows(BusinessException.class, () -> wo.assign(UUID.randomUUID()));
    }

    @Test
    @DisplayName("Deve iniciar execução e mudar para IN_PROGRESS")
    void deveIniciarExecucao() {
        WorkOrder wo = WorkOrder.builder()
                .clientId(UUID.randomUUID())
                .description("Teste")
                .status(WorkOrderStatus.ASSIGNED)
                .build();

        wo.start();

        assertEquals(WorkOrderStatus.IN_PROGRESS, wo.getStatus());
    }

    @Test
    @DisplayName("Deve concluir e definir data de conclusão")
    void deveConcluir() {
        WorkOrder wo = WorkOrder.builder()
                .clientId(UUID.randomUUID())
                .description("Teste")
                .status(WorkOrderStatus.IN_PROGRESS)
                .build();

        wo.complete();

        assertEquals(WorkOrderStatus.COMPLETED, wo.getStatus());
        assertNotNull(wo.getCompletedAt());
    }

    @Test
    @DisplayName("Deve cancelar se não estiver concluída")
    void deveCancelar() {
        WorkOrder wo = WorkOrder.builder()
                .clientId(UUID.randomUUID())
                .description("Teste")
                .status(WorkOrderStatus.OPEN)
                .build();

        wo.cancel("Motivo do cancelamento");

        assertEquals(WorkOrderStatus.CANCELED, wo.getStatus());
        assertEquals("Motivo do cancelamento", wo.getCancelReason());
    }
}
