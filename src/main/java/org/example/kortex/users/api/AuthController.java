package org.example.kortex.users.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.kortex.users.api.dto.auth.LoginRequest;
import org.example.kortex.users.api.dto.auth.RegisterCodeRequest;
import org.example.kortex.users.api.dto.auth.ResetPasswordRequest;
import org.example.kortex.users.api.dto.auth.VerifyRegisterRequest;
import org.example.kortex.users.domain.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Auth", description = "Работа с авторизацией")
public class AuthController {

    private final AuthService authService;

    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(summary = "Вход в систему существуещего пользователя")
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody @Valid LoginRequest loginRequest,
                                   HttpServletResponse response) {
        return ResponseEntity.ok(authService.login(loginRequest, response));
    }

    @Operation(summary = "Выход с системы")
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        return ResponseEntity.ok(authService.logout(response));
    }

    @Operation(summary = "Заполения полей для регистации и отправка кода")
    @PostMapping("/register/send-code")
    public ResponseEntity<?> sendRegistrationCode(@RequestBody @Valid RegisterCodeRequest request) {
        return ResponseEntity.ok(authService.sendRegistrationCode(request));
    }

    @Operation(summary = "Подтверждение регистрации и создания пользователя")
    @PostMapping("/register/verify")
    public ResponseEntity<?> verifyRegistration(
            @RequestBody @Valid VerifyRegisterRequest request,
            HttpServletResponse response) {
        return ResponseEntity.ok(authService.verifyRegistration(request, response));
    }

    @Operation(summary = "Повторная отправка кода")
    @PostMapping("/register/resend-code")
    public ResponseEntity<?> resendVerificationCode(@RequestParam String registrationId) {
        return ResponseEntity.ok(authService.resendVerificationCode(registrationId));
    }

    @Operation(summary = "Заполнения email пользователя который забыл пароль и отправка кода")
    @PostMapping("/password/forgot")
    public ResponseEntity<?> forgotPassword(@RequestParam String email) {
        return ResponseEntity.ok(authService.forgotPassword(email));
    }

    @Operation(summary = "Подтверждение кода")
    @PostMapping("/password/verify")
    public ResponseEntity<?> verifyResetCode(
            @RequestParam String resetId,
            @RequestParam String code) {
        return ResponseEntity.ok(authService.verifyResetCode(resetId, code));
    }

    @Operation(summary = "Смена пароля пользователя")
    @PostMapping("/password/reset")
    public ResponseEntity<?> resetPassword(
            @RequestBody @Valid ResetPasswordRequest request,
            HttpServletResponse response) {
        return ResponseEntity.ok(authService.resetPassword(request, response));
    }
}