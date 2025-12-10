// Конфигурация API
const API_BASE_URL = 'http://localhost:8080';
const API_ENDPOINTS = {
    CREATE_REQUEST: '/api/users/role-request',
    GET_CURRENT_USER: '/api/users/me',
    GET_USER_ORDERS: '/api/users/me-orders',
    UPDATE_ADDRESS: '/api/users/address'
};

// Константы для ролей
const USER_ROLES = {
    CUSTOMER: 'CUSTOMER',
    SELLER: 'SELLER',
    COURIER: 'COURIER',
    ADMIN: 'ADMIN'
};

const TYPE_ACTION = {
    ENHANCE: 'ENHANCE',
    REMOVE: 'REMOVE'
};

const REQUEST_STATUS = {
    PENDING: 'PENDING',
    APPROVED: 'APPROVED',
    REJECTED: 'REJECTED'
};

// Получить заголовки с токеном
function getHeaders(contentType = 'application/x-www-form-urlencoded') {
    const token = localStorage.getItem('authToken');
    const headers = {};
    
    if (contentType) {
        headers['Content-Type'] = contentType;
    }
    
    if (token) {
        headers['Authorization'] = `Bearer ${token}`;
    }
    
    return headers;
}

// Инициализация
document.addEventListener('DOMContentLoaded', async function() {
    console.log('Страница профиля загружена');
    initTabs();
    initProfileForms();
    
    try {
        // Загружаем пользователя с сервера
        const user = await getCurrentUser();
        if (user) {
            loadProfileData(user);
            updateRoleButtons(user.role);
            await loadUserOrders();
        } else {
            loadProfileData();
            displayOrdersHistory();
        }
    } catch (error) {
        console.error('Ошибка загрузки данных:', error);
        loadProfileData();
        displayOrdersHistory();
    }
    
    loadLocalRequests();
    initModals();
});

// Получить текущего пользователя с сервера
async function getCurrentUser() {
    try {
        const response = await fetch(`${API_BASE_URL}${API_ENDPOINTS.GET_CURRENT_USER}`, {
            method: 'GET',
            headers: getHeaders('application/json')
        });
        
        if (response.ok) {
            return await response.json();
        } else if (response.status === 401) {
            console.warn('Пользователь не авторизован');
            window.location.href = '/login';
            return null;
        } else {
            console.error('Ошибка получения пользователя:', response.status);
            return null;
        }
    } catch (error) {
        console.error('Ошибка сети:', error);
        return null;
    }
}

// Загрузить заказы пользователя с сервера
async function loadUserOrders() {
    try {
        const response = await fetch(`${API_BASE_URL}${API_ENDPOINTS.GET_USER_ORDERS}`, {
            method: 'GET',
            headers: getHeaders('application/json')
        });
        
        if (response.ok) {
            const orders = await response.json();
            displayOrdersFromServer(orders);
            updateProfileStats(orders.length);
        } else {
            console.error('Ошибка загрузки заказов:', response.status);
            displayOrdersHistory();
        }
    } catch (error) {
        console.error('Ошибка сети при загрузке заказов:', error);
        displayOrdersHistory();
    }
}

// Отобразить заказы с сервера
function displayOrdersFromServer(orders) {
    const ordersGrid = document.getElementById('ordersGrid');
    if (!ordersGrid) return;
    
    if (!orders || orders.length === 0) {
        ordersGrid.innerHTML = `
            <div class="empty-orders">
                <div class="empty-orders-icon">
                    <img src="images/empty-cart.svg" alt="Пустая история заказов" style="width: 80px; height: 80px;">
                </div>
                <h2>История заказов пуста</h2>
                <p>Здесь появятся ваши завершенные заказы после оформления</p>
                <a href="/" class="btn btn-primary" style="display: inline-block; margin-top: 15px;">
                    Перейти к покупкам
                </a>
            </div>
        `;
        return;
    }
    
    let ordersHtml = '';
    
    orders.forEach(order => {
        order.items?.forEach(item => {
            ordersHtml += `
                <div class="order-item" data-product-id="${item.id || ''}">
                    <div class="product-image">
                        <img src="${item.image || 'images/product-img.png'}" 
                             alt="${item.name || 'Товар'}" 
                             onerror="this.src='images/product-img.png'">
                    </div>
                    <div class="product-price">${formatPrice(item.price || 0)}</div>
                    <div class="product-name">${item.name || 'Неизвестный товар'}</div>
                    <div class="order-date">${formatDate(order.createdAt || new Date())}</div>
                    <div class="order-quantity">Количество: ${item.quantity || 1}</div>
                    <button class="order-btn" onclick="reorderProduct(${item.id || 0})">
                        Заказать снова
                    </button>
                </div>
            `;
        });
    });
    
    ordersGrid.innerHTML = ordersHtml;
    makeOrderItemsClickable();
}

