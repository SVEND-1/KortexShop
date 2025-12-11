// checkoutScript.js

document.addEventListener('DOMContentLoaded', function() {
    initCheckout();
});

function initCheckout() {
    // Загружаем данные корзины с сервера
    loadCartData();

    // Настраиваем обработчики событий
    setupEventHandlers();

    // Инициализируем маску телефона
    initPhoneMask();

    // Настраиваем автозаполнение формы
    setupAutoFill();
}

// Загрузка данных корзины с сервера
async function loadCartData() {
    try {
        showLoading();

        const response = await fetch('/api/orders/me-create', {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json'
            },
            credentials: 'include'
        });

        if (response.status === 401) {
            showError('Для оформления заказа требуется авторизация');
            setTimeout(() => {
                window.location.href = '/login?redirect=/checkout';
            }, 2000);
            return;
        }

        if (!response.ok) {
            throw new Error(`Ошибка загрузки данных: ${response.status}`);
        }

        const data = await response.json();

        // Отображаем данные
        renderOrderItems(data.cartItems || []);
        updateOrderSummary(data.totalPrice || 0, data.totalItems || 0);
        renderUserInfo(data.user || {});

        // Обновляем итоговую сумму в кнопке
        updateCheckoutButton(data.totalPrice || 0);

    } catch (error) {
        console.error('Ошибка при загрузке данных:', error);
        showError(`Ошибка загрузки данных: ${error.message}`);
    }
}

// Отображение товаров в заказе
function renderOrderItems(cartItems) {
    const container = document.getElementById('order-items-container');

    if (!cartItems || cartItems.length === 0) {
        container.innerHTML = '<p class="empty-cart">Корзина пуста</p>';
        return;
    }

    container.innerHTML = cartItems.map(item => {
        const imageUrl = item.productImage ||
            (item.image ? `/uploads/images/${item.image}` : '/images/no-image.png');
        const productName = item.productName || item.name || 'Товар';
        const price = item.price || 0;
        const quantity = item.quantity || 1;
        const total = price * quantity;

        return `
            <div class="order-item">
                <div class="item-image">
                    <img src="${imageUrl}" alt="${productName}" 
                         onerror="this.onerror=null; this.src='/images/no-image.png'">
                </div>
                <div class="item-details">
                    <div class="item-name">${productName}</div>
                    <div class="item-meta">
                        <div class="item-quantity">${quantity} шт.</div>
                        <div class="item-price">${formatPrice(total)}</div>
                    </div>
                </div>
            </div>
        `;
    }).join('');
}

// Обновление итоговой информации
function updateOrderSummary(totalPrice, totalItems) {
    const itemsCount = document.getElementById('items-count');
    const totalPriceElement = document.getElementById('total-price');

    if (itemsCount) {
        itemsCount.textContent = `${totalItems} шт.`;
    }

    if (totalPriceElement) {
        totalPriceElement.textContent = formatPrice(totalPrice);
    }
}

// Отображение информации о пользователе
function renderUserInfo(user) {
    const container = document.getElementById('user-info-section');

    if (!user || !user.email) {
        container.innerHTML = `
            <div class="user-info-header">
                <h3>Информация о покупателе</h3>
            </div>
            <div class="user-details">
                <p style="color: #666; text-align: center;">Войдите в аккаунт</p>
            </div>
        `;
        return;
    }

    const fullName = `${user.firstName || ''} ${user.lastName || ''}`.trim() || 'Не указано';
    const address = user.address || 'Не указан';
    const phone = user.phone || 'Не указан';

    container.innerHTML = `
        <div class="user-info-header">
            <h3>Информация о покупателе</h3>
            <button type="button" class="edit-user-btn" onclick="editUserProfile()">
                Изменить
            </button>
        </div>
        <div class="user-details">
            <div class="user-detail">
                <span>Имя:</span>
                <span>${fullName}</span>
            </div>
            <div class="user-detail">
                <span>Email:</span>
                <span>${user.email}</span>
            </div>
            <div class="user-detail">
                <span>Адрес:</span>
                <span>${address}</span>
            </div>
            <div class="user-detail">
                <span>Телефон:</span>
                <span>${phone}</span>
            </div>
        </div>
    `;
}

// Настройка обработчиков событий
function setupEventHandlers() {
    const checkoutForm = document.getElementById('checkout-form');
    const submitOrderBtn = document.getElementById('submit-order-btn');

    if (checkoutForm) {
        checkoutForm.addEventListener('submit', handleSubmitOrder);
    }

    if (submitOrderBtn) {
        submitOrderBtn.addEventListener('click', function(e) {
            if (!checkoutForm.checkValidity()) {
                e.preventDefault();
                highlightInvalidFields();
            }
        });
    }

    // Валидация полей при вводе
    const requiredFields = checkoutForm.querySelectorAll('input[required], textarea[required]');
    requiredFields.forEach(field => {
        field.addEventListener('blur', validateField);
    });
}

