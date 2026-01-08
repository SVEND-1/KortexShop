package org.example.kortex.roleRequest.domain;

import lombok.extern.slf4j.Slf4j;
import org.example.kortex.roleRequest.api.dto.RolePageResponse;
import org.example.kortex.roleRequest.api.dto.RoleRequestMapper;
import org.example.kortex.roleRequest.api.dto.RoleRequestResponse;
import org.example.kortex.roleRequest.api.RoleRequestFilter;
import org.example.kortex.users.db.Role;
import org.example.kortex.roleRequest.db.RoleRequest;
import org.example.kortex.roleRequest.db.RoleRequestRepository;
import org.example.kortex.users.db.User;
import org.example.kortex.users.domain.AdminService;
import org.example.kortex.notify.kafka.EmailSenderService;
import org.example.kortex.users.domain.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
public class RoleRequestService {
    private final RoleRequestRepository roleRequestRepository;
    private final EmailSenderService emailSenderService;
    private final AdminService adminService;
    private final RoleRequestMapper roleRequestMapper;
    private final UserService userService;

    @Autowired
    public RoleRequestService(RoleRequestRepository roleRequestRepository, EmailSenderService emailSenderService,
                              AdminService adminService, RoleRequestMapper roleRequestMapper, UserService userService) {
        this.roleRequestRepository = roleRequestRepository;
        this.emailSenderService = emailSenderService;
        this.adminService = adminService;
        this.roleRequestMapper = roleRequestMapper;
        this.userService = userService;
    }

    //================================Controller Methods================================================


    public List<RoleRequestResponse> getAllRoleRequestsByUserId() {
        return roleRequestMapper.toDtoList(roleRequestRepository.getAllByUserId(userService.getCurrentUser().getId()));
    }


    public RoleRequestResponse getRoleRequest(Long roleRequestId) {
        return roleRequestMapper.toDto(roleRequestRepository.findById(roleRequestId).orElseThrow(() -> new EntityNotFoundException("Заявка не найдена")));
    }

    @Async("asyncExecutor")
    public CompletableFuture<RolePageResponse> getRoleRequestsPage(RoleRequestFilter filter) {
        return CompletableFuture.supplyAsync(() -> {
            log.info("Получение заявок с фильтром: {}", filter);

            int pageSize = filter.pageSize() != null ? filter.pageSize() : 10;
            int pageNumber = filter.pageNumber() != null ? filter.pageNumber() : 0;
            Pageable pageable = Pageable.ofSize(pageSize).withPage(pageNumber);

            Page<RoleRequest> roleRequests = roleRequestRepository.findSearchFilter(
                    filter.role(),
                    filter.status(),
                    filter.typeAction(),
                    pageable
            );

            RolePageResponse response = roleRequestMapper.toPageResponse(roleRequests);
            log.info("Успешно получено {} заявок", roleRequests.getTotalElements());

            return response;
        });
    }

    public RoleRequestResponse create(Role requestedRole,
                                         RoleRequest.TypeAction typeAction, String message) {
        log.info("Создания подачи заявки на роль");

        try {
            User currentUser = userService.getCurrentUser();

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
            log.info("Заявка создана id: {}", savedRoleRequest.getId());
            return roleRequestMapper.toDto(savedRoleRequest);
        }catch (Exception ex){
            log.error("Не удалось создать заявку, ex={} ", ex.getMessage());
            throw new IllegalArgumentException("Ошибка в заявке на создания role",ex);
        }
    }


    @Transactional
    public RoleRequest downgradeRole(Long roleRequestId) {//TODO RESPONSE
        try {
            RoleRequest roleRequest = roleRequestRepository.findById(roleRequestId)
                    .orElseThrow(() -> new EntityNotFoundException("Запрос не найден"));

            adminService.downgrade(roleRequest.getUser().getId(), roleRequest.getRequestedRole());

            roleRequest.setStatus(RoleRequest.Status.APPROVED);

            RoleRequest savedRoleRequest = roleRequestRepository.save(roleRequest);

            log.info("Понижения пользователя с id={}", roleRequest.getUser().getId());

            emailSenderService.sendMessage(roleRequest.getUser().getEmail(), "Заявка одобрена", "Вы получили понижение");
            return savedRoleRequest;
        }
        catch (Exception ex){
            log.error("Ошибка понижения пользователя, ex={} ", ex.getMessage());
            return null;
        }
    }

    @Transactional
    public RoleRequest approveRole(Long roleRequestId) {//TODO RESPONSE
        try {
            RoleRequest roleRequest = roleRequestRepository.findById(roleRequestId)
                    .orElseThrow(() -> new EntityNotFoundException("Запрос не найден"));

            adminService.appoint(roleRequest.getUser().getId(), roleRequest.getRequestedRole());

            roleRequest.setStatus(RoleRequest.Status.APPROVED);

            RoleRequest savedRoleRequest = roleRequestRepository.save(roleRequest);

            log.info("Повышение пользователя с id={}", roleRequest.getUser().getId());

            emailSenderService.sendMessage(roleRequest.getUser().getEmail(), "Заявка одобрена", "Вы получили повышение");

            return savedRoleRequest;
        }catch (Exception ex){
            log.error("Ошибка повышение пользователя, ex={} ", ex.getMessage());
            return null;
        }
    }

    @Transactional
    public RoleRequest rejectRole(Long roleRequestId) {//TODO RESPONSE
        try {
            RoleRequest roleRequest = roleRequestRepository.findById(roleRequestId)
                    .orElseThrow(() -> new EntityNotFoundException("Запрос не найден"));

            roleRequest.setStatus(RoleRequest.Status.REJECTED);

            RoleRequest savedRoleRequest = roleRequestRepository.save(roleRequest);

            log.info("Запрос пользователя на смену роли отклонен id={}", roleRequestId);

            emailSenderService.sendMessage(roleRequest.getUser().getEmail(), "Ваша заявка отклонена", "Можете отправить повторно позже");
            return savedRoleRequest;
        }catch (Exception ex){
            log.error("Ошибка отмены заявки пользователя, ex={} ", ex.getMessage());
            return null;
        }
    }


    //================================Service Methods================================================

    private boolean hasPendingRequestForSameAction(Long userId) {
        return roleRequestRepository.existsByUserIdAndStatus(userId, RoleRequest.Status.PENDING);
    }
}
