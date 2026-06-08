// profileScript.js - ИСПРАВЛЕННАЯ ВЕРСИЯ

document.addEventListener('DOMContentLoaded', async function() {
    console.log('Загрузка страницы профиля...');

    try {
        // 1. Инициализируем UI
        initTabs();
        initProfileForms();
        initModals();

        // 2. Загружаем профиль пользователя через DTO
        await loadUserProfile();

        // 3. Загружаем заявки пользователя на роли
        await loadUserRoleRequests();

        // 4. Загружаем историю заказов
        await loadUserOrders();

    } catch (error) {
        console.error('Ошибка инициализации профиля:', error);
        showProfileNotification('Ошибка загрузки профиля', 'error');
        renderDefaultProfile();
    }
});

// ============ КОНСТАНТЫ И НАСТРОЙКИ ============

const API_ENDPOINTS = {
    GET_PROFILE: '/api/users/me',
    UPDATE_ADDRESS: '/api/users/address',
    CREATE_ROLE_REQUEST: '/api/users/role-request',
    GET_ROLE_REQUESTS: '/api/users/role-request',
    GET_ORDERS: '/api/orders'
};

const ORDER_STATUS_MAP = {
    'PENDING': { text: 'Ожидает', class: 'pending', icon: '⏳' },
    'DISPATCHED': { text: 'В доставке', class: 'dispatched', icon: '🚚' },
    'DELIVERED_TO_DESTINATION': { text: 'Доставлен', class: 'delivered', icon: '📦' },
    'COMPLETED': { text: 'Завершен', class: 'completed', icon: '✅' },
    'CANCELLED': { text: 'Отменен', class: 'cancelled', icon: '❌' },
    'RETURNED': { text: 'Возвращен', class: 'returned', icon: '🔄' }
};

const ROLE_MAP = {
    'USER': { text: 'Покупатель', icon: '👤', buttonText: 'Профиль' },
    'SELLER': { text: 'Продавец', icon: '🏪', buttonText: 'Для продавца' },
    'COURIER': { text: 'Курьер', icon: '🚚', buttonText: 'Для курьера' },
    'ADMIN': { text: 'Админ', icon: '⚙️', buttonText: 'Для админа' }
};

const REQUEST_STATUS_MAP = {
    'PENDING': { text: '⏳ Ожидает', class: 'pending' },
    'APPROVED': { text: '✅ Одобрено', class: 'approved' },
    'REJECTED': { text: '❌ Отклонено', class: 'rejected' }
};

// ============ ЗАГРУЗКА ИСТОРИИ ЗАКАЗОВ ============

async function loadUserOrders() {
    try {
        console.log('Загрузка истории заказов...');
        showOrdersLoading();

        const response = await fetch(API_ENDPOINTS.GET_ORDERS, {
            method: 'GET',
            headers: { 'Accept': 'application/json' },
            credentials: 'include'
        });

        console.log('Статус заказов:', response.status);

        if (response.status === 401) {
            console.warn('Пользователь не авторизован');
            renderEmptyOrders();
            return;
        }

        if (!response.ok) {
            throw new Error(`Ошибка сервера: ${response.status}`);
        }

        const orders = await response.json();
        console.log('Получены заказы:', orders);

        if (orders && orders.length > 0) {
            renderOrders(orders);
            updateOrderCount(orders.length);
        } else {
            renderEmptyOrders();
        }

    } catch (error) {
        console.error('Ошибка загрузки заказов:', error);
        renderEmptyOrders();
        showProfileNotification('Не удалось загрузить историю заказов', 'warning');
    }
}

function showOrdersLoading() {
    const ordersGrid = document.getElementById('ordersGrid');
    if (ordersGrid) {
        ordersGrid.innerHTML = `
            <div class="loading-orders">
                <div class="loading-spinner"></div>
                <p>Загрузка заказов...</p>
            </div>
        `;
    }
}

function renderOrders(orders) {
    const ordersGrid = document.getElementById('ordersGrid');
    if (!ordersGrid) return;

    let ordersHtml = '';

    orders.forEach(order => {
        const statusInfo = ORDER_STATUS_MAP[order.status] || ORDER_STATUS_MAP.PENDING;
        const orderDate = formatOrderDate(order.orderDate);
        const totalAmount = order.totalAmount || 0;
        const items = order.items || [];
        const itemsCount = items.length;

        ordersHtml += `
            <div class="order-card" data-order-id="${order.orderId}">
                <div class="order-header">
                    <div class="order-info">
                        <span class="order-number">Заказ #${order.orderId}</span>
                        <span class="order-date">${orderDate}</span>
                    </div>
                    <span class="order-status-badge ${statusInfo.class}">
                        ${statusInfo.icon} ${statusInfo.text}
                    </span>
                </div>
                
                <div class="order-items-preview">
                    ${renderOrderItemsPreview(items)}
                </div>
                
                <div class="order-footer">
                    <div class="order-total">
                        <span class="total-label">Итого:</span>
                        <span class="total-amount">${formatPrice(totalAmount)}</span>
                    </div>
                    <div class="order-items-count">
                        📦 ${itemsCount} ${getItemsText(itemsCount)}
                    </div>
                    <button class="btn-view-order" onclick="viewOrderDetails(${order.orderId})">
                        Подробнее →
                    </button>
                </div>
            </div>
        `;
    });

    ordersGrid.innerHTML = ordersHtml;
}

