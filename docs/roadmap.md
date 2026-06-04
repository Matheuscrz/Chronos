# Roadmap do Projeto Chronos

## Objetivo

Este roadmap foi montado para guiar o desenvolvimento do Chronos, um projeto de portfólio que visa demonstrar a construção de um sistema backend moderno, do zero até a produção, utilizando as melhores práticas de engenharia de software.

A ideia central é construir um único sistema, o **Chronos**, de forma incremental. Cada etapa do roadmap adiciona uma camada real ao mesmo sistema, contando uma história técnica completa: do modelo de domínio até o pipeline de deploy, passando por concorrência, eventos, resiliência e observabilidade.

---

## O sistema: Chronos

**Chronos** é um sistema de gestão de ordens de serviço para empresas técnicas. Ele substitui controles manuais no controle de atendimentos, equipamentos e cobranças.

Este domínio foi escolhido por quatro razões:

1.  É simples o suficiente para começar rápido.
2.  Tem problemas reais de concorrência, consistência e eventos.
3.  Evolui naturalmente para DDD, arquitetura orientada a eventos e resiliência.
4.  Representa um produto real com mercado — não é um CRUD inventado.

### Contextos do sistema

| Contexto        | Responsabilidade                                  |
| --------------- | ------------------------------------------------- |
| `auth`          | autenticação via Keycloak, tokens, blacklist      |
| `users`         | cadastro de técnicos, clientes e administradores  |
| `workorders`    | ordens de serviço, status, atribuição             |
| `billing`       | saldo de conta, cobranças, movimentações          |
| `inventory`     | peças e equipamentos usados nas ordens            |
| `files`         | upload e download de fotos e documentos via MinIO |
| `notifications` | envio assíncrono de e-mails e alertas             |
| `audit`         | histórico de todas as ações críticas do sistema   |

---

## Estrutura base do repositório

O projeto seguirá a abordagem de Monólito Modular com Arquitetura Hexagonal.

```
chronos/
├── modules/
│   ├── auth/
│   ├── users/
│   ├── workorders/
│   ├── billing/
│   ├── inventory/
│   ├── files/
│   ├── notifications/
│   └── audit/
│
├── shared/
│
├── infrastructure/
│
└── bootstrap/
```

> **Regra:** nenhum módulo importa diretamente outro módulo. A comunicação é via eventos de integração ou via chamada de `application service` exposta como interface.

---

# Roadmap por etapas

## Etapa 1 — Fundação: Domínio, Concorrência e Consistência

**Foco:** Implementar o núcleo do contexto `billing` e `users`, garantindo o comportamento correto sob concorrência com testes robustos.

**Tecnologias:** Java 21, Spring Boot, PostgreSQL, Testcontainers, Virtual Threads.

**Entregáveis:**

- Contextos `billing` e `users` funcionais.
- Endpoints para criar conta, depositar, sacar e transferir.
- Testes de concorrência com locks pessimistas, otimistas e updates atômicos.

## Etapa 2 — Autenticação e Autorização

**Foco:** Proteger todos os endpoints com autenticação e autorização baseada em papéis (RBAC).

**Tecnologias:** Spring Security, Keycloak, OAuth2/OIDC, Redis.

**Entregáveis:**

- Contexto `auth` com login, refresh e logout.
- Endpoints protegidos com papéis (ADMIN, TECNICO, CLIENTE).
- Blacklist de tokens JWT implementada com Redis.

## Etapa 3 — Ordens de Serviço e Núcleo do Produto

**Foco:** Desenvolver o ciclo de vida completo das ordens de serviço, aplicando os princípios do DDD Tático.

**Tecnologias:** DDD (Agregados, Entidades, VOs), Arquitetura Hexagonal.

**Entregáveis:**

- Contexto `workorders` com a gestão de status (ABERTA, ATRIBUIDA, CONCLUIDA, etc.).
- Endpoints para todo o ciclo de vida da ordem de serviço.
- Publicação de eventos de domínio internos (ex: `OrderCompleted`).

## Etapa 4 — Arquitetura Orientada a Eventos

**Foco:** Desacoplar os contextos `workorders` e `billing` usando comunicação assíncrona.

**Tecnologias:** RabbitMQ, Outbox Pattern.

**Entregáveis:**

- Implementação do Outbox Pattern para garantir a entrega de eventos.
- Consumidor idempotente no módulo `billing` que reage ao evento `WorkOrderCompleted`.
- Configuração de DLQ (Dead Letter Queue) para tratamento de falhas.
- Implementação de uma saga para compensação em caso de falha na cobrança.

## Etapa 5 — Cache e Rate Limiting com Redis

**Foco:** Melhorar a performance e a resiliência da aplicação utilizando Redis para cache e limitação de requisições.

**Tecnologias:** Redis.

**Entregáveis:**

- Cache em listagens e detalhes de ordens de serviço.
- Rate Limiting por IP e por usuário.
- Estratégia de invalidação e reconstrução de cache.

## Etapa 6 — Segurança de Aplicação e WAF

**Foco:** Endurecer a segurança da aplicação seguindo as recomendações da OWASP e preparando a infraestrutura de borda.

**Tecnologias:** Nginx, Cloudflare (planejamento).

**Entregáveis:**

- Revisão de todos os endpoints contra o OWASP Top 10.
- Configuração de headers de segurança (HSTS, CSP, etc.) no Nginx.
- Uso de UUIDs para evitar enumeração de recursos.

