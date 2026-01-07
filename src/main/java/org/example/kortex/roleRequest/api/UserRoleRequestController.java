package org.example.kortex.roleRequest.api;

import lombok.extern.slf4j.Slf4j;
import org.example.kortex.users.db.Role;
import org.example.kortex.roleRequest.db.RoleRequest;
import org.example.kortex.roleRequest.domain.RoleRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
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
    public ResponseEntity<?> create(@RequestParam Role requestedRole,
                                      @RequestParam RoleRequest.TypeAction typeAction,
                                      @RequestParam(required = false) String message
                                    ) {
        return ResponseEntity.ok(roleRequestService.create(requestedRole,typeAction,message));
    }

}
