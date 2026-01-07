// Конфигурация API
const API_BASE = '/api';
let currentUser = null;
let currentFilter = 'assigned';
let currentPage = 0;
const pageSize = 3;
let totalPages = 0;
let totalElements = 0;

// Основная функция инициализации
document.addEventListener('DOMContentLoaded', async function() {
    console.log('Панель курьера загружена');

    try {
        // Получаем данные текущего пользователя
        const userResponse = await fetch(`${API_BASE}/users/me`);

        if (!userResponse.ok) {
            if (userResponse.status === 401) {
                alert('Требуется авторизация!');
                window.location.href = '/login';
                return;
            }
            const errorText = await userResponse.text();
            throw new Error(`HTTP ${userResponse.status}: ${errorText}`);
        }

        const userData = await userResponse.json();
        console.log('Данные пользователя:', userData);

        // Проверяем структуру ответа
        if (userData.error) {
            throw new Error(userData.error);
        }

        currentUser = userData;

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

    } catch (error) {
        console.error('Ошибка инициализации:', error);
        showError(`Ошибка загрузки данных: ${error.message}`);

        // Если ошибка авторизации, перенаправляем на логин
        if (error.message.includes('401') || error.message.includes('авторизация')) {
            setTimeout(() => window.location.href = '/login', 2000);
        }
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
        showError(`Ошибка загрузки данных: ${error.message}`);
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
                const assignedResponse = await fetch(`${API_BASE}/couriers/assignedOrders?courierId=${currentUser.id}&pageSize=100&pageNumber=0`);
                if (assignedResponse.ok) {
                    const result = await assignedResponse.json();
                    console.log('Статистика - назначенные заказы:', result);

                    // Обработка разных форматов ответа
                    myOrders = extractOrdersFromResponse(result);
                }
            } catch (error) {
                console.warn('Ошибка загрузки назначенных заказов для статистики:', error);
            }
        }

        // Получаем доступные заказы для всех
        try {
            const availableResponse = await fetch(`${API_BASE}/couriers/availableOrders?pageSize=100&pageNumber=0`);
            if (availableResponse.ok) {
                const result = await availableResponse.json();
                console.log('Статистика - доступные заказы:', result);

                availableOrders = extractOrdersFromResponse(result);
            }
        } catch (error) {
            console.warn('Ошибка загрузки доступных заказов для статистики:', error);
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

// Извлечение заказов из ответа API (универсальная функция)
function extractOrdersFromResponse(response) {
    if (!response) return [];

    console.log('Извлекаем заказы из ответа:', response);

    // Ваш API возвращает Map с ключами: content, totalPages, totalElements и т.д.
    if (response.content && Array.isArray(response.content)) {
        return response.content;
    }

    // Если API возвращает напрямую массив
    if (Array.isArray(response)) {
        return response;
    }

    // Если это один объект заказа
    if (response.id && response.status) {
        return [response];
    }

    // Пытаемся найти заказы в других возможных полях
    for (const key in response) {
        if (Array.isArray(response[key]) && response[key].length > 0 && response[key][0].id) {
            return response[key];
        }
    }

    return [];
}

// Обновление информации о пагинации
function updatePaginationInfo(response) {
    if (!response || typeof response !== 'object') {
        totalPages = 0;
        totalElements = 0;
        currentPage = 0;
        return;
    }

    // Ваш API использует эти поля (из Spring Data Page)
    totalPages = response.totalPages || 0;
    totalElements = response.totalElements || 0;
    currentPage = response.number !== undefined ? response.number : 0;

    console.log('Пагинация обновлена:', {
        totalPages,
        totalElements,
        currentPage,
        responseKeys: Object.keys(response)
    });
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
        let apiUrl = '';
        let title = '';

        if (filter === 'assigned') {
            if (currentUser.role === 'COURIER') {
                apiUrl = `${API_BASE}/couriers/assignedOrders?courierId=${currentUser.id}&pageSize=${pageSize}&pageNumber=${page}`;
                title = 'Мои заказы';
            } else if (currentUser.role === 'ADMIN') {
                ordersContainer.innerHTML = `
                    <div class="no-orders">
                        <div class="no-orders-icon">📦</div>
                        <h4>Заказов нет</h4>
                        <p>Используйте фильтр "Доступные заказы"</p>
                    </div>
                `;
                if (ordersTitle) ordersTitle.textContent = 'Все заказы';
                return;
            }
        } else if (filter === 'available') {
            apiUrl = `${API_BASE}/couriers/availableOrders?pageSize=${pageSize}&pageNumber=${page}`;
            title = currentUser.role === 'ADMIN' ? 'Заказы без курьера' : 'Доступные заказы';
        }

        console.log('Запрос к API:', apiUrl);

        const response = await fetch(apiUrl);

        if (!response.ok) {
            const errorText = await response.text();
            console.error('Текст ошибки:', errorText);
            throw new Error(`Ошибка сервера: ${response.status}`);
        }

        const result = await response.json();
        console.log('Полный ответ от API:', result);

        // Проверяем успешность операции
        if (result.success === false) {
            throw new Error(result.message || result.error || 'Ошибка сервера');
        }

        // Извлекаем заказы из ответа
        const orders = extractOrdersFromResponse(result);
        console.log('Извлеченные заказы:', orders);

        // Обновляем информацию о пагинации
        updatePaginationInfo(result);

        // Обновляем заголовок
        if (ordersTitle) ordersTitle.textContent = title;

        // Отображаем заказы
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
    const paginationContainer = document.getElementById('paginationContainer');

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

        // Скрываем пагинацию
        if (paginationContainer) {
            paginationContainer.style.display = 'none';
        }
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

    // Добавляем пагинацию если нужно
    if (totalPages > 1 && paginationContainer) {
        addPaginationControls(paginationContainer, filter);
    } else if (paginationContainer) {
        paginationContainer.style.display = 'none';
    }
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

    const orderId = order.id || 'N/A';
    orderCard.setAttribute('data-order-id', orderId);

    // Находим все элементы
    const elements = {
        orderId: orderCard.querySelector('.order-id'),
        customerName: orderCard.querySelector('.customer-name'),
        customerPhone: orderCard.querySelector('.customer-phone'),
        customerAddress: orderCard.querySelector('.customer-address'),
        priceValue: orderCard.querySelector('.price-value'),
        orderDate: orderCard.querySelector('.order-date'),
        courierInfo: orderCard.querySelector('.courier-info'),
        courierName: orderCard.querySelector('.courier-name'),
        orderStatus: orderCard.querySelector('.order-status'),
        acceptBtn: orderCard.querySelector('.accept-btn'),
        startBtn: orderCard.querySelector('.start-btn'),
        completeBtn: orderCard.querySelector('.complete-btn'),
        detailsBtn: orderCard.querySelector('.details-btn'),
        cancelBtn: orderCard.querySelector('.cancel-btn'),
        returnBtn: orderCard.querySelector('.return-btn')
    };

    // Заполняем основные данные
    if (elements.orderId) elements.orderId.textContent = `Заказ #${orderId}`;

    // Информация о клиенте
    const user = order.user || {};
    if (elements.customerName) elements.customerName.textContent = user.name || 'Не указано';
    if (elements.customerPhone) elements.customerPhone.textContent = user.phone || 'Не указано';
    if (elements.customerAddress) {
        elements.customerAddress.textContent = order.shippingAddress || user.address || 'Не указано';
    }

    // Информация о курьере
    const courier = order.courier || {};
    if (elements.courierInfo) {
        if (courier.name) {
            elements.courierInfo.innerHTML = `<strong>Курьер:</strong> <span class="courier-name">${courier.name}</span>`;
        } else {
            elements.courierInfo.innerHTML = '<strong>Курьер:</strong> <span class="courier-name">Не назначен</span>';
        }
    }

    // Цена и дата
    if (elements.priceValue) {
        const price = order.totalAmount || 0;
        elements.priceValue.textContent = `${formatPrice(price)} ₽`;
    }

    if (elements.orderDate && order.orderDate) {
        elements.orderDate.textContent = formatDate(order.orderDate);
    }

    // Статус
    const status = order.status || 'PENDING';
    if (elements.orderStatus) {
        elements.orderStatus.textContent = getStatusText(status);
        elements.orderStatus.className = `order-status status-${status.toLowerCase()}`;
    }

    // Настраиваем кнопки
    setupOrderButtons(elements, status, orderId, filter);

    return orderCard;
}

// Настройка кнопок заказа
function setupOrderButtons(elements, status, orderId, filter) {
    // Сначала скрываем все кнопки
    Object.values(elements).forEach(el => {
        if (el && el.classList && el.classList.contains('btn')) {
            el.style.display = 'none';
        }
    });

    // Показываем кнопку подробностей всегда
    if (elements.detailsBtn) {
        elements.detailsBtn.style.display = 'inline-block';
        elements.detailsBtn.onclick = () => showOrderDetails(orderId);
    }

    // Логика для доступных заказов
    if (filter === 'available') {
        if (elements.acceptBtn) {
            elements.acceptBtn.style.display = 'inline-block';
            elements.acceptBtn.textContent = currentUser.role === 'ADMIN' ? 'Назначить курьера' : 'Принять заказ';
            elements.acceptBtn.onclick = () => {
                if (currentUser.role === 'ADMIN') {
                    assignOrderAsAdmin(orderId);
                } else {
                    assignOrder(orderId);
                }
            };
        }
        return;
    }

    // Логика для назначенных заказов
    const upperStatus = status.toUpperCase();

    switch(upperStatus) {
        case 'PENDING':
            if (elements.startBtn) {
                elements.startBtn.style.display = 'inline-block';
                elements.startBtn.textContent = 'Начать доставку';
                elements.startBtn.onclick = () => updateOrderStatus(orderId, 'DISPATCHED');
            }
            if (elements.cancelBtn) {
                elements.cancelBtn.style.display = 'inline-block';
                elements.cancelBtn.textContent = 'Отменить';
                elements.cancelBtn.onclick = () => updateOrderStatus(orderId, 'CANCELLED');
            }
            break;

        case 'DISPATCHED':
            if (elements.completeBtn) {
                elements.completeBtn.style.display = 'inline-block';
                elements.completeBtn.textContent = 'Доставлен';
                elements.completeBtn.onclick = () => updateOrderStatus(orderId, 'DELIVERED_TO_DESTINATION');
            }
            break;

        case 'DELIVERED_TO_DESTINATION':
            if (elements.completeBtn) {
                elements.completeBtn.style.display = 'inline-block';
                elements.completeBtn.textContent = 'Завершить';
                elements.completeBtn.className = 'btn btn-success btn-small complete-btn';
                elements.completeBtn.onclick = () => updateOrderStatus(orderId, 'COMPLETED');
            }
            if (elements.returnBtn) {
                elements.returnBtn.style.display = 'inline-block';
                elements.returnBtn.textContent = 'Вернуть';
                elements.returnBtn.onclick = () => updateOrderStatus(orderId, 'RETURNED');
            }
            break;

        case 'COMPLETED':
        case 'CANCELLED':
        case 'RETURNED':
            // Только кнопка подробностей
            break;
    }
}

// Принять заказ (для курьера)
async function assignOrder(orderId) {
    if (!confirm(`Принять заказ #${orderId}?`)) return;

    try {
        const response = await fetch(`${API_BASE}/couriers/${orderId}/assign`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Accept': 'application/json'
            }
        });

        const result = await handleApiResponse(response, `Принятие заказа #${orderId}`);
        if (result.success) {
            alert(`Заказ #${orderId} успешно принят!`);
            await refreshData();
        }

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
            headers: {
                'Content-Type': 'application/json',
                'Accept': 'application/json'
            }
        });

        const result = await handleApiResponse(response, `Назначение заказа #${orderId}`);
        if (result.success) {
            alert(`Вы назначены курьером для заказа #${orderId}!`);
            await refreshData();
        }

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
            headers: {
                'Content-Type': 'application/json',
                'Accept': 'application/json'
            }
        });

        const result = await handleApiResponse(response, `Обновление статуса заказа #${orderId}`);
        if (result.success) {
            alert(`Статус заказа #${orderId} обновлен на "${statusText}"!`);
            await refreshData();
        }

    } catch (error) {
        console.error('Ошибка при обновлении статуса:', error);
        alert(`Ошибка: ${error.message}`);
    }
}

