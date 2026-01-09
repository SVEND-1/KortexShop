package org.example.kortex.notify.kafka;

import org.example.kortex.notify.event.NotifyEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class NotifyKafkaProducer {
    private final KafkaTemplate<String, NotifyEvent> kafkaTemplate;

    public NotifyKafkaProducer(KafkaTemplate<String, NotifyEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendMessageToKafka(NotifyEvent notifyEvent) {
        kafkaTemplate.send("notifyUser", notifyEvent.email(), notifyEvent);
    }
}
