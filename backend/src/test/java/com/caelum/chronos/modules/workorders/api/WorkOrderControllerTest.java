package com.caelum.chronos.modules.workorders.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.caelum.chronos.modules.workorders.application.dto.request.WorkOrderCreateRequest;
import com.caelum.chronos.modules.workorders.application.dto.response.WorkOrderResponse;
import com.caelum.chronos.modules.workorders.application.service.WorkOrderService;
import com.caelum.chronos.modules.workorders.domain.enums.WorkOrderStatus;
import com.caelum.chronos.shared.api.error.GlobalExceptionHandler;

class WorkOrderControllerTest {

    private MockMvc mockMvc;
    private WorkOrderService workOrderService;

    @BeforeEach
    void setUp() {
        workOrderService = mock(WorkOrderService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new WorkOrderController(workOrderService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("Deve retornar 201 ao criar ordem")
    void deveCriarOrdem() throws Exception {
        UUID clientId = UUID.randomUUID();
        WorkOrderResponse response = WorkOrderResponse.builder()
                .id(UUID.randomUUID())
                .clientId(clientId)
                .status(WorkOrderStatus.OPEN)
                .description("Desc")
                .build();

        when(workOrderService.create(any(WorkOrderCreateRequest.class))).thenReturn(response);

        mockMvc.perform(post("/work-orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(String.format("{\"clientId\":\"%s\", \"description\":\"Desc\"}", clientId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.clientId").value(clientId.toString()))
                .andExpect(jsonPath("$.status").value("OPEN"));
    }

    @Test
    @DisplayName("Deve retornar 200 ao buscar por ID")
    void deveBuscarPorId() throws Exception {
        UUID woId = UUID.randomUUID();
        WorkOrderResponse response = WorkOrderResponse.builder()
                .id(woId)
                .clientId(UUID.randomUUID())
                .status(WorkOrderStatus.OPEN)
                .description("Desc")
                .build();

        when(workOrderService.findById(woId)).thenReturn(response);

        mockMvc.perform(get("/work-orders/{id}", woId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(woId.toString()));
    }
}
