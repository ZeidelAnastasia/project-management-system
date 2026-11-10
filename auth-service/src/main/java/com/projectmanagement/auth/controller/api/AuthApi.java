package com.projectmanagement.auth.controller.api;

import com.projectmanagement.auth.dto.request.LoginRequest;
import com.projectmanagement.auth.dto.request.RefreshTokenRequest;
import com.projectmanagement.auth.dto.request.RegisterRequest;
import com.projectmanagement.auth.dto.response.AuthResponse;
import com.projectmanagement.auth.dto.response.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Authentication", description = "Регистрация и аутентификация пользователей")
@RequestMapping("/api/auth")
public interface AuthApi {

    @Operation(summary = "Регистрация пользователя")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Пользователь успешно зарегистрирован",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = UserResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "400", description = "Некорректные данные", content = @Content),
            @ApiResponse(responseCode = "409", description = "Пользователь с таким email уже существует", content = @Content)
    })
    @PostMapping("/register")
    ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request);

    @Operation(summary = "Авторизация пользователя")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Успешная авторизация",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = AuthResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "400", description = "Некорректные данные", content = @Content),
            @ApiResponse(responseCode = "401", description = "Неверный email или пароль", content = @Content),
            @ApiResponse(responseCode = "403", description = "Учётная запись деактивирована", content = @Content)
    })
    @PostMapping("/login")
    ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request);

    @Operation(summary = "Обновление токенов")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Токены успешно обновлены",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = AuthResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "400", description = "Недействительный или истёкший refresh token", content = @Content),
            @ApiResponse(responseCode = "403", description = "Учётная запись деактивирована", content = @Content)
    })
    @PostMapping("/refresh")
    ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshTokenRequest request);

    @Operation(summary = "Выход из системы")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Выход выполнен успешно"),
            @ApiResponse(responseCode = "400", description = "Некорректный запрос", content = @Content)
    })
    @PostMapping("/logout")
    ResponseEntity<Void> logout(@Valid @RequestBody RefreshTokenRequest request);

    @Operation(
            summary = "Получение информации о текущем пользователе",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Информация о пользователе",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = UserResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован", content = @Content),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден", content = @Content)
    })
    @GetMapping("/me")
    ResponseEntity<UserResponse> getCurrentUser();
}