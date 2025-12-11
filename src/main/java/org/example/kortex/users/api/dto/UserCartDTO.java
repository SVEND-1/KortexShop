package org.example.kortex.users.api.dto;

import lombok.Data;
import org.example.kortex.users.db.User;

@Data
public class UserCartDTO {
    private Long id;
    private String email;
    private String name;
    private User.Role role;
    private String address;

    // Только ID корзины, а не вся сущность
    private Long cartId;
}
