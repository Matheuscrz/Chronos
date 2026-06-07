package com.caelum.chronos.modules.auth.application.service;

import com.caelum.chronos.modules.auth.application.dto.request.LoginRequest;
import com.caelum.chronos.modules.auth.application.dto.response.AuthResponse;

/**
 * Interface de serviço para autenticação de usuários. Define os métodos para
 * autenticar um usuário com base em suas credenciais de login e para atualizar
 * a sessão do usuário usando um token de refresh.
 */
public interface AuthService {
    /**
     * Autentica um usuário e cria uma nova sessão.
     *
     * @param request O objeto contendo as credenciais de login.
     * @param ipAddress O endereço IP do usuário.
     * @param userAgent O User-Agent do navegador/dispositivo.
     * @return O usuário e o par de tokens.
     */
    AuthResponse authenticate(LoginRequest request, String ipAddress, String userAgent);

    /**
     * Atualiza a sessão do usuário usando um token de refresh.
     * 
     * @param refreshToken O token de refresh.
     * @param ipAddress O endereço IP do usuário.
     * @param userAgent O User-Agent do navegador/dispositivo.
     * @return O usuário e o novo par de tokens.
     */
    AuthResponse refresh(String refreshToken, String ipAddress, String userAgent);

    /**
     * Invalida uma sessão específica.
     * 
     * @param refreshToken O token de refresh da sessão a ser invalidada.
     */
    void logout(String refreshToken);
}