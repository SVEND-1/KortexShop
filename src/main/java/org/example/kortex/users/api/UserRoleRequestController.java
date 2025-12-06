package org.example.kortex.users.api;

import org.example.kortex.users.db.RoleRequest;
import org.example.kortex.users.db.User;
import org.example.kortex.users.domain.RoleRequestService;
import org.example.kortex.users.domain.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users/role-request")
public class UserRoleRequestController {
    private final UserService userService;
    private final RoleRequestService roleRequestService;

    @Autowired
    public UserRoleRequestController(UserService userService, RoleRequestService roleRequestService) {
        this.userService = userService;
        this.roleRequestService = roleRequestService;
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestParam User.Role requestedRole,
                                      @RequestParam RoleRequest.TypeAction typeAction,
                                      @RequestParam(required = false) String message
                                    ) {
        User currentUser = userService.getCurrentUser();
        try {
            RoleRequest request = roleRequestService.createRoleRequest(
                    currentUser, requestedRole, typeAction, message);
            return ResponseEntity.ok(request);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


}
