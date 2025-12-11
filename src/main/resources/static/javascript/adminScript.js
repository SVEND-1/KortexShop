// adminScript.js - АДМИН ПАНЕЛЬ С ЗАЯВКАМИ НА РОЛИ

// Конфигурация API
const API_BASE_URL = window.location.origin;
const API_ENDPOINTS = {
    GET_REQUESTS: '/api/admin/role-request',
    GET_REQUEST_BY_ID: '/api/admin/role-request',
    APPROVE_REQUEST: '/api/admin/role-request',
    REJECT_REQUEST: '/api/admin/role-request',
    DOWNGRADE_REQUEST: '/api/admin/role-request'
};

// Константы
const REQUEST_STATUS = {
    PENDING: 'PENDING',
    APPROVED: 'APPROVED',
    REJECTED: 'REJECTED'
};

// Текущее состояние
let currentPage = 0;
const pageSize = 10;
let totalPages = 0;
let totalRequests = 0;
let currentFilters = {
    status: 'ALL',
    actionType: 'ALL',
    role: 'ALL'
};

// ============ ФУНКЦИИ ДЛЯ ПАГИНАЦИИ ============

function updatePagination(totalItems, currentPage, totalPages) {
    const pagination = document.getElementById('pagination');
    if (!pagination) return;

    totalRequests = totalItems;

    let paginationHtml = '';

    // Информация о странице
    const startItem = currentPage * pageSize + 1;
    const endItem = Math.min((currentPage + 1) * pageSize, totalItems);

    // Кнопка "Назад"
    paginationHtml += `
        <button class="pagination-btn ${currentPage === 0 ? 'disabled' : ''}" 
                onclick="loadRequests(${currentPage - 1})"
                ${currentPage === 0 ? 'disabled' : ''}>
            ← Назад
        </button>
    `;

    // Номера страниц (показываем максимум 5 страниц)
    const startPage = Math.max(0, currentPage - 2);
    const endPage = Math.min(totalPages - 1, currentPage + 2);

    for (let i = startPage; i <= endPage; i++) {
        paginationHtml += `
            <button class="pagination-btn ${i === currentPage ? 'active' : ''}" 
                    onclick="loadRequests(${i})">
                ${i + 1}
            </button>
        `;
    }

    // Кнопка "Вперед"
    paginationHtml += `
        <button class="pagination-btn ${currentPage >= totalPages - 1 ? 'disabled' : ''}" 
                onclick="loadRequests(${currentPage + 1})"
                ${currentPage >= totalPages - 1 ? 'disabled' : ''}>
            Вперед →
        </button>
    `;

    // Информация о записях
    paginationHtml += `
        <div class="pagination-info">
            Показано ${startItem}-${endItem} из ${totalItems}
        </div>
    `;

    pagination.innerHTML = paginationHtml;
}

function updateRequestsCount(count) {
    const countElement = document.getElementById('requestsCount');
    if (countElement) {
        countElement.textContent = `Заявок: ${count}`;
    }
}

// ============ ИНИЦИАЛИЗАЦИЯ ============

document.addEventListener('DOMContentLoaded', function() {
    console.log('Админ панель загружена');

    // Настройка фильтров
    setupFilters();

    // Настройка модальных окон
    setupModals();

    // Настройка кнопок
    setupButtons();

    // Загружаем заявки (после того как все функции определены)
    setTimeout(() => loadRequests(), 100);
});

// ============ ЗАГРУЗКА ДАННЫХ ============

