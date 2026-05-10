// Конфигурация API
const API_BASE = '/api';
let currentUser = null;
let currentFilter = 'assigned';
let currentPage = 0;
const pageSize = 3;
let totalPages = 0;
let totalElements = 0;

// Модальное окно для деталей заказа
let orderDetailsModal = null;

// Основная функция инициализации
document.addEventListener('DOMContentLoaded', async function() {
    console.log('Панель курьера загружена');

    // Создаем модальное окно
    createModal();

    try {
        // Получаем данные текущего пользователя
        const userResponse = await fetch(`${API_BASE}/users/me`);

        if (!userResponse.ok) {
            if (userResponse.status === 401) {
                alert('Требуется авторизация!');
                window.location.href = '/login';
                return;
            }
            throw new Error(`HTTP ${userResponse.status}`);
        }

        const userData = await userResponse.json();
        console.log('Данные пользователя:', userData);

        currentUser = userData;

        if (currentUser.role !== 'COURIER' && currentUser.role !== 'ADMIN') {
            alert('Доступ только для курьеров и администраторов!');
            window.location.href = '/';
            return;
        }

        updateUserInfo();
        await loadData();

    } catch (error) {
        console.error('Ошибка инициализации:', error);
        showError(`Ошибка загрузки данных: ${error.message}`);
    }
});

// Создание модального окна
function createModal() {
    const modalHTML = `
        <div id="orderDetailsModal" class="modal" style="display: none;">
            <div class="modal-content">
                <div class="modal-header">
                    <h2>Детали заказа #<span id="modalOrderId"></span></h2>
                    <span class="modal-close">&times;</span>
                </div>
                <div class="modal-body" id="modalBody">
                    <div class="loading">Загрузка...</div>
                </div>
            </div>
        </div>
    `;

    document.body.insertAdjacentHTML('beforeend', modalHTML);

    // Добавляем стили для модального окна
    const modalStyles = document.createElement('style');
    modalStyles.textContent = `
        .modal {
            display: none;
            position: fixed;
            z-index: 1000;
            left: 0;
            top: 0;
            width: 100%;
            height: 100%;
            background-color: rgba(0,0,0,0.5);
            animation: fadeIn 0.3s;
        }
        
        .modal-content {
            background-color: #fff;
            margin: 5% auto;
            padding: 0;
            width: 90%;
            max-width: 800px;
            border-radius: 12px;
            box-shadow: 0 5px 20px rgba(0,0,0,0.3);
            animation: slideDown 0.3s;
        }
        
        .modal-header {
            padding: 20px 25px;
            border-bottom: 2px solid #f0f0f0;
            display: flex;
            justify-content: space-between;
            align-items: center;
        }
        
        .modal-header h2 {
            margin: 0;
            color: #333;
            font-size: 1.5rem;
        }
        
        .modal-close {
            font-size: 28px;
            font-weight: bold;
            cursor: pointer;
            color: #999;
            transition: color 0.3s;
        }
        
        .modal-close:hover {
            color: #333;
        }
        
        .modal-body {
            padding: 25px;
            max-height: 70vh;
            overflow-y: auto;
        }
        
        .detail-section {
            margin-bottom: 25px;
            padding-bottom: 20px;
            border-bottom: 1px solid #f0f0f0;
        }
        
        .detail-section h3 {
            color: #667eea;
            margin-bottom: 15px;
            font-size: 1.2rem;
        }
        
        .detail-row {
            display: flex;
            margin-bottom: 10px;
            padding: 8px 0;
        }
        
        .detail-label {
            width: 120px;
            font-weight: 600;
            color: #555;
        }
        
        .detail-value {
            flex: 1;
            color: #333;
        }
        
        .items-table {
            width: 100%;
            border-collapse: collapse;
        }
        
        .items-table th,
        .items-table td {
            padding: 12px;
            text-align: left;
            border-bottom: 1px solid #e0e0e0;
        }
        
        .items-table th {
            background-color: #f8f9fa;
            font-weight: 600;
            color: #555;
        }
        
        .items-table tr:hover {
            background-color: #f8f9fa;
        }
        
        .product-image {
            width: 50px;
            height: 50px;
            object-fit: cover;
            border-radius: 8px;
        }
        
        .total-amount {
            margin-top: 20px;
            padding-top: 20px;
            text-align: right;
            font-size: 1.2rem;
            font-weight: bold;
            color: #28a745;
        }
        
        @keyframes fadeIn {
            from { opacity: 0; }
            to { opacity: 1; }
        }
        
        @keyframes slideDown {
            from {
                transform: translateY(-50px);
                opacity: 0;
            }
            to {
                transform: translateY(0);
                opacity: 1;
            }
        }
        
        @media (max-width: 768px) {
            .modal-content {
                width: 95%;
                margin: 10% auto;
            }
            
            .detail-row {
                flex-direction: column;
            }
            
            .detail-label {
                width: 100%;
                margin-bottom: 5px;
            }
            
            .items-table {
                font-size: 0.85rem;
            }
            
            .items-table th,
            .items-table td {
                padding: 8px;
            }
        }
    `;
    document.head.appendChild(modalStyles);

    orderDetailsModal = document.getElementById('orderDetailsModal');
    const closeBtn = orderDetailsModal.querySelector('.modal-close');
    closeBtn.onclick = () => orderDetailsModal.style.display = 'none';

    window.onclick = (event) => {
        if (event.target === orderDetailsModal) {
            orderDetailsModal.style.display = 'none';
        }
    };
}

