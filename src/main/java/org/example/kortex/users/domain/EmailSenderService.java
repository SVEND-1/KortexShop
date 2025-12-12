package org.example.kortex.users.domain;

import com.sendgrid.*;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.Random;

@Slf4j
@Service
@Transactional
public class EmailSenderService {

    @Value("${sendgrid.from.email:onlineshopkortex@gmail.com}")
    private String fromEmail;

    private final SendGrid sendGrid;

    @Autowired
    public EmailSenderService(SendGrid sendGrid) {
        this.sendGrid = sendGrid;
    }

    @Async
    public void sendMessage(String to, String subject, String content) {
        log.info("Отправка сообщения на почту: {}", to);
        sendEmailWithTemplate(to, subject, content);
    }

    @Async
    public void sendPasswordResetEmail(String to, String code) {
        log.info("Отправка сообщения о сбросе пароля на почту: {} с кодом {}", to, code);

        String subject = "Kortex: Сброс пароля [" + code + "]";
        String htmlContent = """
            <html>
            <body style="font-family: Arial, sans-serif; line-height: 1.6;">
                <div style="max-width: 600px; margin: 0 auto; padding: 20px;">
                    <h2 style="color: #333;">Запрос на сброс пароля</h2>
                    
                    <div style="background-color: #f8f9fa; padding: 20px; border-radius: 5px; margin: 20px 0;">
                        <p style="margin: 0; font-size: 18px; font-weight: bold; color: #007bff;">
                            Ваш код подтверждения: <span style="font-size: 24px;">%s</span>
                        </p>
                    </div>
                    
                    <p>Введите этот код на странице подтверждения для сброса пароля.</p>
                    
                    <p style="color: #666; font-size: 14px;">
                        <em>Если вы не запрашивали сброс пароля, проигнорируйте это письмо.</em>
                    </p>
                    
                    <hr style="border: none; border-top: 1px solid #eee; margin: 30px 0;">
                    
                    <p>С уважением,<br>Команда Kortex</p>
                </div>
            </body>
            </html>
            """.formatted(code);

        sendEmailWithTemplate(to, subject, htmlContent);
    }

    @Async
    public String sendVerification(String to, String code) {
        log.info("Отправка кода верификации на почту: {} с кодом {}", to, code);

        String subject = "Kortex: Ваш код для входа [" + code + "]";
        String htmlContent = """
            <html>
            <body style="font-family: Arial, sans-serif; line-height: 1.6;">
                <div style="max-width: 600px; margin: 0 auto; padding: 20px;">
                    <h2 style="color: #333; border-bottom: 2px solid #007bff; padding-bottom: 10px;">
                        Добро пожаловать в Kortex!
                    </h2>
                    
                    <div style="background-color: #e8f4fd; padding: 25px; border-radius: 8px; margin: 25px 0; text-align: center;">
                        <p style="margin: 0 0 15px 0; font-size: 16px;">Ваш код для входа:</p>
                        <div style="font-size: 32px; font-weight: bold; color: #007bff; letter-spacing: 5px;">
                            %s
                        </div>
                        <p style="margin: 15px 0 0 0; font-size: 14px; color: #666;">(действителен 10 минут)</p>
                    </div>
                    
                    <p>Введите этот код на странице подтверждения для завершения входа в ваш аккаунт.</p>
                    
                    <div style="background-color: #fff3cd; border: 1px solid #ffc107; padding: 15px; border-radius: 5px; margin: 20px 0;">
                        <p style="margin: 0; color: #856404;">
                            <strong>Важно:</strong> Если вы не запрашивали вход, пожалуйста, проигнорируйте это письмо.
                        </p>
                    </div>
                    
                    <hr style="border: none; border-top: 1px solid #eee; margin: 30px 0;">
                    
                    <p style="color: #666; font-size: 14px;">
                        С уважением,<br>
                        <strong>Команда Kortex</strong><br>
                        <a href="https://kortexshop.onrender.com" style="color: #007bff;">https://kortexshop.onrender.com</a>
                    </p>
                </div>
            </body>
            </html>
            """.formatted(code);

        sendEmailWithTemplate(to, subject, htmlContent);
        return code;
    }

    private void sendEmailWithTemplate(String to, String subject, String htmlContent) {
        try {
            Email from = new Email(fromEmail);
            Email toEmail = new Email(to);
            Content content = new Content("text/html", htmlContent);
            Mail mail = new Mail(from, subject, toEmail, content);

            Request request = new Request();
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());

            Response response = sendGrid.api(request);

            if (response.getStatusCode() >= 200 && response.getStatusCode() < 300) {
                log.info("✅ Email успешно отправлен на {} (Status: {})", to, response.getStatusCode());
            } else {
                log.error("❌ Ошибка отправки email на {}: Status {}, Body: {}",
                        to, response.getStatusCode(), response.getBody());
            }

        } catch (IOException e) {
            log.error("❌ Ошибка при отправке email на {}: {}", to, e.getMessage(), e);
        }
    }

    public String generateVerificationCode() {
        Random random = new Random();
        return String.valueOf(random.nextInt(100000, 999999));
    }
}