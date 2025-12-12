package org.example.kortex.config;

import com.sendgrid.SendGrid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SendGridConfig {

    @Value("${sendgrid.api.key}")
    private String sendGridApiKey;

    @Bean
    public SendGrid sendGrid() {
        if (sendGridApiKey == null || sendGridApiKey.isEmpty()) {
            throw new IllegalStateException("SendGrid API Key is not configured");
        }
        return new SendGrid(sendGridApiKey);
    }
}
