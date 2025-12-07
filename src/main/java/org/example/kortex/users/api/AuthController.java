package org.example.kortex.users.api;

import org.example.kortex.carts.db.Cart;
import org.example.kortex.users.db.User;
import org.example.kortex.carts.domain.CartService;
import org.example.kortex.users.domain.EmailSenderService;
import org.example.kortex.users.domain.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final CartService cartService;
    private final EmailSenderService emailSenderService;
    private final PasswordEncoder passwordEncoder;
    private final Logger log = LoggerFactory.getLogger(UserService.class);

    // Хранилища в памяти чтобы передавать между страницами
    private static final Map<String, RegistrationData> pendingRegistrations = new ConcurrentHashMap<>();
    private static final Map<String, ResetData> passwordResets = new ConcurrentHashMap<>();

    @Autowired
    public AuthController(UserService userService,
                          CartService cartService,
                          EmailSenderService emailSenderService,
                          PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.cartService = cartService;
        this.emailSenderService = emailSenderService;
        this.passwordEncoder = passwordEncoder;
    }


    @PostMapping("/register/send-code")
    public ResponseEntity<?> sendRegistrationCode(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            String name = request.get("name");
            String password = request.get("password");
            log.info("Отправка пользователю код на email: " + email);

            if (email == null || email.isEmpty()) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "Email обязателен");
                return ResponseEntity.badRequest().body(error);
            }

            if (userService.getByEmail(email) != null) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "Пользователь с таким email уже существует");
                log.info("Пользователь с таким email уже существует");
                return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
            }

            String verificationCode = emailSenderService.generateVerificationCode();
            String registrationId = UUID.randomUUID().toString();

            User tempUser = new User();
            tempUser.setEmail(email);
            tempUser.setName(name != null ? name : "");
            tempUser.setPassword(password != null ? password : "");

            pendingRegistrations.put(registrationId,
                    new RegistrationData(tempUser, verificationCode));

            emailSenderService.sendVerification(email, verificationCode);

            cleanupExpiredData();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("registrationId", registrationId);
            response.put("message", "Код подтверждения отправлен на email");
            log.info("Код подтверждения отправлен на email: " + email);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Ошибка отправки кода: " + e.getMessage());
            log.error("Ошибка отправки кода: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @PostMapping("/register/verify")
    public ResponseEntity<?> verifyRegistration(
            @RequestParam String registrationId,
            @RequestParam String code) {

        try {
            log.info("verify пользователя " + code);
            RegistrationData data = pendingRegistrations.get(registrationId);

            if (data == null) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "Недействительный код регистрации");
                log.error("Недействительный код регистрации");
                return ResponseEntity.badRequest().body(error);
            }

            if (data.isExpired()) {
                pendingRegistrations.remove(registrationId);
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "Время подтверждения истекло");
                log.error("Время подтверждения истекло");
                return ResponseEntity.badRequest().body(error);
            }

            if (!code.equals(data.verificationCode)) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "Неверный код подтверждения");
                log.error("Неверный код подтверждения");
                return ResponseEntity.badRequest().body(error);
            }

            User user = data.user;
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            user.setRole(User.Role.USER);

            User savedUser = userService.create(user);

            Cart cart = new Cart();
            cart.setUser(savedUser);
            cartService.create(cart);

            forceAutoLogin(savedUser.getEmail(), savedUser.getPassword());

            pendingRegistrations.remove(registrationId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Регистрация успешно завершена");
            response.put("user", savedUser);
            response.put("redirectUrl", "/");
            log.info("Пользователь создан id: " + savedUser.getId());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Ошибка при подтверждении: " + e.getMessage());
            log.info("Ошибка при подтверждении: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @PostMapping("/register/resend-code")
    public ResponseEntity<?> resendVerificationCode(@RequestParam String registrationId) {
        try {
            log.info("Повторное отправление кода на почту");
            RegistrationData data = pendingRegistrations.get(registrationId);

            if (data == null || data.isExpired()) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "Регистрация не найдена или истекла");
                log.error("Регистрация не найдена или истекла");
                return ResponseEntity.badRequest().body(error);
            }

            String newCode = emailSenderService.generateVerificationCode();
            data.verificationCode = newCode;
            data.timestamp = System.currentTimeMillis();

            emailSenderService.sendVerification(data.user.getEmail(), newCode);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Новый код отправлен на email");
            log.info("Новый код отправлен на email" + data.user.getEmail());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Ошибка отправки кода: " + e.getMessage());
            log.info("Ошибка отправки повторного кода: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @PostMapping("/password/forgot")
    public ResponseEntity<?> forgotPassword(@RequestParam String email) {
        try {
            log.info("Запрос на забыл пароль email" + email);
            User user = userService.getByEmail(email);
            if (user == null) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "Пользователь с таким email не найден");
                return ResponseEntity.badRequest().body(error);
            }

            String resetCode = emailSenderService.generateVerificationCode();
            String resetId = UUID.randomUUID().toString();

            passwordResets.put(resetId, new ResetData(email, resetCode));

            emailSenderService.sendPasswordResetEmail(email, resetCode);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("resetId", resetId);
            response.put("message", "Код для сброса пароля отправлен на email");
            log.info("Код для сброса пароля отправлен на email");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Ошибка: " + e.getMessage());
            log.info("Ошибка: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @PostMapping("/password/verify")
    public ResponseEntity<?> verifyResetCode(
            @RequestParam String resetId,
            @RequestParam String code) {

        try {
            log.info("Код по 'забыл пароль'");
            ResetData data = passwordResets.get(resetId);

            if (data == null || data.isExpired()) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "Код не найден или истек");
                log.error("Код не найден или истек");
                return ResponseEntity.badRequest().body(error);
            }

            if (!code.equals(data.code)) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "Неверный код подтверждения");
                log.error("Неверный код подтверждения");
                return ResponseEntity.badRequest().body(error);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("resetId", resetId);
            response.put("message", "Код подтвержден");
            log.info("Код подтвержден");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Ошибка: " + e.getMessage());
            log.error("Ошибка: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @PostMapping("/password/reset")
    public ResponseEntity<?> resetPassword(
            @RequestParam String resetId,
            @RequestParam String newPassword,
            @RequestParam String confirmPassword) {

        try {
            ResetData data = passwordResets.get(resetId);
            log.info("Смена пароля у пользователя с email: " + data.email);

            if (data == null) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "Недействительный запрос сброса");
                log.error("Недействительный запрос сброса");
                return ResponseEntity.badRequest().body(error);
            }

            if (!newPassword.equals(confirmPassword)) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "Пароли не совпадают");
                log.error("Пароли не совпадают");
                return ResponseEntity.badRequest().body(error);
            }

            User user = userService.getByEmail(data.email);
            if (user == null) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "Пользователь не найден");
                log.error("Пользователь не найден");
                return ResponseEntity.badRequest().body(error);
            }

            user.setPassword(passwordEncoder.encode(newPassword));
            userService.update(user.getId(), user);

            forceAutoLogin(user.getEmail(), user.getPassword());

            passwordResets.remove(resetId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Пароль успешно изменен");
            response.put("redirectUrl", "/");
            log.info("Пароль успешно изменен");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Ошибка при сбросе пароля: " + e.getMessage());
            log.error("Ошибка при сбросе пароля: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    private static class RegistrationData {
        User user;
        String verificationCode;
        long timestamp;

        RegistrationData(User user, String verificationCode) {
            this.user = user;
            this.verificationCode = verificationCode;
            this.timestamp = System.currentTimeMillis();
        }

        boolean isExpired() {
            return System.currentTimeMillis() - timestamp > 15 * 60 * 1000; // 15 минут
        }
    }

    private static class ResetData {
        String email;
        String code;
        long timestamp;

        ResetData(String email, String code) {
            this.email = email;
            this.code = code;
            this.timestamp = System.currentTimeMillis();
        }

        boolean isExpired() {
            return System.currentTimeMillis() - timestamp > 15 * 60 * 1000; // 15 минут
        }
    }

    private void cleanupExpiredData() {
        pendingRegistrations.entrySet().removeIf(entry -> entry.getValue().isExpired());
        passwordResets.entrySet().removeIf(entry -> entry.getValue().isExpired());
    }

    private void forceAutoLogin(String email, String password) {
        Set<SimpleGrantedAuthority> roles = Collections.singleton(User.Role.USER.toAuthority());
        Authentication authentication = new UsernamePasswordAuthenticationToken(email, password, roles);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}