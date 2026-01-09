package org.example.kortex.notify.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.example.kortex.notify.event.NotifyEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaProducerConfig {
    public Map<String, Object> producerFactory() {
        Map<String, Object> configProperties = new HashMap<>();
        configProperties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        return configProperties;
    }

    @Bean
    public ProducerFactory<String, NotifyEvent> userNotifyProducerFactory(
            ObjectMapper objectMapper
    ) {
        JsonSerializer<NotifyEvent> serializer = new JsonSerializer<>(objectMapper);
        serializer.setAddTypeInfo(false);

        return new DefaultKafkaProducerFactory<>(
                producerFactory(),
                new StringSerializer(),
                serializer
        );
    }

    @Bean
    public KafkaTemplate<String, NotifyEvent> userNotifyKafkaTemplate(
            ProducerFactory<String, NotifyEvent> factory
    ) {
        return new KafkaTemplate<>(factory);
    }
}