function renderOrderItemsPreview(items) {
    if (!items || items.length === 0) {
        return '<div class="no-items">Нет товаров</div>';
    }

    const itemsToShow = items.slice(0, 3);
    const remainingCount = items.length - 3;

    let itemsHtml = '<div class="preview-items">';

    itemsToShow.forEach(item => {
        const productName = item.nameProduct || 'Товар';
        const quantity = item.count || 1;
        const price = item.priceProduct || 0;
        const imageUrl = item.image ? `/uploads/images/${item.image}` : '/images/no-image.png';

        itemsHtml += `
            <div class="preview-item">
                <img src="${imageUrl}" alt="${productName}" class="preview-item-image" 
                     onerror="this.src='/images/no-image.png'">
                <div class="preview-item-info">
                    <div class="preview-item-name">${truncateText(productName, 30)}</div>
                    <div class="preview-item-price">${formatPrice(price)}</div>
                    <div class="preview-item-quantity">${quantity} шт.</div>
                </div>
            </div>
        `;
    });

    if (remainingCount > 0) {
        itemsHtml += `
            <div class="preview-more">
                + еще ${remainingCount} ${getItemsText(remainingCount)}
            </div>
        `;
    }

    itemsHtml += '</div>';
    return itemsHtml;
}

function updateOrderCount(count) {
    const ordersCountEl = document.getElementById('ordersCount');
    if (ordersCountEl) {
        ordersCountEl.textContent = count;
    }
}

function renderEmptyOrders() {
    const ordersGrid = document.getElementById('ordersGrid');
    if (!ordersGrid) return;

    ordersGrid.innerHTML = `
        <div class="empty-orders">
            <div class="empty-orders-icon">📦</div>
            <h3>У вас пока нет заказов</h3>
            <p>Начните shopping прямо сейчас!</p>
            <a href="/" class="btn-shop-now">Перейти в каталог</a>
        </div>
    `;
}

// ============ ДЕТАЛИ ЗАКАЗА (МОДАЛЬНОЕ ОКНО) ============

window.viewOrderDetails = async function(orderId) {
    try {
        const response = await fetch(API_ENDPOINTS.GET_ORDERS, {
            method: 'GET',
            headers: { 'Accept': 'application/json' },
            credentials: 'include'
        });

        if (!response.ok) {
            throw new Error(`Ошибка загрузки: ${response.status}`);
        }

        const orders = await response.json();
        const order = orders.find(o => o.orderId === orderId);

        if (!order) {
            throw new Error('Заказ не найден');
        }

        showOrderDetailsModal(order);

    } catch (error) {
        console.error('Ошибка загрузки деталей заказа:', error);
        showProfileNotification('Не удалось загрузить детали заказа', 'error');
    }
};

function showOrderDetailsModal(order) {
    let modal = document.getElementById('orderDetailsModal');

    if (!modal) {
        modal = document.createElement('div');
        modal.id = 'orderDetailsModal';
        modal.className = 'modal order-details-modal';
        document.body.appendChild(modal);
    }

    const statusInfo = ORDER_STATUS_MAP[order.status] || ORDER_STATUS_MAP.PENDING;
    const orderDate = formatOrderDate(order.orderDate);
    const items = order.items || [];

    modal.innerHTML = `
        <div class="modal-content order-details-content">
            <div class="modal-header">
                <h3>Детали заказа #${order.orderId}</h3>
                <button class="modal-close" onclick="closeOrderDetailsModal()">&times;</button>
            </div>
            
            <div class="modal-body">
                <div class="order-info-block">
                    <div class="info-line">
                        <span class="info-label">📅 Дата заказа:</span>
                        <span class="info-value">${orderDate}</span>
                    </div>
                    <div class="info-line">
                        <span class="info-label">💰 Сумма:</span>
                        <span class="info-value total">${formatPrice(order.totalAmount)}</span>
                    </div>
                    <div class="info-line">
                        <span class="info-label">📦 Статус:</span>
                        <span class="info-value"><span class="status-badge ${statusInfo.class}">${statusInfo.icon} ${statusInfo.text}</span></span>
                    </div>
                    <div class="info-line">
                        <span class="info-label">🎁 Товаров:</span>
                        <span class="info-value">${items.length} шт.</span>
                    </div>
                </div>
                
                <div class="order-items-block">
                    <h4>Состав заказа</h4>
                    <div class="items-scrollable">
                        ${renderOrderItemsList(items)}
                    </div>
                </div>
            </div>
            
            <div class="modal-actions">
                <button class="btn-secondary" onclick="closeOrderDetailsModal()">Закрыть</button>
            </div>
        </div>
    `;

    modal.style.display = 'flex';

    modal.addEventListener('click', function(e) {
        if (e.target === modal) {
            closeOrderDetailsModal();
        }
    });
}

