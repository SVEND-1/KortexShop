package org.example.kortex.carts.api.dto;

import lombok.Data;

@Data
public class UserCartDTO {
    private Long id;
    private String email;
    private String name;
    private String address;
}