// Обработка ответа API
async function handleApiResponse(response, action) {
    if (!response.ok) {
        const errorText = await response.text();
        let errorMessage = `Ошибка при ${action}`;

        try {
            const errorData = JSON.parse(errorText);
            errorMessage = errorData.message || errorData.error || errorMessage;
        } catch (e) {
            errorMessage = errorText || errorMessage;
        }

        throw new Error(errorMessage);
    }

    const result = await response.json();
    console.log(`Ответ ${action}:`, result);

    // Проверяем формат ответа
    if (result.success !== undefined) {
        return result;
    }

    // Если в ответе нет поля success, считаем успешным
    return { success: true, data: result };
}

// Обновление всех данных
async function refreshData() {
    try {
        await Promise.all([
            loadStats(),
            loadOrdersData(currentFilter, currentPage)
        ]);
    } catch (error) {
        console.error('Ошибка при обновлении данных:', error);
    }
}

// Показать детали заказа
async function showOrderDetails(orderId) {
    try {
        // Здесь можно добавить запрос на получение детальной информации о заказе
        alert(`Детали заказа #${orderId}\n\nФункция в разработке`);
    } catch (error) {
        console.error('Ошибка при получении деталей заказа:', error);
        alert(`Ошибка при получении деталей заказа: ${error.message}`);
    }
}

// Вспомогательные функции
function getStatusText(status) {
    const statusMap = {
        'PENDING': 'Ожидает',
        'DISPATCHED': 'В доставке',
        'DELIVERED_TO_DESTINATION': 'Доставлен',
        'COMPLETED': 'Завершен',
        'CANCELLED': 'Отменен',
        'RETURNED': 'Возвращен'
    };
    return statusMap[status.toUpperCase()] || status || 'Неизвестно';
}

function getActionText(status, role) {
    const actionMap = {
        'DISPATCHED': 'начать доставку',
        'DELIVERED_TO_DESTINATION': 'отметить как доставленный',
        'COMPLETED': 'завершить заказ',
        'CANCELLED': 'отменить заказ',
        'RETURNED': 'вернуть заказ'
    };
    return actionMap[status.toUpperCase()] || 'выполнить действие';
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

// Пагинация
function addPaginationControls(container, filter) {
    container.innerHTML = '';
    container.style.display = 'block';

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

    container.appendChild(paginationDiv);
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
    refreshData().catch(console.error);
};