package com.caelum.chronos.modules.workorders.application.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;

import com.caelum.chronos.backend.BackendApplication;
import com.caelum.chronos.modules.workorders.application.dto.request.WorkOrderCreateRequest;
import com.caelum.chronos.modules.workorders.application.dto.response.WorkOrderResponse;
import com.caelum.chronos.modules.workorders.application.service.WorkOrderService;
import com.caelum.chronos.modules.workorders.domain.enums.WorkOrderStatus;
import com.caelum.chronos.modules.workorders.domain.model.WorkOrder;
import com.caelum.chronos.modules.workorders.infra.repository.WorkOrderRepository;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientAutoConfiguration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;

@SpringBootTest(classes = BackendApplication.class)
@ActiveProfiles("test")
@TestPropertySource(properties = {"management.health.redis.enabled=false"})
@EnableAutoConfiguration(exclude = OAuth2ClientAutoConfiguration.class)
class WorkOrderServiceCacheTest {

    @Autowired
    private WorkOrderService workOrderService;

    @MockBean
    private WorkOrderRepository workOrderRepository;

    @MockBean
    private ClientRegistrationRepository clientRegistrationRepository;

    @MockBean
    private RedisConnectionFactory redisConnectionFactory; // Simular falha de conexão Redis

    @MockBean
    private ReactiveRedisConnectionFactory reactiveRedisConnectionFactory; // Previne erro do actuator

    @Test
    @DisplayName("Deve fazer fallback para o banco de dados (Postgres) se o cache (Redis) falhar")
    void deveFazerFallbackParaPostgresSeRedisFalhar() {
        UUID woId = UUID.randomUUID();
        WorkOrder workOrder = WorkOrder.builder()
                .clientId(UUID.randomUUID())
                .description("Teste de Fallback")
                .status(WorkOrderStatus.OPEN)
                .build();
        
        // Mock DB behavior
        when(workOrderRepository.findById(woId)).thenReturn(Optional.of(workOrder));
        
        // Simular falha na conexão do Redis!
        when(redisConnectionFactory.getConnection()).thenThrow(new RedisConnectionFailureException("Redis down"));

        // O erro do Redis deve ser ignorado pelo nosso CustomCacheErrorHandler, e a consulta deve ir para o DB
        WorkOrderResponse response = workOrderService.findById(woId);

        assertThat(response).isNotNull();
        assertThat(response.description()).isEqualTo("Teste de Fallback");
        
        // Verifica se realmente buscou do banco
        verify(workOrderRepository, times(1)).findById(woId);
    }
}