// Обновить статистику профиля
function updateProfileStats(orderCount) {
    const statValue = document.querySelector('.stat-value');
    if (statValue) {
        statValue.textContent = orderCount;
    }
}

// Инициализация табов
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

// Инициализация форм профиля
function initProfileForms() {
    initPersonalForm();
    initSettingsForm();
    initAvatarUpload();
}

// Загрузка данных профиля
function loadProfileData(userData = null) {
    if (userData) {
        document.getElementById('profileName').value = userData.name || 'Не указано';
        document.getElementById('profileEmail').value = userData.email || 'Не указан';
        document.getElementById('profileAddress').value = userData.address || 'Адрес не указан';
        
        localStorage.setItem('currentUser', JSON.stringify(userData));
        localStorage.setItem('userRole', userData.role || USER_ROLES.CUSTOMER);
        
        document.querySelector('.profile-name').textContent = userData.name || 'Не указано';
        document.querySelector('.profile-email').textContent = userData.email || 'Не указан';
        
    } else {
        const profileData = JSON.parse(localStorage.getItem('userProfile') || '{}');
        
        document.getElementById('profileName').value = profileData.name || 'Иван Иванов';
        document.getElementById('profileEmail').value = profileData.email || 'ivan@example.com';
        document.getElementById('profileAddress').value = profileData.address || 'Адрес не указан';
        
        document.querySelector('.profile-name').textContent = profileData.name || 'Иван Иванов';
        document.querySelector('.profile-email').textContent = profileData.email || 'ivan@example.com';
    }
}

// Форма личной информации
function initPersonalForm() {
    const personalForm = document.getElementById('personalForm');
    
    if (personalForm) {
        personalForm.addEventListener('submit', async function(e) {
            e.preventDefault();
            
            const name = document.getElementById('profileName').value;
            const email = document.getElementById('profileEmail').value;
            const address = document.getElementById('profileAddress').value;
            
            try {
                const params = new URLSearchParams();
                params.append('newAddress', address);
                
                const response = await fetch(`${API_BASE_URL}${API_ENDPOINTS.UPDATE_ADDRESS}`, {
                    method: 'POST',
                    headers: getHeaders(),
                    body: params
                });
                
                if (response.ok) {
                    const profileData = {
                        name: name,
                        email: email,
                        address: address,
                        avatar: localStorage.getItem('userAvatar') || 'images/avatar.jpg'
                    };
                    
                    localStorage.setItem('userProfile', JSON.stringify(profileData));
                    
                    document.querySelector('.profile-name').textContent = name;
                    document.querySelector('.profile-email').textContent = email;
                    
                    showProfileNotification('✅ Профиль успешно обновлен!', 'success');
                } else {
                    const errorText = await response.text();
                    showProfileNotification(`❌ Ошибка: ${errorText || 'Не удалось обновить профиль'}`, 'error');
                }
            } catch (error) {
                console.error('Ошибка обновления профиля:', error);
                showProfileNotification('❌ Ошибка сети при обновлении профиля', 'error');
            }
        });
    }
}

// Форма настроек
function initSettingsForm() {
    const settingsForm = document.getElementById('settingsForm');
    
    if (settingsForm) {
        settingsForm.addEventListener('submit', function(e) {
            e.preventDefault();
            showProfileNotification('Настройки сохранены!', 'success');
        });
    }
}

