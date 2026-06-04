# ADR-001: Adoção da Arquitetura de Monólito Modular

## Contexto

Precisamos definir uma arquitetura inicial para o projeto Chronos. A arquitetura deve facilitar o desenvolvimento rápido no início, mas também ser flexível o suficiente para evoluir à medida que o sistema cresce em complexidade. As principais alternativas consideradas foram Microsserviços e um Monólito tradicional (estilo "big ball of mud").

## Decisão

Decidimos adotar a arquitetura de **Monólito Modular**.

O sistema será uma única unidade de deploy (um monólito), mas internamente será dividido em módulos que representam os Bounded Contexts do DDD (e.g., `workorders`, `billing`, `users`). A comunicação entre os módulos deve ser evitada ao máximo. Quando necessária, será feita preferencialmente de forma assíncrona via eventos, ou, em casos síncronos, através de interfaces bem definidas expostas pela camada de aplicação de cada módulo.

## Consequências

### Positivas

- **Simplicidade Operacional:** Menor sobrecarga de infraestrutura e deploy em comparação com microsserviços.
- **Desenvolvimento Simplificado:** Uma única base de código, um único build e um ambiente de desenvolvimento local mais fácil de configurar.
- **Consistência Transacional:** Transações que abrangem múltiplos domínios (embora devam ser evitadas) são mais fáceis de gerenciar dentro de um único processo.
- **Prepara para o Futuro:** Módulos bem definidos e desacoplados são candidatos naturais a serem extraídos para microsserviços no futuro, se e quando a necessidade surgir.

### Negativas

- **Disciplina Necessária:** A equipe precisa ser disciplinada para manter os limites entre os módulos e não criar acoplamento direto, o que poderia degradar a arquitetura para um "big ball of mud".
- **Escalabilidade Unificada:** Todos os módulos são escalados juntos. Não é possível escalar o módulo `workorders` independentemente do módulo `billing`, por exemplo.
- **Tecnologia Única:** Todos os módulos compartilham a mesma stack tecnológica.
