package org.example.kortex.users.domain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Random;

@Service
@Transactional
public class EmailSenderService {
    private JavaMailSender javaMailSender;
    private final Logger log = LoggerFactory.getLogger(UserService.class);

    @Autowired
    public EmailSenderService(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }

    public void sendMessage(String to, String subject, String content) {
        log.info("Отпрвавка сообщение на почту: " + to);
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("onlineshopkortex@gmail.com");
        message.setTo(to);
        message.setSubject(subject);
        message.setText(content);

        javaMailSender.send(message);
        log.info("Сообщение отправлено на почту: " + to);
    }

    public void sendPasswordResetEmail(String to, String code) {
        log.info("Отпрвавка сообщение на изменения сброс на почту: " + to + " с кодом " + code);
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("onlineshopkortex@gmail.com");
        message.setTo(to);
        message.setSubject("Kortex: Сброс пароля [" + code + "]");
        message.setText("""
            Запрос на сброс пароля
            
            Ваш код подтверждения: """ + code + """
            
            Введите этот код на странице подтверждения для сброса пароля.
            
            Если вы не запрашивали сброс пароля, проигнорируйте это письмо.
            
            С уважением,
            Команда Kortex
            """);

        javaMailSender.send(message);
        log.info("Сообщение отправлено на изменения сброс на почту: " + to + " с кодом " + code);
    }

    public String sendVerification(String to,String code) {
        log.info("Отпрвавка сообщение на регистрацию на почту: " + to + " с кодом " + code);
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("onlineshopkortex@gmail.com");

        String subject = "Kortex: Ваш код для входа [" + code + "]";
        String content = """
    Добро пожаловать в Kortex!
    
    Ваш код для входа: """ + code + """
    
    Введите этот код на странице подтверждения для завершения входа в ваш аккаунт.
    
    Если вы не запрашивали вход, пожалуйста, проигнорируйте это письмо.
    
    С уважением,
    Команда Kortex
    """;

        message.setTo(to);
        message.setSubject(subject);
        message.setText(content);

        javaMailSender.send(message);
        log.info("Сообщение отправлено на регистрацию на почту: " + to + " с кодом " + code);
        return code;
    }

    public String generateVerificationCode() {
        Random random = new Random();
        return String.valueOf(random.nextInt(100000, 999999));
    }
}
