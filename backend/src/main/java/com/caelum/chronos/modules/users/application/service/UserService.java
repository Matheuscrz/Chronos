package com.caelum.chronos.modules.users.application.service;

import java.util.UUID;

import org.springframework.lang.NonNull;

import com.caelum.chronos.modules.users.application.dto.request.UserRegistrationRequest;
import com.caelum.chronos.modules.users.application.dto.response.UserResponse;

/**
 * Interface de serviço para operações relacionadas a usuários, como criação e
 * consulta por ID.
 * Define os métodos que serão implementados para manipular os dados dos
 * usuários, garantindo a separação de responsabilidades e facilitando a
 * manutenção do código.
 * <ul>
 * <li><strong>createUser</strong>: Método para criar um novo usuário a partir
 * de uma requisição de registro, retornando um UserResponse com os detalhes do
 * usuário criado.</li>
 * <li><strong>findById</strong>: Método para encontrar um usuário pelo seu ID,
 * retornando um UserResponse com os detalhes do usuário encontrado.</li>
 * </ul>
 */
public interface UserService {
    /**
     * Cria um novo usuário com base na requisição de registro fornecida.
     * 
     * @param req A requisição contendo os dados necessários para criar um usuário,
     *            como
     * @return Um UserResponse contendo os detalhes do usuário criado, incluindo ID,
     *         nome de usuário, nome completo, email e papel.
     */
    UserResponse createUser(UserRegistrationRequest req);

    /**
     * Encontra um usuário pelo seu ID.
     * 
     * @param id O ID do usuário a ser encontrado.
     * @return Um UserResponse contendo os detalhes do usuário encontrado, ou null
     *         se nenhum usuário for encontrado com o ID fornecido.
     */
    UserResponse findById(@NonNull UUID id);
}
