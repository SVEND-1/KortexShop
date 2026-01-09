package org.example.kortex.notify;

import lombok.extern.slf4j.Slf4j;
import org.example.kortex.notify.event.NotifyType;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
public class EmailTemplateService {

    public String getSubject(NotifyType type, Map<String, String> params) {
        if (type == null || params == null) {
            return "Уведомление от Kortex";
        }

        return switch (type) {
            case REGISTER -> String.format("Kortex: Ваш код для входа [%s]",
                    params.getOrDefault("code", ""));
            case PASSWORD_RESET -> String.format("Kortex: Сброс пароля [%s]",
                    params.getOrDefault("code", ""));
            case REPLAY_CODE -> String.format("Kortex: Повторный код [%s]",
                    params.getOrDefault("code", ""));
            case ORDER_CREATED -> "Kortex: Заказ создан";
            case COURIER_TAKE_ORDER -> String.format("Kortex: Курьер принял ваш заказ #%s",
                    params.getOrDefault("courierName", ""));
            case COURIER_BRING_ORDER -> "Kortex: Курьер доставил ваш заказ";
            case REQUEST_APPROVED -> "Kortex: Ваш запрос одобрен";
            case REQUEST_DOWNGRADE -> "Kortex: Ваш запрос на понижение роли одобрен";
            case REQUEST_REJECTED -> "Kortex: Ваш запрос отклонен";
            case LOGIN -> "Kortex: Вход в аккаунт";
            default -> "Уведомление от Kortex";
        };
    }

    public String getContent(NotifyType type, Map<String, String> params) {
        if (type == null || params == null) {
            return "";
        }

        return switch (type) {
            case REGISTER -> String.format("""
                Добро пожаловать в Kortex!
                
                Ваш код для входа: %s
                
                Введите этот код на странице подтверждения для завершения входа в ваш аккаунт.
                
                Если вы не запрашивали вход, пожалуйста, проигнорируйте это письмо.
                
                С уважением,
                Команда Kortex
                """, params.getOrDefault("code", ""));

            case PASSWORD_RESET -> String.format("""
                Запрос на сброс пароля
                
                Ваш код подтверждения: %s
                
                Введите этот код на странице подтверждения для сброса пароля.
                
                Если вы не запрашивали сброс пароля, проигнорируйте это письмо.
                
                С уважением,
                Команда Kortex
                """, params.getOrDefault("code", ""));

            case REPLAY_CODE -> String.format("""
                Был запрошен повторный код
                
                Ваш повторный код: %s
                
                С уважением,
                Команда Kortex
                """, params.getOrDefault("code", ""));

            case ORDER_CREATED -> String.format("""
                Уважаемый %s,
                
                Ваш заказ успешно создан!
                
                Мы сообщим вам когда курьер возьмет его
                
                С уважением,
                Команда Kortex
                """,
                    params.getOrDefault("userName", ""));

            case COURIER_TAKE_ORDER -> String.format("""
                Уважаемый %s,
                
                Курьер %s принял ваш заказ.
                
                С уважением,
                Команда Kortex
                """,
                    params.getOrDefault("userName", ""),
                    params.getOrDefault("courierName", ""));

            case COURIER_BRING_ORDER -> String.format("""
                Уважаемый %s,
                
                Курьер доставил ваш заказ.
                
                Спасибо за покупку!
                
                С уважением,
                Команда Kortex
                """,
                    params.getOrDefault("userName", ""));

            case REQUEST_APPROVED -> String.format("""
                Уважаемый %s,
                
                Ваш запрос на изменение роли одобрен.
                
                Новая роль: %s
                
                С уважением,
                Команда Kortex
                """,
                    params.getOrDefault("userName", ""),
                    params.getOrDefault("newRole", ""));

            case REQUEST_DOWNGRADE -> String.format("""
                Уважаемый %s,
                
                Ваш запрос на понижение роли одобрен.
                
                С уважением,
                Команда Kortex
                """,
                    params.getOrDefault("userName", ""));

            case REQUEST_REJECTED -> String.format("""
                Уважаемый %s,
                
                Ваш запрос на изменение роли отклонен.
                
                С уважением,
                Команда Kortex
                """,
                    params.getOrDefault("userName", ""));

            case LOGIN -> String.format("""
                Уважаемый %s,
                
                В ваш аккаунт был выполнен вход.
                
                Если это были не вы, пожалуйста, свяжитесь со службой поддержки.
                
                С уважением,
                Команда Kortex
                """, params.getOrDefault("userName", ""));

            default -> "";
        };
    }
}