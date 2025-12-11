// courier-panel.js - Полная версия с исправлениями

// Конфигурация API
const API_BASE = '/api';
let currentUser = null;
let currentFilter = 'assigned';

// Основная функция инициализации
document.addEventListener('DOMContentLoaded', async function() {
    console.log('Панель курьера загружена');

    try {
        // Получаем данные текущего пользователя
        const userResponse = await fetch(`${API_BASE}/users/me`);
        if (userResponse.ok) {
            currentUser = await userResponse.json();

            // Проверяем, что пользователь - курьер ИЛИ админ
            if (currentUser.role !== 'COURIER' && currentUser.role !== 'ADMIN') {
                alert('Доступ только для курьеров и администраторов!');
                window.location.href = '/';
                return;
            }

            // Обновляем UI
            updateUserInfo();

            // Загружаем данные
            await loadData();

        } else {
            console.warn('Не удалось получить данные пользователя');
            window.location.href = '/login';
            return;
        }

    } catch (error) {
        console.error('Ошибка инициализации:', error);
        showError('Ошибка загрузки данных');
    }
});

// Обновление информации о пользователе
function updateUserInfo() {
    const courierNameEl = document.getElementById('courierName');
    const courierRoleEl = document.querySelector('.courier-role');
    const headerTitle = document.querySelector('.courier-header h1');

    if (courierNameEl) courierNameEl.textContent = currentUser.name || getUserTitle();
    if (courierRoleEl) courierRoleEl.textContent = getRoleText(currentUser.role);

    // Если админ, меняем заголовок
    if (currentUser.role === 'ADMIN' && headerTitle) {
        headerTitle.textContent = 'Панель управления заказами';
        if (courierRoleEl) courierRoleEl.textContent = 'Администратор';
    }
}

// Загрузка всех данных
async function loadData() {
    try {
        await loadStats();
        await loadOrdersData('assigned');
        setActiveFilter('assigned');

    } catch (error) {
        console.error('Ошибка загрузки данных:', error);
        showError('Ошибка загрузки данных');
    }
}

// Загрузка статистики
async function loadStats() {
    try {
        if (!currentUser) return;

        // Обновляем UI с нулями пока загружаем
        updateStatsUI(0, 0, 0);

        let myOrders = [];
        let availableOrders = [];

        if (currentUser.role === 'COURIER') {
            // Для курьера: получаем его заказы
            try {
                const assignedResponse = await fetch(`${API_BASE}/couriers/assignedOrders?courierId=${currentUser.id}`);
                if (assignedResponse.ok) {
                    const assignedData = await assignedResponse.json();
                    myOrders = getOrdersFromResponse(assignedData);
                }
            } catch (error) {
                console.warn('Ошибка загрузки назначенных заказов:', error);
            }
        }

        // Получаем доступные заказы для всех
        try {
            const availableResponse = await fetch(`${API_BASE}/couriers/availableOrders`);
            if (availableResponse.ok) {
                const availableData = await availableResponse.json();
                availableOrders = getOrdersFromResponse(availableData);
            }
        } catch (error) {
            console.warn('Ошибка загрузки доступных заказов:', error);
        }

        // Считаем статистику
        let activeOrders = 0;
        if (currentUser.role === 'COURIER') {
            activeOrders = myOrders.filter(order => {
                const status = order.status;
                return status === 'PENDING' || status === 'DISPATCHED';
            }).length;
        }

        // Обновляем UI
        updateStatsUI(activeOrders, availableOrders.length, myOrders.length);

    } catch (error) {
        console.error('Ошибка загрузки статистики:', error);
    }
}

// Обновление UI статистики
function updateStatsUI(active, available, total) {
    const activeEl = document.getElementById('activeOrders');
    const availableEl = document.getElementById('availableOrders');
    const totalEl = document.getElementById('totalOrders');

    if (activeEl) activeEl.textContent = active;
    if (availableEl) availableEl.textContent = available;
    if (totalEl) totalEl.textContent = total;
}

