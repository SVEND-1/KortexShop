// Конфигурация API
const API_BASE_URL = 'http://localhost:8080';
const API_ENDPOINTS = {
    CREATE_REQUEST: '/api/users/role-request',
    GET_MY_REQUESTS: '/api/users/role-requests',
    GET_CURRENT_USER: '/api/users/current'
};

// Константы для ролей
const USER_ROLES = {
    CUSTOMER: 'CUSTOMER',
    SELLER: 'SELLER',
    COURIER: 'COURIER',
    ADMIN: 'ADMIN'
};

const TYPE_ACTION = {
    ENHANCE: 'ENHANCE', // Повышение
    REMOVE: 'REMOVE'    // Снятие
};

const REQUEST_STATUS = {
    PENDING: 'PENDING',
    APPROVED: 'APPROVED',
    REJECTED: 'REJECTED'
};

// Получить заголовки с токеном
function getHeaders(contentType = 'application/x-www-form-urlencoded') {
    const token = localStorage.getItem('authToken');
    const headers = {
        'Content-Type': contentType
    };
    
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
    displayOrdersHistory();
    updateProfileStats();
    
    try {
        // Загружаем пользователя с сервера
        const user = await getCurrentUser();
        if (user) {
            loadProfileData(user);
            updateRoleButtons(user.role);
        } else {
            loadProfileData();
        }
        
        // Загружаем заявки с сервера
        await loadUserRequests();
    } catch (error) {
        console.error('Ошибка загрузки данных:', error);
        loadProfileData();
        loadLocalRequests();
    }
    
    // Проверяем статус заявок
    checkUserRequestsStatus();
});