function renderOrderItemsList(items) {
    if (!items || items.length === 0) {
        return '<div class="no-items">Нет товаров в заказе</div>';
    }

    return `
        <div class="items-list">
            ${items.map(item => {
        const imageUrl = item.image ? `/uploads/images/${item.image}` : '/images/no-image.png';
        const productName = item.nameProduct || 'Товар';
        const quantity = item.count || 1;
        const price = item.priceProduct || 0;
        const totalPrice = price * quantity;

        return `
                    <div class="item-row">
                        <div class="item-image">
                            <img src="${imageUrl}" alt="${productName}" onerror="this.src='/images/no-image.png'">
                        </div>
                        <div class="item-info">
                            <div class="item-name">${escapeHtml(productName)}</div>
                            <div class="item-details">
                                <span class="item-price">${formatPrice(price)} × ${quantity} шт.</span>
                                <span class="item-total">= ${formatPrice(totalPrice)}</span>
                            </div>
                        </div>
                    </div>
                `;
    }).join('')}
        </div>
        <div class="items-total">
            <span>Общая сумма:</span>
            <strong>${formatPrice(items.reduce((sum, item) => sum + (item.priceProduct * item.count), 0))}</strong>
        </div>
    `;
}

window.closeOrderDetailsModal = function() {
    const modal = document.getElementById('orderDetailsModal');
    if (modal) {
        modal.style.display = 'none';
    }
};

// ============ ЗАГРУЗКА ПРОФИЛЯ ============

async function loadUserProfile() {
    try {
        console.log('Запрос профиля через DTO...');

        const response = await fetch(API_ENDPOINTS.GET_PROFILE, {
            method: 'GET',
            headers: { 'Accept': 'application/json' },
            credentials: 'include'
        });

        console.log('Статус профиля:', response.status);

        if (response.status === 401) {
            console.warn('Пользователь не авторизован');
            window.location.href = '/login?redirect=/profile';
            return;
        }

        if (!response.ok) {
            throw new Error(`Ошибка сервера: ${response.status}`);
        }

        const userData = await response.json();
        console.log('Получен DTO профиль:', userData);

        renderUserProfile(userData);
        saveProfileToCache(userData);

    } catch (error) {
        console.error('Ошибка загрузки профиля:', error);
        const cachedProfile = loadProfileFromCache();
        if (cachedProfile) {
            console.log('Используем кэшированный профиль');
            renderUserProfile(cachedProfile);
            showProfileNotification('⚠️ Используются локальные данные', 'warning');
        } else {
            renderDefaultProfile();
        }
    }
}

function renderUserProfile(user) {
    console.log('Рендерим профиль из DTO:', user);

    const name = user.name || 'Не указано';
    const email = user.email || 'Не указан';
    const address = user.address || 'Адрес не указан';
    const role = user.role || 'USER';

    document.getElementById('profileName').value = name;
    document.getElementById('profileEmail').value = email;
    document.getElementById('profileAddress').value = address;

    const profileNameElement = document.querySelector('.profile-name');
    const profileEmailElement = document.querySelector('.profile-email');

    if (profileNameElement) profileNameElement.textContent = name;
    if (profileEmailElement) profileEmailElement.textContent = email;

    updateRoleDisplay(role);
    updateRoleButtons(role);

    document.body.dataset.userRole = role;
}

function renderDefaultProfile() {
    const name = 'Гость';
    const email = 'Войдите в аккаунт';

    document.getElementById('profileName').value = name;
    document.getElementById('profileEmail').value = email;
    document.getElementById('profileAddress').value = '';

    document.querySelector('.profile-name').textContent = name;
    document.querySelector('.profile-email').textContent = email;

    const inputs = document.querySelectorAll('#personalForm input, #personalForm textarea');
    inputs.forEach(input => input.disabled = true);

    const submitBtn = document.querySelector('#personalForm .btn-primary');
    if (submitBtn) submitBtn.disabled = true;
}

function updateRoleDisplay(role) {
    const roleInfo = ROLE_MAP[role] || ROLE_MAP.USER;
    const roleElements = document.querySelectorAll('.user-role-display');
    roleElements.forEach(el => {
        el.innerHTML = `${roleInfo.icon} ${roleInfo.text}`;
    });
    updateSidebarRoleButtons(role);
}

function updateSidebarRoleButtons(userRole) {
    const roleButtonsContainer = document.querySelector('.role-buttons');
    if (!roleButtonsContainer) return;

    roleButtonsContainer.innerHTML = '';

    if (userRole === 'USER') {
        roleButtonsContainer.innerHTML = `
            <a href="/seller" class="btn btn-seller" style="width: 100%; margin-bottom: 10px;">🏪 Для продавца</a>
            <a href="/courier" class="btn btn-courier" style="width: 100%; margin-bottom: 10px;">🚚 Для курьера</a>
        `;
    } else if (userRole === 'SELLER') {
        roleButtonsContainer.innerHTML = `<a href="/seller" class="btn btn-seller" style="width: 100%; margin-bottom: 10px;">🏪 Панель продавца</a>`;
    } else if (userRole === 'COURIER') {
        roleButtonsContainer.innerHTML = `<a href="/courier" class="btn btn-courier" style="width: 100%; margin-bottom: 10px;">🚚 Панель курьера</a>`;
    } else if (userRole === 'ADMIN') {
        roleButtonsContainer.innerHTML = `<a href="/admin" class="btn btn-admin" style="width: 100%;">⚙️ Панель администратора</a>`;
    }
}

