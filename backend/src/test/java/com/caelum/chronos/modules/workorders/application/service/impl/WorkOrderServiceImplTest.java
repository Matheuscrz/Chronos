package com.caelum.chronos.modules.workorders.application.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.caelum.chronos.modules.workorders.application.dto.request.WorkOrderCreateRequest;
import com.caelum.chronos.modules.workorders.application.dto.response.WorkOrderResponse;
import com.caelum.chronos.modules.workorders.domain.enums.WorkOrderStatus;
import com.caelum.chronos.modules.workorders.domain.model.WorkOrder;
import com.caelum.chronos.modules.workorders.infra.repository.WorkOrderRepository;

class WorkOrderServiceImplTest {

    @Mock
    private WorkOrderRepository workOrderRepository;

    @InjectMocks
    private WorkOrderServiceImpl workOrderService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("Deve criar uma nova ordem de serviço")
    void deveCriarOrdem() {
        UUID clientId = UUID.randomUUID();
        WorkOrderCreateRequest request = new WorkOrderCreateRequest(clientId, "Desc");
        
        WorkOrder wo = WorkOrder.builder()
                .clientId(clientId)
                .description("Desc")
                .build();
        
        when(workOrderRepository.save(any(WorkOrder.class))).thenReturn(wo);

        WorkOrderResponse response = workOrderService.create(request);

        assertNotNull(response);
        assertEquals(clientId, response.clientId());
        assertEquals(WorkOrderStatus.OPEN, response.status());
        verify(workOrderRepository).save(any(WorkOrder.class));
    }

    @Test
    @DisplayName("Deve atribuir um técnico com sucesso")
    void deveAtribuirTecnico() {
        UUID woId = UUID.randomUUID();
        UUID techId = UUID.randomUUID();
        
        WorkOrder wo = WorkOrder.builder()
                .clientId(UUID.randomUUID())
                .description("Desc")
                .build();
        
        when(workOrderRepository.findById(woId)).thenReturn(Optional.of(wo));
        when(workOrderRepository.save(any(WorkOrder.class))).thenAnswer(i -> i.getArgument(0));

        WorkOrderResponse response = workOrderService.assign(woId, techId);

        assertEquals(WorkOrderStatus.ASSIGNED, response.status());
        assertEquals(techId, response.technicianId());
    }
}