// Загрузка аватарки
function initAvatarUpload() {
    const avatarInput = document.getElementById('avatarInput');
    const avatarPreview = document.getElementById('avatarPreview');
    const removeAvatarBtn = document.getElementById('removeAvatar');
    
    if (!avatarInput || !avatarPreview || !removeAvatarBtn) return;
    
    avatarInput.addEventListener('change', function(e) {
        const file = e.target.files[0];
        if (file) {
            if (file.type.startsWith('image/')) {
                const reader = new FileReader();
                reader.onload = function(e) {
                    const imageUrl = e.target.result;
                    
                    avatarPreview.src = imageUrl;
                    document.querySelector('.profile-avatar').src = imageUrl;
                    
                    localStorage.setItem('userAvatar', imageUrl);
                    
                    const profileData = JSON.parse(localStorage.getItem('userProfile') || '{}');
                    profileData.avatar = imageUrl;
                    localStorage.setItem('userProfile', JSON.stringify(profileData));
                    
                    showProfileNotification('Аватарка успешно обновлена!', 'success');
                };
                reader.readAsDataURL(file);
            } else {
                showProfileNotification('Пожалуйста, выберите файл изображения', 'error');
            }
        }
    });
    
    removeAvatarBtn.addEventListener('click', function() {
        const defaultAvatar = 'images/avatar.jpg';
        
        avatarPreview.src = defaultAvatar;
        document.querySelector('.profile-avatar').src = defaultAvatar;
        
        localStorage.removeItem('userAvatar');
        
        const profileData = JSON.parse(localStorage.getItem('userProfile') || '{}');
        profileData.avatar = defaultAvatar;
        localStorage.setItem('userProfile', JSON.stringify(profileData));
        
        showProfileNotification('Аватарка удалена!', 'success');
    });
}

// Инициализация модальных окон
function initModals() {
    const requestMessage = document.getElementById('requestMessage');
    const downgradeMessage = document.getElementById('downgradeMessage');
    
    if (requestMessage) {
        requestMessage.addEventListener('input', function() {
            document.getElementById('charCount').textContent = this.value.length;
        });
    }
    
    if (downgradeMessage) {
        downgradeMessage.addEventListener('input', function() {
            document.getElementById('downgradeCharCount').textContent = this.value.length;
        });
    }
    
    const requestRole = document.getElementById('requestRole');
    if (requestRole) {
        requestRole.addEventListener('change', function() {
            const roleName = this.options[this.selectedIndex].text;
            document.getElementById('roleName').textContent = roleName.toLowerCase();
        });
    }
    
    const requestForm = document.getElementById('requestForm');
    if (requestForm) {
        requestForm.addEventListener('submit', function(e) {
            e.preventDefault();
            submitRequest();
        });
    }
    
    const downgradeForm = document.getElementById('downgradeForm');
    if (downgradeForm) {
        downgradeForm.addEventListener('submit', function(e) {
            e.preventDefault();
            submitDowngradeRequest();
        });
    }
    
    const modals = document.querySelectorAll('.modal');
    modals.forEach(modal => {
        modal.addEventListener('click', function(e) {
            if (e.target === this) {
                if (this.id === 'requestModal') {
                    closeRequestModal();
                } else if (this.id === 'downgradeModal') {
                    closeDowngradeModal();
                }
            }
        });
    });
    
    document.addEventListener('keydown', function(e) {
        if (e.key === 'Escape') {
            closeRequestModal();
            closeDowngradeModal();
        }
    });
}

// Открыть модальное окно подачи заявки
function openRequestModal(type = null) {
    const modal = document.getElementById('requestModal');
    const form = document.getElementById('requestForm');
    const requestRole = document.getElementById('requestRole');
    
    if (!modal || !form || !requestRole) return;
    
    form.reset();
    document.getElementById('charCount').textContent = '0';
    
    if (type === 'seller') {
        requestRole.value = 'SELLER';
        document.getElementById('roleName').textContent = 'продавцом';
        document.getElementById('requestModalTitle').textContent = 'Заявка на роль продавца';
    } else if (type === 'courier') {
        requestRole.value = 'COURIER';
        document.getElementById('roleName').textContent = 'курьером';
        document.getElementById('requestModalTitle').textContent = 'Заявка на роль курьера';
    } else {
        requestRole.value = '';
        document.getElementById('roleName').textContent = '...';
        document.getElementById('requestModalTitle').textContent = 'Подача заявки';
    }
    
    modal.style.display = 'flex';
}

// Закрыть модальное окно подачи заявки
function closeRequestModal() {
    const modal = document.getElementById('requestModal');
    if (modal) {
        modal.style.display = 'none';
    }
}

// Открыть модальное окно снятия роли
function openDowngradeModal() {
    const modal = document.getElementById('downgradeModal');
    const form = document.getElementById('downgradeForm');
    const userRole = localStorage.getItem('userRole') || USER_ROLES.CUSTOMER;
    
    if (!modal || !form) return;
    
    form.reset();
    document.getElementById('downgradeCharCount').textContent = '0';
    
    const currentRoleSelect = document.getElementById('currentRole');
    
    if (currentRoleSelect) {
        if (userRole !== USER_ROLES.CUSTOMER) {
            currentRoleSelect.value = userRole;
            currentRoleSelect.disabled = true;
            currentRoleSelect.title = 'Текущая роль определена автоматически';
        } else {
            currentRoleSelect.disabled = false;
            currentRoleSelect.value = '';
        }
    }
    
    modal.style.display = 'flex';
}

