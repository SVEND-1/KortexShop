package org.example.kortex.users.domain;

import org.example.kortex.users.db.RoleRequest;
import org.example.kortex.users.db.RoleRequestRepository;
import org.example.kortex.users.db.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoleRequestServiceTest {
    @Mock
    private UserService userService;

    @Mock
    private RoleRequestRepository roleRequestRepository;

    @InjectMocks
    private RoleRequestService roleRequestService;

    @Test
    void createRoleRequest() {
        User currentUser = new User();
        currentUser.setId(1L);

        RoleRequest savedRequest = new RoleRequest();
        savedRequest.setId(1L);
        savedRequest.setStatus(RoleRequest.Status.PENDING);

        when(roleRequestRepository.save(any(RoleRequest.class)))
                .thenReturn(savedRequest);

        RoleRequest result = roleRequestService.createRoleRequest(
                currentUser,
                User.Role.SELLER,
                RoleRequest.TypeAction.ENHANCE,
                "Хочу стать продавцом"
        );

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(RoleRequest.Status.PENDING, result.getStatus());
        verify(roleRequestRepository, times(1)).save(any(RoleRequest.class));
    }

    @Test
    void getAllRoleRequestsByUserId() {
        Long userId = 1L;

        RoleRequest request1 = new RoleRequest();
        request1.setId(1L);
        request1.setStatus(RoleRequest.Status.PENDING);

        RoleRequest request2 = new RoleRequest();
        request2.setId(2L);
        request2.setStatus(RoleRequest.Status.APPROVED);

        when(roleRequestRepository.getAllByUserId(userId))
                .thenReturn(Arrays.asList(request1, request2));

        List<RoleRequest> result = roleRequestService.getAllRoleRequestsByUserId(userId);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).getId());
        assertEquals(RoleRequest.Status.PENDING, result.get(0).getStatus());
        assertEquals(2L, result.get(1).getId());
        assertEquals(RoleRequest.Status.APPROVED, result.get(1).getStatus());
        verify(roleRequestRepository, times(1)).getAllByUserId(userId);
    }

    @Test
    void getRoleRequest() {
        Long requestId = 1L;
        RoleRequest roleRequest = new RoleRequest();
        roleRequest.setId(requestId);

        when(roleRequestRepository.findById(requestId))
                .thenReturn(Optional.of(roleRequest));

        RoleRequest result = roleRequestService.getRoleRequest(requestId);

        assertNotNull(result);
        assertEquals(requestId, result.getId());
        verify(roleRequestRepository, times(1)).findById(requestId);
    }


    @Test
    void downgradeRole() {
        Long requestId = 1L;
        User user = new User();
        user.setId(1L);
        user.setRole(User.Role.SELLER);

        RoleRequest roleRequest = new RoleRequest();
        roleRequest.setId(requestId);
        roleRequest.setUser(user);
        roleRequest.setRequestedRole(User.Role.SELLER);
        roleRequest.setStatus(RoleRequest.Status.PENDING);

        User downgradedUser = new User();
        downgradedUser.setRole(User.Role.USER);

        when(roleRequestRepository.findById(requestId))
                .thenReturn(Optional.of(roleRequest));
        when(userService.downgrade(user.getId(), User.Role.SELLER))
                .thenReturn(downgradedUser);
        when(roleRequestRepository.save(roleRequest))
                .thenReturn(roleRequest);

        RoleRequest result = roleRequestService.downgradeRole(requestId);

        assertNotNull(result);
        assertEquals(RoleRequest.Status.APPROVED, result.getStatus());
        verify(userService, times(1)).downgrade(user.getId(), User.Role.SELLER);
        verify(roleRequestRepository, times(1)).save(roleRequest);
    }

    @Test
    void approveRole() {
        Long requestId = 1L;
        User user = new User();
        user.setId(1L);
        user.setRole(User.Role.USER);

        RoleRequest roleRequest = new RoleRequest();
        roleRequest.setId(requestId);
        roleRequest.setUser(user);
        roleRequest.setRequestedRole(User.Role.SELLER);
        roleRequest.setStatus(RoleRequest.Status.PENDING);

        User appointedUser = new User();
        appointedUser.setRole(User.Role.SELLER);

        when(roleRequestRepository.findById(requestId))
                .thenReturn(Optional.of(roleRequest));
        when(userService.appoint(user.getId(), User.Role.SELLER))
                .thenReturn(appointedUser);
        when(roleRequestRepository.save(roleRequest))
                .thenReturn(roleRequest);

        RoleRequest result = roleRequestService.approveRole(requestId);

        assertNotNull(result);
        assertEquals(RoleRequest.Status.APPROVED, result.getStatus());
        verify(userService, times(1)).appoint(user.getId(), User.Role.SELLER);
        verify(roleRequestRepository, times(1)).save(roleRequest);
    }

    @Test
    void rejectRole() {
        Long requestId = 1L;
        RoleRequest roleRequest = new RoleRequest();
        roleRequest.setId(requestId);
        roleRequest.setStatus(RoleRequest.Status.PENDING);

        when(roleRequestRepository.findById(requestId))
                .thenReturn(Optional.of(roleRequest));
        when(roleRequestRepository.save(roleRequest))
                .thenReturn(roleRequest);

        RoleRequest result = roleRequestService.rejectRole(requestId);

        assertNotNull(result);
        assertEquals(RoleRequest.Status.REJECTED, result.getStatus());
        verify(roleRequestRepository, times(1)).save(roleRequest);
        verify(userService, never()).appoint(anyLong(), any());
        verify(userService, never()).downgrade(anyLong(), any());
    }
}