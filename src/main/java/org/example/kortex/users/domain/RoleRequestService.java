package org.example.kortex.users.domain;

import org.example.kortex.users.api.RoleRequestFilter;
import org.example.kortex.users.db.RoleRequest;
import org.example.kortex.users.db.RoleRequestRepository;
import org.example.kortex.users.db.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import java.util.List;

@Service
public class RoleRequestService {
    private final UserService userService;
    private final RoleRequestRepository roleRequestRepository;
    private final Logger log = LoggerFactory.getLogger(UserService.class);

    @Autowired
    public RoleRequestService(UserService userService, RoleRequestRepository roleRequestRepository) {
        this.userService = userService;
        this.roleRequestRepository = roleRequestRepository;
    }
    //User
    //подать заявку на курьера и продавца и на снятия роли
    public RoleRequest createRoleRequest(User currentUser, User.Role requestedRole,
                                         RoleRequest.TypeAction typeAction, String message) {
        log.info("Создания подачи заявки на роль");

        if (hasPendingRequestForSameAction(currentUser)) {
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

    //Admin
    //все запросы по роли курьер и продавец
    //все запросы по статусу
    //все на снятия с роли

    public RoleRequest getRoleRequest(Long roleRequestId) {
        return roleRequestRepository.findById(roleRequestId).orElseThrow(() -> new EntityNotFoundException("Заявка не найдена"));
    }

    public List<RoleRequest> getRoleRequests(RoleRequestFilter filter) {
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


    private boolean hasPendingRequestForSameAction(User currentUser){
         return currentUser.getRoleRequests()
                .stream()
                .anyMatch(roleRequest -> roleRequest.getStatus() == RoleRequest.Status.PENDING);
    }
}
