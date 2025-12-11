package org.example.kortex.users.api.dto;

import org.example.kortex.users.db.RoleRequest;
import org.example.kortex.users.db.User;
import org.springframework.stereotype.Component;

@Component
public class UserDTOMapper {
    public UserCartDTO toDto(User user) {
        if (user == null) {
            return null;
        }

        UserCartDTO dto = new UserCartDTO();
        dto.setId(user.getId());
        dto.setRole(user.getRole());
        dto.setAddress(user.getAddress());
        dto.setEmail(user.getEmail());
        dto.setName(user.getName());


        return dto;
    }
}
