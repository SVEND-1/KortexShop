package org.example.kortex;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class KortexApplication {//TODO НЕ ИСКАТЬ ПОЛЬЗОВАТЕЛЯ ЕСЛИ УЖЕ НАЙДЕТ
    public static void main(String[] args) {
        SpringApplication.run(KortexApplication.class, args);
    }

}
