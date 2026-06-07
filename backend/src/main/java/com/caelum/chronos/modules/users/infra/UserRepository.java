package com.caelum.chronos.modules.users.infra;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;

import com.caelum.chronos.modules.users.domain.model.User;

/**
 * Repositório para a entidade User, responsável por realizar operações de
 * persistência e consulta no banco de dados.
 * Estende JpaRepository, fornecendo métodos CRUD básicos e a capacidade de
 * definir consultas personalizadas.
 * <ul>
 * <li><strong>findByUsername</strong>: Método para encontrar um usuário pelo
 * seu nome de usuário, retornando um Optional<User>.</li>
 * <li><strong>findByEmail</strong>: Método para encontrar um usuário pelo seu
 * email, retornando um Optional<User>.</li>
 * <li><strong>existsByUsername</strong>: Método para verificar se um usuário
 * com determinado nome de usuário já existe, retornando um boolean.</li>
 * <li><strong>existsByEmail</strong>: Método para verificar se um usuário com
 * determinado email já existe, retornando um boolean.</li>
 * </ul>
 */
public interface UserRepository extends JpaRepository<User, UUID> {
    /**
     * Encontra um usuário pelo seu nome de usuário.
     * 
     * @param username O nome de usuário a ser pesquisado.
     * @return Um Optional contendo o usuário encontrado, ou vazio se nenhum usuário
     *         for encontrado.
     */
    Optional<User> findByUsername(String username);

    /**
     * Encontra um usuário pelo seu email.
     * 
     * @param email O email a ser pesquisado.
     * @return Um Optional contendo o usuário encontrado, ou vazio se nenhum usuário
     *         for encontrado.
     */
    Optional<User> findByEmail(String email);

    /**
     * Encontra um usuário pelo seu ID.
     * 
     * @param id O ID do usuário a ser pesquisado.
     * @return Um Optional contendo o usuário encontrado, ou vazio se nenhum usuário
     *         for encontrado.
     */
    @NonNull
    Optional<User> findById(@NonNull UUID id);

    /**
     * Verifica se um usuário com determinado nome de usuário já existe.
     * 
     * @param username O nome de usuário a ser verificado.
     * @return true se um usuário com o nome de usuário existir, false caso
     *         contrário.
     */
    boolean existsByUsername(String username);

    /**
     * Verifica se um usuário com determinado email já existe.
     * 
     * @param email O email a ser verificado.
     * @return true se um usuário com o email existir, false caso contrário.
     */
    boolean existsByEmail(String email);
}