// Обновление информации о пользователе
function updateUserInfo() {
    const courierNameEl = document.getElementById('courierName');
    const courierRoleEl = document.querySelector('.courier-role');
    const headerTitle = document.querySelector('.courier-header h1');

    if (courierNameEl) courierNameEl.textContent = currentUser.name || getUserTitle();
    if (courierRoleEl) courierRoleEl.textContent = getRoleText(currentUser.role);

    if (currentUser.role === 'ADMIN' && headerTitle) {
        headerTitle.textContent = 'Панель управления заказами';
        if (courierRoleEl) courierRoleEl.textContent = 'Администратор';
    }
}

// Загрузка всех данных
async function loadData() {
    try {
        await loadStats();
        await loadOrdersData('assigned', 0);
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

        updateStatsUI(0, 0, 0);

        let myOrders = [];
        let availableOrders = [];

        if (currentUser.role === 'COURIER') {
            try {
                const assignedResponse = await fetch(`${API_BASE}/couriers/assignedOrders?pageSize=100&pageNumber=0`);
                if (assignedResponse.ok) {
                    const result = await assignedResponse.json();
                    myOrders = result.content || [];
                }
            } catch (error) {
                console.warn('Ошибка загрузки назначенных заказов:', error);
            }
        }

        try {
            const availableResponse = await fetch(`${API_BASE}/couriers/availableOrders?pageSize=100&pageNumber=0`);
            if (availableResponse.ok) {
                const result = await availableResponse.json();
                availableOrders = result.content || [];
            }
        } catch (error) {
            console.warn('Ошибка загрузки доступных заказов:', error);
        }

        let activeOrders = 0;
        if (currentUser.role === 'COURIER') {
            activeOrders = myOrders.filter(order => {
                const status = order.status;
                return status === 'PENDING' || status === 'DISPATCHED';
            }).length;
        }

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
async function loadOrdersData(filter = 'assigned', page = 0) {
    currentFilter = filter;
    currentPage = page;

    const ordersContainer = document.getElementById('ordersContainer');
    const ordersTitle = document.getElementById('ordersTitle');
    const paginationContainer = document.getElementById('paginationContainer');

    if (!ordersContainer) return;

    ordersContainer.innerHTML = '<div class="loading">Загрузка заказов...</div>';
    if (paginationContainer) paginationContainer.style.display = 'none';

    try {
        let apiUrl = '';
        let title = '';

        if (filter === 'assigned') {
            if (currentUser.role === 'COURIER') {
                apiUrl = `${API_BASE}/couriers/assignedOrders?pageSize=${pageSize}&pageNumber=${page}`;
                title = 'Мои заказы';
            } else {
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
            throw new Error(`Ошибка сервера: ${response.status}`);
        }

        const result = await response.json();
        console.log('Ответ API:', result);

        // Извлекаем данные из Page Response
        const orders = result.content || [];
        totalPages = result.totalPages || 0;
        totalElements = result.totalElements || 0;
        currentPage = result.pageNumber || 0;

        if (ordersTitle) ordersTitle.textContent = title;

        displayOrders(orders, filter);

    } catch (error) {
        console.error('Ошибка загрузки заказов:', error);
        ordersContainer.innerHTML = `
            <div class="error-message">
                Ошибка загрузки заказов: ${error.message}
                <br><br>
                <button onclick="loadOrdersData('${filter}', ${currentPage})" class="btn btn-outline btn-small">
                    Повторить
                </button>
            </div>
        `;
        if (paginationContainer) paginationContainer.style.display = 'none';
    }
}

// Отображение заказов
function displayOrders(orders, filter) {
    const ordersContainer = document.getElementById('ordersContainer');
    const paginationContainer = document.getElementById('paginationContainer');

    if (!ordersContainer) return;

    if (!orders || orders.length === 0) {
        let message = filter === 'assigned' ? 'У вас нет назначенных заказов' : 'В данный момент нет доступных заказов';
        ordersContainer.innerHTML = `
            <div class="no-orders">
                <div class="no-orders-icon">📦</div>
                <h4>Заказов нет</h4>
                <p>${message}</p>
            </div>
        `;
        if (paginationContainer) paginationContainer.style.display = 'none';
        return;
    }

    ordersContainer.innerHTML = '';

    orders.forEach(order => {
        try {
            const orderElement = createOrderElement(order, filter);
            ordersContainer.appendChild(orderElement);
        } catch (error) {
            console.error('Ошибка создания элемента заказа:', error, order);
        }
    });

    if (totalPages > 1 && paginationContainer) {
        addPaginationControls(paginationContainer, filter);
    } else if (paginationContainer) {
        paginationContainer.style.display = 'none';
    }
}

// Создание элемента заказа (адаптировано под ваше DTO)
function createOrderElement(order, filter) {
    const template = document.getElementById('orderTemplate');
    if (!template) throw new Error('Шаблон не найден');

    const clone = template.content.cloneNode(true);
    const orderCard = clone.querySelector('.order-card');
    if (!orderCard) throw new Error('Элемент .order-card не найден');

    const orderId = order.id;
    orderCard.setAttribute('data-order-id', orderId);

    // Заполняем данные
    const orderIdSpan = orderCard.querySelector('.order-id');
    const orderNumber = orderCard.querySelector('.order-number');
    if (orderIdSpan) orderIdSpan.textContent = orderId;
    if (orderNumber && orderIdSpan) {
        orderNumber.textContent = `Заказ #${orderId}`;
    }

    // Информация о клиенте (customer из вашего DTO)
    const customer = order.customer || {};
    const customerNameSpan = orderCard.querySelector('.customer-name');
    const customerPhoneSpan = orderCard.querySelector('.customer-phone');
    const customerAddressSpan = orderCard.querySelector('.customer-address');

    if (customerNameSpan) customerNameSpan.textContent = customer.fullName || 'Не указано';
    if (customerPhoneSpan) customerPhoneSpan.textContent = customer.phone || 'Не указан';
    if (customerAddressSpan) customerAddressSpan.textContent = order.shippingAddress || 'Не указан';

    // Цена
    const priceSpan = orderCard.querySelector('.price-value');
    if (priceSpan) {
        const totalAmount = order.totalAmount || 0;
        priceSpan.textContent = `${totalAmount.toLocaleString('ru-RU')} ₽`;
    }

    // Дата
    const orderDateSpan = orderCard.querySelector('.order-date');
    if (orderDateSpan && order.orderDate) {
        orderDateSpan.textContent = formatDate(order.orderDate);
    }

    // Статус
    const statusSpan = orderCard.querySelector('.order-status');
    const status = order.status || 'PENDING';
    if (statusSpan) {
        statusSpan.textContent = getStatusText(status);
        statusSpan.className = `order-status ${status}`;
    }

    // Настраиваем кнопки
    const acceptBtn = orderCard.querySelector('.accept-btn');
    const startBtn = orderCard.querySelector('.start-btn');
    const completeBtn = orderCard.querySelector('.complete-btn');
    const cancelBtn = orderCard.querySelector('.cancel-btn');
    const returnBtn = orderCard.querySelector('.return-btn');
    const detailsBtn = orderCard.querySelector('.details-btn');

    // Скрываем все кнопки сначала
    [acceptBtn, startBtn, completeBtn, cancelBtn, returnBtn].forEach(btn => {
        if (btn) btn.style.display = 'none';
    });

    // Кнопка подробностей всегда видна
    if (detailsBtn) {
        detailsBtn.style.display = 'inline-block';
        detailsBtn.onclick = () => showOrderDetails(orderId);
    }

    // Логика для доступных заказов
    if (filter === 'available') {
        if (acceptBtn) {
            acceptBtn.style.display = 'inline-block';
            acceptBtn.textContent = currentUser.role === 'ADMIN' ? 'Назначить' : 'Принять заказ';
            acceptBtn.onclick = () => assignOrder(orderId);
        }
        return orderCard;
    }

    // Логика для назначенных заказов
    switch(status) {
        case 'PENDING':
            if (startBtn) {
                startBtn.style.display = 'inline-block';
                startBtn.onclick = () => updateOrderStatus(orderId, 'DISPATCHED');
            }
            if (cancelBtn) {
                cancelBtn.style.display = 'inline-block';
                cancelBtn.onclick = () => updateOrderStatus(orderId, 'CANCELLED');
            }
            break;

        case 'DISPATCHED':
            if (completeBtn) {
                completeBtn.style.display = 'inline-block';
                completeBtn.textContent = 'Доставлен';
                completeBtn.onclick = () => updateOrderStatus(orderId, 'DELIVERED_TO_DESTINATION');
            }
            break;

        case 'DELIVERED_TO_DESTINATION':
            if (completeBtn) {
                completeBtn.style.display = 'inline-block';
                completeBtn.textContent = 'Завершить';
                completeBtn.classList.add('btn-success');
                completeBtn.onclick = () => updateOrderStatus(orderId, 'COMPLETED');
            }
            if (returnBtn) {
                returnBtn.style.display = 'inline-block';
                returnBtn.onclick = () => updateOrderStatus(orderId, 'RETURNED');
            }
            break;

        case 'COMPLETED':
        case 'CANCELLED':
        case 'RETURNED':
            // Только кнопка подробностей
            break;
    }

    return orderCard;
}

// Принять заказ
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

        if (!response.ok) {
            const errorText = await response.text();
            throw new Error(errorText || 'Ошибка при принятии заказа');
        }

        const result = await response.json();

        if (result.success !== false) {
            alert(`Заказ #${orderId} успешно принят!`);
            await refreshData();
        } else {
            throw new Error(result.message || 'Ошибка');
        }

    } catch (error) {
        console.error('Ошибка:', error);
        alert(`Ошибка: ${error.message}`);
    }
}

// Обновить статус заказа
async function updateOrderStatus(orderId, status) {
    const statusText = getStatusText(status);
    if (!confirm(`Вы уверены, что хотите изменить статус заказа #${orderId} на "${statusText}"?`)) return;

    try {
        const response = await fetch(`${API_BASE}/couriers/orders/${orderId}/status?status=${status}`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Accept': 'application/json'
            }
        });

        if (!response.ok) {
            const errorText = await response.text();
            throw new Error(errorText || 'Ошибка при обновлении статуса');
        }

        const result = await response.json();

        if (result.success !== false) {
            alert(`Статус заказа #${orderId} обновлен на "${statusText}"!`);
            await refreshData();
        } else {
            throw new Error(result.message || 'Ошибка');
        }

    } catch (error) {
        console.error('Ошибка:', error);
        alert(`Ошибка: ${error.message}`);
    }
}

