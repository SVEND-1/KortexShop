package org.example.kortex.users.api.dto.user;

import org.example.kortex.carts.db.Cart;
import org.example.kortex.orders.db.Order;
import org.example.kortex.users.db.Role;
import org.example.kortex.users.db.RoleRequest;
import org.example.kortex.users.db.User;

import java.util.List;

public record UserDTO(
        Long id,
        String email,
        String name,
        String password,
        Role role,
        String address
//        List<OrderDTO> orders,
//        CartDTO cart,
//        List<RoleRequestDTO> roleRequests
){
}
