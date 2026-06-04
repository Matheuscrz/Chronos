package com.caelum.chronos.modules.users.domain.enums;

/**
 * Enum representando os papéis de usuário no sistema. Os papéis incluem ADMIN
 * (administrador), TECNICO (técnico) e CLIENTE (cliente). Este enum é utilizado
 * para definir as permissões e o acesso dos usuários no sistema, permitindo que
 * o sistema controle as ações que cada tipo de usuário pode realizar, como
 * gerenciamento de usuários, acesso a funcionalidades específicas, etc.
 */
public enum UserRole {
    ADMIN,
    TECNICO,
    CLIENTE;
}
