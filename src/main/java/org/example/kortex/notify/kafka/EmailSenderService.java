package org.example.kortex.notify.kafka;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Random;

@Slf4j
@Service
public class EmailSenderService {
    private final JavaMailSender javaMailSender;

    @Autowired
    public EmailSenderService(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }

    @Async("asyncExecutor")
    public void sendMessage(String to, String subject, String content) {
        try {
            log.info("Отпрвавка сообщение на email={}", to);
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("onlineshopkortex@gmail.com");
            message.setTo(to);
            message.setSubject(subject);
            message.setText(content);

            javaMailSender.send(message);
            log.info("Сообщение отправлено на email={}", to);
        }catch (Exception e){
            log.error("Не удалось отправить сообщение на email={}, ex={}", to,e.getMessage());
        }
    }

    @Async("asyncExecutor")
    public void sendPasswordResetEmail(String to, String code) {
        try {
            log.info("Отпрвавка сообщение на изменения сброс на email={}, code={}", to, code);
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("onlineshopkortex@gmail.com");
            message.setTo(to);
            message.setSubject("Kortex: Сброс пароля [" + code + "]");
            message.setText("""
                    Запрос на сброс пароля
                   
                    Ваш код подтверждения:""" + code + """
                    
                    Введите этот код на странице подтверждения для сброса пароля.
                    
                    Если вы не запрашивали сброс пароля, проигнорируйте это письмо.
                    
                    С уважением,
                    Команда Kortex
                    """);

            javaMailSender.send(message);
            log.info("Сообщение отправлено на изменения сброс на email={}, code={}", to, code);
        }
        catch (Exception e){
            log.error("Не удалось отправить сообщение о смене пароля на email={}, ex={}", to,e.getMessage());
        }
    }

    @Async("asyncExecutor")
    public String sendVerification(String to,String code) {
        try {
            log.info("Отпрвавка сообщение на регистрацию на email={}, code={}", to, code);
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("onlineshopkortex@gmail.com");

            String subject = "Kortex: Ваш код для входа [" + code + "]";
            String content = """
                    Добро пожаловать в Kortex!
                    
                    Ваш код для входа:""" + code + """
                    
                    Введите этот код на странице подтверждения для завершения входа в ваш аккаунт.
                    
                    Если вы не запрашивали вход, пожалуйста, проигнорируйте это письмо.
                    
                    С уважением,
                    Команда Kortex
                    """;

            message.setTo(to);
            message.setSubject(subject);
            message.setText(content);

            javaMailSender.send(message);
            log.info("Сообщение отправлено на регистрацию на email={}, code={}", to, code);
            return code;
        }catch (Exception e){
            log.error("Не удалось отправить сообщение на регистрацию на email={}, ex={}", to,e.getMessage());
            return null;
        }
    }

    public String generateVerificationCode() {
        Random random = new Random();
        return String.valueOf(random.nextInt(100000, 999999));
    }
}