function updateRoleButtons(userRole) {
    const requestCards = document.querySelectorAll('.request-card');
    if (!requestCards || requestCards.length < 3) return;

    const [sellerCard, courierCard, downgradeCard] = requestCards;

    [sellerCard, courierCard, downgradeCard].forEach(card => {
        card.style.opacity = '1';
        const button = card.querySelector('button');
        if (button) button.disabled = false;
    });

    if (userRole === 'USER') {
        sellerCard.querySelector('button').textContent = '📝 Стать продавцом';
        courierCard.querySelector('button').textContent = '📝 Стать курьером';
        downgradeCard.style.opacity = '0.6';
        downgradeCard.querySelector('button').disabled = true;
    } else if (userRole === 'SELLER') {
        sellerCard.style.opacity = '0.6';
        sellerCard.querySelector('button').disabled = true;
        sellerCard.querySelector('button').textContent = '✅ Вы уже продавец';
        downgradeCard.querySelector('button').textContent = '📝 Сняться с роли продавца';
    } else if (userRole === 'COURIER') {
        courierCard.style.opacity = '0.6';
        courierCard.querySelector('button').disabled = true;
        courierCard.querySelector('button').textContent = '✅ Вы уже курьер';
        downgradeCard.querySelector('button').textContent = '📝 Сняться с роли курьера';
    } else if (userRole === 'ADMIN') {
        [sellerCard, courierCard, downgradeCard].forEach(card => {
            card.style.opacity = '0.6';
            const button = card.querySelector('button');
            button.disabled = true;
            button.textContent = '🚫 Недоступно для админа';
        });
    }
}

// ============ ОБНОВЛЕНИЕ ПРОФИЛЯ ============

function initProfileForms() {
    const personalForm = document.getElementById('personalForm');
    if (personalForm) {
        personalForm.addEventListener('submit', async function(e) {
            e.preventDefault();
            await updateUserProfile();
        });
    }
}

async function updateUserProfile() {
    const name = document.getElementById('profileName').value.trim();
    const address = document.getElementById('profileAddress').value.trim();

    if (!name) {
        showProfileNotification('Имя не может быть пустым', 'error');
        return;
    }

    try {
        const submitBtn = document.querySelector('#personalForm .btn-primary');
        const originalText = submitBtn.innerHTML;
        submitBtn.disabled = true;
        submitBtn.innerHTML = 'Сохранение...';

        let serverUpdated = false;
        try {
            const params = new URLSearchParams();
            params.append('newAddress', address);

            const response = await fetch(API_ENDPOINTS.UPDATE_ADDRESS, {
                method: 'POST',
                headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                body: params,
                credentials: 'include'
            });

            if (response.ok) {
                serverUpdated = true;
            }
        } catch (serverError) {
            console.warn('Не удалось обновить на сервере:', serverError);
        }

        const currentUser = loadProfileFromCache() || {};
        const updatedProfile = { ...currentUser, name, address, updatedAt: new Date().toISOString() };

        saveProfileToCache(updatedProfile);
        document.querySelector('.profile-name').textContent = name;

        showProfileNotification(serverUpdated ? '✅ Профиль успешно обновлен!' : '⚠️ Изменения сохранены локально', serverUpdated ? 'success' : 'warning');

    } catch (error) {
        console.error('Ошибка обновления профиля:', error);
        showProfileNotification('❌ Ошибка при обновлении', 'error');
    } finally {
        const submitBtn = document.querySelector('#personalForm .btn-primary');
        if (submitBtn) {
            submitBtn.disabled = false;
            submitBtn.innerHTML = 'Сохранить изменения';
        }
    }
}

// ============ ЗАЯВКИ НА РОЛИ (ИСПРАВЛЕННАЯ ВЕРСИЯ) ============

async function loadUserRoleRequests() {
    try {
        const response = await fetch(API_ENDPOINTS.GET_ROLE_REQUESTS, {
            method: 'GET',
            headers: { 'Accept': 'application/json' },
            credentials: 'include'
        });

        if (response.ok) {
            const requests = await response.json();
            console.log('Получены заявки:', requests); // Для отладки
            renderRoleRequests(requests);
        } else {
            renderEmptyRequests();
        }
    } catch (error) {
        console.error('Ошибка загрузки заявок:', error);
        renderEmptyRequests();
    }
}

