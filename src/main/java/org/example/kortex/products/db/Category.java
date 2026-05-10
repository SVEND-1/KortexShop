package org.example.kortex.products.db;

import org.springframework.security.core.authority.SimpleGrantedAuthority;

public enum Category {
    ELECTRONICS("Электроника"), CLOTHING("Одежда"), BOOKS("Книги"), FOOD("Еда"),
    SPORTS("Спорт товары"), HOME("Товары для дома"), BEAUTY("Красота"), OTHER("Другое");

    private final String displayName;

    Category(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}