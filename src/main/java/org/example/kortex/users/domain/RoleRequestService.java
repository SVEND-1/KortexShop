package org.example.kortex.users.domain;

import org.example.kortex.users.api.RoleRequestFilter;
import org.example.kortex.users.db.RoleRequest;
import org.example.kortex.users.db.RoleRequestRepository;
import org.example.kortex.users.db.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import java.util.ArrayList;
import java.util.List;

@Service
public class RoleRequestService {
    private final UserService userService;
    private final RoleRequestRepository roleRequestRepositoryRepository;

    @Autowired
    public RoleRequestService(UserService userService, RoleRequestRepository roleRequestRepositoryRepository) {
        this.userService = userService;
        this.roleRequestRepositoryRepository = roleRequestRepositoryRepository;
    }
    //User
    //подать заявку на курьера и продавца и на снятия роли
    public RoleRequest createRoleRequest(RoleRequest roleRequest) {
        return roleRequestRepositoryRepository.save(roleRequest);
    }


    //Admin
    //все запросы по роли курьер и продавец
    //все запросы по статусу
    //все на снятия с роли
    public List<RoleRequest> getRoleRequests(RoleRequestFilter filter) {
        int pageSize = filter.pageSize() != null ? filter.pageSize() : 10;
        int pageNumber = filter.pageNumber() != null ? filter.pageNumber() : 0;
        Pageable pageable = Pageable.ofSize(pageSize).withPage(pageNumber);

        return roleRequestRepositoryRepository.findSearchFilter(filter.role(),filter.status(),  pageable);
    }


    public RoleRequest takeOffRole(Long roleRequestId) {
        RoleRequest roleRequest = roleRequestRepositoryRepository.findById(roleRequestId)
                .orElseThrow(() -> new EntityNotFoundException("Запрос не найден"));

        userService.appoint(roleRequest.getUser().getId(),roleRequest.getRequestedRole());

        roleRequest.setStatus(RoleRequest.Status.APPROVED);
        return roleRequestRepositoryRepository.save(roleRequest);
    }

    public RoleRequest approveRole(Long roleRequestId) {
        RoleRequest roleRequest = roleRequestRepositoryRepository.findById(roleRequestId)
                .orElseThrow(() -> new EntityNotFoundException("Запрос не найден"));

        userService.appoint(roleRequest.getUser().getId(),roleRequest.getRequestedRole());

        roleRequest.setStatus(RoleRequest.Status.APPROVED);
        return roleRequestRepositoryRepository.save(roleRequest);
    }
    public RoleRequest rejectRole(Long roleRequestId) {
        RoleRequest roleRequest = roleRequestRepositoryRepository.findById(roleRequestId)
                .orElseThrow(() -> new EntityNotFoundException("Запрос не найден"));

        roleRequest.setStatus(RoleRequest.Status.REJECTED);
        return roleRequestRepositoryRepository.save(roleRequest);
    }

}
