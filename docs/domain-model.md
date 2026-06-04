# Modelo de Domínio

Este documento descreve os principais agregados, entidades e objetos de valor para cada Bounded Context do sistema Chronos.

---

## Contexto: `workorders`

O contexto de Ordens de Serviço é o Core Domain do sistema.

### Agregado: `WorkOrder`

A Ordem de Serviço é a raiz de agregação e representa uma solicitação de trabalho completa, desde sua criação até a conclusão.

- **Raiz do Agregado:** `WorkOrder`

#### Entidades dentro do Agregado

- **`WorkOrder` (Raiz):**
  - `id`: `WorkOrderId` (VO) - Identificador único.
  - `clientId`: `UserId` (VO) - Cliente que solicitou o serviço.
  - `technicianId`: `UserId` (VO) - Técnico atribuído.
  - `status`: `WorkOrderStatus` (Enum/VO) - Estado atual da ordem (e.g., OPEN, ASSIGNED, COMPLETED).
  - `description`: `String` - Descrição do problema.
  - `createdAt`: `Instant` - Data de criação.
  - `completedAt`: `Instant` (opcional) - Data de conclusão.
  - `items`: `List<WorkOrderItem>` - Lista de itens de serviço ou peças.

- **`WorkOrderItem`:**
  - `id`: `WorkOrderItemId` (VO).
  - `inventoryItemId`: `InventoryItemId` (VO) - ID do item de inventário usado.
  - `quantity`: `int` - Quantidade utilizada.
  - `price`: `BigDecimal` - Preço unitário no momento do uso.

#### Objetos de Valor (Value Objects - VOs)

- `WorkOrderId`: Garante que o ID de uma ordem de serviço seja tratado como um tipo específico, e não um simples `UUID` ou `Long`.
- `UserId`: Representa a identidade de um usuário (seja cliente ou técnico) vinda do contexto `users`.
- `InventoryItemId`: Representa a identidade de um item de estoque vindo do contexto `inventory`.

#### Invariantes (Regras de Negócio)

O agregado `WorkOrder` é responsável por garantir as seguintes regras:

1.  Uma ordem de serviço só pode ser atribuída a um técnico se estiver no status `OPEN`.
2.  Uma ordem de serviço só pode ser iniciada (`IN_PROGRESS`) se estiver `ASSIGNED`.
3.  Uma ordem de serviço só pode ser concluída (`COMPLETED`) se estiver `IN_PROGRESS`.
4.  Uma ordem de serviço `COMPLETED` ou `CANCELED` não pode mais ser alterada.
5.  O `technicianId` não pode ser nulo quando o status for `ASSIGNED` ou superior.

---

## Contexto: `billing`

### Agregado: `Account`

Representa a conta financeira de um usuário (cliente ou técnico) no sistema.

- **Raiz do Agregado:** `Account`
  - `id`: `AccountId` (VO)
  - `userId`: `UserId` (VO)
  - `balance`: `Money` (VO) - Saldo atual.
  - `transactions`: `List<Transaction>` (Entidade)

_(Esta seção será detalhada futuramente)_
