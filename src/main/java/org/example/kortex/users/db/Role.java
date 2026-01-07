package org.example.kortex.users.db;

import org.springframework.security.core.authority.SimpleGrantedAuthority;


public enum Role {
    USER, ADMIN, COURIER, SELLER;

    public SimpleGrantedAuthority toAuthority() {
        return new SimpleGrantedAuthority("ROLE_" + this.name());
    }
}