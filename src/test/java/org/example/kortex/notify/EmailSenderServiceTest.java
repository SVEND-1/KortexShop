package org.example.kortex.notify;

import org.example.kortex.notify.event.NotifyEvent;
import org.example.kortex.notify.event.NotifyType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailSenderServiceTest {
    @Mock
    private JavaMailSender javaMailSender;

    @Mock
    private EmailTemplateService templateService;

    @InjectMocks
    private EmailSenderService emailSenderService;


    private final String testEmail = "test@example.com";
    private final String testFromEmail = "noreply@example.com";
    private NotifyEvent testEvent;

    @BeforeEach
    void setUp() {
        //Это замена проперти
        setField(emailSenderService, "fromEmail", testFromEmail);//Это замена проперти

        Map<String, String> testParams = Map.of("param1", "value1", "param2", "value2");
        testEvent = new NotifyEvent(testEmail, testParams,NotifyType.REGISTER);
    }

    private void setField(Object target, String fieldName, Object value) {
        try {
            var field = EmailSenderService.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void sendEmail() {
        String expectedSubject = "Test Subject";
        String expectedContent = "Test Content";

        when(templateService.getSubject(eq(NotifyType.REGISTER), any(Map.class)))
                .thenReturn(expectedSubject);
        when(templateService.getContent(eq(NotifyType.REGISTER), any(Map.class)))
                .thenReturn(expectedContent);
        doNothing().when(javaMailSender).send(any(SimpleMailMessage.class));//Чтобы не отправился по настоящему mail

        emailSenderService.sendEmail(testEvent);

        verify(templateService).getSubject(NotifyType.REGISTER, testEvent.parameters());
        verify(templateService).getContent(NotifyType.REGISTER, testEvent.parameters());

        //Получает то что отправлено было в mail
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(javaMailSender).send(messageCaptor.capture());

        SimpleMailMessage sentMessage = messageCaptor.getValue();
        assertNotNull(sentMessage);
        assertEquals(expectedSubject, sentMessage.getSubject());
        assertEquals(expectedContent, sentMessage.getText());

        verifyNoMoreInteractions(javaMailSender, templateService);
    }

    @Test
    void generateVerificationCode() {
        String code = emailSenderService.generateVerificationCode();

        assertNotNull(code);
        assertEquals(6, code.length());
        assertTrue(code.matches("\\d{6}"), "Код должен содержать только цифры");

        int codeInt = Integer.parseInt(code);
        assertTrue(codeInt >= 100000 && codeInt <= 999999,
                "Код должен быть в диапазоне от 100000 до 999999");
    }
}