package org.example.kortex.users.domain;

import lombok.extern.slf4j.Slf4j;
import org.example.kortex.users.api.RoleRequestFilter;
import org.example.kortex.users.db.RoleRequest;
import org.example.kortex.users.db.RoleRequestRepository;
import org.example.kortex.users.db.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.util.List;

@Slf4j
@Service
@Transactional
public class RoleRequestService {
    private final UserService userService;
    private final RoleRequestRepository roleRequestRepository;

    @Autowired
    public RoleRequestService(UserService userService, RoleRequestRepository roleRequestRepository) {
        this.userService = userService;
        this.roleRequestRepository = roleRequestRepository;
    }

    public List<RoleRequest> getAllRoleRequestsByUserId(Long userId) {
        return roleRequestRepository.getAllByUserId(userId);
    }

    public RoleRequest createRoleRequest(User currentUser, User.Role requestedRole,
                                         RoleRequest.TypeAction typeAction, String message) {
        log.info("Создания подачи заявки на роль");

        if (hasPendingRequestForSameAction(currentUser.getId())) {
            log.warn("У вас уже есть активная заявка на это действие");
            throw new IllegalStateException("У вас уже есть активная заявка на это действие");
        }

        RoleRequest roleRequest = new RoleRequest();
        roleRequest.setUser(currentUser);
        roleRequest.setRequestedRole(requestedRole);
        roleRequest.setTypeAction(typeAction);
        roleRequest.setMessage(message);
        roleRequest.setStatus(RoleRequest.Status.PENDING);

        RoleRequest savedRoleRequest = roleRequestRepository.save(roleRequest);
        log.info("Заявка создана id: " + savedRoleRequest.getId());
        return savedRoleRequest;
    }


    public RoleRequest getRoleRequest(Long roleRequestId) {
        return roleRequestRepository.findById(roleRequestId).orElseThrow(() -> new EntityNotFoundException("Заявка не найдена"));
    }

    public Page<RoleRequest> getRoleRequests(RoleRequestFilter filter) {
        int pageSize = filter.pageSize() != null ? filter.pageSize() : 10;
        int pageNumber = filter.pageNumber() != null ? filter.pageNumber() : 0;

        Pageable pageable = Pageable.ofSize(pageSize).withPage(pageNumber);

        return roleRequestRepository.findSearchFilter(filter.role(),filter.status(),filter.typeAction() ,pageable);
    }

    public RoleRequest downgradeRole(Long roleRequestId) {
        RoleRequest roleRequest = roleRequestRepository.findById(roleRequestId)
                .orElseThrow(() -> new EntityNotFoundException("Запрос не найден"));

        userService.downgrade(roleRequest.getUser().getId(),roleRequest.getRequestedRole());

        roleRequest.setStatus(RoleRequest.Status.APPROVED);
        return roleRequestRepository.save(roleRequest);
    }

    public RoleRequest approveRole(Long roleRequestId) {
        RoleRequest roleRequest = roleRequestRepository.findById(roleRequestId)
                .orElseThrow(() -> new EntityNotFoundException("Запрос не найден"));

        userService.appoint(roleRequest.getUser().getId(),roleRequest.getRequestedRole());

        roleRequest.setStatus(RoleRequest.Status.APPROVED);
        return roleRequestRepository.save(roleRequest);
    }
    public RoleRequest rejectRole(Long roleRequestId) {
        RoleRequest roleRequest = roleRequestRepository.findById(roleRequestId)
                .orElseThrow(() -> new EntityNotFoundException("Запрос не найден"));

        roleRequest.setStatus(RoleRequest.Status.REJECTED);
        return roleRequestRepository.save(roleRequest);
    }


    private boolean hasPendingRequestForSameAction(Long userId) {
        // Используем репозиторий вместо обращения к коллекции пользователя
        return roleRequestRepository.existsByUserIdAndStatus(userId, RoleRequest.Status.PENDING);
    }
}
