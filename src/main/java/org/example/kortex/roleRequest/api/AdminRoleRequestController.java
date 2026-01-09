package org.example.kortex.roleRequest.api;

import org.example.kortex.roleRequest.api.dto.RolePageResponse;
import org.example.kortex.roleRequest.domain.RoleRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/admin/role-request")
public class AdminRoleRequestController {//Перенести в другую
    private final RoleRequestService roleRequestService;

    @Autowired
    public AdminRoleRequestController(RoleRequestService roleRequestService) {
        this.roleRequestService = roleRequestService;
    }

    @GetMapping
    public CompletableFuture<ResponseEntity<RolePageResponse>> getAdminRoleRequest(@ModelAttribute RoleRequestFilter filter) {
        return roleRequestService.getRoleRequestsPage(filter)
                .thenApply(ResponseEntity::ok)
                .exceptionally(ex -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getAdminRoleRequest(@PathVariable("id") long id){
        return ResponseEntity.ok().body(roleRequestService.getRoleRequest(id));
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