async function loadRequests(page = 0) {
    try {
        showLoading(true);
        currentPage = page;

        // Собираем параметры запроса
        const params = new URLSearchParams();

        // Пагинация
        params.append('pageSize', pageSize);
        params.append('pageNumber', page);

        // Фильтры
        if (currentFilters.status !== 'ALL') {
            params.append('status', currentFilters.status);
        }

        if (currentFilters.actionType !== 'ALL') {
            params.append('actionType', currentFilters.actionType);
        }

        if (currentFilters.role !== 'ALL') {
            params.append('role', currentFilters.role);
        }

        // Делаем запрос
        const url = `${API_ENDPOINTS.GET_REQUESTS}?${params.toString()}`;
        console.log('Запрос заявок:', url);

        const response = await fetch(url, {
            method: 'GET',
            headers: {
                'Accept': 'application/json',
                'Content-Type': 'application/json'
            },
            credentials: 'include'
        });

        console.log('Статус ответа:', response.status);

        if (response.status === 401 || response.status === 403) {
            showNotification('❌ Доступ запрещен. Требуется авторизация как администратор.', 'error');
            setTimeout(() => window.location.href = '/login', 2000);
            return;
        }

        if (!response.ok) {
            const errorText = await response.text();
            throw new Error(`Ошибка сервера: ${response.status} - ${errorText}`);
        }

        const responseData = await response.json();
        console.log('Получены данные:', responseData);

        // Обрабатываем данные в зависимости от формата
        let requests = [];
        let totalElements = 0;
        let currentPageNum = 0;
        let totalPagesNum = 1;

        // Проверяем формат ответа
        if (Array.isArray(responseData)) {
            // Ответ - массив
            requests = responseData;
            totalElements = requests.length;
            totalPagesNum = Math.ceil(totalElements / pageSize);
        } else if (responseData && typeof responseData === 'object') {
            // Ответ - объект с пагинацией
            if (responseData.content && Array.isArray(responseData.content)) {
                requests = responseData.content;
                totalElements = responseData.totalElements || requests.length;
                totalPagesNum = responseData.totalPages || Math.ceil(totalElements / pageSize);
                currentPageNum = responseData.page || page;
            } else {
                // Если content нет, но есть массив напрямую
                const keys = Object.keys(responseData);
                if (keys.length > 0 && Array.isArray(responseData[keys[0]])) {
                    requests = responseData[keys[0]];
                    totalElements = requests.length;
                    totalPagesNum = Math.ceil(totalElements / pageSize);
                } else {
                    // Пробуем найти массив в любом свойстве
                    for (const key in responseData) {
                        if (Array.isArray(responseData[key])) {
                            requests = responseData[key];
                            totalElements = requests.length;
                            totalPagesNum = Math.ceil(totalElements / pageSize);
                            break;
                        }
                    }
                }
            }
        }

        console.log('Обработанные данные:', {
            requestsCount: requests.length,
            totalElements,
            currentPageNum,
            totalPagesNum
        });

        // Рендерим таблицу
        renderRequestsTable(requests);
        updatePagination(totalElements, currentPageNum, totalPagesNum);
        updateRequestsCount(totalElements);

    } catch (error) {
        console.error('Ошибка загрузки заявок:', error);
        showNotification(`❌ Ошибка загрузки: ${error.message}`, 'error');
        renderRequestsTable([]);
        updatePagination(0, currentPage, 1);
        updateRequestsCount(0);
    } finally {
        showLoading(false);
    }
}

// ============ ОТОБРАЖЕНИЕ ТАБЛИЦЫ ============

function renderRequestsTable(requests) {
    const tbody = document.getElementById('requestsList');
    if (!tbody) return;

    if (!requests || requests.length === 0) {
        tbody.innerHTML = `
            <tr>
                <td colspan="7" class="loading">
                    <div style="display: flex; flex-direction: column; align-items: center; gap: 10px; padding: 40px;">
                        <div style="font-size: 48px;">📭</div>
                        <h3 style="margin: 0; color: #666;">Заявок не найдено</h3>
                        <p style="margin: 0; color: #999; text-align: center;">
                            Попробуйте изменить фильтры или загрузить заново
                        </p>
                    </div>
                </td>
            </tr>
        `;
        return;
    }

    // Сортируем по дате (новые сверху)
    requests.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt));

    // Рендерим строки
    tbody.innerHTML = requests.map(request => {
        const statusClass = getStatusClass(request.status);
        const statusText = getStatusText(request.status);
        const requestType = getRequestTypeText(request.typeAction, request.requestedRole);
        const requestTypeClass = request.typeAction === 'ENHANCE' ? 'type-upgrade' : 'type-downgrade';
        const userInfo = request.user || {};

        // Получаем ID пользователя (может быть в разных полях)
        const userId = request.userId || userInfo.id || request.id || 'N/A';
        const userName = request.name || userInfo.name || 'Не указано';
        const userEmail = request.email || userInfo.email || 'Не указан';

        return `
            <tr>
                <td class="request-id">#${request.id}</td>
                <td>
                    <div class="user-info">
                        <strong>${userName}</strong>
                        <small>ID: ${userId}</small>
                    </div>
                </td>
                <td>${userEmail}</td>
                <td>
                    <span class="request-type ${requestTypeClass}">
                        ${requestType}
                    </span>
                </td>
                <td>
                    <span class="status-badge ${statusClass}">${statusText}</span>
                </td>
                <td>${formatDate(request.createdAt)}</td>
                <td>
                    <div class="action-buttons">
                        <button class="btn-table btn-table-primary" 
                                onclick="viewRequestDetails(${request.id})"
                                title="Просмотр деталей">
                            👁️ Подробно
                        </button>
                        
                        ${request.status === 'PENDING' ? `
                            ${request.typeAction === 'ENHANCE' ? `
                                <button class="btn-table btn-table-success" 
                                        onclick="approveRequest(${request.id})"
                                        title="Одобрить повышение">
                                    ⬆️ Повысить
                                </button>
                            ` : `
                                <button class="btn-table btn-table-warning" 
                                        onclick="downgradeRequest(${request.id})"
                                        title="Одобрить понижение">
                                    ⬇️ Понизить
                                </button>
                            `}
                            <button class="btn-table btn-table-danger" 
                                    onclick="rejectRequest(${request.id})"
                                    title="Отклонить заявку">
                                ❌ Отклонить
                            </button>
                        ` : ''}
                    </div>
                </td>
            </tr>
        `;
    }).join('');
}

