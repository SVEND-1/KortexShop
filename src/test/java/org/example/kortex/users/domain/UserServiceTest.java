package org.example.kortex.users.domain;

import org.example.kortex.orders.api.dto.OrderResponseDTO;
import org.example.kortex.orders.domain.mapper.OrderMapper;
import org.example.kortex.orders.db.Order;
import org.example.kortex.users.api.dto.user.UserResponse;
import org.example.kortex.users.domain.mapper.UserMapper;
import org.example.kortex.users.db.Role;
import org.example.kortex.users.db.User;
import org.example.kortex.users.db.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private UserMapper userMapper;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private UserResponse testUserResponse;
    private final String testEmail = "test@example.com";
    private final Long testUserId = 1L;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(testUserId);
        testUser.setEmail(testEmail);
        testUser.setName("testUser");
        testUser.setPassword("password123");
        testUser.setAddress("Test Address");

        testUserResponse = new UserResponse(
                testUserId,
                testEmail,
                "Test name",
                Role.ADMIN,
                "Test Address"
                );

        SecurityContextHolder.setContext(securityContext);
    }


    @Test
    void getProfile() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(testEmail);
        when(userRepository.findByEmailEqualsIgnoreCase(testEmail)).thenReturn(testUser);
        when(userMapper.convertEntityToDto(testUser)).thenReturn(testUserResponse);

        UserResponse result = userService.getProfile();

        assertNotNull(result);
        assertEquals(testUserId, result.id());
        assertEquals(testEmail, result.email());
        verify(userRepository).findByEmailEqualsIgnoreCase(testEmail);
        verify(userMapper).convertEntityToDto(testUser);
    }

    @Test
    void changeAddress() {
        String newPassword = "newPassword123";
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(testUser)).thenReturn(testUser);

        User result = userService.changePassword(testUserId, newPassword);

        assertNotNull(result);
        assertEquals(newPassword, testUser.getPassword());
        verify(userRepository).findById(testUserId);
        verify(userRepository, times(2)).save(testUser);
    }

    @Test
    void meOrders() {
        Order order1 = mock(Order.class);
        Order order2 = mock(Order.class);
        List<Order> orders = Arrays.asList(order1, order2);

        OrderResponseDTO dto1 = new OrderResponseDTO(1L,null,null,null,null);
        OrderResponseDTO dto2 = new OrderResponseDTO(2L,null,null,null,null);
        List<OrderResponseDTO> expectedDtos = Arrays.asList(dto1, dto2);

        User userWithOrders = new User();
        userWithOrders.setId(testUserId);
        userWithOrders.setOrders(orders);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(testEmail);
        when(userRepository.findByIdWithOrders(testEmail)).thenReturn(userWithOrders);
        when(orderMapper.toDtoList(orders)).thenReturn(expectedDtos);

        List<OrderResponseDTO> result = userService.meOrders();

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(userRepository).findByIdWithOrders(testEmail);
        verify(orderMapper).toDtoList(orders);
    }

    @Test
    void getCurrentUser() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(testEmail);
        when(userRepository.findByEmailEqualsIgnoreCase(testEmail)).thenReturn(testUser);

        User result = userService.getCurrentUser();

        assertNotNull(result);
        assertEquals(testUserId, result.getId());
        assertEquals(testEmail, result.getEmail());
        verify(userRepository).findByEmailEqualsIgnoreCase(testEmail);
    }

    @Test
    void getCurrentUserCart() {
        User userWithCart = new User();
        userWithCart.setId(testUserId);
        userWithCart.setEmail(testEmail);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(testEmail);
        when(userRepository.findByIdWithCart(testEmail)).thenReturn(userWithCart);

        User result = userService.getCurrentUserCart();

        assertNotNull(result);
        assertEquals(testUserId, result.getId());
        verify(userRepository).findByIdWithCart(testEmail);

    }

    @Test
    void getCurrentUserOrders() {
        User userWithOrders = new User();
        userWithOrders.setId(testUserId);
        userWithOrders.setEmail(testEmail);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(testEmail);
        when(userRepository.findByIdWithOrders(testEmail)).thenReturn(userWithOrders);

        User result = userService.getCurrentUserOrders();

        assertNotNull(result);
        assertEquals(testUserId, result.getId());
        verify(userRepository).findByIdWithOrders(testEmail);
    }

    @Test
    void getById() {
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));

        User result = userService.getById(testUserId);

        assertNotNull(result);
        assertEquals(testUserId, result.getId());
        verify(userRepository).findById(testUserId);
    }

    @Test
    void getByEmail() {
        when(userRepository.findByEmailEqualsIgnoreCase(testEmail)).thenReturn(testUser);

        User result = userService.getByEmail(testEmail);

        assertNotNull(result);
        assertEquals(testEmail, result.getEmail());
        verify(userRepository).findByEmailEqualsIgnoreCase(testEmail);
    }

    @Test
    void create() {
        when(userRepository.save(testUser)).thenReturn(testUser);

        User result = userService.create(testUser);

        assertNotNull(result);
        assertEquals(testUserId, result.getId());
        verify(userRepository).save(testUser);
    }

    @Test
    void update() {
        User updatedUserData = new User();
        updatedUserData.setEmail("new@example.com");
        updatedUserData.setName("New Name");
        updatedUserData.setPassword("newpassword");
        updatedUserData.setAddress("New Address");


        User existingUser = new User();
        existingUser.setId(testUserId);
        existingUser.setEmail("old@example.com");
        existingUser.setName("Old Name");
        existingUser.setPassword("oldpassword");
        existingUser.setAddress("Old Address");
        existingUser.setOrders(Collections.emptyList());
        existingUser.setCart(null);
        existingUser.setRoleRequests(Collections.emptyList());

        when(userRepository.findById(testUserId)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenReturn(existingUser);

        User result = userService.update(testUserId, updatedUserData);

        assertNotNull(result);
        verify(userRepository).findById(testUserId);
        verify(userRepository, times(2)).save(any(User.class));
    }

    @Test
    void changePassword() {
        String newPassword = "newPassword123";
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(testUser)).thenReturn(testUser);

        User result = userService.changePassword(testUserId, newPassword);

        assertNotNull(result);
        verify(userRepository).findById(testUserId);
        verify(userRepository, times(2)).save(testUser);
    }
}