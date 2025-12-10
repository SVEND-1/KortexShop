package org.example.kortex.users.domain;

import org.example.kortex.users.db.User;
import org.example.kortex.users.db.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.persistence.EntityNotFoundException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class UserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private SecurityContext securityContext;
    @Mock private Authentication authentication;
    @InjectMocks private UserService userService;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
    }

    @Test
    void getCurrentUser() {
        String email = "test@example.com";
        User user = new User();
        user.setEmail(email);

        when(authentication.getName()).thenReturn(email);
        when(userRepository.findByEmailEqualsIgnoreCase(email)).thenReturn(user);

        User result = userService.getCurrentUser();

        assertEquals(email, result.getEmail());
    }

    @Test
    void getCurrentUserCart() {
        String email = "test@example.com";
        User user = new User();
        user.setId(1L);
        user.setEmail(email);

        when(authentication.getName()).thenReturn(email);
        when(userRepository.findByEmailEqualsIgnoreCase(email)).thenReturn(user);
        when(userRepository.findByIdWithCart(email)).thenReturn(user);

        User result = userService.getCurrentUserCart();

        assertNotNull(result);
    }

    @Test
    void getCurrentUserOrders() {
        String email = "test@example.com";
        User user = new User();
        user.setId(1L);
        user.setEmail(email);

        when(authentication.getName()).thenReturn(email);
        when(userRepository.findByEmailEqualsIgnoreCase(email)).thenReturn(user);
        when(userRepository.findByIdWithOrders(email)).thenReturn(user);

        User result = userService.getCurrentUserOrders();

        assertNotNull(result);
    }

    @Test
    void getCurrentUserFull() {
        String email = "test@example.com";
        User user = new User();
        user.setId(1L);
        user.setEmail(email);

        when(authentication.getName()).thenReturn(email);
        when(userRepository.findByEmailEqualsIgnoreCase(email)).thenReturn(user);
        when(userRepository.findByIdWithEverything(email)).thenReturn(user);

        User result = userService.getCurrentUserFull();

        assertNotNull(result);
    }

    @Test
    void getById() {
        Long userId = 1L;
        User user = new User();
        user.setId(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        User result = userService.getById(userId);

        assertEquals(userId, result.getId());
    }


    @Test
    void appoint() {
        Long userId = 1L;
        User user = new User();
        user.setId(userId);
        user.setRole(User.Role.USER);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        User result = userService.appoint(userId, User.Role.SELLER);

        assertEquals(User.Role.SELLER, result.getRole());
    }

    @Test
    void downgrade() {
        Long userId = 1L;
        User user = new User();
        user.setId(userId);
        user.setRole(User.Role.SELLER);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        User result = userService.downgrade(userId, User.Role.SELLER);

        assertEquals(User.Role.USER, result.getRole());
    }

    @Test
    void getByEmail() {
        String email = "test@example.com";
        User user = new User();
        user.setEmail(email);

        when(userRepository.findByEmailEqualsIgnoreCase(email)).thenReturn(user);

        User result = userService.getByEmail(email);

        assertEquals(email, result.getEmail());
    }

    @Test
    void create() {
        User user = new User();
        user.setEmail("test@example.com");

        when(userRepository.save(user)).thenReturn(user);

        User result = userService.create(user);

        assertNotNull(result);
    }

    @Test
    void update() {
        Long userId = 1L;
        User existingUser = new User();
        existingUser.setId(userId);

        User updatedUser = new User();
        updatedUser.setEmail("new@example.com");
        updatedUser.setName("New Name");
        updatedUser.setPassword("newpass");
        updatedUser.setRole(User.Role.USER);

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);

        User result = userService.update(userId, updatedUser);

        assertNotNull(result);
    }

}