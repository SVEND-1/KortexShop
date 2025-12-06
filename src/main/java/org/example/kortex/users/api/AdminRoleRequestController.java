package org.example.kortex.users.api;

import org.example.kortex.users.db.RoleRequest;
import org.example.kortex.users.db.RoleRequestRepository;
import org.example.kortex.users.db.User;
import org.example.kortex.users.domain.RoleRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityNotFoundException;
import java.util.List;

@RestController
@RequestMapping("/api/admin/role-request")
public class AdminRoleRequestController {
    private final RoleRequestService roleRequestService;

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
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getAdminRoleRequest(@PathVariable("id") long id){
        try{
            return ResponseEntity.ok().body(roleRequestService.getRoleRequest(id));
        }
        catch(EntityNotFoundException e){
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<?> approveAdminRoleRequest(@PathVariable("id") long id) {
        try{
            return ResponseEntity.ok().body(roleRequestService.approveRole(id));
        }
        catch(Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<?> rejectAdminRoleRequest(@PathVariable("id") long id) {
        try{
            return ResponseEntity.ok().body(roleRequestService.rejectRole(id));
        }
        catch(Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
