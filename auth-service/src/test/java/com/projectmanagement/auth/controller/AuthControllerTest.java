package com.projectmanagement.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.projectmanagement.auth.dto.request.RegisterRequest;
import com.projectmanagement.auth.dto.response.UserResponse;
import com.projectmanagement.auth.security.config.SecurityConfig;
import com.projectmanagement.auth.service.AuthService;
import com.projectmanagement.auth.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {AuthController.class, UserController.class})
@Import(SecurityConfig.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    @DisplayName("Регистрация с корректными данными возвращает 201 Created")
    void register_ValidData_Returns201Created() throws Exception {
        RegisterRequest request = new RegisterRequest("valid.user@example.com", "SecurePassword123!");
        UserResponse response = mock(UserResponse.class);

        when(authService.register(any(RegisterRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("Регистрация с некорректными данными возвращает 400 Bad Request")
    void register_InvalidData_Returns400BadRequest() throws Exception {
        RegisterRequest invalidRequest = new RegisterRequest("invalid-email", "");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Запрос /api/auth/me без JWT возвращает 401 Unauthorized")
    void getCurrentUser_WithoutJwt_Returns401Unauthorized() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Запрос /api/users с ролью EMPLOYEE возвращает 403 Forbidden")
    void getAllUsers_WithEmployeeRole_Returns403Forbidden() throws Exception {
        mockMvc.perform(get("/api/users")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_EMPLOYEE"))))
                .andExpect(status().isForbidden());
    }
}