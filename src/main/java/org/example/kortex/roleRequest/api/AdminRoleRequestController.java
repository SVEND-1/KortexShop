package org.example.kortex.roleRequest.api;

import lombok.extern.slf4j.Slf4j;
import org.example.kortex.users.api.RoleRequestFilter;
import org.example.kortex.roleRequest.api.dto.RoleRequestMapper;
import org.example.kortex.users.db.Role;
import org.example.kortex.roleRequest.db.RoleRequest;
import org.example.kortex.roleRequest.domain.RoleRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityNotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Slf4j
@RestController
@RequestMapping("/api/admin/role-request")
public class AdminRoleRequestController {//Перенести в другую
    private final RoleRequestService roleRequestService;
    private final RoleRequestMapper roleRequestMapper;

    @Autowired
    public AdminRoleRequestController(RoleRequestService roleRequestService,RoleRequestMapper roleRequestMapper) {
        this.roleRequestService = roleRequestService;
        this.roleRequestMapper = roleRequestMapper;
    }

    @GetMapping
    public CompletableFuture<ResponseEntity<Map<String,Object>>> getAdminRoleRequest(@RequestParam(name = "role",required = false) Role role,
                                                                                     @RequestParam(name = "status",required = false) RoleRequest.Status status,
                                                                                     @RequestParam(name = "actionType",required = false) RoleRequest.TypeAction actionType,
                                                                                     @RequestParam(name = "pageSize",required = false) Integer pageSize,
                                                                                     @RequestParam(name = "pageNumber", required = false) Integer pageNumber) {
        RoleRequestFilter filter = new RoleRequestFilter(role, status, actionType, pageSize, pageNumber);

        return roleRequestService.getRoleRequestsPage(filter)
                .thenApply(roleRequests -> {
                   Map<String,Object> response = roleRequestMapper.toPageResponse(roleRequests);
                   return ResponseEntity.ok(response);
                })
                .exceptionally(ex ->{
                    Map<String,Object> error = new HashMap<>();
                    error.put("error","Ошибка при загрузке всех заявок");
                    error.put("message",ex.getMessage());
                    log.info("Ошибка при загрузке всех заявок: {}", error);
                    return ResponseEntity.badRequest().body(error);
                });
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getAdminRoleRequest(@PathVariable("id") long id){
        return ResponseEntity.ok().body(roleRequestMapper.toDto(roleRequestService.getRoleRequest(id)));
    }

    @PostMapping("/{id}/downgrade")
    public ResponseEntity<?> downgradeAdminRoleRequest(@PathVariable("id") long id) {
        return ResponseEntity.ok().body(roleRequestService.downgradeRole(id));
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<?> approveAdminRoleRequest(@PathVariable("id") long id) {
        return ResponseEntity.ok().body(roleRequestService.approveRole(id));
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<?> rejectAdminRoleRequest(@PathVariable("id") long id) {
        return ResponseEntity.ok().body(roleRequestService.rejectRole(id));
    }
}