// Закрыть модальное окно снятия роли
function closeDowngradeModal() {
    const modal = document.getElementById('downgradeModal');
    if (modal) {
        modal.style.display = 'none';
    }
}

// Отправить заявку на повышение
async function submitRequest() {
    const role = document.getElementById('requestRole').value;
    const message = document.getElementById('requestMessage').value.trim();
    
    if (!role) {
        showProfileNotification('Пожалуйста, выберите роль', 'error');
        return;
    }
    
    if (message.length < 20) {
        showProfileNotification('Пожалуйста, напишите более подробное описание (минимум 20 символов)', 'error');
        return;
    }
    
    if (message.length > 500) {
        showProfileNotification('Сообщение не должно превышать 500 символов', 'error');
        return;
    }
    
    try {
        const params = new URLSearchParams();
        params.append('requestedRole', role);
        params.append('typeAction', TYPE_ACTION.ENHANCE);
        params.append('message', message);
        
        console.log('Отправка заявки на:', `${API_BASE_URL}${API_ENDPOINTS.CREATE_REQUEST}`);
        console.log('Параметры:', params.toString());
        
        const response = await fetch(`${API_BASE_URL}${API_ENDPOINTS.CREATE_REQUEST}`, {
            method: 'POST',
            headers: getHeaders(),
            body: params
        });
        
        const responseText = await response.text();
        console.log('Ответ сервера:', responseText);
        
        if (response.ok) {
            try {
                const requestData = JSON.parse(responseText);
                showProfileNotification('✅ Заявка успешно отправлена! Ожидайте рассмотрения администратором.', 'success');
                closeRequestModal();
                
                addRequestToLocalHistory({
                    id: requestData.id || Date.now(),
                    typeAction: TYPE_ACTION.ENHANCE,
                    requestedRole: role,
                    message: message,
                    status: REQUEST_STATUS.PENDING,
                    createdAt: new Date().toISOString()
                });
                
            } catch (e) {
                showProfileNotification('✅ Заявка успешно отправлена!', 'success');
                closeRequestModal();
                
                addRequestToLocalHistory({
                    id: Date.now(),
                    typeAction: TYPE_ACTION.ENHANCE,
                    requestedRole: role,
                    message: message,
                    status: REQUEST_STATUS.PENDING,
                    createdAt: new Date().toISOString()
                });
            }
        } else {
            showProfileNotification(`❌ Ошибка: ${responseText || 'Сервер вернул ошибку'}`, 'error');
        }
    } catch (error) {
        console.error('Ошибка отправки заявки:', error);
        showProfileNotification('❌ Ошибка сети при отправке заявки', 'error');
    }
}

// Отправить заявку на снятие роли
async function submitDowngradeRequest() {
    const currentRole = document.getElementById('currentRole').value;
    const message = document.getElementById('downgradeMessage').value.trim();
    
    if (!currentRole) {
        showProfileNotification('Пожалуйста, выберите текущую роль', 'error');
        return;
    }
    
    if (message.length < 20) {
        showProfileNotification('Пожалуйста, напишите более подробное описание (минимум 20 символов)', 'error');
        return;
    }
    
    if (message.length > 500) {
        showProfileNotification('Сообщение не должно превышать 500 символов', 'error');
        return;
    }
    
    try {
        const params = new URLSearchParams();
        params.append('requestedRole', USER_ROLES.CUSTOMER);
        params.append('typeAction', TYPE_ACTION.REMOVE);
        params.append('message', message);
        
        console.log('Отправка заявки на снятие на:', `${API_BASE_URL}${API_ENDPOINTS.CREATE_REQUEST}`);
        console.log('Параметры:', params.toString());
        
        const response = await fetch(`${API_BASE_URL}${API_ENDPOINTS.CREATE_REQUEST}`, {
            method: 'POST',
            headers: getHeaders(),
            body: params
        });
        
        const responseText = await response.text();
        console.log('Ответ сервера:', responseText);
        
        if (response.ok) {
            showProfileNotification('✅ Заявка на снятие роли успешно отправлена!', 'success');
            closeDowngradeModal();
            
            addRequestToLocalHistory({
                id: Date.now(),
                typeAction: TYPE_ACTION.REMOVE,
                requestedRole: USER_ROLES.CUSTOMER,
                message: message,
                status: REQUEST_STATUS.PENDING,
                createdAt: new Date().toISOString(),
                currentRole: currentRole
            });
            
        } else {
            showProfileNotification(`❌ Ошибка: ${responseText || 'Сервер вернул ошибку'}`, 'error');
        }
    } catch (error) {
        console.error('Ошибка отправки заявки:', error);
        showProfileNotification('❌ Ошибка сети при отправке заявки', 'error');
    }
}

