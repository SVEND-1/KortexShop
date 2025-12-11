// Конфигурация API
const API_BASE = '/api';
let currentUser = null;
let currentFilter = 'assigned';
let currentPage = 0;
const pageSize = 3; // Количество заказов на странице
let totalPages = 0;
let totalElements = 0;

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
                const assignedResponse = await fetch(`${API_BASE}/couriers/assignedOrders?courierId=${currentUser.id}&pageSize=${pageSize}&pageNumber=0`);
                if (assignedResponse.ok) {
                    const assignedData = await assignedResponse.json();
                    myOrders = getOrdersFromResponse(assignedData);
                    updatePaginationInfo(assignedData);
                }
            } catch (error) {
                console.warn('Ошибка загрузки назначенных заказов:', error);
            }
        }

        // Получаем доступные заказы для всех
        try {
            const availableResponse = await fetch(`${API_BASE}/couriers/availableOrders?pageSize=${pageSize}&pageNumber=0`);
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

// Обновление информации о пагинации
function updatePaginationInfo(response) {
    if (response && typeof response === 'object') {
        // Проверяем разные возможные форматы ответа
        totalPages = response.totalPages || response.totalPages || 0;
        totalElements = response.totalElements || response.totalElements || 0;
        currentPage = response.number !== undefined ? response.number :
            (response.pageNumber !== undefined ? response.pageNumber : 0);

        console.log('Пагинация:', {
            totalPages,
            totalElements,
            currentPage,
            responseFormat: Object.keys(response)
        });
    } else {
        totalPages = 0;
        totalElements = 0;
        currentPage = 0;
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
async function loadOrdersData(filter = 'assigned', page = 0) {
    currentFilter = filter;
    currentPage = page;

    const ordersContainer = document.getElementById('ordersContainer');
    const ordersTitle = document.getElementById('ordersTitle');
    const paginationContainer = document.getElementById('paginationContainer');

    if (!ordersContainer) {
        console.error('Элемент ordersContainer не найден');
        return;
    }

    // Показываем индикатор загрузки
    ordersContainer.innerHTML = '<div class="loading">Загрузка заказов...</div>';

    // Скрываем пагинацию во время загрузки
    if (paginationContainer) {
        paginationContainer.style.display = 'none';
    }

    try {
        let orders = [];
        let title = '';
        let apiUrl = '';

        if (filter === 'assigned') {
            if (currentUser.role === 'COURIER') {
                // Мои заказы для курьера
                apiUrl = `${API_BASE}/couriers/assignedOrders?courierId=${currentUser.id}&pageSize=${pageSize}&pageNumber=${page}`;
                title = 'Мои заказы';
            } else if (currentUser.role === 'ADMIN') {
                // Для админа пока пустой список
                orders = [];
                title = 'Все заказы';
                displayOrders(orders, filter);
                return;
            }

        } else if (filter === 'available') {
            // Доступные заказы для всех
            apiUrl = `${API_BASE}/couriers/availableOrders?pageSize=${pageSize}&pageNumber=${page}`;
            title = currentUser.role === 'ADMIN' ? 'Заказы без курьера' : 'Доступные заказы';
        }

        console.log('Запрос к API:', apiUrl);

        const response = await fetch(apiUrl);
        console.log('Статус ответа:', response.status);

        if (!response.ok) {
            throw new Error(`HTTP ${response.status}`);
        }

        const data = await response.json();
        console.log('Данные от сервера:', data);

        orders = getOrdersFromResponse(data);
        console.log('Распарсенные заказы:', orders);

        updatePaginationInfo(data);

        if (ordersTitle) ordersTitle.textContent = title;
        displayOrders(orders, filter);

    } catch (error) {
        console.error('Ошибка загрузки заказов:', error);
        ordersContainer.innerHTML = `
            <div class="error-message">
                Ошибка загрузки заказов: ${getErrorMessage(error)}
                <br><br>
                <button onclick="loadOrdersData('${filter}', ${currentPage})" class="btn btn-outline btn-small">
                    Повторить
                </button>
            </div>
        `;

        // Скрываем пагинацию при ошибке
        if (paginationContainer) {
            paginationContainer.style.display = 'none';
        }
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

    // Добавляем пагинацию
    if (totalPages > 1) {
        addPaginationControls(ordersContainer, filter);
    }
}

// Добавление элементов пагинации
function addPaginationControls(container, filter) {
    const paginationContainer = document.getElementById('paginationContainer');
    if (!paginationContainer) return;

    // Очищаем контейнер
    paginationContainer.innerHTML = '';

    // Если только одна страница или нет страниц - скрываем пагинацию
    if (totalPages <= 1) {
        paginationContainer.style.display = 'none';
        return;
    }

    // Показываем контейнер
    paginationContainer.style.display = 'block';

    // Создаем контейнер для кнопок
    const paginationDiv = document.createElement('div');
    paginationDiv.className = 'pagination';

    // Информация о странице
    const pageInfo = document.createElement('span');
    pageInfo.className = 'page-info';
    pageInfo.textContent = `Страница ${currentPage + 1} из ${totalPages}`;

    // Кнопка "Назад"
    const prevButton = document.createElement('button');
    prevButton.className = 'btn btn-outline pagination-btn';
    prevButton.innerHTML = '&larr; Назад';
    prevButton.disabled = currentPage === 0;
    prevButton.onclick = function() {
        if (currentPage > 0) {
            loadOrdersData(filter, currentPage - 1);
        }
    };

    // Кнопка "Вперед"
    const nextButton = document.createElement('button');
    nextButton.className = 'btn btn-outline pagination-btn';
    nextButton.innerHTML = 'Вперед &rarr;';
    nextButton.disabled = currentPage >= totalPages - 1;
    nextButton.onclick = function() {
        if (currentPage < totalPages - 1) {
            loadOrdersData(filter, currentPage + 1);
        }
    };

    // Добавляем элементы
    paginationDiv.appendChild(prevButton);
    paginationDiv.appendChild(pageInfo);
    paginationDiv.appendChild(nextButton);

    // Добавляем в контейнер
    paginationContainer.appendChild(paginationDiv);
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
        statusElement.className = `order-status ${status}`;
    }

    // Настраиваем кнопки
    setupOrderButtons(orderCard, status, order.id, filter);

    return orderCard;
}

// Настройка кнопок заказа - обновленная версия
function setupOrderButtons(orderCard, status, orderId, filter) {
    const acceptBtn = orderCard.querySelector('.accept-btn');
    const startBtn = orderCard.querySelector('.start-btn');
    const completeBtn = orderCard.querySelector('.complete-btn');
    const detailsBtn = orderCard.querySelector('.details-btn');
    const cancelBtn = orderCard.querySelector('.cancel-btn');
    const returnBtn = orderCard.querySelector('.return-btn');

    // Сначала скрываем все кнопки
    if (acceptBtn) acceptBtn.style.display = 'none';
    if (startBtn) startBtn.style.display = 'none';
    if (completeBtn) completeBtn.style.display = 'none';
    if (cancelBtn) cancelBtn.style.display = 'none';
    if (returnBtn) returnBtn.style.display = 'none';
    if (detailsBtn) detailsBtn.style.display = 'none';

    // Логика для разных фильтров и ролей
    if (filter === 'available') {
        // Фильтр "Доступные заказы"
        if (acceptBtn) {
            acceptBtn.style.display = 'block';
            acceptBtn.textContent = currentUser.role === 'ADMIN' ? 'Назначить курьера' : 'Принять заказ';
            acceptBtn.onclick = () => {
                if (currentUser.role === 'ADMIN') {
                    assignOrderAsAdmin(orderId);
                } else {
                    assignOrder(orderId);
                }
            };
        }
        if (detailsBtn) {
            detailsBtn.style.display = 'block';
            detailsBtn.textContent = 'Подробнее';
            detailsBtn.onclick = () => showOrderDetails(orderCard);
        }
    } else {
        // Фильтр "Мои заказы" (или аналогичный)
        switch(status) {
            case 'PENDING':
                // Статус 1: PENDING
                if (startBtn) {
                    startBtn.style.display = 'block';
                    startBtn.textContent = 'Начать доставку';
                    startBtn.onclick = () => updateOrderStatus(orderId, 'DISPATCHED');
                }
                if (cancelBtn) {
                    cancelBtn.style.display = 'block';
                    cancelBtn.textContent = 'Отмена';
                    cancelBtn.onclick = () => updateOrderStatus(orderId, 'CANCELLED');
                }
                if (detailsBtn) {
                    detailsBtn.style.display = 'block';
                    detailsBtn.textContent = 'Подробнее';
                    detailsBtn.onclick = () => showOrderDetails(orderCard);
                }
                break;

            case 'DISPATCHED':
                // Статус 2: DISPATCHED (после нажатия "Начать доставку")
                if (completeBtn) {
                    completeBtn.style.display = 'block';
                    completeBtn.textContent = 'Доставил';
                    completeBtn.onclick = () => updateOrderStatus(orderId, 'DELIVERED_TO_DESTINATION');
                }
                if (detailsBtn) {
                    detailsBtn.style.display = 'block';
                    detailsBtn.textContent = 'Подробнее';
                    detailsBtn.onclick = () => showOrderDetails(orderCard);
                }
                break;

            case 'DELIVERED_TO_DESTINATION':
                // Статус 3: DELIVERED_TO_DESTINATION (после "Доставил")
                // Кнопка "Завершить заказ" (переход в COMPLETED)
                if (completeBtn) {
                    completeBtn.style.display = 'block';
                    completeBtn.textContent = 'Завершить заказ';
                    completeBtn.className = 'btn btn-success btn-small complete-btn';
                    completeBtn.onclick = () => updateOrderStatus(orderId, 'COMPLETED');
                }
                // Кнопка "Вернуть"
                if (returnBtn) {
                    returnBtn.style.display = 'block';
                    returnBtn.onclick = () => updateOrderStatus(orderId, 'RETURNED');
                }
                if (detailsBtn) {
                    detailsBtn.style.display = 'block';
                    detailsBtn.textContent = 'Подробнее';
                    detailsBtn.onclick = () => showOrderDetails(orderCard);
                }
                break;

            case 'COMPLETED':
            case 'CANCELLED':
            case 'RETURNED':
                // Финальные статусы - только детали
                if (detailsBtn) {
                    detailsBtn.style.display = 'block';
                    detailsBtn.textContent = 'Подробнее';
                    detailsBtn.onclick = () => showOrderDetails(orderCard);
                }
                break;

            default:
                // Для остальных статусов показываем только детали
                if (detailsBtn) {
                    detailsBtn.style.display = 'block';
                    detailsBtn.textContent = 'Подробнее';
                    detailsBtn.onclick = () => showOrderDetails(orderCard);
                }
        }
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
        await loadOrdersData(currentFilter, currentPage);
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
        await loadOrdersData(currentFilter, currentPage);
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
        await loadOrdersData(currentFilter, currentPage);
        await loadStats();

    } catch (error) {
        console.error('Ошибка при обновлении статуса:', error);
        alert(`Ошибка: ${error.message}`);
    }
}

// Отменить заказ
async function cancelOrder(orderId) {
    if (!confirm(`Отменить заказ #${orderId}?`)) return;

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
        await loadOrdersData(currentFilter, currentPage);
        await loadStats();

    } catch (error) {
        console.error('Ошибка при отмене заказа:', error);
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

    console.log('Ответ от сервера:', response);

    // 1. Если response уже массив
    if (Array.isArray(response)) return response;

    // 2. Если это объект с полем content (Page format)
    if (response.content && Array.isArray(response.content)) {
        return response.content;
    }

    // 3. Если это объект с данными заказа (один заказ)
    if (typeof response === 'object' && response.id) {
        return [response];
    }

    // 4. Если это пустой объект или неожиданный формат
    return [];
}

function getStatusText(status) {
    const statusMap = {
        'PENDING': 'Ожидает',
        'DISPATCHED': 'В доставке',
        'DELIVERED_TO_DESTINATION': 'Доставлен в пункт назначения',
        'COMPLETED': 'Завершен',
        'CANCELLED': 'Отменен',
        'RETURNED': 'Возвращен'
    };
    return statusMap[status] || status || 'Неизвестно';
}

function getActionText(status, role) {
    const actionMap = {
        'DISPATCHED': role === 'ADMIN' ? 'отправить в доставку' : 'начать доставку',
        'DELIVERED_TO_DESTINATION': 'отметить как доставленный',
        'COMPLETED': 'завершить заказ',
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
    currentPage = 0;
    loadOrdersData(filter, 0).catch(error => {
        console.error('Ошибка при смене фильтра:', error);
        showError('Ошибка загрузки заказов');
    });
};

window.refreshOrders = function() {
    loadOrdersData(currentFilter, currentPage).catch(console.error);
    loadStats().catch(console.error);
};

// Удаляем старые глобальные функции которые могут конфликтовать
delete window.loadOrders;
delete window.loadCourierData;
delete window.acceptOrder;
delete window.startDelivery;
delete window.completeOrder;
delete window.showOrderDetails;