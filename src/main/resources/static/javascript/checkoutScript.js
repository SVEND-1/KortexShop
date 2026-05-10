// checkoutScript.js - С поддержкой платежной системы

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

        renderOrderItems(data.cartItems || []);
        updateOrderSummary(data.totalPrice || 0, data.totalItems || 0);
        renderUserDeliveryInfo(data.user || {});
        checkAddressAndUpdateButton(data.user || {});
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
        const comment = document.getElementById('comment')?.value || '';

        console.log('Отправка комментария:', comment);

        const controller = new AbortController();
        const timeoutId = setTimeout(() => controller.abort(), 10000);

        const response = await fetch(`/api/orders?comment=${encodeURIComponent(comment)}`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Accept': 'application/json'
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

        let result;
        try {
            result = await response.json();
            console.log('Результат создания заказа (PaymentCreateResponse):', result);
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

        // Если есть URL для оплаты - перенаправляем на платежную страницу
        if (result.urlPay) {
            console.log('Перенаправление на страницу оплаты:', result.urlPay);

            // Сохраняем информацию о заказе в localStorage
            const paymentData = {
                orderId: result.orderId,
                paymentId: result.paymentId,
                urlPay: result.urlPay,
                createdAt: new Date().toISOString()
            };
            localStorage.setItem('pendingPayment', JSON.stringify(paymentData));

            // Перенаправляем на страницу оплаты
            window.location.href = result.urlPay;
        } else {
            // Если платеж не требуется - показываем успех
            showSuccessModal(result.orderId);
        }

    } catch (error) {
        console.error('Ошибка при оформлении заказа:', error);

        let errorMessage = 'Не удалось оформить заказ. ';

        if (error.name === 'AbortError') {
            errorMessage += 'Время ожидания истекло. Попробуйте позже.';
        } else if (error.message.includes('Failed to fetch')) {
            errorMessage += 'Проблема с подключением к серверу.';
        } else {
            errorMessage += error.message;
        }

        showError(errorMessage);

        submitBtn.disabled = false;
        submitBtn.innerHTML = originalText;
    }
}

// Функция для проверки статуса платежа после возврата
async function checkPaymentStatus(orderId) {
    try {
        console.log('Проверка статуса платежа для заказа:', orderId);

        const response = await fetch(`/api/orders/${orderId}/status`, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json',
                'Accept': 'application/json'
            },
            credentials: 'include'
        });

        if (response.ok) {
            const orderData = await response.json();
            console.log('Статус заказа:', orderData);

            if (orderData.status === 'PENDING' || orderData.status === 'PAID') {
                // Платеж успешен
                showSuccessModal(orderId);
                // Очищаем сохраненные данные
                localStorage.removeItem('pendingPayment');
                return true;
            } else if (orderData.status === 'PAYMENT') {
                // Еще ожидает оплаты
                showPaymentPendingModal(orderId);
                return false;
            }
        }
        return false;
    } catch (error) {
        console.error('Ошибка проверки статуса:', error);
        return false;
    }
}

// Показ модального окна ожидания оплаты
function showPaymentPendingModal(orderId) {
    const modal = document.getElementById('payment-pending-modal');
    if (!modal) {
        // Создаем модальное окно если его нет
        createPaymentPendingModal();
    }

    const pendingModal = document.getElementById('payment-pending-modal');
    const orderIdSpan = document.getElementById('pending-order-id');

    if (orderIdSpan && orderId) {
        orderIdSpan.textContent = `#${orderId}`;
    }

    if (pendingModal) {
        pendingModal.classList.add('active');
        document.body.style.overflow = 'hidden';
    }
}

function createPaymentPendingModal() {
    const modalHTML = `
        <div id="payment-pending-modal" class="modal">
            <div class="modal-content">
                <div class="modal-icon pending">⏳</div>
                <h3>Ожидание оплаты</h3>
                <p>Заказ <strong id="pending-order-id">#00000</strong> ожидает оплаты</p>
                <p>После успешной оплаты статус заказа обновится автоматически</p>
                <div class="modal-actions">
                    <button class="btn btn-primary" onclick="checkPaymentAgain()">
                        Проверить статус
                    </button>
                    <button class="btn btn-secondary" onclick="window.location.href='/profile'">
                        Перейти в профиль
                    </button>
                </div>
            </div>
        </div>
    `;
    document.body.insertAdjacentHTML('beforeend', modalHTML);
}

// Функция для повторной проверки платежа
window.checkPaymentAgain = async function() {
    const pendingPayment = localStorage.getItem('pendingPayment');
    if (pendingPayment) {
        const data = JSON.parse(pendingPayment);
        await checkPaymentStatus(data.orderId);
    } else {
        closeAllModals();
        window.location.href = '/profile';
    }
};

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

    // Очищаем корзину в localStorage
    localStorage.removeItem('cart');
}

function showLoading() {
    const itemsContainer = document.getElementById('order-items-container');

    if (itemsContainer) {
        itemsContainer.innerHTML = '<div class="loading-spinner">Загрузка...</div>';
    }
}

function hideLoading() {}

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

function closeModal() {
    document.querySelectorAll('.modal').forEach(modal => {
        modal.classList.remove('active');
    });
    document.body.style.overflow = '';
}

function closeAllModals() {
    closeModal();
    const pendingModal = document.getElementById('payment-pending-modal');
    if (pendingModal) {
        pendingModal.classList.remove('active');
    }
}

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
window.closeAllModals = closeAllModals;

// При загрузке страницы проверяем, не вернулись ли с оплаты
document.addEventListener('DOMContentLoaded', function() {
    const pendingPayment = localStorage.getItem('pendingPayment');
    if (pendingPayment) {
        const data = JSON.parse(pendingPayment);
        console.log('Обнаружен ожидающий платеж для заказа:', data.orderId);
        checkPaymentStatus(data.orderId);
    }
});

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