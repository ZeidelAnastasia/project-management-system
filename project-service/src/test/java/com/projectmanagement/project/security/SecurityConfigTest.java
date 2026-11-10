package com.projectmanagement.project.security;

import com.projectmanagement.project.config.JwtDecoderConfig;
import com.projectmanagement.project.config.SecurityConfig;
import com.projectmanagement.project.controller.ProjectController;
import com.projectmanagement.project.dto.response.ProjectResponse;
import com.projectmanagement.project.security.CurrentUser;
import com.projectmanagement.project.security.CurrentUserProvider;
import com.projectmanagement.project.security.JwtRoleConverter;
import com.projectmanagement.project.service.ProjectService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@WebMvcTest(controllers = ProjectController.class)
@Import({
        SecurityConfig.class,
        JwtDecoderConfig.class,
        JwtRoleConverter.class
})
class SecurityConfigTest {

    private static final UUID USER_ID = UUID.randomUUID();

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProjectService projectService;

    @MockitoBean
    private CurrentUserProvider currentUserProvider;

    @BeforeEach
    void setUp() {
        when(currentUserProvider.getCurrentUser())
                .thenReturn(new CurrentUser(USER_ID, "ADMIN"));

        when(projectService.getProjects(any(UUID.class), any(String.class)))
                .thenReturn(List.<ProjectResponse>of());
    }

    @Test
    void shouldReturnUnauthorizedWithoutToken() throws Exception {
        mockMvc.perform(get("/api/projects"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturnUnauthorizedWithInvalidToken() throws Exception {
        mockMvc.perform(get("/api/projects")
                        .header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldAllowRequestWithValidJwt() throws Exception {
        mockMvc.perform(get("/api/projects")
                        .with(jwt()
                                .jwt(token -> token
                                        .subject(USER_ID.toString())
                                        .claim("role", "ADMIN"))
                                .authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isOk());
    }

    @Test
    void shouldAllowSwaggerWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/api-docs"))
                .andExpect(status().is(org.hamcrest.Matchers.not(401)));
    }

    @Test
    void shouldAllowHealthCheckWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().is(org.hamcrest.Matchers.not(401)));
    }
}