function renderRoleRequests(requests) {
    const requestsList = document.getElementById('requestsList');
    if (!requestsList) return;

    if (!requests || requests.length === 0) {
        renderEmptyRequests();
        return;
    }

    let requestsHtml = '';
    requests.forEach(request => {
        const statusInfo = REQUEST_STATUS_MAP[request.status] || REQUEST_STATUS_MAP.PENDING;
        const date = formatDate(request.createdAt);

        // ИСПРАВЛЕНО: правильное получение текста роли
        let roleText = 'Роль не указана';
        if (request.requestedRole) {
            roleText = ROLE_MAP[request.requestedRole]?.text || request.requestedRole;
        }

        const actionText = request.typeAction === 'ENHANCE' ? 'Повышение до' : 'Снятие роли';

        requestsHtml += `
            <div class="request-item status-${statusInfo.class}">
                <div class="request-header">
                    <span class="request-action">${actionText}</span>
                    <span class="request-role">${roleText}</span>
                    <span class="request-date">${date}</span>
                </div>
                <div class="request-body">
                    <p class="request-message">${escapeHtml(request.message) || 'Без описания'}</p>
                </div>
                <div class="request-footer">
                    <span class="request-status ${statusInfo.class}">${statusInfo.text}</span>
                    <span class="request-id">ID: ${request.id}</span>
                </div>
            </div>
        `;
    });
    requestsList.innerHTML = requestsHtml;
}

function renderEmptyRequests() {
    const requestsList = document.getElementById('requestsList');
    if (requestsList) {
        requestsList.innerHTML = `
            <div class="no-requests">
                <div class="no-requests-icon">📭</div>
                <p>У вас еще нет отправленных заявок</p>
                <p class="no-requests-hint">Отправьте заявку на изменение роли выше</p>
            </div>
        `;
    }
}

// ============ ОТПРАВКА ЗАЯВКИ ============

async function submitRoleRequest(type, requestedRole, message) {
    try {
        const payload = {
            requestedRole: requestedRole,
            typeAction: type,
            message: message,
            create_at: new Date().toISOString()
        };

        console.log('Отправка заявки:', payload);

        const response = await fetch(API_ENDPOINTS.CREATE_ROLE_REQUEST, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Accept': 'application/json'
            },
            body: JSON.stringify(payload),
            credentials: 'include'
        });

        if (response.ok) {
            const result = await response.json();
            showProfileNotification('✅ Заявка успешно отправлена!', 'success');
            await loadUserRoleRequests();
            return result;
        } else {
            const errorText = await response.text();
            console.error('Ошибка сервера:', errorText);

            let errorMessage = errorText;
            try {
                const errorJson = JSON.parse(errorText);
                errorMessage = errorJson.message || errorJson.errorMessage || errorText;
            } catch(e) {
                // Если не JSON, оставляем как есть
            }

            throw new Error(errorMessage || 'Ошибка сервера');
        }
    } catch (error) {
        console.error('Ошибка отправки заявки:', error);
        throw error;
    }
}

// ============ ИНИЦИАЛИЗАЦИЯ МОДАЛЬНЫХ ОКОН ============

function initModals() {
    const textAreas = document.querySelectorAll('textarea[maxlength]');
    textAreas.forEach(textarea => {
        textarea.addEventListener('input', function() {
            const counter = this.parentNode.querySelector('.char-counter span');
            if (counter) counter.textContent = this.value.length;
        });
    });

    const requestForm = document.getElementById('requestForm');
    if (requestForm) {
        requestForm.addEventListener('submit', async function(e) {
            e.preventDefault();
            await handleEnhanceRequest();
        });
    }

    const downgradeForm = document.getElementById('downgradeForm');
    if (downgradeForm) {
        downgradeForm.addEventListener('submit', async function(e) {
            e.preventDefault();
            await handleDowngradeRequest();
        });
    }

    const modals = document.querySelectorAll('.modal');
    modals.forEach(modal => {
        modal.addEventListener('click', function(e) {
            if (e.target === this || e.target.classList.contains('modal-close')) {
                this.style.display = 'none';
                const form = this.querySelector('form');
                if (form) form.reset();
            }
        });
    });

    document.addEventListener('keydown', function(e) {
        if (e.key === 'Escape') {
            modals.forEach(modal => {
                modal.style.display = 'none';
                const form = modal.querySelector('form');
                if (form) form.reset();
            });
        }
    });

    const roleSelect = document.getElementById('requestRole');
    if (roleSelect) {
        roleSelect.addEventListener('change', function() {
            const roleName = this.options[this.selectedIndex].text;
            document.getElementById('roleName').textContent = roleName.toLowerCase();
        });
    }
}

async function handleEnhanceRequest() {
    const role = document.getElementById('requestRole').value;
    const message = document.getElementById('requestMessage').value.trim();
    const requestForm = document.getElementById('requestForm');

    if (!role) {
        showProfileNotification('Выберите роль', 'error');
        return;
    }

    if (message.length < 20) {
        showProfileNotification('Опишите причину подробнее (минимум 20 символов)', 'error');
        return;
    }

    if (message.length > 500) {
        showProfileNotification('Сообщение не должно превышать 500 символов', 'error');
        return;
    }

    try {
        const submitBtn = requestForm.querySelector('button[type="submit"]');
        const originalText = submitBtn.innerHTML;
        submitBtn.disabled = true;
        submitBtn.innerHTML = 'Отправка...';

        await submitRoleRequest('ENHANCE', role, message);
        closeRequestModal();
        showProfileNotification('✅ Заявка успешно отправлена!', 'success');
    } catch (error) {
        showProfileNotification(`❌ Ошибка: ${error.message}`, 'error');
    } finally {
        const submitBtn = requestForm.querySelector('button[type="submit"]');
        if (submitBtn) {
            submitBtn.disabled = false;
            submitBtn.innerHTML = 'Отправить заявку';
        }
    }
}

