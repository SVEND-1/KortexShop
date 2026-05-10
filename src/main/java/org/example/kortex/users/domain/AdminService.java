package org.example.kortex.users.domain;

import lombok.extern.slf4j.Slf4j;
import org.example.kortex.users.domain.exception.IncorrectUpdateRoleException;
import org.example.kortex.users.db.Role;
import org.example.kortex.users.db.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class AdminService {

    private final UserService userService;

    @Autowired
    public AdminService(UserService userService) {
        this.userService = userService;
    }

    //================================Controller Methods================================================


    //================================Service Methods================================================
    @Transactional
    public void appoint(Long userId, Role role) {
        try {
            User user = userService.getById(userId);
            if (isValidRoleAppoint(user.getRole(), role)) {
                throw new IncorrectUpdateRoleException("Нельзя назначить на роль" + role.name() + " пользователя с ролью: " + user.getRole());
            }

            user.setRole(role);
            userService.update(user.getId(),user);
        }
        catch(Exception e) {
            log.error("Ошибка при повышение пользователя, ex={}", e.getMessage());
            throw new IncorrectUpdateRoleException(e.getMessage());
        }
    }

    @Transactional
    public void downgrade(Long userId, Role role) {
        try {
            User user = userService.getById(userId);
            if (isValidRoleDowngrade(user.getRole(), role)) {
                throw new IncorrectUpdateRoleException("Нельзя забрать роль " + role + " у пользователя с ролью: " + user.getRole());
            }
            user.setRole(Role.USER);
            userService.update(user.getId(),user);
        }
        catch(Exception e) {
            log.error("Ошибка при повышение понижении, ex={}", e.getMessage());
            throw new IncorrectUpdateRoleException(e.getMessage());
        }
    }


    private boolean isValidRoleAppoint(Role userRole, Role updateRole) {
        if(Role.COURIER.equals(updateRole)) {
            if(userRole.equals(Role.ADMIN) ||
                    userRole.equals(Role.SELLER)) {
                log.warn("Нельзя назначить курьером пользователя с ролью: {}", userRole);
                return true;
            }
        }
        if(Role.SELLER.equals(updateRole)) {
            if(userRole.equals(Role.ADMIN) ||
                    userRole.equals(Role.COURIER)) {
                log.warn("Нельзя назначить продавцом пользователя с ролью: {}", userRole);
                return true;
            }
        }
        return false;
    }

    private boolean isValidRoleDowngrade(Role userRole, Role updateRole) {
        if (userRole.equals(updateRole)) {
            log.warn("Нельзя забрать роль {} у пользователя с ролью: {}", updateRole, userRole);
            return false;
        }
        return true;
    }


}
