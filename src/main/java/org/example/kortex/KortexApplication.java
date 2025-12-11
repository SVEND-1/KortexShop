package org.example.kortex;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class KortexApplication {
    public static void main(String[] args) {
        SpringApplication.run(KortexApplication.class, args);
    }

}
