package org.example.kortex.users.api;

import lombok.extern.slf4j.Slf4j;
import org.example.kortex.users.api.dto.RoleRequestMapper;
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
    private final RoleRequestMapper roleRequestMapper;

    @Autowired
    public UserRoleRequestController(UserService userService, RoleRequestService roleRequestService, RoleRequestMapper roleRequestMapper) {
        this.userService = userService;
        this.roleRequestService = roleRequestService;
        this.roleRequestMapper = roleRequestMapper;
    }

    @GetMapping
    public ResponseEntity<?> getUserRoleRequests(){
        try {
            return ResponseEntity.ok().body(roleRequestMapper.toDtoList(roleRequestService.getAllRoleRequestsByUserId(userService.getCurrentUser().getId())));
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
            return ResponseEntity.ok(roleRequestMapper.toDto(request));
        } catch (IllegalArgumentException | IllegalStateException e) {
            log.error("Не удалось создать заявку: " + e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


}
