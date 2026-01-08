package org.example.kortex.users.api.dto.user;

import org.example.kortex.users.db.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {
    public UserResponse toDto(User user) {
        if (user == null) {
            return null;
        }
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getRole(),
                user.getAddress()
        );
    }
}
