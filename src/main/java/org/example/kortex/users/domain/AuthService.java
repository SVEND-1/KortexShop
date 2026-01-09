package org.example.kortex.users.domain;

import lombok.extern.slf4j.Slf4j;
import org.example.kortex.carts.db.Cart;
import org.example.kortex.carts.domain.CartService;
import org.example.kortex.config.JwtTokenProvider;
import org.example.kortex.notify.EmailSenderService;
import org.example.kortex.notify.event.NotifyEvent;
import org.example.kortex.notify.event.NotifyType;
import org.example.kortex.notify.kafka.NotifyKafkaProducer;
import org.example.kortex.users.api.dto.auth.*;
import org.example.kortex.users.db.Role;
import org.example.kortex.users.db.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class AuthService {
    private final UserService userService;
    private final CartService cartService;
    private final NotifyKafkaProducer kafkaProducer;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;
    private final EmailSenderService emailSenderService;

    private static final Map<String, RegistrationData> pendingRegistrations = new ConcurrentHashMap<>();
    private static final Map<String, ResetData> passwordResets = new ConcurrentHashMap<>();

    @Autowired
    public AuthService(UserService userService, CartService cartService,
                       NotifyKafkaProducer kafkaProducer , PasswordEncoder passwordEncoder,
                       JwtTokenProvider jwtTokenProvider, AuthenticationManager authenticationManager, EmailSenderService emailSenderService) {
        this.userService = userService;
        this.cartService = cartService;
        this.kafkaProducer = kafkaProducer;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.authenticationManager = authenticationManager;
        this.emailSenderService = emailSenderService;
    }

    //================================Controller Methods================================================

    public LoginResponse login(LoginRequest loginRequest, HttpServletResponse response) {
        try {
            log.info("Попытка входа для email={}", loginRequest.email());

            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.email(), loginRequest.password()
                    )
            );

            User user = userService.getByEmail(loginRequest.email());

            Cookie cookie = new Cookie("jwtToken", jwtTokenProvider.createToken(user.getEmail(), user.getRole().name()));
            cookie.setHttpOnly(true);
            cookie.setPath("/");
            cookie.setMaxAge(24 * 60 * 60);
            response.addCookie(cookie);
            log.debug("Куки сохранены");

            Set<SimpleGrantedAuthority> roles = Collections.singleton(user.getRole().toAuthority());
            Authentication authToken = new UsernamePasswordAuthenticationToken(
                    user.getEmail(),
                    null,
                    roles
            );
            SecurityContextHolder.getContext().setAuthentication(authToken);

            NotifyEvent notifyEvent = new NotifyEvent(
                    user.getEmail(),
                    Map.of("userName",user.getName()),
                    NotifyType.LOGIN
            );
            kafkaProducer.sendMessageToKafka(notifyEvent);

            log.info("Пользователь вошел: {}, ID={}", loginRequest.email(), user.getId());
            return new LoginResponse(true, "Успешный вход", "/");

        } catch (Exception e) {
            log.error("Ошибка входа для {}: {}", loginRequest.email(), e.getMessage());
            return new LoginResponse(false, "Неверный email или пароль", null);
        }
    }

    public SimpleResponse logout(HttpServletResponse response) {
        try {
            SecurityContextHolder.clearContext();

            Cookie cookie = new Cookie("jwtToken", null);
            cookie.setPath("/");
            cookie.setHttpOnly(true);
            cookie.setMaxAge(0);
            response.addCookie(cookie);

            return new SimpleResponse(true, "Успешный выход");

        } catch (Exception e) {
            log.error("Ошибка выхода: {}", e.getMessage());
            return new SimpleResponse(false, "Ошибка при выходе");
        }
    }

    public Object sendRegistrationCode(RegisterCodeRequest request) {
        try {
            String email = request.email();
            String password = request.password();
            log.info("Отправка кода регистрации на email={}", email);

            if (userService.getByEmail(email) != null) {
                log.warn("Попытка регистрации существующего email: {}", email);
                return new SimpleResponse(false, "Пользователь с таким email уже существует");
            }

            String verificationCode = emailSenderService.generateVerificationCode();
            String registrationId = UUID.randomUUID().toString();

            User tempUser = new User();
            tempUser.setEmail(email);
            tempUser.setPassword(passwordEncoder.encode(password));
            if (request.name() != null && !request.name().isEmpty()) {
                tempUser.setName(request.name());
            }

            pendingRegistrations.put(registrationId,
                    new RegistrationData(tempUser, verificationCode));

            NotifyEvent notifyEvent = new NotifyEvent(
                    email,
                    Map.of("code",verificationCode),
                    NotifyType.REGISTER
            );
            kafkaProducer.sendMessageToKafka(notifyEvent);

            cleanupExpiredData();

            log.info("Код подтверждения отправлен на email={}", email);
            return new RegistrationResponse(true, "Код подтверждения отправлен на email", registrationId);

        } catch (Exception e) {
            log.error("Ошибка отправки кода: {}", e.getMessage());
            return new SimpleResponse(false, "Ошибка при отправке кода: " + e.getMessage());
        }
    }

    public Object verifyRegistration(VerifyRegisterRequest request, HttpServletResponse response) {
        try {
            log.info("Подтверждение регистрации для ID={}", request.registrationId());

            RegistrationData data = pendingRegistrations.get(request.registrationId());

            if (data == null) {
                return new SimpleResponse(false, "Регистрация не найдена");
            }

            if (data.isExpired()) {
                pendingRegistrations.remove(request.registrationId());
                return new SimpleResponse(false, "Время действия кода истекло");
            }

            if (!request.code().equals(data.verificationCode)) {
                return new SimpleResponse(false, "Неверный код подтверждения");
            }

            User user = data.user;
            user.setRole(Role.USER);
            User savedUser = userService.create(user);

            Cart cart = new Cart();
            cart.setUser(savedUser);
            cartService.create(cart);

            String token = jwtTokenProvider.createToken(savedUser.getEmail(), savedUser.getRole().name());
            Cookie cookie = new Cookie("jwtToken", token);
            cookie.setHttpOnly(true);
            cookie.setPath("/");
            cookie.setMaxAge(24 * 60 * 60);
            response.addCookie(cookie);

            Set<SimpleGrantedAuthority> roles = Collections.singleton(Role.USER.toAuthority());
            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    savedUser.getEmail(), null, roles);
            SecurityContextHolder.getContext().setAuthentication(authentication);

            pendingRegistrations.remove(request.registrationId());

            log.info("Пользователь создан id={}, email={}", savedUser.getId(), savedUser.getEmail());
            return new LoginResponse(true, "Регистрация успешно завершена", "/");

        } catch (Exception e) {
            log.error("Ошибка при подтверждении: {}", e.getMessage());
            return new SimpleResponse(false, "Ошибка при подтверждении регистрации: " + e.getMessage());
        }
    }

    public SimpleResponse resendVerificationCode(String registrationId) {
        try {
            log.info("Повторная отправка кода для registrationId={}", registrationId);

            RegistrationData data = pendingRegistrations.get(registrationId);

            if (data == null || data.isExpired()) {
                log.error("Регистрация не найдена или истекла");
                return new SimpleResponse(false, "Регистрация не найдена или истекла");
            }

            String newCode = emailSenderService.generateVerificationCode();
            data.verificationCode = newCode;
            data.timestamp = System.currentTimeMillis();

            NotifyEvent notifyEvent = new NotifyEvent(
                    data.user.getEmail(),
                    Map.of("code",newCode),
                    NotifyType.REPLAY_CODE
            );
            kafkaProducer.sendMessageToKafka(notifyEvent);

            log.info("Новый код отправлен на email: {}", data.user.getEmail());
            return new SimpleResponse(true, "Новый код отправлен на email");

        } catch (Exception e) {
            log.error("Ошибка отправки повторного кода: {}", e.getMessage());
            return new SimpleResponse(false, "Ошибка отправки кода: " + e.getMessage());
        }
    }

    public Object forgotPassword(String email) {
        try {
            log.info("Запрос на восстановление пароля для email={}", email);

            User user = userService.getByEmail(email);
            if (user == null) {
                log.warn("Пользователь не найден: {}", email);
                return new SimpleResponse(false, "Пользователь с таким email не найден");
            }

            String resetCode = emailSenderService.generateVerificationCode();
            String resetId = UUID.randomUUID().toString();

            passwordResets.put(resetId, new ResetData(email, resetCode));

            NotifyEvent notifyEvent = new NotifyEvent(
                    email,
                    Map.of("code", resetCode),
                    NotifyType.PASSWORD_RESET
            );
            kafkaProducer.sendMessageToKafka(notifyEvent);

            log.info("Код для сброса пароля отправлен на email: {}", email);
            return new PasswordResetResponse(true, "Код для сброса пароля отправлен на email", resetId);

        } catch (Exception e) {
            log.error("Ошибка при запросе восстановления пароля: {}", e.getMessage());
            return new SimpleResponse(false, "Ошибка: " + e.getMessage());
        }
    }

    public Object verifyResetCode(String resetId, String code) {
        try {
            log.info("Проверка кода сброса пароля для resetId={}", resetId);

            ResetData data = passwordResets.get(resetId);

            if (data == null || data.isExpired()) {
                log.error("Код не найден или истек");
                return new SimpleResponse(false, "Код не найден или истек");
            }

            if (!code.equals(data.code)) {
                log.error("Неверный код подтверждения");
                return new SimpleResponse(false, "Неверный код подтверждения");
            }

            log.info("Код подтвержден для resetId={}", resetId);
            return new PasswordResetResponse(true, "Код подтвержден", resetId);

        } catch (Exception e) {
            log.error("Ошибка при проверке кода: {}", e.getMessage());
            return new SimpleResponse(false, "Ошибка: " + e.getMessage());
        }
    }

    public Object resetPassword(ResetPasswordRequest request, HttpServletResponse response) {
        try {
            ResetData data = passwordResets.get(request.resetId());

            if (data == null) {
                log.error("Недействительный запрос сброса");
                return new SimpleResponse(false, "Недействительный запрос сброса");
            }

            log.info("Смена пароля для пользователя с email: {}", data.email);

            if (!request.newPassword().equals(request.confirmPassword())) {
                log.error("Пароли не совпадают");
                return new SimpleResponse(false, "Пароли не совпадают");
            }

            User user = userService.getByEmail(data.email);
            if (user == null) {
                log.error("Пользователь не найден");
                return new SimpleResponse(false, "Пользователь не найден");
            }

            userService.changePassword(user.getId(), passwordEncoder.encode(request.newPassword()));

            Cookie cookie = new Cookie("jwtToken", jwtTokenProvider.createToken(user.getEmail(), user.getRole().name()));
            cookie.setHttpOnly(true);
            cookie.setPath("/");
            cookie.setMaxAge(24 * 60 * 60);
            response.addCookie(cookie);

            Set<SimpleGrantedAuthority> roles = Collections.singleton(user.getRole().toAuthority());
            Authentication authentication = new UsernamePasswordAuthenticationToken(user.getEmail(), null, roles);
            SecurityContextHolder.getContext().setAuthentication(authentication);

            passwordResets.remove(request.resetId());

            log.info("Пароль успешно изменен для пользователя: {}", data.email);
            return new LoginResponse(true, "Пароль успешно изменен", "/");

        } catch (Exception e) {
            log.error("Ошибка при сбросе пароля: {}", e.getMessage());
            return new SimpleResponse(false, "Ошибка при сбросе пароля: " + e.getMessage());
        }
    }

    //================================Service Methods================================================

    private void cleanupExpiredData() {
        pendingRegistrations.entrySet().removeIf(entry -> entry.getValue().isExpired());
        passwordResets.entrySet().removeIf(entry -> entry.getValue().isExpired());
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
}