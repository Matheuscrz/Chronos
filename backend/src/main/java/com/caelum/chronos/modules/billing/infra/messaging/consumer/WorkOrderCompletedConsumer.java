package com.caelum.chronos.modules.billing.infra.messaging.consumer;

import java.io.IOException;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.caelum.chronos.modules.billing.application.dto.request.MoneyOperationRequest;
import com.caelum.chronos.modules.billing.application.service.BillingService;
import com.caelum.chronos.modules.billing.domain.model.BillingAccount;
import com.caelum.chronos.modules.billing.infra.BillingAccountRepository;
import com.caelum.chronos.modules.workorders.domain.events.WorkOrderCompletedEvent;
import com.caelum.chronos.shared.application.service.NotificationService;
import com.caelum.chronos.shared.infra.config.messaging.RabbitMQConfig;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class WorkOrderCompletedConsumer {

    private final BillingService billingService;
    private final BillingAccountRepository billingAccountRepository;
    private final ObjectMapper objectMapper;
    private final NotificationService notificationService;

    @RabbitListener(queues = RabbitMQConfig.WORK_ORDER_COMPLETED_QUEUE)
    public void consume(String payload) throws IOException {
        log.info("Received WorkOrderCompleted event: {}", payload);
        
        WorkOrderCompletedEvent event = objectMapper.readValue(payload, WorkOrderCompletedEvent.class);
        
        try {
            // Buscar conta pelo proprietário (clientId)
            BillingAccount account = billingAccountRepository.findByOwner_Id(event.clientId())
                    .orElseThrow(() -> new RuntimeException("Billing account not found for client: " + event.clientId()));

            // Realizar o "saque" automático da conta (cobrança)
            billingService.withdraw(account.getId(), new MoneyOperationRequest(event.totalAmount()));
            
            log.info("Successfully processed billing for WorkOrder: {}", event.workOrderId());

            // Enviar notificação em tempo real via WebSocket
            notificationService.sendNotification("/topic/workorders/" + event.clientId(), 
                    "Faturamento processado para a Ordem de Serviço: " + event.workOrderId());

        } catch (Exception e) {
            log.error("Failed to process billing for WorkOrder: {}", event.workOrderId(), e);
            // Ao lançar a exceção, o RabbitMQ fará o retry baseado na configuração do application.yml
            // Se atingir o limite, enviará para a DLQ configurada no RabbitMQConfig.
            throw e;
        }
    }
}