// Инициализация табов
function initTabs() {
    const tabButtons = document.querySelectorAll('.tab-button');
    
    tabButtons.forEach(button => {
        button.addEventListener('click', function() {
            const tabId = this.getAttribute('data-tab');
            
            // Убираем активный класс у всех кнопок и вкладок
            tabButtons.forEach(btn => btn.classList.remove('active'));
            document.querySelectorAll('.tab-pane').forEach(pane => pane.classList.remove('active'));
            
            // Добавляем активный класс текущей кнопке и вкладке
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
    initModals();
}

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
            // Пользователь не авторизован
            console.warn('Пользователь не авторизован');
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

// Загрузка данных профиля
function loadProfileData(userData = null) {
    if (userData) {
        // Используем данные с сервера
        document.getElementById('profileName').value = userData.name || 'Не указано';
        document.getElementById('profileEmail').value = userData.email || 'Не указан';
        document.getElementById('profileAddress').value = userData.address || 'Адрес не указан';
        
        // Пытаемся получить аватар, если есть
        const avatar = userData.avatar || 'images/avatar.jpg';
        document.getElementById('avatarPreview').src = avatar;
        
        // Обновляем сайдбар
        document.querySelector('.profile-name').textContent = userData.name || 'Не указано';
        document.querySelector('.profile-email').textContent = userData.email || 'Не указан';
        document.querySelector('.profile-avatar').src = avatar;
        
        // Сохраняем текущую роль
        localStorage.setItem('currentUser', JSON.stringify(userData));
        localStorage.setItem('userRole', userData.role || USER_ROLES.CUSTOMER);
    } else {
        // Локальные данные для демо
        const profileData = JSON.parse(localStorage.getItem('userProfile')) || {
            name: 'Иван Иванов',
            email: 'ivan@example.com',
            address: 'Адрес не указан',
            avatar: 'images/avatar.jpg',
            role: USER_ROLES.CUSTOMER
        };
        
        document.getElementById('profileName').value = profileData.name;
        document.getElementById('profileEmail').value = profileData.email;
        document.getElementById('profileAddress').value = profileData.address;
        document.getElementById('avatarPreview').src = profileData.avatar;
        
        document.querySelector('.profile-name').textContent = profileData.name;
        document.querySelector('.profile-email').textContent = profileData.email;
        document.querySelector('.profile-avatar').src = profileData.avatar;
        
        localStorage.setItem('userRole', profileData.role);
    }
}

// Форма личной информации
function initPersonalForm() {
    const personalForm = document.getElementById('personalForm');
    
    if (personalForm) {
        personalForm.addEventListener('submit', function(e) {
            e.preventDefault();
            
            const name = document.getElementById('profileName').value;
            const email = document.getElementById('profileEmail').value;
            const address = document.getElementById('profileAddress').value;
            
            // Сохраняем в localStorage
            const profileData = {
                name: name,
                email: email,
                address: address,
                avatar: localStorage.getItem('userAvatar') || 'images/avatar.jpg'
            };
            
            localStorage.setItem('userProfile', JSON.stringify(profileData));
            
            // Обновляем сайдбар
            document.querySelector('.profile-name').textContent = name;
            document.querySelector('.profile-email').textContent = email;
            
            showProfileNotification('Профиль успешно обновлен!', 'success');
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
    
    // Обработчик выбора файла
    avatarInput.addEventListener('change', function(e) {
        const file = e.target.files[0];
        if (file) {
            if (file.type.startsWith('image/')) {
                const reader = new FileReader();
                reader.onload = function(e) {
                    const imageUrl = e.target.result;
                    
                    // Обновляем превью
                    avatarPreview.src = imageUrl;
                    document.querySelector('.profile-avatar').src = imageUrl;
                    
                    // Сохраняем в localStorage
                    localStorage.setItem('userAvatar', imageUrl);
                    
                    // Обновляем профиль в localStorage
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
    
    // Удаление аватарки
    removeAvatarBtn.addEventListener('click', function() {
        const defaultAvatar = 'images/avatar.jpg';
        
        avatarPreview.src = defaultAvatar;
        document.querySelector('.profile-avatar').src = defaultAvatar;
        
        // Удаляем из localStorage
        localStorage.removeItem('userAvatar');
        
        // Обновляем профиль в localStorage
        const profileData = JSON.parse(localStorage.getItem('userProfile') || '{}');
        profileData.avatar = defaultAvatar;
        localStorage.setItem('userProfile', JSON.stringify(profileData));
        
        showProfileNotification('Аватарка удалена!', 'success');
    });
}

// Инициализация модальных окон
function initModals() {
    // Инициализация счетчиков символов
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
    
    // Изменение выбора роли
    const requestRole = document.getElementById('requestRole');
    if (requestRole) {
        requestRole.addEventListener('change', function() {
            const roleName = this.options[this.selectedIndex].text;
            document.getElementById('roleName').textContent = roleName.toLowerCase();
        });
    }
    
    // Обработка формы подачи заявки
    const requestForm = document.getElementById('requestForm');
    if (requestForm) {
        requestForm.addEventListener('submit', function(e) {
            e.preventDefault();
            submitRequest();
        });
    }
    
    // Обработка формы снятия роли
    const downgradeForm = document.getElementById('downgradeForm');
    if (downgradeForm) {
        downgradeForm.addEventListener('submit', function(e) {
            e.preventDefault();
            submitDowngradeRequest();
        });
    }
    
    // Закрытие модальных окон по клику вне их
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
    
    // ESC для закрытия модальных окон
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
    
    // Сбросить форму
    form.reset();
    document.getElementById('charCount').textContent = '0';
    
    // Установить роль, если указана
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
    
    // Сбросить форму
    form.reset();
    document.getElementById('downgradeCharCount').textContent = '0';
    
    // Установить текущую роль пользователя
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
        // Формируем параметры запроса
        const params = new URLSearchParams();
        params.append('requestedRole', role);
        params.append('typeAction', TYPE_ACTION.ENHANCE);
        params.append('message', message);
        
        console.log('Отправка заявки с параметрами:', params.toString());
        
        // Отправляем запрос на бэкенд
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
                
                // Обновляем список заявок
                await loadUserRequests();
            } catch (e) {
                showProfileNotification('✅ Заявка успешно отправлена!', 'success');
                closeRequestModal();
                await loadUserRequests();
            }
        } else {
            showProfileNotification(`❌ Ошибка: ${responseText || response.status}`, 'error');
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
        // Для REMOVE запрашиваем роль CUSTOMER (возврат к покупателю)
        const params = new URLSearchParams();
        params.append('requestedRole', USER_ROLES.CUSTOMER);
        params.append('typeAction', TYPE_ACTION.REMOVE);
        params.append('message', message);
        
        console.log('Отправка заявки на снятие:', params.toString());
        
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
            
            // Обновляем список заявок
            await loadUserRequests();
        } else {
            showProfileNotification(`❌ Ошибка: ${responseText || response.status}`, 'error');
        }
    } catch (error) {
        console.error('Ошибка отправки заявки:', error);
        showProfileNotification('❌ Ошибка сети при отправке заявки', 'error');
    }
}

// Загрузить заявки с сервера
async function loadUserRequests() {
    const requestsList = document.getElementById('requestsList');
    if (!requestsList) return;
    
    try {
        // Показываем загрузку
        requestsList.innerHTML = '<div class="loading">⌛ Загрузка заявок...</div>';
        
        // Загружаем заявки с сервера
        const response = await fetch(`${API_BASE_URL}${API_ENDPOINTS.GET_MY_REQUESTS}`, {
            method: 'GET',
            headers: getHeaders('application/json')
        });
        
        if (response.ok) {
            const requests = await response.json();
            
            if (!requests || requests.length === 0) {
                requestsList.innerHTML = `
                    <div class="no-requests">
                        <p>📭 У вас еще нет отправленных заявок</p>
                    </div>
                `;
                return;
            }
            
            // Сортируем по дате (новые сверху)
            requests.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt));
            
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
            
        } else if (response.status === 404) {
            // Эндпоинт не найден или нет заявок
            requestsList.innerHTML = `
                <div class="no-requests">
                    <p>📭 У вас еще нет отправленных заявок</p>
                </div>
            `;
        } else {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
    } catch (error) {
        console.error('Ошибка загрузки заявок:', error);
        loadLocalRequests();
    }
}

// Загрузить локальные заявки (запасной вариант)
function loadLocalRequests() {
    const requestsList = document.getElementById('requestsList');
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
        
        return `
            <div class="request-item">
                <div class="request-info">
                    <h4>${requestTypeText}</h4>
                    <p>${date} | ${request.message ? request.message.substring(0, 50) : 'Без описания'}${request.message && request.message.length > 50 ? '...' : ''}</p>
                </div>
                <span class="request-status status-${request.status.toLowerCase()}">
                    ${statusText}
                </span>
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
        // Пользователь уже продавец или курьер
        requestCards.forEach((card, index) => {
            if (index < 2) { // Первые две карточки - для повышения
                card.style.opacity = '0.6';
                card.style.pointerEvents = 'none';
                
                const button = card.querySelector('button');
                if (button) {
                    button.textContent = 'Роль уже активна';
                    button.disabled = true;
                    button.classList.remove('btn-primary');
                    button.classList.add('btn-secondary');
                }
            } else if (index === 2) { // Третья карточка - для снятия
                // Обновляем текст кнопки
                const button = card.querySelector('button');
                if (button) {
                    button.textContent = `Сняться с роли ${getRoleText(userRole).toLowerCase()}`;
                    button.classList.remove('btn-secondary');
                    button.classList.add('btn-primary');
                }
            }
        });
    } else if (userRole === USER_ROLES.CUSTOMER) {
        // Покупатель - доступны только кнопки повышения
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
        // Админ - все кнопки недоступны
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
    
    // Анимация появления
    setTimeout(() => notification.classList.add('show'), 100);
    
    // Автоматическое скрытие
    setTimeout(() => {
        notification.classList.remove('show');
        setTimeout(() => notification.remove(), 300);
    }, 3000);
}

// Отображение истории заказов
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
                <a href="mainForm.html" class="btn btn-primary" style="display: inline-block; margin-top: 15px;">
                    Перейти к покупкам
                </a>
            </div>
        `;
        return;
    }
    
    // Собираем все товары из всех заказов
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
    
    // Делаем карточки кликабельными
    makeOrderItemsClickable();
}

// Обновление статистики профиля
function updateProfileStats() {
    const statValue = document.querySelector('.stat-value');
    if (!statValue) return;
    
    const orders = window.cartManager ? window.cartManager.getOrdersHistory() : [];
    const totalOrders = orders.reduce((total, order) => total + order.items.length, 0);
    
    statValue.textContent = totalOrders;
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
async function checkUserRequestsStatus() {
    try {
        const response = await fetch(`${API_BASE_URL}${API_ENDPOINTS.GET_MY_REQUESTS}`, {
            method: 'GET',
            headers: getHeaders('application/json')
        });
        
        if (response.ok) {
            const requests = await response.json();
            const pendingRequests = requests.filter(r => r.status === REQUEST_STATUS.PENDING);
            
            if (pendingRequests.length > 0) {
                console.log(`У вас ${pendingRequests.length} активных заявок`);
            }
        }
    } catch (error) {
        console.error('Ошибка проверки заявок:', error);
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