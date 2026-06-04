# ADR-002: Uso do Keycloak como Provedor de Identidade

## Contexto

O sistema Chronos precisa de um mecanismo robusto e seguro para autenticação e autorização de usuários. As opções são: construir uma solução própria, usar uma biblioteca de segurança ou adotar uma solução de mercado de Gerenciamento de Identidade e Acesso (IAM). Construir do zero é complexo e propenso a erros.

## Decisão

Utilizar o **Keycloak** como o provedor de identidade e acesso centralizado para o Chronos.

A aplicação Chronos (Spring Boot) atuará como um _Resource Server_ no fluxo OAuth2, validando os JWTs emitidos pelo Keycloak. O Keycloak será responsável por toda a gestão de usuários, papéis, credenciais e emissão de tokens.

## Consequências

### Positivas

- **Delegação de Responsabilidade:** A complexidade da autenticação (login, registro, recuperação de senha, 2FA, etc.) é removida da nossa aplicação principal.
- **Segurança:** Utiliza uma solução de mercado, open-source, madura e amplamente testada, que implementa os padrões OIDC e OAuth2 corretamente.
- **Flexibilidade:** Facilita a implementação de Single Sign-On (SSO) e login social (Google, GitHub, etc.) no futuro.
- **UI de Administração:** O Keycloak fornece uma interface de administração completa para gerenciar usuários, papéis e clientes.

### Negativas

- **Dependência Externa:** Adiciona um novo serviço para ser implantado, configurado, mantido e monitorado.
- **Curva de Aprendizado:** A equipe precisa entender os conceitos de OAuth2/OIDC e a configuração específica do Keycloak.
