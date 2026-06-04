package com.caelum.chronos.modules.auth.application.service;

import com.caelum.chronos.modules.auth.application.dto.request.LoginRequest;
import com.caelum.chronos.modules.users.domain.model.User;

/**
 * Interface de serviço para autenticação de usuários. Define os métodos para
 * autenticar um usuário com base em suas credenciais de login e para atualizar
 * a
 * sessão do usuário usando um token de refresh. A implementação desta interface
 * é responsável por validar as credenciais do usuário, gerar tokens de
 * autenticação e refresh, e garantir que apenas usuários autenticados possam
 * acessar os recursos protegidos do sistema.
 */
public interface AuthService {
    /**
     * Autentica um usuário com base em suas credenciais de login e senha.
     *
     * @param request O objeto contendo as credenciais de login do usuário.
     * @return O usuário autenticado, caso as credenciais sejam válidas.
     * @throws InvalidCredentialsException Se as credenciais forem inválidas ou o
     *                                     usuário não for encontrado.
     */
    User authenticate(LoginRequest request);

    /**
     * Atualiza a sessão do usuário usando um token de refresh, retornando o usuário
     * associado ao token.
     * 
     * @param refreshToken O token de refresh utilizado para atualizar a sessão do
     *                     usuário.
     * @return O usuário associado ao token de refresh, caso o token seja válido.
     * @throws InvalidCredentialsException Se o token de refresh for inválido ou o
     *                                     usuário não for encontrado.
     */
    User refresh(String refreshToken);
}