# Chronos Backend

Backend da aplicação **Chronos**, construído com **Spring Boot 3.5.14**, **Java 21** e uma arquitetura orientada a desacoplamento, com foco em segurança, observabilidade e configuração por ambiente.

## Tecnologias e bibliotecas

### Core

- Spring Boot Web
- Spring Boot Validation
- Spring Boot Actuator

### Segurança

- Spring Boot Security
- Argon2id via `Argon2PasswordEncoder`

### Documentação

- Springdoc OpenAPI UI

### Persistência

- Spring Boot Data JPA
- PostgreSQL Driver
- Flyway Core
- Flyway PostgreSQL

### Cache

- Spring Boot Data Redis

### Mensageria

- Spring Boot AMQP

### Logs

- Logstash Logback Encoder

### Testes

- Spring Boot Test
- Testcontainers
- JUnit Jupiter

### Outros

- Lombok

## Estrutura de pastas

```text
src/
├── main/
│   ├── java/
│   │   └── com/caelum/chronos/
│   │       ├── backend/
│   │       │   └── BackendApplication.java
│   │       └── shared/
│   │           └── infra/
│   │               ├── logging/
│   │               │   ├── CorrelationIdFilter.java
│   │               │   └── LogContext.java
│   │               └── security/
│   │                   ├── SecurityConfig.java
│   │                   └── SecurityProperties.java
│   └── resources/
│       ├── application.yml
│       └── logback-spring.xml
└── test/
    └── java/
        └── com/caelum/chronos/backend/
            └── BackendApplicationTests.java
```

## Classes já criadas

### `BackendApplication`

Classe principal da aplicação Spring Boot.

### `SecurityConfig`

Configuração central do Spring Security, com:

- CORS via propriedades
- Swagger liberado
- CSRF desabilitado
- sessão stateless
- `PasswordEncoder` com Argon2id

### `SecurityProperties`

Mapeia as propriedades de segurança vindas do `application.yml` e do `.env`.

### `LogContext`

Centraliza o uso de MDC para:

- correlation id
- trace id
- span id
- user id
- request path
- event id
- event type

### `CorrelationIdFilter`

Filtro servlet que:

- lê ou gera `X-Correlation-Id`
- adiciona contexto de log
- devolve o correlation id na resposta

### `BackendApplicationTests`

Teste básico de carga do contexto Spring.

## Configurações atuais

### `application.yml`

Configurado para:

- conexão com PostgreSQL via variáveis de ambiente
- `ddl-auto` com valor padrão `validate`
- propriedades de segurança via ambiente
- métricas e health checks expostos

### `logback-spring.xml`

Configuração de logs com:

- formato legível para dev/local
- formato JSON para docker/prod/hml
- suporte a MDC

## Variáveis de ambiente esperadas

Exemplo:

```env
SERVER_PORT=8081

DB_HOST=localhost
DB_PORT=5432
DB_NAME=chronos_db
DB_USERNAME=chronos_user
DB_PASSWORD=chronos_pass

JPA_DDL_AUTO=validate

SECURITY_PERMIT_ALL=true

CORS_ALLOWED_ORIGINS=http://localhost:3000,http://localhost:5173,http://localhost:8080
CORS_ALLOWED_METHODS=GET,POST,PUT,PATCH,DELETE,OPTIONS
CORS_ALLOWED_HEADERS=Authorization,Content-Type,X-Requested-With,X-Correlation-Id
CORS_EXPOSED_HEADERS=X-Correlation-Id
CORS_ALLOW_CREDENTIALS=true

ARGON2_SALT_LENGTH=16
ARGON2_HASH_LENGTH=32
ARGON2_PARALLELISM=1
ARGON2_MEMORY=65536
ARGON2_ITERATIONS=3
```

## Endpoints úteis

- `GET /swagger-ui/index.html`
- `GET /v3/api-docs`
- `GET /actuator/health`
- `GET /actuator/info`

## Observações

- O pacote raiz da aplicação está configurado com `scanBasePackages = "com.caelum.chronos"`.
- O `SecurityConfig` deve ser carregado automaticamente a partir desse pacote raiz.
- O encoder configurado usa **Argon2id**.
