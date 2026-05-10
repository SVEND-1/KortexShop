// checkoutScript.js - Упрощенная версия с поддержкой комментария

document.addEventListener('DOMContentLoaded', function() {
    initCheckout();
});

let currentUserData = null;

function initCheckout() {
    loadCartData();
    setupEventHandlers();
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

        currentUserData = data.user || {};

        // Отображаем данные
        renderOrderItems(data.cartItems || []);
        updateOrderSummary(data.totalPrice || 0, data.totalItems || 0);
        renderUserDeliveryInfo(data.user || {});

        // Проверяем наличие адреса
        checkAddressAndUpdateButton(data.user || {});

        // Обновляем итоговую сумму в кнопке
        updateCheckoutButton(data.totalPrice || 0);

    } catch (error) {
        console.error('Ошибка при загрузке данных:', error);
        showError(`Ошибка загрузки данных: ${error.message}`);
    } finally {
        hideLoading();
    }
}

// Проверка адреса и обновление кнопки
function checkAddressAndUpdateButton(user) {
    const submitBtn = document.getElementById('submit-order-btn');
    const addressWarning = document.getElementById('addressWarning');

    const hasAddress = user.address && user.address.trim() !== '' && user.address !== 'null' && user.address !== 'Адрес не указан';

    if (!hasAddress) {
        if (addressWarning) addressWarning.style.display = 'flex';
        if (submitBtn) {
            submitBtn.disabled = true;
            submitBtn.style.opacity = '0.5';
            submitBtn.title = 'Для оформления заказа необходимо указать адрес в профиле';
        }
        return false;
    } else {
        if (addressWarning) addressWarning.style.display = 'none';
        if (submitBtn) {
            submitBtn.disabled = false;
            submitBtn.style.opacity = '1';
        }
        return true;
    }
}

// Отображение информации о доставке
function renderUserDeliveryInfo(user) {
    const nameEl = document.getElementById('display-name');
    const emailEl = document.getElementById('display-email');
    const addressEl = document.getElementById('display-address');

    const fullName = user.fullName || user.name || 'Не указано';
    const email = user.email || 'Не указан';
    const address = user.address && user.address !== 'null' ? user.address : 'Не указан';

    if (nameEl) nameEl.textContent = fullName;
    if (emailEl) emailEl.textContent = email;
    if (addressEl) addressEl.textContent = address;

    // Подсветка адреса если не заполнен
    if (!address || address === 'Не указан') {
        if (addressEl) {
            addressEl.style.color = '#dc3545';
            addressEl.style.fontWeight = 'bold';
        }
    } else {
        if (addressEl) {
            addressEl.style.color = '#28a745';
        }
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
                    <div class="item-name">${escapeHtml(productName)}</div>
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

// Настройка обработчиков событий
function setupEventHandlers() {
    const submitOrderBtn = document.getElementById('submit-order-btn');

    if (submitOrderBtn) {
        submitOrderBtn.addEventListener('click', handleSubmitOrder);
    }
}

// Обработка нажатия на кнопку "Оформить заказ"
async function handleSubmitOrder(e) {
    e.preventDefault();

    console.log('Начинаем оформление заказа...');

    // Проверяем наличие адреса еще раз
    if (!currentUserData || !currentUserData.address || currentUserData.address.trim() === '' || currentUserData.address === 'null') {
        showError('Для оформления заказа необходимо указать адрес доставки в профиле');
        return;
    }

    const submitBtn = document.getElementById('submit-order-btn');
    if (!submitBtn) return;

    const originalText = submitBtn.innerHTML;

    submitBtn.disabled = true;
    submitBtn.innerHTML = '<span>Оформление...</span>';

    try {
        // Получаем комментарий из textarea
        const comment = document.getElementById('comment')?.value || '';

        // Отправляем комментарий как Query Parameter (как требует ваш контроллер)
        // @PostMapping() public ResponseEntity<?> createOrder(@RequestParam String comment)

        console.log('Отправка комментария:', comment);

        const controller = new AbortController();
        const timeoutId = setTimeout(() => controller.abort(), 10000);

        // ВАЖНО: отправляем comment как Query Parameter, а не в body!
        const response = await fetch(`/api/orders?comment=${encodeURIComponent(comment)}`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Accept': 'application/json'
            },
            credentials: 'include',
            signal: controller.signal
            // НЕ отправляем body, так как используем @RequestParam
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

        let result;
        try {
            result = await response.json();
            console.log('Результат создания заказа:', result);
        } catch (jsonError) {
            if (response.status === 200 || response.status === 201) {
                result = { success: true };
            } else {
                throw new Error('Сервер вернул невалидный ответ');
            }
        }

        if (!response.ok && result && result.error) {
            throw new Error(result.error || `Ошибка ${response.status}`);
        }

        // Показываем модальное окно успеха
        const orderId = result.id || result.orderId;
        showSuccessModal(orderId);

    } catch (error) {
        console.error('Ошибка при оформлении заказа:', error);

        let errorMessage = 'Не удалось оформить заказ. ';

        if (error.name === 'AbortError') {
            errorMessage += 'Время ожидания истекло. Попробуйте позже.';
        } else if (error.message.includes('Failed to fetch')) {
            errorMessage += 'Проблема с подключением к серверу.';
        } else if (error.message.includes('comment')) {
            errorMessage += 'Ошибка при отправке комментария.';
        } else {
            errorMessage += error.message;
        }

        showError(errorMessage);

        submitBtn.disabled = false;
        submitBtn.innerHTML = originalText;
    }
}

// Показ модального окна успеха
function showSuccessModal(orderId) {
    const modal = document.getElementById('success-modal');
    const orderIdSpan = document.getElementById('order-id');

    if (orderIdSpan && orderId) {
        orderIdSpan.textContent = `#${orderId}`;
    }

    if (modal) {
        modal.classList.add('active');
        document.body.style.overflow = 'hidden';
    }
}

// Показ состояния загрузки
function showLoading() {
    const itemsContainer = document.getElementById('order-items-container');

    if (itemsContainer) {
        itemsContainer.innerHTML = '<div class="loading-spinner">Загрузка...</div>';
    }
}

function hideLoading() {}

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

function escapeHtml(text) {
    if (!text) return '';
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

// Глобальные функции
window.closeModal = closeModal;

document.addEventListener('click', function(e) {
    if (e.target.classList.contains('modal')) {
        closeModal();
    }
});

document.addEventListener('keydown', function(e) {
    if (e.key === 'Escape') {
        closeModal();
    }
});