// Загрузка заказов
async function loadOrdersData(filter = 'assigned') {
    currentFilter = filter;

    const ordersContainer = document.getElementById('ordersContainer');
    const ordersTitle = document.getElementById('ordersTitle');

    if (!ordersContainer) {
        console.error('Элемент ordersContainer не найден');
        return;
    }

    // Показываем индикатор загрузки
    ordersContainer.innerHTML = '<div class="loading">Загрузка заказов...</div>';

    try {
        let orders = [];
        let title = '';

        if (filter === 'assigned') {
            if (currentUser.role === 'COURIER') {
                // Мои заказы для курьера
                const response = await fetch(`${API_BASE}/couriers/assignedOrders?courierId=${currentUser.id}`);
                if (!response.ok) throw new Error(`HTTP ${response.status}`);

                const data = await response.json();
                orders = getOrdersFromResponse(data);
                title = 'Мои заказы';
            } else if (currentUser.role === 'ADMIN') {
                // Для админа пока пустой список (или можно сделать запрос всех заказов)
                orders = [];
                title = 'Все заказы';
            }

        } else if (filter === 'available') {
            // Доступные заказы для всех
            const response = await fetch(`${API_BASE}/couriers/availableOrders`);
            if (!response.ok) throw new Error(`HTTP ${response.status}`);

            const data = await response.json();
            orders = getOrdersFromResponse(data);
            title = currentUser.role === 'ADMIN' ? 'Заказы без курьера' : 'Доступные заказы';
        }

        if (ordersTitle) ordersTitle.textContent = title;
        displayOrders(orders, filter);

    } catch (error) {
        console.error('Ошибка загрузки заказов:', error);
        ordersContainer.innerHTML = `
            <div class="error-message">
                Ошибка загрузки заказов: ${getErrorMessage(error)}
                <br><br>
                <button onclick="loadOrdersData('${filter}')" class="btn btn-outline btn-small">
                    Повторить
                </button>
            </div>
        `;
    }
}

// Отображение заказов
function displayOrders(orders, filter) {
    const ordersContainer = document.getElementById('ordersContainer');
    if (!ordersContainer) return;

    if (!Array.isArray(orders) || orders.length === 0) {
        let message = '';
        if (currentUser.role === 'ADMIN') {
            message = filter === 'assigned' ? 'Нет заказов для отображения' : 'Нет заказов без курьера';
        } else {
            message = filter === 'assigned' ? 'У вас нет назначенных заказов' : 'В данный момент нет доступных заказов';
        }

        ordersContainer.innerHTML = `
            <div class="no-orders">
                <div class="no-orders-icon">📦</div>
                <h4>Заказов нет</h4>
                <p>${message}</p>
            </div>
        `;
        return;
    }

    // Очищаем контейнер
    ordersContainer.innerHTML = '';

    // Добавляем заказы
    orders.forEach(order => {
        try {
            const orderElement = createOrderElement(order, filter);
            ordersContainer.appendChild(orderElement);
        } catch (error) {
            console.error('Ошибка создания элемента заказа:', error, order);
        }
    });
}

// Создание элемента заказа
function createOrderElement(order, filter) {
    const template = document.getElementById('orderTemplate');
    if (!template) {
        throw new Error('Шаблон orderTemplate не найден');
    }

    const clone = template.content.cloneNode(true);
    const orderCard = clone.querySelector('.order-card');
    if (!orderCard) {
        throw new Error('Элемент .order-card не найден в шаблоне');
    }

    // Заполняем данные
    const orderId = order.id || 'N/A';
    orderCard.setAttribute('data-order-id', orderId);

    // Находим элементы
    const orderIdEl = orderCard.querySelector('.order-id');
    const customerNameEl = orderCard.querySelector('.customer-name');
    const customerPhoneEl = orderCard.querySelector('.customer-phone');
    const customerAddressEl = orderCard.querySelector('.customer-address');
    const priceValueEl = orderCard.querySelector('.price-value');
    const dateElement = orderCard.querySelector('.order-date');
    const courierInfoEl = orderCard.querySelector('.courier-info');
    const courierNameEl = orderCard.querySelector('.courier-name');
    const statusElement = orderCard.querySelector('.order-status');

    // Заполняем данные
    if (orderIdEl) orderIdEl.textContent = orderId;

    // Информация о пользователе
    const user = order.user || {};
    if (customerNameEl) customerNameEl.textContent = user.name || 'Не указан';
    if (customerPhoneEl) customerPhoneEl.textContent = user.phone || 'Не указан';
    if (customerAddressEl) customerAddressEl.textContent = order.shippingAddress || user.address || 'Не указан';

    // Информация о курьере
    const courier = order.courier || {};
    if (courierInfoEl && courierNameEl) {
        if (courier.name) {
            courierInfoEl.innerHTML = `<strong>Курьер:</strong> <span class="courier-name">${courier.name}</span>`;
        } else {
            courierInfoEl.innerHTML = '<strong>Курьер:</strong> <span class="courier-name">Не назначен</span>';
        }
    }

    // Цена заказа
    if (priceValueEl && order.totalAmount) {
        priceValueEl.textContent = `${formatPrice(order.totalAmount)} ₽`;
    }

    // Дата заказа
    if (dateElement && order.orderDate) {
        dateElement.textContent = formatDate(order.orderDate);
    }

    // Статус
    const status = order.status || 'PENDING';
    if (statusElement) {
        statusElement.textContent = getStatusText(status);
        statusElement.className = `order-status ${status.toLowerCase()}`;
    }

    // Настраиваем кнопки
    setupOrderButtons(orderCard, status, order.id, filter);

    return orderCard;
}

