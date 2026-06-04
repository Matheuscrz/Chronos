# Chronos

> Plataforma moderna de gestão operacional desenvolvida para demonstrar práticas avançadas de Engenharia de Software Backend utilizando Java 21, Spring Boot, DDD, Arquitetura Hexagonal, Event-Driven Architecture, Observabilidade e DevOps.

---

## Objetivo

O Chronos é um projeto de portfólio criado para consolidar conhecimentos avançados de desenvolvimento backend e arquitetura de software.

O sistema será desenvolvido incrementalmente, evoluindo de um monólito modular para uma plataforma altamente observável, resiliente e preparada para execução em ambientes produtivos.

O foco principal é demonstrar domínio de:

- Domain-Driven Design (DDD)
- Bounded Contexts
- Arquitetura Hexagonal
- Event-Driven Architecture
- PostgreSQL
- Redis
- RabbitMQ
- MinIO
- Keycloak
- OpenTelemetry
- Prometheus
- Grafana
- Loki
- Docker
- Docker Swarm
- GitHub Actions
- Cloudflare
- WAF

---

# Arquitetura

O projeto segue a abordagem:

Monólito Modular + DDD + Arquitetura Hexagonal

Cada módulo possui seu próprio domínio, casos de uso, persistência e interfaces.

```text
chronos
│
├── modules
│   ├── auth
│   ├── users
│   ├── workorders
│   ├── inventory
│   ├── billing
│   ├── notifications
│   ├── files
│   └── audit
│
├── shared
│
├── infrastructure
│
└── bootstrap
```

---

# Stack Tecnológica

## Backend

- Java 21
- Spring Boot
- Spring Security
- Spring Data JPA
- Bean Validation

## Banco de Dados

- PostgreSQL

## Cache

- Redis

## Mensageria

- RabbitMQ

## Armazenamento

- MinIO

## Identidade e Segurança

- Keycloak
- OAuth2
- OpenID Connect
- JWT

## Observabilidade

- OpenTelemetry
- Prometheus
- Grafana
- Loki
- Tempo

## DevOps

- Docker
- Docker Compose
- Docker Swarm
- GitHub Actions

## Edge Security

- Cloudflare
- WAF
- Rate Limiting

---

# Roadmap

O roadmap completo do projeto, detalhando as fases de entrega desde a fundação até a implantação em produção, pode ser encontrado em [docs/roadmap/roadmap.md](./docs/roadmap/roadmap.md).

---

# Documentação

A documentação completa do projeto está organizada na pasta `docs/` e inclui:

- Visão do Produto
- Roadmap
- Mapeamento de Contextos
- Modelo de Domínio
- Eventos de Integração
- Estratégia de Segurança
- Estratégia de Deploy
- Decisões de Arquitetura (ADRs)
- Runbook de Rollback

---

# Status

🚧 Em desenvolvimento