// ============ ДЕТАЛИ ЗАЯВКИ ============

async function viewRequestDetails(requestId) {
    try {
        showLoading(true);

        const response = await fetch(`${API_ENDPOINTS.GET_REQUEST_BY_ID}/${requestId}`, {
            method: 'GET',
            headers: {
                'Accept': 'application/json',
                'Content-Type': 'application/json'
            },
            credentials: 'include'
        });

        if (!response.ok) {
            throw new Error(`Ошибка ${response.status}`);
        }

        const request = await response.json();
        console.log('Детали заявки:', request);

        fillRequestModal(request);
        showModal('requestModal');

    } catch (error) {
        console.error('Ошибка загрузки деталей:', error);
        showNotification(`❌ Не удалось загрузить детали заявки: ${error.message}`, 'error');
    } finally {
        showLoading(false);
    }
}

function fillRequestModal(request) {
    const userInfo = request.user || {};

    // Основная информация
    document.getElementById('modalRequestId').textContent = request.id;
    document.getElementById('modalUserId').textContent = request.userId || userInfo.id || 'N/A';
    document.getElementById('modalUserName').textContent = request.name || userInfo.name || 'Не указано';
    document.getElementById('modalUserEmail').textContent = request.email || userInfo.email || 'Не указан';
    document.getElementById('modalCurrentRole').textContent = getRoleText(userInfo.role);
    document.getElementById('modalRequestedRole').textContent = getRoleText(request.requestedRole);

    // Статус
    const statusElement = document.getElementById('modalStatus');
    statusElement.textContent = getStatusText(request.status);
    statusElement.className = `info-value status-badge status-${request.status.toLowerCase()}`;

    document.getElementById('modalCreatedAt').textContent = formatDate(request.createdAt);
    document.getElementById('modalDescription').textContent = request.message || 'Описание отсутствует';

    // Действия
    const actionsSection = document.getElementById('modalActions');
    const actionsInfo = document.getElementById('actionsInfo');
    const actionsButtons = document.getElementById('actionsButtons');

    if (request.status === 'PENDING') {
        actionsSection.style.display = 'block';

        if (request.typeAction === 'ENHANCE') {
            actionsInfo.innerHTML = `
                <p><strong>⚠️ Эта заявка ожидает рассмотрения</strong></p>
                <p>Пользователь запрашивает <strong>повышение роли</strong>.</p>
                <p><strong>Текущая роль:</strong> ${getRoleText(userInfo.role || 'USER')}</p>
                <p><strong>Запрашиваемая роль:</strong> ${getRoleText(request.requestedRole)}</p>
            `;

            actionsButtons.innerHTML = `
                <button class="btn btn-success" onclick="approveRequest(${request.id})">
                    ⬆️ Одобрить повышение
                </button>
                <button class="btn btn-danger" onclick="rejectRequest(${request.id})">
                    ❌ Отклонить заявку
                </button>
                <button class="btn btn-secondary" onclick="closeModal()">
                    Закрыть
                </button>
            `;
        } else {
            actionsInfo.innerHTML = `
                <p><strong>⚠️ Эта заявка ожидает рассмотрения</strong></p>
                <p>Пользователь запрашивает <strong>снятие с роли</strong>.</p>
                <p><strong>Текущая роль:</strong> ${getRoleText(userInfo.role || 'USER')}</p>
                <p><strong>Станет:</strong> ${getRoleText('USER')}</p>
            `;

            actionsButtons.innerHTML = `
                <button class="btn btn-warning" onclick="downgradeRequest(${request.id})">
                    ⬇️ Одобрить понижение
                </button>
                <button class="btn btn-danger" onclick="rejectRequest(${request.id})">
                    ❌ Отклонить заявку
                </button>
                <button class="btn btn-secondary" onclick="closeModal()">
                    Закрыть
                </button>
            `;
        }
    } else {
        actionsSection.style.display = 'none';
    }
}

