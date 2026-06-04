# Runbook: Rollback de Deploy

Este documento descreve o procedimento para reverter uma versão (fazer o rollback) da aplicação Chronos em produção.

---

## Quando usar este Runbook?

Este procedimento deve ser executado em caso de um **incidente crítico** detectado após um deploy, como:

- Aumento significativo na taxa de erros (5xx).
- Impacto funcional grave que impede os usuários de realizarem operações essenciais.
- Problema de segurança introduzido pela nova versão.
- Falha nos `health checks` após o deploy, impedindo a inicialização do serviço.

A decisão de rollback deve ser rápida. A prioridade é restaurar o serviço. A investigação da causa raiz do problema deve ser feita **após** o rollback.

---

## Pré-requisitos

1.  Acesso SSH à VPS de produção.
2.  Permissões para executar comandos Docker.
3.  Conhecimento da **tag da imagem da versão estável anterior**. Essa informação deve estar disponível nos logs do pipeline de CI/CD ou no registry de imagens.

---

## Procedimento de Rollback Manual

1.  **Conecte-se à VPS de produção via SSH:**

    ```bash
    ssh user@your_vps_ip
    ```

2.  **Execute o comando de atualização do serviço no Docker Swarm, especificando a tag da imagem anterior:**

    ```bash
    docker service update --image <registry>/chronos:<tag_da_versao_anterior> chronos_app
    ```

    - Substitua `<registry>` pelo seu registry de imagens (e.g., `docker.io/youruser`, `ghcr.io/youruser`).
    - Substitua `<tag_da_versao_anterior>` pela tag da imagem que era considerada estável (e.g., `1.2.0`).
    - `chronos_app` é o nome do serviço definido no arquivo `docker-compose.yml` do stack.

3.  **Monitore o status do deploy:**
    O Docker Swarm executará um `rolling update`, substituindo os containers da versão com falha pelos da versão anterior. Monitore o progresso:

    ```bash
    docker service ps chronos_app
    ```

    Aguarde até que todos os containers estejam no estado `Running`.

4.  **Verifique a saúde da aplicação:**
    - Acesse a aplicação publicamente e verifique se o comportamento problemático foi resolvido.
    - Monitore os logs e os dashboards de observabilidade (Grafana) para garantir que a taxa de erros voltou ao normal.

---

## Pós-Rollback

1.  **Comunique:** Informe às partes interessadas que o rollback foi concluído e o serviço está restaurado.
2.  **Investigue:** Inicie a investigação da causa raiz do problema na versão que falhou.
3.  **Bloqueie o Deploy:** Crie um bloqueio temporário para impedir que a versão com falha seja implantada novamente por engano.
4.  **Postmortem:** Conduza uma análise postmortem para documentar o incidente, a causa e as ações para prevenir que aconteça novamente.