// Настройка автозаполнения формы
function setupAutoFill() {
    // Через некоторое время после загрузки данных попробуем заполнить форму
    setTimeout(() => {
        const userEmail = document.querySelector('#user-info-section .user-detail:nth-child(2) span:last-child')?.textContent;
        const userName = document.querySelector('#user-info-section .user-detail:nth-child(1) span:last-child')?.textContent;
        const userPhone = document.querySelector('#user-info-section .user-detail:nth-child(4) span:last-child')?.textContent;
        const userAddress = document.querySelector('#user-info-section .user-detail:nth-child(3) span:last-child')?.textContent;

        if (userEmail && userEmail !== 'Не указан') {
            document.getElementById('email').value = userEmail;
        }

        if (userName && userName !== 'Не указано') {
            document.getElementById('recipient-name').value = userName;
        }

        if (userPhone && userPhone !== 'Не указан') {
            document.getElementById('phone').value = userPhone;
        }

        if (userAddress && userAddress !== 'Не указан') {
            document.getElementById('address').value = userAddress;
        }
    }, 500);
}

// Маска для телефона
function initPhoneMask() {
    const phoneInput = document.getElementById('phone');
    if (!phoneInput) return;

    phoneInput.addEventListener('input', function(e) {
        let value = e.target.value.replace(/\D/g, '');

        if (value.length > 0) {
            if (value.length === 1 && value[0] === '8') {
                value = '7' + value.substring(1);
            }

            if (value[0] === '7' || value[0] === '8') {
                let formatted = '+7';

                if (value.length > 1) {
                    formatted += ' (' + value.substring(1, 4);
                }
                if (value.length > 4) {
                    formatted += ') ' + value.substring(4, 7);
                }
                if (value.length > 7) {
                    formatted += '-' + value.substring(7, 9);
                }
                if (value.length > 9) {
                    formatted += '-' + value.substring(9, 11);
                }

                e.target.value = formatted;
            }
        }
    });
}

// Обработка отправки формы
async function handleSubmitOrder(e) {
    e.preventDefault();

    if (!validateForm()) {
        return;
    }

    try {
        const submitBtn = document.getElementById('submit-order-btn');
        const originalText = submitBtn.innerHTML;

        submitBtn.disabled = true;
        submitBtn.innerHTML = '<span>Оформление...</span>';

        // Получаем данные формы
        const formData = {
            recipientName: document.getElementById('recipient-name').value,
            email: document.getElementById('email').value,
            address: document.getElementById('address').value,
            phone: document.getElementById('phone').value,
            comment: document.getElementById('comment').value,
            paymentMethod: document.querySelector('input[name="payment"]:checked').value
        };

        // Отправляем заказ на сервер
        const response = await fetch('/api/orders', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            credentials: 'include'
        });

        if (response.status === 401) {
            showError('Сессия истекла. Пожалуйста, войдите снова.');
            setTimeout(() => {
                window.location.href = '/login?redirect=/checkout';
            }, 2000);
            return;
        }

        const result = await response.json();

        if (!response.ok) {
            throw new Error(result.error || 'Ошибка при создании заказа');
        }

        // Показываем успешное сообщение
        showSuccess(result.order);

    } catch (error) {
        console.error('Ошибка при оформлении заказа:', error);
        showError(`Ошибка: ${error.message}`);

        // Восстанавливаем кнопку
        const submitBtn = document.getElementById('submit-order-btn');
        submitBtn.disabled = false;
        submitBtn.innerHTML = originalText;
    }
}

// Валидация формы
function validateForm() {
    let isValid = true;

    // Проверяем обязательные поля
    const requiredFields = [
        'recipient-name',
        'email',
        'address',
        'phone'
    ];

    requiredFields.forEach(fieldId => {
        const field = document.getElementById(fieldId);
        if (!field.value.trim()) {
            highlightFieldError(field, 'Это поле обязательно для заполнения');
            isValid = false;
        } else if (fieldId === 'email' && !isValidEmail(field.value)) {
            highlightFieldError(field, 'Введите корректный email');
            isValid = false;
        } else if (fieldId === 'phone' && !isValidPhone(field.value)) {
            highlightFieldError(field, 'Введите корректный номер телефона');
            isValid = false;
        } else {
            clearFieldError(field);
        }
    });

    // Проверяем согласие с условиями
    const agreeTerms = document.getElementById('agree-terms');
    if (!agreeTerms.checked) {
        showError('Необходимо согласиться с условиями использования');
        isValid = false;
    }

    return isValid;
}