// ============ ОПЕРАЦИИ С ЗАЯВКАМИ ============

async function approveRequest(requestId) {
    if (!confirm('Вы уверены, что хотите одобрить повышение роли?')) return;

    try {
        const response = await fetch(`${API_ENDPOINTS.APPROVE_REQUEST}/${requestId}/approve`, {
            method: 'POST',
            headers: {
                'Accept': 'application/json',
                'Content-Type': 'application/json'
            },
            credentials: 'include'
        });

        if (!response.ok) {
            const errorText = await response.text();
            throw new Error(errorText || `Ошибка ${response.status}`);
        }

        showNotification('✅ Заявка одобрена успешно!', 'success');
        closeModal();
        loadRequests(currentPage);

    } catch (error) {
        console.error('Ошибка одобрения:', error);
        showNotification(`❌ Ошибка: ${error.message}`, 'error');
    }
}

async function downgradeRequest(requestId) {
    if (!confirm('Вы уверены, что хотите одобрить понижение роли?')) return;

    try {
        const response = await fetch(`${API_ENDPOINTS.DOWNGRADE_REQUEST}/${requestId}/downgrade`, {
            method: 'POST',
            headers: {
                'Accept': 'application/json',
                'Content-Type': 'application/json'
            },
            credentials: 'include'
        });

        if (!response.ok) {
            const errorText = await response.text();
            throw new Error(errorText || `Ошибка ${response.status}`);
        }

        showNotification('✅ Понижение роли одобрено!', 'success');
        closeModal();
        loadRequests(currentPage);

    } catch (error) {
        console.error('Ошибка понижения:', error);
        showNotification(`❌ Ошибка: ${error.message}`, 'error');
    }
}

async function rejectRequest(requestId) {
    if (!confirm('Вы уверены, что хотите отклонить заявку?')) return;

    try {
        const response = await fetch(`${API_ENDPOINTS.REJECT_REQUEST}/${requestId}/reject`, {
            method: 'POST',
            headers: {
                'Accept': 'application/json',
                'Content-Type': 'application/json'
            },
            credentials: 'include'
        });

        if (!response.ok) {
            const errorText = await response.text();
            throw new Error(errorText || `Ошибка ${response.status}`);
        }

        showNotification('❌ Заявка отклонена', 'warning');
        closeModal();
        loadRequests(currentPage);

    } catch (error) {
        console.error('Ошибка отклонения:', error);
        showNotification(`❌ Ошибка: ${error.message}`, 'error');
    }
}

// ============ ФИЛЬТРЫ ============

function setupFilters() {
    const statusFilter = document.getElementById('statusFilter');
    const requestTypeFilter = document.getElementById('requestTypeFilter');
    const refreshBtn = document.querySelector('.filter-actions .btn-primary');
    const clearBtn = document.querySelector('.filter-actions .btn-secondary');

    if (statusFilter) {
        statusFilter.addEventListener('change', function() {
            currentFilters.status = this.value;
            loadRequests(0);
        });
    }

    if (requestTypeFilter) {
        requestTypeFilter.addEventListener('change', function() {
            // Конвертируем значение фильтра в формат бэкенда
            const filterValue = this.value;
            switch(filterValue) {
                case 'UPGRADE_TO_SELLER':
                    currentFilters.actionType = 'ENHANCE';
                    currentFilters.role = 'SELLER';
                    break;
                case 'UPGRADE_TO_COURIER':
                    currentFilters.actionType = 'ENHANCE';
                    currentFilters.role = 'COURIER';
                    break;
                case 'DOWNGRADE_TO_CUSTOMER':
                    currentFilters.actionType = 'REMOVE';
                    currentFilters.role = 'USER';
                    break;
                default:
                    currentFilters.actionType = 'ALL';
                    currentFilters.role = 'ALL';
            }
            loadRequests(0);
        });
    }

    if (refreshBtn) {
        refreshBtn.addEventListener('click', function() {
            loadRequests(currentPage);
        });
    }

    if (clearBtn) {
        clearBtn.addEventListener('click', function() {
            clearFilters();
        });
    }
}

function clearFilters() {
    currentFilters = {
        status: 'ALL',
        actionType: 'ALL',
        role: 'ALL'
    };

    const statusFilter = document.getElementById('statusFilter');
    const requestTypeFilter = document.getElementById('requestTypeFilter');

    if (statusFilter) statusFilter.value = 'ALL';
    if (requestTypeFilter) requestTypeFilter.value = 'ALL';

    loadRequests(0);
}

// ============ МОДАЛЬНЫЕ ОКНА ============