// Добавить заявку в локальную историю
function addRequestToLocalHistory(request) {
    const requests = JSON.parse(localStorage.getItem('userRoleRequests') || '[]');
    requests.unshift(request);
    localStorage.setItem('userRoleRequests', JSON.stringify(requests));
    
    loadLocalRequests();
}

// Загрузить локальные заявки
function loadLocalRequests() {
    const requestsList = document.getElementById('requestsList');
    if (!requestsList) return;
    
    const requests = JSON.parse(localStorage.getItem('userRoleRequests') || '[]');
    
    if (requests.length === 0) {
        requestsList.innerHTML = `
            <div class="no-requests">
                <p>📭 У вас еще нет отправленных заявок</p>
            </div>
        `;
        return;
    }
    
    const requestsHtml = requests.map(request => {
        const requestTypeText = getRequestTypeText(request.typeAction, request.requestedRole);
        const statusText = getStatusText(request.status);
        const date = formatDate(request.createdAt);
        const message = request.message || 'Без описания';
        
        return `
            <div class="request-item">
                <div class="request-info">
                    <h4>${requestTypeText}</h4>
                    <p><strong>Дата:</strong> ${date}</p>
                    <p><strong>Сообщение:</strong> ${message.substring(0, 60)}${message.length > 60 ? '...' : ''}</p>
                    <p><strong>Статус:</strong> <span class="status-text status-${request.status.toLowerCase()}">${statusText}</span></p>
                </div>
                <div class="request-id">
                    #${request.id}
                </div>
            </div>
        `;
    }).join('');
    
    requestsList.innerHTML = requestsHtml;
}

// Обновить кнопки в зависимости от роли
function updateRoleButtons(userRole) {
    const requestCards = document.querySelectorAll('.request-card');
    
    if (!requestCards) return;
    
    if (userRole === USER_ROLES.SELLER || userRole === USER_ROLES.COURIER) {
        requestCards.forEach((card, index) => {
            if (index < 2) {
                card.style.opacity = '0.6';
                card.style.pointerEvents = 'none';
                
                const button = card.querySelector('button');
                if (button) {
                    button.textContent = 'Роль уже активна';
                    button.disabled = true;
                    button.classList.remove('btn-primary');
                    button.classList.add('btn-secondary');
                }
            } else if (index === 2) {
                const button = card.querySelector('button');
                if (button) {
                    button.textContent = `Сняться с роли ${getRoleText(userRole).toLowerCase()}`;
                    button.classList.remove('btn-secondary');
                    button.classList.add('btn-primary');
                }
            }
        });
    } else if (userRole === USER_ROLES.CUSTOMER) {
        const downgradeCard = requestCards[2];
        if (downgradeCard) {
            downgradeCard.style.opacity = '0.6';
            downgradeCard.style.pointerEvents = 'none';
            
            const button = downgradeCard.querySelector('button');
            if (button) {
                button.textContent = 'Доступно только для продавцов/курьеров';
                button.disabled = true;
                button.classList.remove('btn-primary');
                button.classList.add('btn-secondary');
            }
        }
    } else if (userRole === USER_ROLES.ADMIN) {
        requestCards.forEach(card => {
            card.style.opacity = '0.6';
            card.style.pointerEvents = 'none';
            
            const button = card.querySelector('button');
            if (button) {
                button.textContent = 'Недоступно для админа';
                button.disabled = true;
                button.classList.remove('btn-primary');
                button.classList.add('btn-secondary');
            }
        });
    }
}

// Показать уведомление
function showProfileNotification(message, type = 'success') {
    const notification = document.createElement('div');
    notification.className = `profile-notification ${type === 'error' ? 'error' : 'success'}`;
    notification.textContent = message;
    
    document.body.appendChild(notification);
    
    setTimeout(() => notification.classList.add('show'), 100);
    
    setTimeout(() => {
        notification.classList.remove('show');
        setTimeout(() => notification.remove(), 300);
    }, 3000);
}

