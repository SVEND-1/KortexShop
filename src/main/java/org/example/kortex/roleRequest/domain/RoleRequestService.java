package org.example.kortex.roleRequest.domain;

import lombok.extern.slf4j.Slf4j;
import org.example.kortex.notify.event.NotifyEvent;
import org.example.kortex.notify.event.NotifyType;
import org.example.kortex.notify.kafka.NotifyKafkaProducer;
import org.example.kortex.roleRequest.api.dto.RolePageResponse;
import org.example.kortex.roleRequest.domain.exception.PendingRequestException;
import org.example.kortex.roleRequest.domain.mapper.RoleRequestMapper;
import org.example.kortex.roleRequest.api.dto.RoleRequestResponse;
import org.example.kortex.roleRequest.api.dto.RoleRequestFilter;
import org.example.kortex.users.db.Role;
import org.example.kortex.roleRequest.db.RoleRequest;
import org.example.kortex.roleRequest.db.RoleRequestRepository;
import org.example.kortex.users.db.User;
import org.example.kortex.users.domain.AdminService;
import org.example.kortex.users.domain.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
public class RoleRequestService {
    private final RoleRequestRepository roleRequestRepository;
    private final AdminService adminService;
    private final RoleRequestMapper roleRequestMapper;
    private final UserService userService;
    private final NotifyKafkaProducer kafkaProducer;

    @Autowired
    public RoleRequestService(RoleRequestRepository roleRequestRepository,
                              AdminService adminService, RoleRequestMapper roleRequestMapper, UserService userService, NotifyKafkaProducer notifyKafkaProducer) {
        this.roleRequestRepository = roleRequestRepository;
        this.adminService = adminService;
        this.roleRequestMapper = roleRequestMapper;
        this.userService = userService;
        this.kafkaProducer = notifyKafkaProducer;
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
            int pageSize = filter.pageSize() != null ? filter.pageSize() : 10;
            int pageNumber = filter.pageNumber() != null ? filter.pageNumber() : 0;
            Pageable pageable = Pageable.ofSize(pageSize).withPage(pageNumber);

            Page<RoleRequest> roleRequests = roleRequestRepository.findSearchFilter(
                    filter.role(),
                    filter.status(),
                    filter.actionType(),
                    pageable
            );

            RolePageResponse response = roleRequestMapper.toPageResponse(roleRequests);
            log.info("Успешно получено {} заявок", roleRequests.getTotalElements());

            return response;
        });
    }

    public RoleRequestResponse create(Role requestedRole,
                                         RoleRequest.TypeAction typeAction, String message) {
        try {
            User currentUser = userService.getCurrentUser();

            if (hasPendingRequestForSameAction(currentUser.getId())) {
                log.warn("У вас уже есть активная заявка на это действие");
                throw new PendingRequestException("У вас уже есть активная заявка на это действие");
            }

            RoleRequest roleRequest = RoleRequest.builder()
                    .user(currentUser)
                    .requestedRole(requestedRole)
                    .typeAction(typeAction)
                    .message(message)
                    .status(RoleRequest.Status.PENDING)
                    .createdAt(LocalDateTime.now())
                    .build();
            return roleRequestMapper.toDto(roleRequestRepository.save(roleRequest));
        }catch (Exception ex){
            log.error("Не удалось создать заявку, ex={} ", ex.getMessage());
            throw new IllegalArgumentException("Ошибка в заявке на создания role ex=" + ex.getMessage(),ex);
        }
    }


    @Transactional
    public RoleRequestResponse downgradeRole(Long roleRequestId) {
        try {
            RoleRequest roleRequest = roleRequestRepository.findById(roleRequestId)
                    .orElseThrow(() -> new EntityNotFoundException("Запрос не найден"));

            adminService.downgrade(roleRequest.getUser().getId(), roleRequest.getRequestedRole());

            roleRequest.setStatus(RoleRequest.Status.APPROVED);
            RoleRequest savedRoleRequest = roleRequestRepository.save(roleRequest);

            notify(roleRequest,Map.of("userName", savedRoleRequest.getUser().getName()));
            return roleRequestMapper.toDto(savedRoleRequest);
        }
        catch (Exception ex){
            log.error("Ошибка понижения пользователя, ex={} ", ex.getMessage());
            throw new RuntimeException("Не удалось понижить пользователя: " + ex.getMessage());
        }
    }

    @Transactional
    public RoleRequestResponse approveRole(Long roleRequestId) {
        try {
            RoleRequest roleRequest = roleRequestRepository.findById(roleRequestId)
                    .orElseThrow(() -> new EntityNotFoundException("Запрос не найден"));

            adminService.appoint(roleRequest.getUser().getId(), roleRequest.getRequestedRole());

            roleRequest.setStatus(RoleRequest.Status.APPROVED);
            RoleRequest savedRoleRequest = roleRequestRepository.save(roleRequest);

            notify(roleRequest, Map.of(
                    "userName", savedRoleRequest.getUser().getName(),
                    "newRole", savedRoleRequest.getRequestedRole().name()));

            return roleRequestMapper.toDto(savedRoleRequest);
        }catch (Exception ex){
            log.error("Ошибка повышение пользователя, ex={} ", ex.getMessage());
            throw new RuntimeException("Не повысить пользователя: " + ex.getMessage());
        }
    }

    @Transactional
    public RoleRequestResponse rejectRole(Long roleRequestId) {
        try {
            RoleRequest roleRequest = roleRequestRepository.findById(roleRequestId)
                    .orElseThrow(() -> new EntityNotFoundException("Запрос не найден"));

            roleRequest.setStatus(RoleRequest.Status.REJECTED);
            RoleRequest savedRoleRequest = roleRequestRepository.save(roleRequest);

            notify(roleRequest,Map.of("userName", savedRoleRequest.getUser().getName()));
            return roleRequestMapper.toDto(savedRoleRequest);
        }catch (Exception ex){
            log.error("Ошибка отмены заявки пользователя, ex={} ", ex.getMessage());
            throw new RuntimeException("Не удалось отклонить заявку: " + ex.getMessage());
        }
    }


    //================================Service Methods================================================

    private boolean hasPendingRequestForSameAction(Long userId) {
        return roleRequestRepository.existsByUserIdAndStatus(userId, RoleRequest.Status.PENDING);
    }

    private void notify(RoleRequest roleRequest,Map<String,String> message) {
        NotifyEvent notifyEvent = new NotifyEvent(
                roleRequest.getUser().getEmail(),
                message,
                NotifyType.REQUEST_REJECTED
        );
        kafkaProducer.sendMessageToKafka(notifyEvent);
    }
}
