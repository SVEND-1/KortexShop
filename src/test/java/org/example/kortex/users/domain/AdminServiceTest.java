package org.example.kortex.users.domain;

import org.example.kortex.users.db.Role;
import org.example.kortex.users.db.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private AdminService adminService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setName("Test User");
    }

    @Test
    void appoint() {
        testUser.setRole(Role.USER);
        User updatedUser = new User();
        updatedUser.setId(1L);
        updatedUser.setRole(Role.SELLER);

        when(userService.getById(1L)).thenReturn(testUser);
        when(userService.update(eq(1L), any(User.class))).thenReturn(updatedUser);

        // Act
        adminService.appoint(1L, Role.SELLER);

        // Assert
        verify(userService).getById(1L);
        verify(userService).update(eq(1L), argThat(user ->
                user.getRole() == Role.SELLER
        ));
    }



    @Test
    void downgrade() {
        // Arrange
        testUser.setRole(Role.USER);
        User updatedUser = new User();
        updatedUser.setId(1L);
        updatedUser.setRole(Role.SELLER);

        when(userService.getById(1L)).thenReturn(testUser);
        when(userService.update(eq(1L), any(User.class))).thenReturn(updatedUser);

        // Act
        adminService.downgrade(1L, Role.USER);

        // Assert
        verify(userService).getById(1L);
        verify(userService).update(eq(1L), argThat(user ->
                user.getRole() == Role.USER
        ));
    }


}