// Отображение истории заказов (локальной)
function displayOrdersHistory() {
    const ordersGrid = document.getElementById('ordersGrid');
    if (!ordersGrid) return;
    
    const orders = window.cartManager ? window.cartManager.getOrdersHistory() : [];
    
    if (orders.length === 0) {
        ordersGrid.innerHTML = `
            <div class="empty-orders">
                <div class="empty-orders-icon">
                    <img src="images/empty-cart.svg" alt="Пустая история заказов" style="width: 80px; height: 80px;">
                </div>
                <h2>История заказов пуста</h2>
                <p>Здесь появятся ваши завершенные заказы после оформления</p>
                <a href="/" class="btn btn-primary" style="display: inline-block; margin-top: 15px;">
                    Перейти к покупкам
                </a>
            </div>
        `;
        return;
    }
    
    const allOrderItems = [];
    orders.forEach(order => {
        order.items.forEach(item => {
            allOrderItems.push({
                ...item,
                orderDate: order.date,
                orderId: order.id
            });
        });
    });
    
    ordersGrid.innerHTML = allOrderItems.map(item => `
        <div class="order-item" data-product-id="${item.id}">
            <div class="product-image">
                <img src="${window.imageUploader ? window.imageUploader.getImage(item.id) : item.image}" 
                     alt="${item.name}" 
                     onerror="this.src='images/product-img.png'">
            </div>
            <div class="product-price">${formatPrice(item.price)}</div>
            <div class="product-name">${item.name}</div>
            <div class="order-date">${item.orderDate}</div>
            <div class="order-quantity">Количество: ${item.quantity}</div>
            <button class="order-btn" onclick="reorderProduct(${item.id})">
                Заказать снова
            </button>
        </div>
    `).join('');
    
    makeOrderItemsClickable();
}

// Перезаказ товара
function reorderProduct(productId) {
    if (window.cartManager && window.cartManager.addToCart(productId)) {
        const product = window.productManager.getProductById(productId);
        showProfileNotification(`Товар "${product.name}" добавлен в корзину!`);
    } else {
        showProfileNotification('Ошибка при добавлении товара в корзину');
    }
}

// Делаем товары в истории заказов кликабельными
function makeOrderItemsClickable() {
    const orderItems = document.querySelectorAll('.order-item');
    orderItems.forEach(item => {
        item.addEventListener('click', function(e) {
            if (!e.target.closest('button')) {
                const productId = this.getAttribute('data-product-id');
                viewProductDetails(parseInt(productId));
            }
        });
    });
}

// Просмотр деталей товара
function viewProductDetails(productId) {
    const product = window.productManager.getProductById(productId);
    if (product) {
        localStorage.setItem('currentProduct', JSON.stringify(product));
        window.location.href = 'productForm.html';
    }
}

// Форматирование цены
function formatPrice(price) {
    return new Intl.NumberFormat('ru-RU', {
        style: 'currency',
        currency: 'RUB',
        minimumFractionDigits: 0
    }).format(price);
}

// Проверить статус заявок
function checkUserRequestsStatus() {
    const requests = JSON.parse(localStorage.getItem('userRoleRequests') || '[]');
    const pendingRequests = requests.filter(r => r.status === REQUEST_STATUS.PENDING);
    
    if (pendingRequests.length > 0) {
        console.log(`У вас ${pendingRequests.length} активных заявок`);
    }
}

// Вспомогательные функции
function getRequestTypeText(typeAction, requestedRole) {
    if (typeAction === 'ENHANCE') {
        if (requestedRole === 'SELLER') return '📈 Стать продавцом';
        if (requestedRole === 'COURIER') return '📈 Стать курьером';
        return '📈 Повышение роли';
    } else if (typeAction === 'REMOVE') {
        return '📉 Снятие с роли';
    }
    return typeAction;
}

function getStatusText(status) {
    const statusMap = {
        'PENDING': '⏳ Ожидает',
        'APPROVED': '✅ Одобрено',
        'REJECTED': '❌ Отклонено'
    };
    return statusMap[status] || status;
}

function getRoleText(role) {
    const roleMap = {
        'CUSTOMER': 'Покупатель',
        'SELLER': 'Продавец',
        'COURIER': 'Курьер',
        'ADMIN': 'Администратор'
    };
    return roleMap[role] || role;
}

function formatDate(dateString) {
    if (!dateString) return 'Не указана';
    
    const date = new Date(dateString);
    if (isNaN(date.getTime())) return dateString;
    
    return date.toLocaleDateString('ru-RU', {
        day: '2-digit',
        month: '2-digit',
        year: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
    });
}