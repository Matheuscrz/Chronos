# Estratégia de Deploy

Este documento descreve o processo de build, teste, deploy e operação do sistema Chronos em um ambiente de produção.

---

## 1. Ambiente de Produção

- **Plataforma:** Uma VPS (Virtual Private Server) em um provedor de nuvem (e.g., DigitalOcean, Hetzner).
- **Containerização:** Toda a aplicação e seus serviços de apoio (banco de dados, filas, etc.) serão executados em containers Docker.
- **Orquestração:** Será utilizado o **Docker Swarm** para gerenciar os containers, permitindo `rolling updates` e `health checks`.

## 2. Pipeline de CI/CD (Integração e Entrega Contínua)

O pipeline será implementado com **GitHub Actions**.

**Gatilho:** `push` para a branch `main` ou criação de uma `tag`.

**Passos do Pipeline:**

1.  **Checkout:** Baixa o código-fonte do repositório.
2.  **Lint & Static Analysis:** Executa o SonarQube para verificar a qualidade do código e potenciais bugs. O pipeline falha se o _Quality Gate_ não for atingido.
3.  **Test:** Executa todos os testes (unitários, integração com Testcontainers).
4.  **Build:** Compila a aplicação e constrói a imagem Docker usando um `Dockerfile` multi-stage para gerar uma imagem final otimizada e leve.
5.  **Push:** Envia a imagem Docker versionada para um registry (e.g., Docker Hub, GitHub Container Registry). A tag da imagem corresponderá à tag do Git ou ao hash do commit.
6.  **Deploy:** Conecta-se via SSH à VPS de produção e executa o comando `docker stack deploy` para atualizar o serviço no Docker Swarm com a nova imagem. O Docker Swarm realizará um `rolling update` para substituir os containers antigos pelos novos sem downtime.

## 3. Arquitetura de Rede em Produção

```
Internet -> Cloudflare (DNS, WAF, SSL) -> VPS -> Nginx (Reverse Proxy) -> Docker Swarm -> [Chronos App Container]
```

- **Cloudflare:** Gerencia o DNS, provê o certificado SSL/TLS para o domínio e atua como a primeira linha de defesa (WAF, DDoS).
- **Nginx:** Roda em um container na VPS. Atua como reverse proxy, encaminhando o tráfego para o container da aplicação Chronos. É responsável pela terminação TLS (se não for feita no Cloudflare) e por adicionar headers de segurança.
- **Docker Network:** Todos os containers (Nginx, Chronos, PostgreSQL, etc.) se comunicam através de uma rede Docker privada e isolada.

## 4. Health Checks

A aplicação Spring Boot exporá endpoints de `health check` via **Spring Boot Actuator**. O Docker Swarm usará esses endpoints para garantir que um container está saudável antes de direcionar tráfego para ele e para reiniciar containers que não estejam respondendo.

## 5. Estratégia de Rollback

- **Rollback Automatizado (via Docker Swarm):** O Docker Swarm pode ser configurado para reverter automaticamente para a versão anterior se o deploy da nova versão falhar nos health checks.
- **Rollback Manual:** Em caso de um bug funcional detectado após o deploy, o rollback pode ser feito manualmente executando o comando `docker service update` com a tag da imagem da versão estável anterior. Este processo deve ser rápido (menos de 1 minuto).
- **Runbook:** Um runbook (`docs/rollback.md`) documentará o passo a passo para o rollback manual.
