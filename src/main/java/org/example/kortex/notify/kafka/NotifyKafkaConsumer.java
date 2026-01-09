package org.example.kortex.notify.kafka;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.example.kortex.notify.EmailSenderService;
import org.example.kortex.notify.event.NotifyEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;



@Service
public class NotifyKafkaConsumer {
    private final EmailSenderService emailSenderService;

    public NotifyKafkaConsumer(EmailSenderService emailSenderService) {
        this.emailSenderService = emailSenderService;
    }

    @KafkaListener(topics = "notifyUser")
    public void consumeNotify(NotifyEvent event) {
        emailSenderService.sendEmail(event);
    }
}
