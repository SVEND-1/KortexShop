package org.example.kortex.roleRequest.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.kortex.roleRequest.api.dto.RolePageResponse;
import org.example.kortex.roleRequest.api.dto.RoleRequestFilter;
import org.example.kortex.roleRequest.domain.RoleRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/admin/role-request")
@Tag(name = "AdminRole",description = "Работа с заявками на роль в админе")
public class AdminRoleRequestController {//Перенести в другую
    private final RoleRequestService roleRequestService;

    @Autowired
    public AdminRoleRequestController(RoleRequestService roleRequestService) {
        this.roleRequestService = roleRequestService;
    }

    @Operation(summary = "Получение заявок с фильтром")
    @GetMapping
    public CompletableFuture<ResponseEntity<RolePageResponse>> getAdminRoleRequest(@ModelAttribute RoleRequestFilter filter) {
        return roleRequestService.getRoleRequestsPage(filter)
                .thenApply(ResponseEntity::ok)
                .exceptionally(ex -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
    }

    @Operation(summary = "Получение деталей заявки")
    @GetMapping("/{id}")
    public ResponseEntity<?> getAdminRoleRequest(@PathVariable("id") long id){
        return ResponseEntity.ok().body(roleRequestService.getRoleRequest(id));
    }

    @Operation(summary = "Понижение человека через заявку")
    @PostMapping("/{id}/downgrade")
    public ResponseEntity<?> downgradeAdminRoleRequest(@PathVariable("id") long id) {
        return ResponseEntity.ok().body(roleRequestService.downgradeRole(id));
    }

    @Operation(summary = "Повышение человека через заявку")
    @PostMapping("/{id}/approve")
    public ResponseEntity<?> approveAdminRoleRequest(@PathVariable("id") long id) {
        return ResponseEntity.ok().body(roleRequestService.approveRole(id));
    }

    @Operation(summary = "Отклонения заявки")
    @PostMapping("/{id}/reject")
    public ResponseEntity<?> rejectAdminRoleRequest(@PathVariable("id") long id) {
        return ResponseEntity.ok().body(roleRequestService.rejectRole(id));
    }
}