async function handleDowngradeRequest() {
    const currentRole = document.getElementById('currentRole').value;
    const message = document.getElementById('downgradeMessage').value.trim();
    const downgradeForm = document.getElementById('downgradeForm');

    if (!currentRole) {
        showProfileNotification('Выберите текущую роль', 'error');
        return;
    }

    if (message.length < 20) {
        showProfileNotification('Опишите причину подробнее (минимум 20 символов)', 'error');
        return;
    }

    if (message.length > 500) {
        showProfileNotification('Сообщение не должно превышать 500 символов', 'error');
        return;
    }

    try {
        const submitBtn = downgradeForm.querySelector('button[type="submit"]');
        const originalText = submitBtn.innerHTML;
        submitBtn.disabled = true;
        submitBtn.innerHTML = 'Отправка...';

        await submitRoleRequest('REMOVE', 'USER', message);
        closeDowngradeModal();
        showProfileNotification('✅ Заявка на снятие роли отправлена!', 'success');
    } catch (error) {
        showProfileNotification(`❌ Ошибка: ${error.message}`, 'error');
    } finally {
        const submitBtn = downgradeForm.querySelector('button[type="submit"]');
        if (submitBtn) {
            submitBtn.disabled = false;
            submitBtn.innerHTML = 'Отправить заявку';
        }
    }
}

// ============ ВСПОМОГАТЕЛЬНЫЕ ФУНКЦИИ ============

function getItemsText(count) {
    if (count % 10 === 1 && count % 100 !== 11) return 'товар';
    if (count % 10 >= 2 && count % 10 <= 4 && (count % 100 < 10 || count % 100 >= 20)) return 'товара';
    return 'товаров';
}

function truncateText(text, maxLength) {
    if (!text) return '';
    if (text.length <= maxLength) return text;
    return text.substring(0, maxLength) + '...';
}

function formatOrderDate(dateString) {
    if (!dateString) return 'Не указана';
    try {
        const date = new Date(dateString);
        return date.toLocaleDateString('ru-RU', {
            day: '2-digit',
            month: '2-digit',
            year: 'numeric'
        });
    } catch (e) {
        return dateString;
    }
}

function formatDate(dateString) {
    if (!dateString) return 'Не указана';
    try {
        const date = new Date(dateString);
        return date.toLocaleDateString('ru-RU', {
            day: '2-digit',
            month: '2-digit',
            year: 'numeric',
            hour: '2-digit',
            minute: '2-digit'
        });
    } catch (e) {
        return dateString;
    }
}

function formatPrice(price) {
    return new Intl.NumberFormat('ru-RU', {
        style: 'decimal',
        minimumFractionDigits: 0,
        maximumFractionDigits: 2
    }).format(price) + ' ₽';
}

