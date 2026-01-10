package org.example.kortex.roleRequest.api;

import org.example.kortex.roleRequest.api.dto.RoleCreateRequest;
import org.example.kortex.users.db.Role;
import org.example.kortex.roleRequest.db.RoleRequest;
import org.example.kortex.roleRequest.domain.RoleRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/users/role-request")
public class UserRoleRequestController {
    private final RoleRequestService roleRequestService;

    @Autowired
    public UserRoleRequestController(RoleRequestService roleRequestService) {
        this.roleRequestService = roleRequestService;
    }

    @GetMapping
    public ResponseEntity<?> getUserRoleRequests(){
        return ResponseEntity.ok().body(roleRequestService.getAllRoleRequestsByUserId());
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody @Valid RoleCreateRequest request) {
        return ResponseEntity.ok(roleRequestService.create(request.requestedRole(), request.typeAction(),request.message()));
    }

}
