# Estratégia de Segurança

Este documento descreve a abordagem de segurança em camadas para o sistema Chronos.

---

## 1. Autenticação

- **Provedor de Identidade:** A autenticação será centralizada no **Keycloak**. A aplicação Spring Boot atuará como um _Resource Server_, delegando a geração e validação de credenciais para o Keycloak.
- **Padrões:** Serão utilizados os padrões **OAuth2** e **OpenID Connect (OIDC)**.
- **Tokens:** O sistema usará **JSON Web Tokens (JWT)**.
  - `Access Token`: Vida curta (5-15 minutos), enviado no header `Authorization` para autorizar requisições.
  - `Refresh Token`: Vida longa (horas/dias), armazenado em um cookie `HttpOnly`, `Secure` e `SameSite=Strict` para renovar o `Access Token` de forma segura.

## 2. Autorização

- **Controle de Acesso:** A autorização será baseada em papéis (Role-Based Access Control - RBAC).
- **Implementação:** Será utilizado o **Spring Security** com anotações `@PreAuthorize` nos endpoints para verificar os papéis (`ROLES`) e escopos (`SCOPES`) presentes no JWT.
- **Papéis Iniciais:** `ADMIN`, `TECNICO`, `CLIENTE`.

## 3. Segurança na Aplicação

- **OWASP Top 10:** Todos os endpoints serão desenvolvidos com as práticas recomendadas para mitigar os riscos do OWASP Top 10, incluindo:
  - **Input Validation:** Validação rigorosa de todos os dados de entrada usando `Bean Validation`.
  - **IDOR (Insecure Direct Object References):** Uso de **UUIDs** em vez de IDs sequenciais para recursos públicos e verificação de pertencimento do recurso ao usuário autenticado.
  - **SQL Injection:** Prevenção garantida pelo uso de Spring Data JPA e queries parametrizadas.
- **Headers de Segurança:** O reverse proxy (Nginx) será configurado para adicionar headers de segurança em todas as respostas, como `HSTS`, `X-Content-Type-Options`, `X-Frame-Options` e `Content-Security-Policy (CSP)`.
- **CORS:** A política de Cross-Origin Resource Sharing será restritiva, permitindo apenas origens conhecidas.

## 4. Segurança de Infraestrutura e Rede

- **Defesa de Borda (Edge):**
  - **Cloudflare:** Será utilizado como a primeira camada de defesa, provendo proteção contra DDoS, WAF (Web Application Firewall) e Rate Limiting na borda.
  - **DNS:** Gerenciado pelo Cloudflare.
- **Reverse Proxy:**
  - **Nginx:** Atuará como reverse proxy, fazendo a terminação de TLS/SSL. A comunicação entre o Nginx e a aplicação Spring Boot será feita dentro de uma rede Docker privada.
- **Isolamento de Rede:**
  - Os serviços de infraestrutura (PostgreSQL, Redis, RabbitMQ, MinIO, Keycloak) **nunca** serão expostos publicamente. Eles só serão acessíveis pela aplicação dentro da rede Docker privada.

## 5. Gestão de Segredos

- Senhas, chaves de API e outras informações sensíveis não serão "hard-coded" no código-fonte.
- Em ambiente de desenvolvimento, serão usadas variáveis de ambiente via arquivos `.env` ou `docker-compose.yml`.
- Para produção, será avaliado o uso de um cofre de segredos (como HashiCorp Vault ou o gerenciador de segredos da nuvem).
