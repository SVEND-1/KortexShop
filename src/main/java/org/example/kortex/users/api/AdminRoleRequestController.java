package org.example.kortex.users.api;

import org.example.kortex.users.db.RoleRequest;
import org.example.kortex.users.db.RoleRequestRepository;
import org.example.kortex.users.db.User;
import org.example.kortex.users.domain.RoleRequestService;
import org.example.kortex.users.domain.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityNotFoundException;
import java.util.List;

@RestController
@RequestMapping("/api/admin/role-request")
public class AdminRoleRequestController {
    private final RoleRequestService roleRequestService;
    private final Logger log = LoggerFactory.getLogger(UserService.class);

    @Autowired
    public AdminRoleRequestController(RoleRequestService roleRequestService) {
        this.roleRequestService = roleRequestService;
    }

    @GetMapping
    public ResponseEntity<?> getAdminRoleRequest(@RequestParam(name = "role",required = false) User.Role role,
                                                 @RequestParam(name = "status",required = false) RoleRequest.Status status,
                                                 @RequestParam(name = "actionType",required = false) RoleRequest.TypeAction actionType,
                                                 @RequestParam(name = "pageSize",required = false) Integer pageSize,
                                                 @RequestParam(name = "pageNumber", required = false) Integer pageNumber) {
        try {
            RoleRequestFilter filter = new RoleRequestFilter(role, status, actionType, pageSize, pageNumber);
            return ResponseEntity.ok().body(roleRequestService.getRoleRequests(filter));
        }
        catch (EntityNotFoundException e) {
            log.error("Не удалось загрузить товары с фильтром,Ошибка: " + e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getAdminRoleRequest(@PathVariable("id") long id){
        try{
            return ResponseEntity.ok().body(roleRequestService.getRoleRequest(id));
        }
        catch(EntityNotFoundException e){
            log.error("Заявка на смену роли не найдена с id: " + id + ", Ошибка: " + e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{id}/downgrade")
    public ResponseEntity<?> downgradeAdminRoleRequest(@PathVariable("id") long id) {
        try{
            RoleRequest roleRequest  = roleRequestService.downgradeRole(id);
            log.info("Понижения пользователя с id: " + roleRequest.getUser().getId());
            return ResponseEntity.ok().body(roleRequest);
        }
        catch(Exception e){
            log.error("Не удалось понизить пользователя id запроса: " + id + ", Ошибка: " + e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<?> approveAdminRoleRequest(@PathVariable("id") long id) {
        try{
            RoleRequest roleRequest = roleRequestService.approveRole(id);
            log.info("Повышение пользователя с id: " + roleRequest.getUser().getId());
            return ResponseEntity.ok().body(roleRequest);
        }
        catch(Exception e){
            log.error("Не удалось повысить пользователя id запроса: " + id + ", Ошибка: " + e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<?> rejectAdminRoleRequest(@PathVariable("id") long id) {
        try{
            RoleRequest roleRequest = roleRequestService.rejectRole(id);
            log.info("Запрос пользователя на смену роли отклонен id запроса: " + id);
            return ResponseEntity.ok().body(roleRequest);
        }
        catch(Exception e){
            log.error("Запрос пользователя на смену роли не получилось отклонить id: " + id + ",Ошибка: " + e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
