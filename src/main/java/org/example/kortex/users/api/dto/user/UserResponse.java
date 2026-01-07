package org.example.kortex.users.api.dto.user;

import lombok.Data;
import org.example.kortex.users.db.Role;
import org.example.kortex.users.db.User;

@Data
public class UserResponse {
    private Long id;
    private String email;
    private String name;
    private Role role;
    private String address;
}
