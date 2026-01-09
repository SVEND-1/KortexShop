// profileScript.js - ПОЛНАЯ ВЕРСИЯ С ЗАЯВКАМИ НА РОЛИ

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

        // 4. Скрываем историю заказов (временно)
        hideOrdersSection();

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
    GET_ROLE_REQUESTS: '/api/users/role-request'
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

        // Пробуем загрузить из кэша
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

// ============ ОТОБРАЖЕНИЕ ПРОФИЛЯ ============

function renderUserProfile(user) {
    console.log('Рендерим профиль из DTO:', user);

    // Основные поля из UserCartDTO
    const name = user.name || 'Не указано';
    const email = user.email || 'Не указан';
    const address = user.address || 'Адрес не указан';
    const role = user.role || 'USER';
    const cartId = user.cartId || null;

    // Заполняем форму
    document.getElementById('profileName').value = name;
    document.getElementById('profileEmail').value = email;
    document.getElementById('profileAddress').value = address;

    // Заголовок профиля
    const profileNameElement = document.querySelector('.profile-name');
    const profileEmailElement = document.querySelector('.profile-email');

    if (profileNameElement) {
        profileNameElement.textContent = name;
    }

    if (profileEmailElement) {
        profileEmailElement.textContent = email;
    }

    // Отображаем роль
    updateRoleDisplay(role);

    // Обновляем кнопки ролей
    updateRoleButtons(role);

    // Сохраняем роль для использования в формах
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

    // Блокируем форму
    const inputs = document.querySelectorAll('#personalForm input, #personalForm textarea');
    inputs.forEach(input => input.disabled = true);

    const submitBtn = document.querySelector('#personalForm .btn-primary');
    if (submitBtn) submitBtn.disabled = true;
}

function updateRoleDisplay(role) {
    const roleInfo = ROLE_MAP[role] || ROLE_MAP.USER;

    // Обновляем отображение роли
    const roleElements = document.querySelectorAll('.user-role-display');
    roleElements.forEach(el => {
        el.innerHTML = `${roleInfo.icon} ${roleInfo.text}`;
    });

    // Обновляем кнопки в боковой панели на основе реальной роли
    updateSidebarRoleButtons(role);
}

function updateSidebarRoleButtons(userRole) {
    const roleButtonsContainer = document.querySelector('.role-buttons');
    if (!roleButtonsContainer) return;

    // Очищаем контейнер
    roleButtonsContainer.innerHTML = '';

    // Добавляем кнопки в зависимости от роли пользователя
    if (userRole === 'USER') {
        // Пользователь может подать заявку на продавца или курьера
        roleButtonsContainer.innerHTML = `
            <a href="/seller" class="btn btn-seller" style="width: 100%; margin-bottom: 10px;">
                🏪 Для продавца
            </a>
            <a href="/courier" class="btn btn-courier" style="width: 100%; margin-bottom: 10px;">
                🚚 Для курьера
            </a>
            <a href="/admin" class="btn btn-admin" style="width: 100%; display: none;">
                ⚙️ Для админа
            </a>
        `;
    } else if (userRole === 'SELLER') {
        // Продавец видит кнопку для своей панели
        roleButtonsContainer.innerHTML = `
            <a href="/seller" class="btn btn-seller" style="width: 100%; margin-bottom: 10px;">
                🏪 Панель продавца
            </a>
            <a href="/courier" class="btn btn-courier" style="width: 100%; margin-bottom: 10px; opacity: 0.6; cursor: not-allowed;" onclick="return false;">
                🚚 Для курьера
            </a>
            <a href="/admin" class="btn btn-admin" style="width: 100%; display: none;">
                ⚙️ Для админа
            </a>
        `;
    } else if (userRole === 'COURIER') {
        // Курьер видит кнопку для своей панели
        roleButtonsContainer.innerHTML = `
            <a href="/seller" class="btn btn-seller" style="width: 100%; margin-bottom: 10px; opacity: 0.6; cursor: not-allowed;" onclick="return false;">
                🏪 Для продавца
            </a>
            <a href="/courier" class="btn btn-courier" style="width: 100%; margin-bottom: 10px;">
                🚚 Панель курьера
            </a>
            <a href="/admin" class="btn btn-admin" style="width: 100%; display: none;">
                ⚙️ Для админа
            </a>
        `;
    } else if (userRole === 'ADMIN') {
        // Админ видит кнопку для админ панели
        roleButtonsContainer.innerHTML = `
            <a href="/seller" class="btn btn-seller" style="width: 100%; margin-bottom: 10px;">
                🏪 Панель продавца
            </a>
            <a href="/courier" class="btn btn-courier" style="width: 100%; margin-bottom: 10px;">
                🚚 Панель курьера
            </a>
            <a href="/admin" class="btn btn-admin" style="width: 100%;">
                ⚙️ Панель администратора
            </a>
        `;
    }
}

