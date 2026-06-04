# Eventos de Integração

Este documento detalha os principais eventos de integração publicados pelos Bounded Contexts para permitir a comunicação assíncrona.

Todos os eventos são publicados utilizando o **Outbox Pattern** para garantir a entrega atômica.

---

| Evento               | Contexto de Origem | Contexto(s) Consumidor(es)          | Descrição                                                              |
| -------------------- | ------------------ | ----------------------------------- | ---------------------------------------------------------------------- |
| `WorkOrderCreated`   | `workorders`       | `notifications`, `audit`            | Disparado quando uma nova ordem de serviço é criada.                   |
| `WorkOrderAssigned`  | `workorders`       | `notifications`, `audit`            | Disparado quando um técnico é atribuído a uma ordem de serviço.        |
| `WorkOrderStarted`   | `workorders`       | `notifications`, `audit`            | Disparado quando o técnico inicia o atendimento da ordem de serviço.   |
| `WorkOrderCompleted` | `workorders`       | `billing`, `notifications`, `audit` | Disparado quando uma ordem de serviço é concluída. Inicia a cobrança.  |
| `InvoiceGenerated`   | `billing`          | `notifications`, `audit`            | Disparado quando uma fatura é gerada a partir de uma ordem de serviço. |
| `InvoicePaid`        | `billing`          | `notifications`, `audit`            | Disparado quando uma fatura é paga pelo cliente.                       |
| `FileUploaded`       | `files`            | `workorders`, `audit`               | Disparado após um arquivo ser carregado com sucesso no MinIO.          |

---

## Estrutura de Exemplo de um Evento

Os eventos seguirão uma estrutura padronizada, contendo metadados e o payload de dados.

**Exemplo: `WorkOrderCompleted`**

```json
{
  "eventId": "uuid-for-this-event",
  "eventType": "WorkOrderCompleted",
  "timestamp": "2026-06-04T14:30:00Z",
  "version": "1.0",
  "correlationId": "uuid-for-the-request",
  "payload": {
    "workOrderId": "uuid-of-the-workorder",
    "clientId": "uuid-of-the-client",
    "technicianId": "uuid-of-the-technician",
    "completedAt": "2026-06-04T14:25:10Z",
    "totalAmount": "150.75",
    "currency": "BRL",
    "items": [
      {
        "description": "Troca de filtro de ar",
        "amount": "150.75"
      }
    ]
  }
}
```
