package com.caelum.chronos.modules.users.api;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.caelum.chronos.modules.users.application.dto.response.UserResponse;
import com.caelum.chronos.modules.users.application.service.UserService;
import com.caelum.chronos.shared.api.error.GlobalExceptionHandler;

class UserControllerTest {

    private MockMvc mockMvc;
    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = mock(UserService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new UserController(userService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void meDeveRetornarPerfilDoUsuarioAutenticado() throws Exception {
        UUID userId = UUID.randomUUID();
        UserResponse response = UserResponse.builder().id(userId).username("test").build();
        
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn(userId.toString());

        when(userService.findById(userId)).thenReturn(response);

        mockMvc.perform(get("/users/me").principal(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId.toString()));
    }

    @Test
    void findByIdDeveRetornarUsuarioParaAdmin() throws Exception {
        UUID userId = UUID.randomUUID();
        UserResponse response = UserResponse.builder().id(userId).username("test").build();

        when(userService.findById(userId)).thenReturn(response);

        mockMvc.perform(get("/users/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId.toString()));
    }
}