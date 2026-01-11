package org.example.kortex.users.domain;

import org.example.kortex.carts.db.Cart;
import org.example.kortex.carts.domain.CartService;
import org.example.kortex.config.JwtTokenProvider;
import org.example.kortex.notify.EmailSenderService;
import org.example.kortex.notify.kafka.NotifyKafkaProducer;
import org.example.kortex.users.api.dto.auth.*;
import org.example.kortex.users.db.Role;
import org.example.kortex.users.db.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {//ВСЕ ЧТО В Try написано для получение приватных методов и другого НАПИСАНО НЕ МНОЮ

    @Mock
    private UserService userService;
    @Mock
    private CartService cartService;
    @Mock
    private NotifyKafkaProducer kafkaProducer;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtTokenProvider jwtTokenProvider;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private EmailSenderService emailSenderService;
    @Mock
    private HttpServletResponse response;
    @Mock
    private SecurityContext securityContext;

    @InjectMocks
    private AuthService authService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setName("Test User");
        testUser.setRole(Role.USER);
        testUser.setPassword("encodedPassword");
    }

    @Test
    void login() {
        // Подготовка
        LoginRequest request = new LoginRequest("test@example.com", "password");

        when(authenticationManager.authenticate(any())).thenReturn(mock(Authentication.class));
        when(userService.getByEmail("test@example.com")).thenReturn(testUser);
        when(jwtTokenProvider.createToken(anyString(), anyString())).thenReturn("jwt-token");

        // Действие
        LoginResponse result = authService.login(request, response);

        // Проверка
        assertTrue(result.success());
        assertEquals("Успешный вход", result.message());
        assertEquals("/", result.redirectUrl());

        verify(response).addCookie(any(Cookie.class));
        verify(kafkaProducer).sendMessageToKafka(any());
    }


    @Test
    void logout() {
        SimpleResponse result = authService.logout(response);

        assertTrue(result.success());
        assertEquals("Успешный выход", result.message());
        verify(response).addCookie(argThat(cookie ->
                cookie.getName().equals("jwtToken") && cookie.getMaxAge() == 0
        ));
    }

    @Test
    void sendRegistrationCode() {
        RegisterCodeRequest request = new RegisterCodeRequest(
                "new@example.com",
                "password",
                "New User"
        );

        when(userService.getByEmail("new@example.com")).thenReturn(null);
        when(emailSenderService.generateVerificationCode()).thenReturn("123456");
        when(passwordEncoder.encode("password")).thenReturn("encodedPassword");

        Object result = authService.sendRegistrationCode(request);

        assertTrue(result instanceof RegistrationResponse);
        RegistrationResponse response = (RegistrationResponse) result;
        assertTrue(response.success());
        assertNotNull(response.registrationId());
    }


    @Test
    void verifyRegistration() {
        User tempUser = new User();
        tempUser.setEmail("new@example.com");
        tempUser.setPassword("encodedPassword");
        tempUser.setName("New User");

        try {
            var field = AuthService.class.getDeclaredField("pendingRegistrations");
            field.setAccessible(true);
            var pendingRegistrations = field.get(authService);

            Class<?> dataClass = Class.forName(
                    "org.example.kortex.users.domain.AuthService$RegistrationData");
            var constructor = dataClass.getDeclaredConstructor(User.class, String.class);
            constructor.setAccessible(true);
            Object data = constructor.newInstance(tempUser, "123456");

            var map = (java.util.Map<String, Object>) pendingRegistrations;
            map.put("test-reg-id", data);
        } catch (Exception e) {
            fail("Ошибка настройки теста: " + e.getMessage());
        }

        when(userService.create(any())).thenReturn(testUser);
        when(jwtTokenProvider.createToken(anyString(), anyString())).thenReturn("jwt-token");

        Object result = authService.verifyRegistration(
                new VerifyRegisterRequest("test-reg-id", "123456"),
                response
        );

        LoginResponse loginResponse = (LoginResponse) result;
        assertTrue(loginResponse.success());
    }


    @Test
    void forgotPassword() {
        when(userService.getByEmail("test@example.com")).thenReturn(testUser);
        when(emailSenderService.generateVerificationCode()).thenReturn("654321");

        Object result = authService.forgotPassword("test@example.com");

        PasswordResetResponse response = (PasswordResetResponse) result;
        assertTrue(response.success());
        assertNotNull(response.resetId());
    }


    @Test
    void verifyResetCode() {
        try {
            var field = AuthService.class.getDeclaredField("passwordResets");
            field.setAccessible(true);
            var passwordResets = field.get(authService);

            Class<?> dataClass = Class.forName(
                    "org.example.kortex.users.domain.AuthService$ResetData");
            var constructor = dataClass.getDeclaredConstructor(String.class, String.class);
            constructor.setAccessible(true);
            Object data = constructor.newInstance("test@example.com", "123456");

            var map = (java.util.Map<String, Object>) passwordResets;
            map.put("reset-id", data);
        } catch (Exception e) {
            fail("Ошибка настройки теста: " + e.getMessage());
        }

        Object result = authService.verifyResetCode("reset-id", "123456");

        PasswordResetResponse response = (PasswordResetResponse) result;
        assertTrue(response.success());
        assertEquals("Код подтвержден", response.message());
    }

    @Test
    void resetPassword() {
        try {
            var field = AuthService.class.getDeclaredField("passwordResets");
            field.setAccessible(true);
            var passwordResets = field.get(authService);

            Class<?> dataClass = Class.forName(
                    "org.example.kortex.users.domain.AuthService$ResetData");
            var constructor = dataClass.getDeclaredConstructor(String.class, String.class);
            constructor.setAccessible(true);
            Object data = constructor.newInstance("test@example.com", "123456");

            var map = (java.util.Map<String, Object>) passwordResets;
            map.put("reset-id", data);
        } catch (Exception e) {
            fail("Ошибка настройки теста: " + e.getMessage());
        }

        when(userService.getByEmail("test@example.com")).thenReturn(testUser);
        when(passwordEncoder.encode("newPassword")).thenReturn("encodedNewPassword");
        when(jwtTokenProvider.createToken(anyString(), anyString())).thenReturn("jwt-token");

        Object result = authService.resetPassword(
                new ResetPasswordRequest("reset-id", "newPassword", "newPassword"),
                response
        );

        assertTrue(result instanceof LoginResponse);
        LoginResponse loginResponse = (LoginResponse) result;
        assertTrue(loginResponse.success());
        assertEquals("Пароль успешно изменен", loginResponse.message());

        verify(userService).changePassword(eq(1L), eq("encodedNewPassword"));
    }


}