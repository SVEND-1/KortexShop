# Kortex - Маркетплейс с доставкой до двери

<div align="center">

![Spring Boot](https://img.shields.io/badge/Spring%20Boot-green?logo=spring)
![Java](https://img.shields.io/badge/Java%2B-blue?logo=openjdk)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL%2B-blue?logo=postgresql)
![Kafka](https://img.shields.io/badge/Apache%20Kafka-black?logo=apachekafka)
![Docker](https://img.shields.io/badge/Docker-2496ED?logo=docker&logoColor=white)
![Liquibase](https://img.shields.io/badge/Liquibase-2962FF?logo=liquibase&logoColor=white)

**Современный маркетплейс для удобных покупок с доставкой прямо до дома**

[Описание](#-о-проекте) • [Технологии](#-технологический-стек) • [Архитектура](#-архитектура) 

</div>

## О проекте

Kortex — это полнофункциональный маркетплейс, предоставляющий пользователям возможность:
-  **Покупать товары** с удобной доставкой до двери
-  **Продавать товары** через собственную витрину
-  **Работать курьером** и доставлять заказы
-  **Управлять платформой** (административные функции)

Платформа поддерживает полный цикл покупки — от выбора товара в каталоге до получения заказа и отслеживания статуса доставки.

## Технологический стек

### **Backend**
![Spring Boot](https://img.shields.io/badge/Spring_Boo-6DB33F?logo=springboot&logoColor=white)
![Spring Security](https://img.shields.io/badge/Spring_Security-6DB33F?logo=springsecurity&logoColor=white)
![Spring Data JPA](https://img.shields.io/badge/Spring_Data_JPA-6DB33F?logo=spring&logoColor=white)
![Spring Web](https://img.shields.io/badge/Spring_Web-6DB33F?logo=spring&logoColor=white)
![Spring Mail](https://img.shields.io/badge/Spring_Mail-6DB33F?logo=spring&logoColor=white)

### **База данных**
![PostgreSQL](https://img.shields.io/badge/PostgreSQL%2B-336791?logo=postgresql&logoColor=white)
![Hibernate](https://img.shields.io/badge/Hibernate-59666C?logo=hibernate&logoColor=white)
![Liquibase](https://img.shields.io/badge/Liquibase-2962FF?logo=liquibase&logoColor=white)

### **Интеграция и обмен сообщениями**
![Apache Kafka](https://img.shields.io/badge/Apache_Kafka-231F20?logo=apachekafka&logoColor=white)
![JWT](https://img.shields.io/badge/JWT-Authentication?logo=jsonwebtokens&logoColor=white)

### **DevOps**
![Docker](https://img.shields.io/badge/Docker-2496ED?logo=docker&logoColor=white)
![Docker Compose](https://img.shields.io/badge/Docker_Compose-2496ED?logo=docker&logoColor=white)

### **Утилиты и библиотеки**
![Lombok](https://img.shields.io/badge/Lombok-pink?logo=lombok&logoColor=white)
![MapStruct](https://img.shields.io/badge/MapStruct-orange?logo=&logoColor=white)
![JUnit](https://img.shields.io/badge/JUnit-5-25A162?logo=junit5&logoColor=white)

## Архитектура проекта

### **Структура контроллеров**

#### **CartController** — Управление корзиной покупок
| Метод | Описание |
|-------|----------|
| `getCartPage` | Отображение корзины пользователя
| `addItemToCart` | Добавление товара в корзину 
| `increaseQuantity` | Увеличение количества товара 
| `decreaseQuantity` | Уменьшение количества товара 

#### **OrderController** — Управление заказами
| Метод | Описание | 
|-------|----------|
| `getMeCreateOrders` | Страница оформления заказа 
| `createOrder` | Создание нового заказа

#### **ProductController** — Каталог товаров
| Метод | Описание | 
|-------|----------|
| `getProducts` | Каталог с фильтрацией и пагинацией |
| `productDetailPage` | Детальная информация о товаре | 

#### **AdminRoleRequestController** — Администрирование ролей
| Метод | Описание | 
|-------|----------|
| `getAdminRoleRequest` | Список заявок на роль админа |
| `getAdminRoleRequest` | Детали заявки | 
| `downgradeAdminRoleRequest` | Понижение пользователя | 
| `approveAdminRoleRequest` | Одобрение заявки |
| `rejectAdminRoleRequest` | Отклонение заявки | 

#### **UserRoleRequestController** — Заявки пользователей
| Метод | Описание |
|-------|----------|
| `getUserRoleRequests` | Заявки текущего пользователя |
| `create` | Создание новой заявки | 

####  **AuthController** — Аутентификация и авторизация
| Метод | Описание |
|-------|----------|
| `login` | Вход в аккаунт |
| `logout` | Выход из аккаунта | 
| `sendRegistrationCode` | Отправка кода регистрации |
| `verifyRegistration` | Подтверждение регистрации |
| `resendVerificationCode` | Повторная отправка кода | 
| `forgotPassword` | Запрос сброса пароля |
| `verifyResetCode` | Подтверждение кода сброса |
| `resetPassword` | Установка нового пароля |

####  **CourierController** — Панель курьера
| Метод | Описание |
|-------|----------|
| `getAssignedOrders` | Назначенные заказы |
| `getAvailableOrders` | Доступные заказы |
| `assignOrder` | Взять заказ в работу |
| `setStatus` | Изменить статус заказа | 

####  **SellerController** — Панель продавца
| Метод | Описание | 
|-------|----------|
| `getMyProducts` | Товары продавца |
| `getProduct` | Детали товара |
| `createProduct` | Создание товара |
| `updateProduct` | Обновление товара |
| `deleteProduct` | Удаление товара | 

####  **UserController** — Профиль пользователя
| Метод | Описание | 
|-------|----------|
| `profile` | Профиль пользователя |
| `orders` | История заказов |
| `changeAddress` | Смена адреса доставки |

####  **Вспомогательные контроллеры**
- **`GlobalExceptionHandler`** — Централизованная обработка исключений
- **`PageController`** — Отрисовка HTML-страниц интерфейса