function escapeHtml(text) {
    if (!text) return '';
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

function saveProfileToCache(profile) {
    try {
        localStorage.setItem('userProfileCache', JSON.stringify({
            ...profile,
            cachedAt: new Date().toISOString()
        }));
    } catch (e) {
        console.warn('Не удалось сохранить в кэш:', e);
    }
}

function loadProfileFromCache() {
    try {
        const cached = localStorage.getItem('userProfileCache');
        if (cached) {
            const data = JSON.parse(cached);
            const cachedAt = new Date(data.cachedAt);
            const now = new Date();
            const hoursDiff = (now - cachedAt) / (1000 * 60 * 60);
            if (hoursDiff < 24) return data;
        }
    } catch (e) {
        console.warn('Не удалось загрузить из кэша:', e);
    }
    return null;
}

function initTabs() {
    const tabButtons = document.querySelectorAll('.tab-button');
    tabButtons.forEach(button => {
        button.addEventListener('click', function() {
            const tabId = this.getAttribute('data-tab');
            tabButtons.forEach(btn => btn.classList.remove('active'));
            document.querySelectorAll('.tab-pane').forEach(pane => pane.classList.remove('active'));
            this.classList.add('active');
            document.getElementById(`${tabId}-tab`).classList.add('active');
        });
    });
}

function showProfileNotification(message, type = 'success') {
    const notification = document.createElement('div');
    notification.className = `profile-notification ${type}`;
    const icon = type === 'success' ? '✅' : type === 'warning' ? '⚠️' : '❌';
    const bgColor = type === 'success' ? '#28a745' : type === 'warning' ? '#ffc107' : '#dc3545';
    const textColor = type === 'warning' ? '#212529' : 'white';

    notification.innerHTML = `<div class="notification-content"><span class="notification-icon">${icon}</span><span class="notification-text">${message}</span></div>`;
    notification.style.cssText = `position: fixed; top: 20px; right: 20px; background: ${bgColor}; color: ${textColor}; padding: 15px 20px; border-radius: 8px; box-shadow: 0 4px 12px rgba(0,0,0,0.15); z-index: 9999; animation: slideIn 0.3s ease;`;

    document.body.appendChild(notification);
    setTimeout(() => {
        notification.style.animation = 'slideOut 0.3s ease';
        setTimeout(() => notification.remove(), 300);
    }, 3000);
}

// ============ ГЛОБАЛЬНЫЕ ФУНКЦИИ ============

window.openRequestModal = function(type) {
    const modal = document.getElementById('requestModal');
    if (!modal) return;

    const roleSelect = document.getElementById('requestRole');
    const roleNameSpan = document.getElementById('roleName');
    const title = document.getElementById('requestModalTitle');

    if (type === 'seller') {
        roleSelect.value = 'SELLER';
        roleNameSpan.textContent = 'продавцом';
        title.textContent = 'Заявка на роль продавца';
    } else if (type === 'courier') {
        roleSelect.value = 'COURIER';
        roleNameSpan.textContent = 'курьером';
        title.textContent = 'Заявка на роль курьера';
    }
    modal.style.display = 'flex';
};

window.closeRequestModal = function() {
    const modal = document.getElementById('requestModal');
    if (modal) {
        modal.style.display = 'none';
        const form = modal.querySelector('form');
        if (form) form.reset();
        const charCount = document.getElementById('charCount');
        if (charCount) charCount.textContent = '0';
    }
};

window.openDowngradeModal = function() {
    const modal = document.getElementById('downgradeModal');
    if (!modal) return;

    const currentRole = document.body.dataset.userRole || 'USER';
    const roleSelect = document.getElementById('currentRole');
    if (roleSelect && currentRole !== 'USER') {
        roleSelect.value = currentRole;
        roleSelect.disabled = true;
    }
    modal.style.display = 'flex';
};

window.closeDowngradeModal = function() {
    const modal = document.getElementById('downgradeModal');
    if (modal) {
        modal.style.display = 'none';
        const form = modal.querySelector('form');
        if (form) form.reset();
        const downgradeCharCount = document.getElementById('downgradeCharCount');
        if (downgradeCharCount) downgradeCharCount.textContent = '0';
        const roleSelect = document.getElementById('currentRole');
        if (roleSelect) roleSelect.disabled = false;
    }
};

// ============ CSS СТИЛИ ============

const style = document.createElement('style');
style.textContent = `
    @keyframes slideIn {
        from { transform: translateX(100%); opacity: 0; }
        to { transform: translateX(0); opacity: 1; }
    }
    @keyframes slideOut {
        from { transform: translateX(0); opacity: 1; }
        to { transform: translateX(100%); opacity: 0; }
    }
    
    .order-card {
        background: white;
        border-radius: 16px;
        padding: 20px;
        margin-bottom: 20px;
        box-shadow: 0 2px 8px rgba(0,0,0,0.05);
        transition: all 0.3s ease;
        border: 1px solid #e0e0e0;
    }
    .order-card:hover {
        box-shadow: 0 4px 16px rgba(0,0,0,0.1);
        transform: translateY(-2px);
    }
    .order-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 15px;
        padding-bottom: 10px;
        border-bottom: 1px solid #f0f0f0;
        flex-wrap: wrap;
        gap: 10px;
    }
    .order-number { font-weight: bold; font-size: 16px; color: #333; }
    .order-date { font-size: 12px; color: #999; margin-left: 10px; }
    .order-status-badge { padding: 4px 12px; border-radius: 20px; font-size: 12px; font-weight: 600; }
    .order-status-badge.pending { background: #fff3cd; color: #856404; }
    .order-status-badge.dispatched { background: #cce5ff; color: #004085; }
    .order-status-badge.delivered { background: #d4edda; color: #155724; }
    .order-status-badge.completed { background: #d4edda; color: #155724; }
    .order-status-badge.cancelled { background: #f8d7da; color: #721c24; }
    .order-status-badge.returned { background: #fff3cd; color: #856404; }
    
    .preview-items { display: flex; gap: 15px; flex-wrap: wrap; margin-bottom: 15px; }
    .preview-item { display: flex; align-items: center; gap: 10px; background: #f8f9fa; padding: 8px 12px; border-radius: 8px; }
    .preview-item-image { width: 40px; height: 40px; object-fit: cover; border-radius: 8px; }
    .preview-item-name { font-size: 13px; color: #333; }
    .preview-item-price { font-size: 12px; color: #28a745; font-weight: bold; }
    .preview-item-quantity { font-size: 11px; color: #999; }
    .preview-more { display: flex; align-items: center; padding: 0 12px; color: #667eea; font-size: 13px; }
    
    .order-footer { display: flex; justify-content: space-between; align-items: center; padding-top: 15px; border-top: 1px solid #f0f0f0; flex-wrap: wrap; gap: 15px; }
    .order-total { font-weight: bold; }
    .total-amount { color: #28a745; font-size: 18px; margin-left: 8px; }
    .btn-view-order { padding: 8px 20px; background: linear-gradient(135deg, #667eea, #764ba2); color: white; border: none; border-radius: 20px; cursor: pointer; font-size: 13px; transition: all 0.3s ease; }
    .btn-view-order:hover { transform: translateY(-2px); box-shadow: 0 4px 12px rgba(102,126,234,0.3); }
    
    .empty-orders { text-align: center; padding: 60px 20px; background: white; border-radius: 16px; }
    .empty-orders-icon { font-size: 64px; margin-bottom: 20px; }
    .btn-shop-now { display: inline-block; padding: 12px 30px; background: linear-gradient(135deg, #667eea, #764ba2); color: white; text-decoration: none; border-radius: 30px; }
    
    .loading-orders { text-align: center; padding: 40px; }
    .loading-spinner { width: 40px; height: 40px; margin: 0 auto 20px; border: 3px solid #f3f3f3; border-top: 3px solid #667eea; border-radius: 50%; animation: spin 1s linear infinite; }
    @keyframes spin { 0% { transform: rotate(0deg); } 100% { transform: rotate(360deg); } }
    
    .order-details-modal .modal-content {
        max-width: 600px;
        width: 90%;
        border-radius: 20px;
    }
    
    .order-info-block {
        background: #f8f9fa;
        padding: 15px;
        border-radius: 12px;
        margin-bottom: 20px;
    }
    
    .info-line {
        display: flex;
        margin-bottom: 10px;
        padding: 5px 0;
    }
    
    .info-line:last-child {
        margin-bottom: 0;
    }
    
    .info-label {
        width: 110px;
        font-weight: 600;
        color: #555;
    }
    
    .info-value {
        flex: 1;
        color: #333;
    }
    
    .info-value.total {
        color: #28a745;
        font-weight: bold;
        font-size: 18px;
    }
    
    .status-badge {
        display: inline-block;
        padding: 4px 12px;
        border-radius: 20px;
        font-size: 12px;
        font-weight: 600;
    }
    
    .status-badge.pending { background: #fff3cd; color: #856404; }
    .status-badge.dispatched { background: #cce5ff; color: #004085; }
    .status-badge.delivered { background: #d4edda; color: #155724; }
    .status-badge.completed { background: #d4edda; color: #155724; }
    .status-badge.cancelled { background: #f8d7da; color: #721c24; }
    .status-badge.returned { background: #fff3cd; color: #856404; }
    
    .order-items-block h4 {
        margin-bottom: 15px;
        color: #333;
        font-size: 16px;
    }
    
    .items-scrollable {
        max-height: 400px;
        overflow-y: auto;
        padding-right: 10px;
    }
    
    .items-scrollable::-webkit-scrollbar {
        width: 6px;
    }
    
    .items-scrollable::-webkit-scrollbar-track {
        background: #f1f1f1;
        border-radius: 3px;
    }
    
    .items-scrollable::-webkit-scrollbar-thumb {
        background: #667eea;
        border-radius: 3px;
    }
    
    .item-row {
        display: flex;
        gap: 12px;
        padding: 12px;
        background: #fff;
        border-radius: 12px;
        margin-bottom: 10px;
        border: 1px solid #e0e0e0;
        transition: all 0.3s ease;
    }
    
    .item-row:hover {
        transform: translateX(5px);
        box-shadow: 0 2px 8px rgba(0,0,0,0.1);
    }
    
    .item-image {
        width: 60px;
        height: 60px;
        border-radius: 10px;
        overflow: hidden;
        flex-shrink: 0;
    }
    
    .item-image img {
        width: 100%;
        height: 100%;
        object-fit: cover;
    }
    
    .item-info {
        flex: 1;
    }
    
    .item-name {
        font-weight: 600;
        color: #333;
        margin-bottom: 8px;
        font-size: 14px;
    }
    
    .item-details {
        display: flex;
        justify-content: space-between;
        align-items: center;
        flex-wrap: wrap;
        gap: 10px;
    }
    
    .item-price {
        color: #666;
        font-size: 13px;
    }
    
    .item-total {
        color: #28a745;
        font-weight: bold;
        font-size: 14px;
    }
    
    .items-total {
        margin-top: 15px;
        padding-top: 15px;
        border-top: 2px solid #e0e0e0;
        text-align: right;
        font-size: 16px;
    }
    
    .items-total strong {
        color: #28a745;
        font-size: 18px;
        margin-left: 10px;
    }
    
    .no-items {
        text-align: center;
        padding: 40px;
        color: #999;
    }
    
    @media (max-width: 768px) {
        .item-row {
            flex-direction: column;
            text-align: center;
        }
        
        .item-image {
            width: 80px;
            height: 80px;
            margin: 0 auto;
        }
        
        .item-details {
            flex-direction: column;
        }
        
        .info-line {
            flex-direction: column;
        }
        
        .info-label {
            width: auto;
            margin-bottom: 5px;
        }
    }
`;
document.head.appendChild(style);