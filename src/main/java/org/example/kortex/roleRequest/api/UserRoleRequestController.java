package org.example.kortex.roleRequest.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.kortex.roleRequest.api.dto.RoleCreateRequest;
import org.example.kortex.roleRequest.domain.RoleRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/users/role-request")
@Tag(name = "UserRole",description = "Работа с заявки у пользователя")
public class UserRoleRequestController {
    private final RoleRequestService roleRequestService;

    @Autowired
    public UserRoleRequestController(RoleRequestService roleRequestService) {
        this.roleRequestService = roleRequestService;
    }

    @Operation(summary = "Получение истории заявок")
    @GetMapping
    public ResponseEntity<?> getUserRoleRequests(){
        return ResponseEntity.ok().body(roleRequestService.getAllRoleRequestsByUserId());
    }

    @Operation(summary = "Создание заявки")
    @PostMapping
    public ResponseEntity<?> create(@RequestBody @Valid RoleCreateRequest request) {
        return ResponseEntity.ok(roleRequestService.create(request.requestedRole(), request.typeAction(),request.message()));
    }

}