// Показать детали заказа (ПОЛНОСТЬЮ РАБОТАЮЩАЯ ВЕРСИЯ)
async function showOrderDetails(orderId) {
    if (!orderDetailsModal) {
        console.error('Модальное окно не создано');
        return;
    }

    const modalBody = document.getElementById('modalBody');
    const modalOrderIdSpan = document.getElementById('modalOrderId');

    if (modalOrderIdSpan) modalOrderIdSpan.textContent = orderId;
    if (modalBody) modalBody.innerHTML = '<div class="loading">Загрузка деталей заказа...</div>';

    orderDetailsModal.style.display = 'block';

    try {
        // Пытаемся получить заказ по ID через доступные эндпоинты
        let order = null;

        // Сначала пробуем получить из назначенных заказов
        const assignedResponse = await fetch(`${API_BASE}/couriers/assignedOrders?pageSize=100&pageNumber=0`);
        if (assignedResponse.ok) {
            const assignedData = await assignedResponse.json();
            order = assignedData.content?.find(o => o.id === orderId);
        }

        // Если не нашли, пробуем из доступных
        if (!order) {
            const availableResponse = await fetch(`${API_BASE}/couriers/availableOrders?pageSize=100&pageNumber=0`);
            if (availableResponse.ok) {
                const availableData = await availableResponse.json();
                order = availableData.content?.find(o => o.id === orderId);
            }
        }

        if (!order) {
            modalBody.innerHTML = `
                <div class="error-message">
                    Не удалось найти заказ #${orderId}
                    <br><br>
                    <button onclick="orderDetailsModal.style.display='none'" class="btn btn-primary">Закрыть</button>
                </div>
            `;
            return;
        }

        // Отображаем детали заказа
        displayOrderDetails(order, modalBody);

    } catch (error) {
        console.error('Ошибка при получении деталей заказа:', error);
        modalBody.innerHTML = `
            <div class="error-message">
                Ошибка при загрузке деталей заказа: ${error.message}
                <br><br>
                <button onclick="orderDetailsModal.style.display='none'" class="btn btn-primary">Закрыть</button>
            </div>
        `;
    }
}