// Настройка кнопок заказа
function setupOrderButtons(orderCard, status, orderId, filter) {
    const acceptBtn = orderCard.querySelector('.accept-btn');
    const startBtn = orderCard.querySelector('.start-btn');
    const completeBtn = orderCard.querySelector('.complete-btn');
    const detailsBtn = orderCard.querySelector('.btn-outline');
    const cancelBtn = orderCard.querySelector('.cancel-btn');

    // Сначала скрываем все кнопки
    if (acceptBtn) acceptBtn.style.display = 'none';
    if (startBtn) startBtn.style.display = 'none';
    if (completeBtn) completeBtn.style.display = 'none';
    if (cancelBtn) cancelBtn.style.display = 'none';

    if (currentUser.role === 'COURIER') {
        if (filter === 'available') {
            if (acceptBtn) {
                acceptBtn.style.display = 'block';
                acceptBtn.onclick = () => assignOrder(orderId);
            }
        } else {
            switch(status) {
                case 'PENDING':
                    if (startBtn) {
                        startBtn.style.display = 'block';
                        startBtn.onclick = () => updateOrderStatus(orderId, 'DISPATCHED');
                    }
                    if (cancelBtn) {
                        cancelBtn.style.display = 'block';
                        cancelBtn.textContent = 'Отменить';
                        cancelBtn.onclick = () => cancelOrder(orderId); // CANCELLED
                    }
                    break;
                case 'DISPATCHED':
                    if (completeBtn) {
                        completeBtn.style.display = 'block';
                        completeBtn.onclick = () => updateOrderStatus(orderId, 'DELIVERED_TO_DESTINATION');
                    }
                    if (cancelBtn) {
                        cancelBtn.style.display = 'block';
                        cancelBtn.textContent = 'Вернуть';
                        cancelBtn.onclick = () => returnOrder(orderId); // RETURNED
                    }
                    break;
                case 'DELIVERED_TO_DESTINATION':
                    // После доставки можно только вернуть
                    if (cancelBtn) {
                        cancelBtn.style.display = 'block';
                        cancelBtn.textContent = 'Вернуть заказ';
                        cancelBtn.onclick = () => returnOrder(orderId); // RETURNED
                    }
                    break;
            }
        }
    } else if (currentUser.role === 'ADMIN') {
        if (filter === 'available') {
            if (acceptBtn) {
                acceptBtn.style.display = 'block';
                acceptBtn.textContent = 'Назначить курьера';
                acceptBtn.onclick = () => assignOrderAsAdmin(orderId);
            }
        } else {
            switch(status) {
                case 'PENDING':
                    if (startBtn) {
                        startBtn.style.display = 'block';
                        startBtn.textContent = 'Отправить в доставку';
                        startBtn.onclick = () => updateOrderStatus(orderId, 'DISPATCHED');
                    }
                    if (cancelBtn) {
                        cancelBtn.style.display = 'block';
                        cancelBtn.textContent = 'Отменить';
                        cancelBtn.onclick = () => cancelOrder(orderId);
                    }
                    break;
                case 'DISPATCHED':
                    if (completeBtn) {
                        completeBtn.style.display = 'block';
                        completeBtn.textContent = 'Завершить доставку';
                        completeBtn.onclick = () => updateOrderStatus(orderId, 'DELIVERED_TO_DESTINATION');
                    }
                    if (cancelBtn) {
                        cancelBtn.style.display = 'block';
                        cancelBtn.textContent = 'Вернуть заказ';
                        cancelBtn.onclick = () => returnOrder(orderId);
                    }
                    break;
                case 'DELIVERED_TO_DESTINATION':
                    if (cancelBtn) {
                        cancelBtn.style.display = 'block';
                        cancelBtn.textContent = 'Вернуть заказ';
                        cancelBtn.onclick = () => returnOrder(orderId);
                    }
                    break;
            }
        }
    }

    if (detailsBtn) {
        detailsBtn.onclick = () => showOrderDetails(orderCard);
    }
}

