package org.example.kortex.users.api.dto.courier;

import lombok.Data;

@Data
public class CustomerInfoDTO {
    private Long id;
    private String fullName;
    private String phone;
    private String email;
}
