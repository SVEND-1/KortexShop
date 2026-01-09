package org.example.kortex.users.api.mapper;

import org.example.kortex.users.api.dto.user.UserResponse;
import org.example.kortex.users.db.User;
import org.mapstruct.Mapper;

@Mapper
public interface UserMapper {

    UserResponse convertEntityToDto(User user);
}