// ============ ОБНОВЛЕНИЕ КНОПОК ЗАЯВОК НА РОЛИ ============

function updateRoleButtons(userRole) {
    const requestCards = document.querySelectorAll('.request-card');
    if (!requestCards || requestCards.length < 3) return;

    const sellerCard = requestCards[0];
    const courierCard = requestCards[1];
    const downgradeCard = requestCards[2];

    // Сбрасываем все карточки
    [sellerCard, courierCard, downgradeCard].forEach(card => {
        card.style.opacity = '1';
        const button = card.querySelector('button');
        if (button) {
            button.disabled = false;
        }
    });

    // Настраиваем в зависимости от роли
    if (userRole === 'USER') {
        // Пользователь может подать заявку на продавца или курьера
        sellerCard.querySelector('button').textContent = '📝 Стать продавцом';
        courierCard.querySelector('button').textContent = '📝 Стать курьером';
        sellerCard.style.opacity = '1';
        courierCard.style.opacity = '1';
        downgradeCard.style.opacity = '0.6';
        downgradeCard.querySelector('button').disabled = true;
        downgradeCard.querySelector('button').textContent = '📝 Сняться с роли';

    } else if (userRole === 'SELLER') {
        // Продавец не может подать заявку на продавца
        sellerCard.style.opacity = '0.6';
        sellerCard.querySelector('button').disabled = true;
        sellerCard.querySelector('button').textContent = '✅ Вы уже продавец';

        // Может подать заявку на курьера
        courierCard.querySelector('button').textContent = '📝 Стать курьером';
        courierCard.style.opacity = '1';

        // Может подать заявку на снятие с роли
        downgradeCard.style.opacity = '1';
        downgradeCard.querySelector('button').disabled = false;
        downgradeCard.querySelector('button').textContent = '📝 Сняться с роли продавца';

    } else if (userRole === 'COURIER') {
        // Курьер не может подать заявку на курьера
        courierCard.style.opacity = '0.6';
        courierCard.querySelector('button').disabled = true;
        courierCard.querySelector('button').textContent = '✅ Вы уже курьер';

        // Может подать заявку на продавца
        sellerCard.querySelector('button').textContent = '📝 Стать продавцом';
        sellerCard.style.opacity = '1';

        // Может подать заявку на снятие с роли
        downgradeCard.style.opacity = '1';
        downgradeCard.querySelector('button').disabled = false;
        downgradeCard.querySelector('button').textContent = '📝 Сняться с роли курьера';

    } else if (userRole === 'ADMIN') {
        // Админ не может подавать заявки на смену роли
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

        // Обновляем адрес через API
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
                console.log('Адрес обновлен на сервере');
            }
        } catch (serverError) {
            console.warn('Не удалось обновить на сервере:', serverError);
        }

        // Обновляем локальные данные
        const currentUser = loadProfileFromCache() || {};
        const updatedProfile = {
            ...currentUser,
            name: name,
            address: address,
            updatedAt: new Date().toISOString()
        };

        saveProfileToCache(updatedProfile);
        document.querySelector('.profile-name').textContent = name;

        // Показываем уведомление
        if (serverUpdated) {
            showProfileNotification('✅ Профиль успешно обновлен!', 'success');
        } else {
            showProfileNotification('⚠️ Изменения сохранены локально', 'warning');
        }

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

// ============ ЗАЯВКИ НА РОЛИ ============

