package org.example.kortex.users.api;

import lombok.extern.slf4j.Slf4j;
import org.example.kortex.users.db.RoleRequest;
import org.example.kortex.users.db.User;
import org.example.kortex.users.domain.RoleRequestService;
import org.example.kortex.users.domain.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
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

    @GetMapping
    public ResponseEntity<?> getUserRoleRequests(){
        try {
            return ResponseEntity.ok().body(roleRequestService.getAllRoleRequestsByUserId(userService.getCurrentUser().getId()));
        }
        catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestParam User.Role requestedRole,
                                      @RequestParam RoleRequest.TypeAction typeAction,
                                      @RequestParam(required = false) String message
                                    ) {
        User currentUser = userService.getCurrentUser();
        log.info("Создания запроса на изменение роли у пользователя id:" + currentUser.getId());
        try {
            RoleRequest request = roleRequestService.createRoleRequest(
                    currentUser, requestedRole, typeAction, message);
            log.info("Заявка создана id: " + request.getId());
            return ResponseEntity.ok(request);
        } catch (IllegalArgumentException | IllegalStateException e) {
            log.error("Не удалось создать заявку: " + e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


}