function setupModals() {
    // Закрытие по клику вне окна
    document.addEventListener('click', function(e) {
        if (e.target.classList.contains('modal')) {
            closeModal();
            closeConfirmModal();
        }
    });

    // Закрытие по Escape
    document.addEventListener('keydown', function(e) {
        if (e.key === 'Escape') {
            closeModal();
            closeConfirmModal();
        }
    });

    // Кнопки закрытия
    document.querySelectorAll('.modal-close').forEach(btn => {
        btn.addEventListener('click', function() {
            const modal = this.closest('.modal');
            if (modal) modal.style.display = 'none';
        });
    });
}

function showModal(modalId) {
    const modal = document.getElementById(modalId);
    if (modal) {
        modal.style.display = 'flex';
    }
}

function closeModal() {
    const modal = document.getElementById('requestModal');
    if (modal) {
        modal.style.display = 'none';
    }
}

function closeConfirmModal() {
    const modal = document.getElementById('confirmModal');
    if (modal) {
        modal.style.display = 'none';
    }
}

// ============ УВЕДОМЛЕНИЯ ============

function showNotification(message, type = 'success') {
    const notification = document.getElementById('notification');
    const notificationText = document.getElementById('notificationText');

    if (!notification || !notificationText) return;

    notificationText.textContent = message;
    notification.className = `notification ${type}`;
    notification.style.display = 'flex';

    // Автоматическое скрытие через 5 секунд
    setTimeout(() => {
        notification.style.display = 'none';
    }, 5000);
}

function hideNotification() {
    const notification = document.getElementById('notification');
    if (notification) {
        notification.style.display = 'none';
    }
}

// ============ ВСПОМОГАТЕЛЬНЫЕ ФУНКЦИИ ============

function setupButtons() {
    // Кнопка обновления списка
    const refreshBtn = document.querySelector('.filter-actions .btn-primary');
    if (refreshBtn) {
        refreshBtn.addEventListener('click', function() {
            loadRequests(currentPage);
        });
    }

    // Кнопка очистки фильтров
    const clearBtn = document.querySelector('.filter-actions .btn-secondary');
    if (clearBtn) {
        clearBtn.addEventListener('click', function() {
            clearFilters();
        });
    }
}

function showLoading(show) {
    const tbody = document.getElementById('requestsList');
    if (!tbody) return;

    if (show) {
        tbody.innerHTML = `
            <tr>
                <td colspan="7" class="loading">
                    ⏳ Загрузка заявок...
                </td>
            </tr>
        `;
    }
}

function getStatusClass(status) {
    switch(status) {
        case 'PENDING': return 'pending';
        case 'APPROVED': return 'approved';
        case 'REJECTED': return 'rejected';
        default: return '';
    }
}

function getStatusText(status) {
    switch(status) {
        case 'PENDING': return 'Ожидает';
        case 'APPROVED': return 'Одобрено';
        case 'REJECTED': return 'Отклонено';
        default: return status;
    }
}

function getRoleText(role) {
    const roleMap = {
        'USER': 'Покупатель',
        'SELLER': 'Продавец',
        'COURIER': 'Курьер',
        'ADMIN': 'Админ'
    };
    return roleMap[role] || role;
}

function getRequestTypeText(typeAction, requestedRole) {
    if (typeAction === 'ENHANCE') {
        if (requestedRole === 'SELLER') return 'Стать продавцом';
        if (requestedRole === 'COURIER') return 'Стать курьером';
        return `Повышение до ${getRoleText(requestedRole)}`;
    } else {
        return 'Снятие с роли';
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

// ============ ДЕБАГ ФУНКЦИИ ============

// Функция для тестирования API
async function testApi() {
    try {
        const response = await fetch('/api/admin/role-request?pageSize=5&pageNumber=0', {
            headers: {
                'Accept': 'application/json'
            }
        });

        const data = await response.json();
        console.log('API Response:', data);

        // Показываем структуру ответа
        alert(JSON.stringify(data, null, 2));

        return data;
    } catch (error) {
        console.error('API Test Error:', error);
        alert(`API Error: ${error.message}`);
    }
}

// ============ ГЛОБАЛЬНЫЕ ФУНКЦИИ ============

window.loadRequests = loadRequests;
window.clearFilters = clearFilters;
window.viewRequestDetails = viewRequestDetails;
window.approveRequest = approveRequest;
window.downgradeRequest = downgradeRequest;
window.rejectRequest = rejectRequest;
window.closeModal = closeModal;
window.closeConfirmModal = closeConfirmModal;
window.hideNotification = hideNotification;
window.testApi = testApi; // Для отладки