async function loadUserRoleRequests() {
    try {
        console.log('Загрузка заявок на роли...');

        const response = await fetch(API_ENDPOINTS.GET_ROLE_REQUESTS, {
            method: 'GET',
            headers: { 'Accept': 'application/json' },
            credentials: 'include'
        });

        if (response.ok) {
            const requests = await response.json();
            console.log('Получены заявки:', requests);
            renderRoleRequests(requests);
        } else {
            console.warn('Не удалось загрузить заявки:', response.status);
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
        const roleText = getRoleText(request.requestedRole);
        const actionText = request.typeAction === 'ENHANCE' ? 'Повышение до' : 'Снятие роли';

        requestsHtml += `
            <div class="request-item status-${statusInfo.class}">
                <div class="request-header">
                    <span class="request-action">${actionText}</span>
                    <span class="request-role">${roleText}</span>
                    <span class="request-date">${date}</span>
                </div>
                <div class="request-body">
                    <p class="request-message">${request.message || 'Без описания'}</p>
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
    if (!requestsList) return;

    requestsList.innerHTML = `
        <div class="no-requests">
            <div class="no-requests-icon">📭</div>
            <p>У вас еще нет отправленных заявок</p>
            <p class="no-requests-hint">Отправьте заявку на изменение роли выше</p>
        </div>
    `;
}

async function submitRoleRequest(type, requestedRole, message) {
    try {
        const params = new URLSearchParams();
        params.append('requestedRole', requestedRole);
        params.append('typeAction', type);
        params.append('message', message);

        console.log('Отправка заявки:', { type, requestedRole, message });

// Вместо URLSearchParams используем JSON
        const response = await fetch(API_ENDPOINTS.CREATE_ROLE_REQUEST, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',  // ✅ Меняем на JSON
                'Accept': 'application/json'
            },
            body: JSON.stringify({                    // ✅ Отправляем JSON
                requestedRole: requestedRole,
                typeAction: type,
                message: message
            }),
            credentials: 'include'
        });
        if (response.ok) {
            const result = await response.json();
            console.log('Заявка создана:', result);

            showProfileNotification('✅ Заявка успешно отправлена!', 'success');

            // Обновляем список заявок
            await loadUserRoleRequests();

            return result;
        } else {
            const errorText = await response.text();
            throw new Error(errorText || 'Ошибка сервера');
        }

    } catch (error) {
        console.error('Ошибка отправки заявки:', error);
        throw error;
    }
}

// ============ ИНИЦИАЛИЗАЦИЯ МОДАЛЬНЫХ ОКОН ============

function initModals() {
    // Счетчики символов
    const textAreas = document.querySelectorAll('textarea[maxlength]');
    textAreas.forEach(textarea => {
        textarea.addEventListener('input', function() {
            const counter = this.parentNode.querySelector('.char-counter span');
            if (counter) {
                counter.textContent = this.value.length;
            }
        });
    });

    // Обработка формы повышения роли
    const requestForm = document.getElementById('requestForm');
    if (requestForm) {
        requestForm.addEventListener('submit', async function(e) {
            e.preventDefault();
            await handleEnhanceRequest();
        });
    }

    // Обработка формы снятия роли
    const downgradeForm = document.getElementById('downgradeForm');
    if (downgradeForm) {
        downgradeForm.addEventListener('submit', async function(e) {
            e.preventDefault();
            await handleDowngradeRequest();
        });
    }

    // Закрытие модалок
    const modals = document.querySelectorAll('.modal');
    modals.forEach(modal => {
        modal.addEventListener('click', function(e) {
            if (e.target === this || e.target.classList.contains('modal-close')) {
                this.style.display = 'none';
                this.querySelector('form')?.reset();
            }
        });
    });

    // Закрытие по Escape
    document.addEventListener('keydown', function(e) {
        if (e.key === 'Escape') {
            modals.forEach(modal => {
                modal.style.display = 'none';
                modal.querySelector('form')?.reset();
            });
        }
    });

    // Обновление текста при выборе роли
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

    if (!role) {
        showProfileNotification('Выберите роль', 'error');
        return;
    }

    if (message.length < 20) {
        showProfileNotification('Опишите причину подробнее (минимум 20 символов)', 'error');
        return;
    }

    try {
        const submitBtn = requestForm.querySelector('button[type="submit"]');
        const originalText = submitBtn.innerHTML;
        submitBtn.disabled = true;
        submitBtn.innerHTML = 'Отправка...';

        await submitRoleRequest('ENHANCE', role, message);

        closeRequestModal();

    } catch (error) {
        console.error('Ошибка отправки заявки:', error);
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

    if (!currentRole) {
        showProfileNotification('Выберите текущую роль', 'error');
        return;
    }

    if (message.length < 20) {
        showProfileNotification('Опишите причину подробнее (минимум 20 символов)', 'error');
        return;
    }

    try {
        const submitBtn = downgradeForm.querySelector('button[type="submit"]');
        const originalText = submitBtn.innerHTML;
        submitBtn.disabled = true;
        submitBtn.innerHTML = 'Отправка...';

        // При downgrade requestedRole = USER
        await submitRoleRequest('REMOVE', 'USER', message);

        closeDowngradeModal();

    } catch (error) {
        console.error('Ошибка отправки заявки:', error);
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

function hideOrdersSection() {
    const ordersSection = document.querySelector('.orders-history');
    if (ordersSection) {
        ordersSection.style.display = 'none';
    }
}

function getRoleText(role) {
    return ROLE_MAP[role]?.text || role;
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

// ============ КЭШИРОВАНИЕ ============

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

            // Проверяем свежесть кэша (24 часа)
            const cachedAt = new Date(data.cachedAt);
            const now = new Date();
            const hoursDiff = (now - cachedAt) / (1000 * 60 * 60);

            if (hoursDiff < 24) {
                return data;
            }
        }
    } catch (e) {
        console.warn('Не удалось загрузить из кэша:', e);
    }
    return null;
}

// ============ UI КОМПОНЕНТЫ ============

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

    const icon = type === 'success' ? '✅' :
        type === 'warning' ? '⚠️' : '❌';

    const bgColor = type === 'success' ? '#28a745' :
        type === 'warning' ? '#ffc107' : '#dc3545';

    const textColor = type === 'warning' ? '#212529' : 'white';

    notification.innerHTML = `
        <div class="notification-content">
            <span class="notification-icon">${icon}</span>
            <span class="notification-text">${message}</span>
        </div>
    `;

    notification.style.cssText = `
        position: fixed;
        top: 20px;
        right: 20px;
        background: ${bgColor};
        color: ${textColor};
        padding: 15px 20px;
        border-radius: 8px;
        box-shadow: 0 4px 12px rgba(0,0,0,0.15);
        z-index: 9999;
        animation: slideIn 0.3s ease;
    `;

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
        modal.querySelector('form').reset();
        document.getElementById('charCount').textContent = '0';
    }
};

window.openDowngradeModal = function() {
    const modal = document.getElementById('downgradeModal');
    if (!modal) return;

    // Автоматически заполняем текущую роль пользователя
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
        modal.querySelector('form').reset();
        document.getElementById('downgradeCharCount').textContent = '0';

        // Разблокируем select
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
    
    .user-role-display {
        display: inline-flex;
        align-items: center;
        gap: 5px;
        padding: 4px 8px;
        background: #f0f0f0;
        border-radius: 4px;
        font-size: 12px;
        margin-left: 10px;
    }
    
    .request-item {
        border: 1px solid #e0e0e0;
        border-radius: 8px;
        padding: 15px;
        margin-bottom: 15px;
        background: white;
    }
    
    .request-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 10px;
    }
    
    .request-action {
        font-weight: bold;
    }
    
    .request-role {
        background: #667eea;
        color: white;
        padding: 2px 8px;
        border-radius: 4px;
        font-size: 12px;
    }
    
    .request-date {
        color: #666;
        font-size: 12px;
    }
    
    .request-message {
        color: #333;
        margin-bottom: 10px;
        line-height: 1.5;
    }
    
    .request-footer {
        display: flex;
        justify-content: space-between;
        align-items: center;
        font-size: 12px;
    }
    
    .request-status {
        padding: 2px 8px;
        border-radius: 4px;
        font-weight: bold;
    }
    
    .status-pending {
        background: #fff3cd;
        color: #856404;
    }
    
    .status-approved {
        background: #d4edda;
        color: #155724;
    }
    
    .status-rejected {
        background: #f8d7da;
        color: #721c24;
    }
    
    .no-requests {
        text-align: center;
        padding: 40px 20px;
        color: #666;
    }
    
    .no-requests-icon {
        font-size: 48px;
        margin-bottom: 15px;
    }
    
    .no-requests-hint {
        font-size: 14px;
        margin-top: 10px;
        color: #999;
    }
    
    input:disabled, textarea:disabled {
        background-color: #f8f9fa;
        cursor: not-allowed;
    }
`;
document.head.appendChild(style);