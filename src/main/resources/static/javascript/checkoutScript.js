// checkoutScript.js - ИСПРАВЛЕННАЯ ВЕРСИЯ

document.addEventListener('DOMContentLoaded', function() {
    initCheckout();
});

function initCheckout() {
    loadCartData();
    setupEventHandlers();
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
        console.log('Данные с сервера:', data);

        // Отображаем данные
        renderOrderItems(data.cartItems || []);
        updateOrderSummary(data.totalPrice || 0, data.totalItems || 0);
        renderUserInfo(data.user || {});

        // Обновляем итоговую сумму в кнопке
        updateCheckoutButton(data.totalPrice || 0);

    } catch (error) {
        console.error('Ошибка при загрузке данных:', error);
        showError(`Ошибка загрузки данных: ${error.message}`);
    } finally {
        hideLoading();
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
    console.log('Рендерим user:', user);

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

    // Пробуем разные варианты названий полей
    const fullName = user.fullName ||
        `${user.firstName || ''} ${user.lastName || ''}`.trim() ||
        user.name ||
        'Не указано';

    const address = user.address ||
        user.deliveryAddress ||
        user.shippingAddress ||
        'Не указан';

    container.innerHTML = `
        <div class="user-info-header">
            <h3>Информация о покупателе</h3>
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
        </div>
    `;
}

// Настройка обработчиков событий
function setupEventHandlers() {
    const submitOrderBtn = document.getElementById('submit-order-btn');

    if (submitOrderBtn) {
        submitOrderBtn.addEventListener('click', handleSubmitOrder);
    }

    // Валидация полей при вводе
    const requiredFields = document.querySelectorAll('#checkout-form input[required]');
    requiredFields.forEach(field => {
        field.addEventListener('blur', validateField);
    });
}

// Настройка автозаполнения формы из данных пользователя
function setupAutoFill() {
    setTimeout(() => {
        const userEmail = document.querySelector('#user-info-section .user-detail:nth-child(2) span:last-child')?.textContent;
        const userName = document.querySelector('#user-info-section .user-detail:nth-child(1) span:last-child')?.textContent;
        const userAddress = document.querySelector('#user-info-section .user-detail:nth-child(3) span:last-child')?.textContent;

        // Автозаполняем только если данные есть и не "Не указано"
        if (userEmail && userEmail !== 'Не указан') {
            const emailInput = document.getElementById('email');
            if (emailInput) emailInput.value = userEmail;
        }

        if (userName && userName !== 'Не указано') {
            const nameInput = document.getElementById('recipient-name');
            if (nameInput) nameInput.value = userName;
        }

        if (userAddress && userAddress !== 'Не указан') {
            const addressInput = document.getElementById('address');
            if (addressInput) addressInput.value = userAddress;
        }
    }, 500);
}

// Обработка нажатия на кнопку "Оформить заказ" - ИСПРАВЛЕННАЯ ВЕРСИЯ
async function handleSubmitOrder(e) {
    e.preventDefault();

    console.log('Начинаем оформление заказа...');

    if (!validateForm()) {
        console.log('Форма не валидна');
        return;
    }

    const submitBtn = document.getElementById('submit-order-btn');
    if (!submitBtn) return;

    // Сохраняем оригинальный текст ДО try-catch
    const originalText = submitBtn.innerHTML;

    // Блокируем кнопку
    submitBtn.disabled = true;
    submitBtn.innerHTML = '<span>Оформление...</span>';

    try {
        // Отправляем POST запрос на создание заказа
        console.log('Отправляем запрос на /api/orders...');

        // Добавляем таймаут для запроса
        const controller = new AbortController();
        const timeoutId = setTimeout(() => controller.abort(), 10000); // 10 секунд таймаут

        const response = await fetch('/api/orders', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            credentials: 'include',
            signal: controller.signal
        });

        clearTimeout(timeoutId);

        console.log('Ответ сервера:', response.status);

        if (response.status === 401) {
            showError('Сессия истекла. Пожалуйста, войдите снова.');
            setTimeout(() => {
                window.location.href = '/login?redirect=/checkout';
            }, 2000);
            return;
        }

        // Пробуем прочитать ответ, даже если он неполный
        let result;
        try {
            result = await response.json();
            console.log('Результат создания заказа:', result);
        } catch (jsonError) {
            console.warn('Не удалось распарсить JSON ответ:', jsonError);
            // Если не удалось распарсить JSON, но статус 201 - считаем успехом
            if (response.status === 201) {
                result = { success: true };
            } else {
                throw new Error('Сервер вернул невалидный ответ');
            }
        }

        if (!response.ok && result && result.error) {
            throw new Error(result.error || `Ошибка ${response.status}`);
        }

        // Показываем сообщение об успехе и переходим на главную
        alert('✅ Заказ успешно оформлен!' +
            (result.order && result.order.id ? ' Номер заказа: #' + result.order.id : ''));

        // Немедленный редирект на главную
        window.location.href = '/';

    } catch (error) {
        console.error('Ошибка при оформлении заказа:', error);

        // Показываем понятное сообщение об ошибке
        let errorMessage = 'Не удалось оформить заказ. ';

        if (error.name === 'AbortError') {
            errorMessage += 'Время ожидания истекло. Попробуйте позже.';
        } else if (error.message.includes('Failed to fetch')) {
            errorMessage += 'Проблема с подключением к серверу.';
        } else {
            errorMessage += error.message;
        }

        showError(errorMessage);

        // Восстанавливаем кнопку
        submitBtn.disabled = false;
        submitBtn.innerHTML = originalText;

    } finally {
        // Убедимся, что кнопка разблокирована в любом случае
        if (submitBtn.disabled) {
            submitBtn.disabled = false;
            submitBtn.innerHTML = originalText;
        }
    }
}

// Валидация формы
function validateForm() {
    let isValid = true;

    // Проверяем только обязательные поля
    const requiredFields = [
        'recipient-name',
        'email',
        'address'
    ];

    requiredFields.forEach(fieldId => {
        const field = document.getElementById(fieldId);
        if (!field || !field.value.trim()) {
            highlightFieldError(field, 'Это поле обязательно для заполнения');
            isValid = false;
        } else if (fieldId === 'email' && !isValidEmail(field.value)) {
            highlightFieldError(field, 'Введите корректный email');
            isValid = false;
        } else {
            clearFieldError(field);
        }
    });

    // Проверяем согласие с условиями
    const agreeTerms = document.getElementById('agree-terms');
    if (!agreeTerms || !agreeTerms.checked) {
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
    } else {
        clearFieldError(field);
    }
}

function highlightFieldError(field, message) {
    if (!field) return;

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
    if (!field) return;

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

// Показ состояния загрузки
function showLoading() {
    const itemsContainer = document.getElementById('order-items-container');
    const userContainer = document.getElementById('user-info-section');

    if (itemsContainer) {
        itemsContainer.innerHTML = '<div class="loading-spinner">Загрузка...</div>';
    }

    if (userContainer) {
        userContainer.innerHTML = '<div class="loading-spinner">Загрузка...</div>';
    }
}

function hideLoading() {
    // Можно добавить, если нужно явно скрывать загрузку
}

// Показ ошибки
function showError(message) {
    const modal = document.getElementById('error-modal');
    const messageElement = document.getElementById('error-message');

    if (messageElement) {
        messageElement.textContent = message;
    }

    if (modal) {
        modal.classList.add('active');
        document.body.style.overflow = 'hidden';
    }
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
        minimumFractionDigits: 0,
        maximumFractionDigits: 0
    }).format(price) + ' ₽';
}

// Глобальные функции
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