// Принять заказ (для курьера)
async function assignOrder(orderId) {
    if (!confirm(`Принять заказ #${orderId}?`)) return;

    try {
        const response = await fetch(`${API_BASE}/couriers/${orderId}/assign`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' }
        });

        if (!response.ok) {
            const error = await response.json();
            throw new Error(error.error || 'Ошибка при принятии заказа');
        }

        const data = await response.json();
        alert(`Заказ #${orderId} успешно принят!`);
        await loadOrdersData(currentFilter);
        await loadStats();

    } catch (error) {
        console.error('Ошибка при принятии заказа:', error);
        alert(`Ошибка: ${error.message}`);
    }
}

// Назначить заказ курьеру (для админа)
async function assignOrderAsAdmin(orderId) {
    if (!confirm(`Назначить себя курьером для заказа #${orderId}?`)) return;

    try {
        const response = await fetch(`${API_BASE}/couriers/${orderId}/assign`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' }
        });

        if (!response.ok) {
            const error = await response.json();
            throw new Error(error.error || 'Ошибка при назначении заказа');
        }

        const data = await response.json();
        alert(`Вы назначены курьером для заказа #${orderId}!`);
        await loadOrdersData(currentFilter);
        await loadStats();

    } catch (error) {
        console.error('Ошибка при назначении заказа:', error);
        alert(`Ошибка: ${error.message}`);
    }
}

// Обновить статус заказа
async function updateOrderStatus(orderId, status) {
    const statusText = getStatusText(status);
    const actionText = getActionText(status, currentUser.role);

    if (!confirm(`Вы уверены, что хотите ${actionText} заказа #${orderId}?`)) return;

    try {
        const response = await fetch(`${API_BASE}/couriers/orders/${orderId}/status?status=${status}`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' }
        });

        if (!response.ok) {
            const error = await response.json();
            throw new Error(error.error || 'Ошибка при обновлении статуса');
        }

        const data = await response.json();
        alert(`Статус заказа #${orderId} обновлен на "${statusText}"!`);
        await loadOrdersData(currentFilter);
        await loadStats();

    } catch (error) {
        console.error('Ошибка при обновлении статуса:', error);
        alert(`Ошибка: ${error.message}`);
    }
}

// Отменить заказ (CANCELLED - отмена ДО начала доставки)
async function cancelOrder(orderId) {
    if (!confirm(`Отменить заказ #${orderId}? Заказ еще не начат.`)) return;

    try {
        const response = await fetch(`${API_BASE}/couriers/orders/${orderId}/status?status=CANCELLED`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' }
        });

        if (!response.ok) {
            const error = await response.json();
            throw new Error(error.error || 'Ошибка при отмене заказа');
        }

        const data = await response.json();
        alert(`Заказ #${orderId} отменен!`);
        await loadOrdersData(currentFilter);
        await loadStats();

    } catch (error) {
        console.error('Ошибка при отмене заказа:', error);
        alert(`Ошибка: ${error.message}`);
    }
}

// Вернуть заказ (RETURNED - отмена ПОСЛЕ начала доставки)
async function returnOrder(orderId) {
    if (!confirm(`Вернуть заказ #${orderId}? Заказ уже в процессе доставки.`)) return;

    try {
        const response = await fetch(`${API_BASE}/couriers/orders/${orderId}/status?status=RETURNED`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' }
        });

        if (!response.ok) {
            const error = await response.json();
            throw new Error(error.error || 'Ошибка при возврате заказа');
        }

        const data = await response.json();
        alert(`Заказ #${orderId} возвращен!`);
        await loadOrdersData(currentFilter);
        await loadStats();

    } catch (error) {
        console.error('Ошибка при возврате заказа:', error);
        alert(`Ошибка: ${error.message}`);
    }
}

// Показать детали заказа
function showOrderDetails(orderCard) {
    const orderId = orderCard.querySelector('.order-id')?.textContent || 'N/A';
    const customerName = orderCard.querySelector('.customer-name')?.textContent || 'Не указан';
    const customerPhone = orderCard.querySelector('.customer-phone')?.textContent || 'Не указан';
    const customerAddress = orderCard.querySelector('.customer-address')?.textContent || 'Не указан';
    const status = orderCard.querySelector('.order-status')?.textContent || 'Неизвестно';
    const price = orderCard.querySelector('.price-value')?.textContent || '0 ₽';
    const orderDate = orderCard.querySelector('.order-date')?.textContent || 'Не указана';
    const courierName = orderCard.querySelector('.courier-name')?.textContent || 'Не назначен';

    let details = `
        Детали заказа #${orderId}:

        Клиент: ${customerName}
        Телефон: ${customerPhone}
        Адрес доставки: ${customerAddress}
        Курьер: ${courierName}
        Сумма: ${price}
        Статус: ${status}
        Дата заказа: ${orderDate}
    `;

    alert(details);
}