// Валидация отдельного поля
function validateField(e) {
    const field = e.target;

    if (field.hasAttribute('required') && !field.value.trim()) {
        highlightFieldError(field, 'Это поле обязательно для заполнения');
    } else if (field.type === 'email' && !isValidEmail(field.value)) {
        highlightFieldError(field, 'Введите корректный email');
    } else if (field.id === 'phone' && !isValidPhone(field.value)) {
        highlightFieldError(field, 'Введите корректный номер телефона');
    } else {
        clearFieldError(field);
    }
}

// Подсветка ошибок полей
function highlightInvalidFields() {
    const requiredFields = document.querySelectorAll('input[required], textarea[required]');

    requiredFields.forEach(field => {
        if (!field.value.trim()) {
            highlightFieldError(field, 'Это поле обязательно для заполнения');
        }
    });
}

function highlightFieldError(field, message) {
    field.style.borderColor = '#dc3545';
    field.style.boxShadow = '0 0 0 3px rgba(220, 53, 69, 0.1)';

    // Удаляем предыдущее сообщение об ошибке
    const existingError = field.parentNode.querySelector('.field-error');
    if (existingError) existingError.remove();

    // Добавляем новое сообщение об ошибке
    const errorElement = document.createElement('div');
    errorElement.className = 'field-error';
    errorElement.style.color = '#dc3545';
    errorElement.style.fontSize = '13px';
    errorElement.style.marginTop = '5px';
    errorElement.textContent = message;

    field.parentNode.appendChild(errorElement);
}

function clearFieldError(field) {
    field.style.borderColor = '#e9ecef';
    field.style.boxShadow = 'none';

    const existingError = field.parentNode.querySelector('.field-error');
    if (existingError) existingError.remove();
}

// Вспомогательные функции валидации
function isValidEmail(email) {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return emailRegex.test(email);
}

function isValidPhone(phone) {
    // Простая валидация для российских номеров
    const phoneRegex = /^\+7\s?\(?\d{3}\)?\s?\d{3}-?\d{2}-?\d{2}$/;
    return phoneRegex.test(phone);
}

// Показ состояния загрузки
function showLoading() {
    const itemsContainer = document.getElementById('order-items-container');
    const userContainer = document.getElementById('user-info-section');

    if (itemsContainer) {
        itemsContainer.innerHTML = '<div class="loading-spinner"></div>';
    }

    if (userContainer) {
        userContainer.innerHTML = '<div class="loading-spinner"></div>';
    }
}

// Показ успешного сообщения
function showSuccess(order) {
    const modal = document.getElementById('success-modal');
    const orderIdElement = document.getElementById('order-id');

    if (order && order.id) {
        orderIdElement.textContent = `#${order.id}`;
    }

    modal.classList.add('active');
    document.body.style.overflow = 'hidden';

    // Автоматическое перенаправление через 5 секунд
    setTimeout(() => {
        window.location.href = '/';
    }, 5000);
}

// Показ ошибки
function showError(message) {
    const modal = document.getElementById('error-modal');
    const messageElement = document.getElementById('error-message');

    messageElement.textContent = message;
    modal.classList.add('active');
    document.body.style.overflow = 'hidden';
}

// Закрытие модального окна
function closeModal() {
    document.querySelectorAll('.modal').forEach(modal => {
        modal.classList.remove('active');
    });
    document.body.style.overflow = '';
}

// Обновление кнопки оформления
function updateCheckoutButton(total) {
    const button = document.getElementById('submit-order-btn');
    if (button) {
        const priceElement = button.querySelector('.button-price');
        if (!priceElement) {
            const priceSpan = document.createElement('span');
            priceSpan.className = 'button-price';
            priceSpan.textContent = formatPrice(total);
            button.querySelector('.button-text').after(priceSpan);
        } else {
            priceElement.textContent = formatPrice(total);
        }
    }
}

// Форматирование цены
function formatPrice(price) {
    return new Intl.NumberFormat('ru-RU', {
        style: 'decimal',
        minimumFractionDigits: 2,
        maximumFractionDigits: 2
    }).format(price) + ' ₽';
}

// Глобальные функции
window.editUserProfile = function() {
    window.location.href = '/profile/edit';
};

window.closeModal = closeModal;

// Обработка клика вне модального окна
document.addEventListener('click', function(e) {
    if (e.target.classList.contains('modal')) {
        closeModal();
    }
});

// Обработка Escape для закрытия модальных окон
document.addEventListener('keydown', function(e) {
    if (e.key === 'Escape') {
        closeModal();
    }
});