// Отображение деталей заказа в модальном окне
function displayOrderDetails(order, modalBody) {
    const customer = order.customer || {};
    const orderItems = order.orderItems || [];

    let itemsHtml = '';
    if (orderItems.length > 0) {
        itemsHtml = `
            <table class="items-table">
                <thead>
                    <tr>
                   
                        <th>Товар</th>
                        <th>Кол-во</th>
                        <th>Цена</th>
                        <th>Сумма</th>
                    </tr>
                </thead>
                <tbody>
                    ${orderItems.map(item => `
                        <tr> 
                            <td>
                                <strong>${item.productName || 'Товар'}</strong><br>
                                <small style="color:#666">Код: ${item.productCode || '-'}</small><br>
                                <small style="color:#999">${item.productDescription || ''}</small>
                            </td>
                            <td>${item.quantity || 0} шт.</td>
                            <td>${(item.price || 0).toLocaleString('ru-RU')} ₽</td>
                            <td><strong>${(item.totalPrice || 0).toLocaleString('ru-RU')} ₽</strong></td>
                        </tr>
                    `).join('')}
                </tbody>
            </table>
            <div class="total-amount">
                Итого: ${(order.totalAmount || 0).toLocaleString('ru-RU')} ₽
            </div>
        `;
    } else {
        itemsHtml = '<p style="color:#999; text-align:center;">Нет товаров в заказе</p>';
    }

    modalBody.innerHTML = `
        <div class="detail-section">
            <h3>Информация о заказе</h3>
            <div class="detail-row">
                <div class="detail-label">Номер заказа:</div>
                <div class="detail-value">#${order.id}</div>
            </div>
            <div class="detail-row">
                <div class="detail-label">Дата заказа:</div>
                <div class="detail-value">${formatDate(order.orderDate)}</div>
            </div>
            <div class="detail-row">
                <div class="detail-label">Статус:</div>
                <div class="detail-value"><span class="order-status ${order.status}">${getStatusText(order.status)}</span></div>
            </div>
            <div class="detail-row">
                <div class="detail-label">Сообщение:</div>
                <div class="detail-value">${order.message || 'Нет сообщения'}</div>
            </div>
        </div>
        
        <div class="detail-section">
            <h3>Информация о клиенте</h3>
            <div class="detail-row">
                <div class="detail-label">Имя:</div>
                <div class="detail-value">${customer.fullName || 'Не указано'}</div>
            </div>
            <div class="detail-row">
                <div class="detail-label">Email:</div>
                <div class="detail-value">${customer.email || 'Не указан'}</div>
            </div>
            <div class="detail-row">
                <div class="detail-label">Адрес доставки:</div>
                <div class="detail-value">${order.shippingAddress || 'Не указан'}</div>
            </div>
        </div>
        
        <div class="detail-section">
            <h3>Состав заказа</h3>
            ${itemsHtml}
        </div>
        
        <div style="text-align: center; margin-top: 20px;">
            <button onclick="orderDetailsModal.style.display='none'" class="btn btn-primary">Закрыть</button>
        </div>
    `;
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
    return statusMap[status] || status || 'Неизвестно';
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

function setActiveFilter(filter) {
    document.querySelectorAll('.filter-btn').forEach(btn => {
        btn.classList.remove('active');
        if ((filter === 'assigned' && btn.textContent.includes('Мои')) ||
            (filter === 'available' && btn.textContent.includes('Доступные'))) {
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

    const pageInfo = document.createElement('span');
    pageInfo.className = 'page-info';
    pageInfo.textContent = `Страница ${currentPage + 1} из ${totalPages}`;

    const prevButton = document.createElement('button');
    prevButton.className = 'btn btn-outline pagination-btn';
    prevButton.innerHTML = '&larr; Назад';
    prevButton.disabled = currentPage === 0;
    prevButton.onclick = () => {
        if (currentPage > 0) {
            loadOrdersData(filter, currentPage - 1);
        }
    };

    const nextButton = document.createElement('button');
    nextButton.className = 'btn btn-outline pagination-btn';
    nextButton.innerHTML = 'Вперед &rarr;';
    nextButton.disabled = currentPage >= totalPages - 1;
    nextButton.onclick = () => {
        if (currentPage < totalPages - 1) {
            loadOrdersData(filter, currentPage + 1);
        }
    };

    paginationDiv.appendChild(prevButton);
    paginationDiv.appendChild(pageInfo);
    paginationDiv.appendChild(nextButton);
    container.appendChild(paginationDiv);
}

// Глобальные функции для HTML
window.setFilter = function(filter) {
    setActiveFilter(filter);
    currentPage = 0;
    loadOrdersData(filter, 0);
};

window.refreshOrders = function() {
    refreshData();
};

// Для доступа из модального окна
window.orderDetailsModal = orderDetailsModal;