// Вспомогательные функции
function getOrdersFromResponse(response) {
    if (!response) return [];
    if (Array.isArray(response)) return response;
    if (response.content && Array.isArray(response.content)) return response.content;
    if (typeof response === 'object' && !Array.isArray(response)) {
        // Если это один объект, оборачиваем в массив
        if (response.id) return [response];
    }
    return [];
}

function getStatusText(status) {
    const statusMap = {
        'PENDING': 'Ожидает',
        'DISPATCHED': 'В доставке',
        'DELIVERED_TO_DESTINATION': 'Доставлен',
        'DELIVERED': 'Доставлен',
        'CANCELLED': 'Отменен',
        'RETURNED': 'Возвращен'
    };
    return statusMap[status] || status || 'Неизвестно';
}

function getActionText(status, role) {
    const actionMap = {
        'DISPATCHED': role === 'ADMIN' ? 'отправить в доставку' : 'начать доставку',
        'DELIVERED_TO_DESTINATION': 'завершить доставку',
        'DELIVERED': 'завершить доставку',
        'CANCELLED': 'отменить заказ',
        'RETURNED': 'вернуть заказ'
    };
    return actionMap[status] || 'выполнить действие';
}

function getRoleText(role) {
    const roleMap = {
        'COURIER': 'Курьер',
        'ADMIN': 'Администратор',
        'SELLER': 'Продавец',
        'USER': 'Пользователь'
    };
    return roleMap[role] || role;
}

function getUserTitle() {
    return currentUser?.role === 'ADMIN' ? 'Администратор' : 'Курьер';
}

function formatPrice(price) {
    if (!price) return '0';
    const numPrice = typeof price === 'string' ? parseFloat(price) : Number(price);
    return isNaN(numPrice) ? '0' : numPrice.toLocaleString('ru-RU', {
        minimumFractionDigits: 0,
        maximumFractionDigits: 2
    });
}

function formatDate(dateString) {
    try {
        const date = new Date(dateString);
        return date.toLocaleDateString('ru-RU', {
            day: '2-digit',
            month: '2-digit',
            year: 'numeric',
            hour: '2-digit',
            minute: '2-digit'
        });
    } catch (error) {
        return 'Неизвестная дата';
    }
}

function getErrorMessage(error) {
    if (error.message && error.message.includes('Failed to fetch')) {
        return 'Ошибка соединения с сервером';
    }
    if (error.message && error.message.includes('HTTP')) {
        return 'Ошибка сервера';
    }
    return error.message || 'Неизвестная ошибка';
}

function setActiveFilter(filter) {
    document.querySelectorAll('.filter-btn').forEach(btn => {
        btn.classList.remove('active');
        if (btn.textContent.includes(filter === 'assigned' ? 'Мои' : 'Доступные')) {
            btn.classList.add('active');
        }
    });
}

function showError(message) {
    const ordersContainer = document.getElementById('ordersContainer');
    if (!ordersContainer) return;

    ordersContainer.innerHTML = `
        <div class="error-message">
            ${message}
            <br><br>
            <button onclick="loadData()" class="btn btn-outline btn-small">
                Повторить
            </button>
        </div>
    `;
}

// Глобальные функции для HTML
window.setFilter = function(filter) {
    setActiveFilter(filter);
    loadOrdersData(filter).catch(console.error);
};

window.refreshOrders = function() {
    loadOrdersData(currentFilter).catch(console.error);
    loadStats().catch(console.error);
};

// Удаляем старые глобальные функции которые могут конфликтовать
delete window.loadOrders;
delete window.loadCourierData;
delete window.acceptOrder;
delete window.startDelivery;
delete window.completeOrder;
delete window.showOrderDetails;

// Экспортируем функции для использования в консоли (отладка)
if (typeof window !== 'undefined') {
    window.loadOrdersData = loadOrdersData;
    window.loadStats = loadStats;
    window.assignOrder = assignOrder;
    window.returnOrder = returnOrder;
    window.cancelOrder = cancelOrder;
    window.updateOrderStatus = updateOrderStatus;
}