## Etapa 7 — Resiliência

**Foco:** Tornar o sistema resiliente a falhas em suas dependências externas.

**Tecnologias:** Resilience4j.

**Entregáveis:**

- Implementação de Circuit Breakers, Retries e Timeouts nas integrações.
- Estratégias de fallback para quando Redis, RabbitMQ ou MinIO estiverem indisponíveis.
- Testes que simulam falhas e validam o comportamento de degradação graciosa.

## Etapa 8 — Armazenamento de Arquivos

**Foco:** Implementar o upload e download de arquivos associados às ordens de serviço.

**Tecnologias:** MinIO (S3-compatible).

**Entregáveis:**

- Contexto `files` para gestão de metadados de arquivos.
- Endpoints para upload e para geração de URLs pré-assinadas para download.
- Integração com o MinIO para armazenamento de objetos.

## Etapa 9 — Observabilidade

**Foco:** Implementar uma stack completa de observabilidade para monitorar o sistema em produção.

**Tecnologias:** OpenTelemetry, Prometheus, Grafana, Loki, Tempo.

**Entregáveis:**

- Logs estruturados em JSON com `correlation ID`.
- Métricas de negócio e de performance.
- Traces distribuídos que perpassam APIs, filas e consumidores.
- Dashboards no Grafana e alertas configurados.

## Etapa 10 — Testes Abrangentes

**Foco:** Garantir a qualidade e a confiabilidade do sistema com uma estratégia de testes completa.

**Tecnologias:** JUnit, Testcontainers, k6/Gatling.

**Entregáveis:**

- Cobertura de testes unitários para o domínio.
- Testes de integração com banco de dados e outros serviços reais.
- Testes de carga e concorrência para os fluxos críticos.

## Etapa 11 — CI/CD, Deploy e Rollback

**Foco:** Automatizar o processo de entrega do software em produção.

**Tecnologias:** Docker, Docker Swarm, GitHub Actions.

**Entregáveis:**

- Pipeline de CI/CD no GitHub Actions (testes, build, push de imagem).
- Estratégia de deploy com rolling update no Docker Swarm.
- Script e documentação para rollback de versão.

## Etapa 12 — Qualidade de Código

**Foco:** Manter a saúde do código-fonte a longo prazo.

**Tecnologias:** SonarQube, OWASP Dependency Check.

**Entregáveis:**

- Análise estática de código integrada ao pipeline.
- Monitoramento de dependências vulneráveis.

## Etapa 13 — Escalabilidade Avançada

**Foco:** Explorar padrões de arquitetura para escalar partes específicas do sistema.

**Tecnologias:** CQRS.

**Entregáveis:**

- Aplicação do padrão CQRS no contexto de `workorders` com um `read model` otimizado para consultas.
- ADR (Architecture Decision Record) documentando a decisão.

## Etapa 14 — Frontend Mínimo

**Foco:** Criar uma interface de usuário simples para demonstrar o backend.

**Tecnologias:** React/Vue (a definir).

**Entregáveis:**

- SPA (Single Page Application) para login, visualização e criação de ordens de serviço.

## Etapa 15 — Portfólio no Ar

**Foco:** Colocar o sistema completo em produção em um ambiente real.

**Tecnologias:** VPS, Cloudflare, Nginx.

**Entregáveis:**

- Sistema acessível publicamente com domínio próprio e HTTPS.
- Cloudflare configurado com WAF e outras proteções de borda.
- Documentação finalizada e README atualizado.

---

## Ordem recomendada de execução

### Fase A — Fundação (Etapas 1 a 3)

1. Domínio, concorrência e consistência
2. Autenticação e autorização
3. Ordens de serviço e DDD

### Fase B — Consistência distribuída (Etapas 4 a 6)

4. Arquitetura orientada a eventos
5. Redis profissional
6. Segurança e WAF

### Fase C — Robustez (Etapas 7 a 10)

7. Resiliência
8. Arquivos com MinIO
9. Observabilidade
10. Testes

### Fase D — Maturidade de entrega (Etapas 11 a 13)

11. CI/CD, deploy e rollback
12. Qualidade de código
13. Escalabilidade avançada

### Fase E — Exposição pública (Etapas 14 e 15)

14. Frontend mínimo
15. Portfólio no ar

---

## Resultado esperado no final

Quando terminar essa trilha, você vai conseguir:

- desenvolver backend Java com segurança e maturidade
- estruturar monólito modular com bounded contexts reais
- proteger APIs com Keycloak, JWT e WAF
- lidar com concorrência real sem corrupção de dados
- usar Redis com inteligência: cache, rate limit, blacklist e locks
- aplicar arquitetura orientada a eventos com outbox e idempotência
- fazer fallback e resiliência sem travar o sistema
- observar o sistema em produção com logs, métricas e traces
- testar com confiança: unitário, integração, carga e concorrência
- fazer deploy com rollback automatizado
- colocar o portfólio no ar com Cloudflare e VPS própria

---

## Regra final de estudo

Não tente aprender tudo de uma vez.
Estude por problema, por consequência real, por camada do sistema.

A pergunta certa não é:

> "qual tecnologia estudar agora?"

A pergunta certa é:

> "qual falha eu quero aprender a impedir, detectar ou recuperar no Chronos?"

Esse é o salto para backend pleno forte.
