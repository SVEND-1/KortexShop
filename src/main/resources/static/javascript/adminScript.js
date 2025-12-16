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

// ============ ИНИЦИАЛИЗАЦИЯ ============

document.addEventListener('DOMContentLoaded', function() {
    console.log('Админ панель загружена');

    // Ждем немного, чтобы страница точно загрузилась
    setTimeout(initializeAdminPanel, 100);
});

function initializeAdminPanel() {
    console.log('Инициализация админ-панели...');

    // Проверяем, что все необходимые элементы существуют
    if (!document.getElementById('requestsList')) {
        console.error('Элемент requestsList не найден! Проверьте HTML структуру');
        showNotification('❌ Ошибка загрузки панели: не найдена таблица заявок', 'error');
        return;
    }

    // Настройка фильтров
    setupFilters();

    // Настройка модальных окон
    setupModals();

    // Настройка кнопок
    setupButtons();

    // Загружаем заявки
    loadRequests();
}

// ============ ЗАГРУЗКА ДАННЫХ ============

async function loadRequests(page = 0) {
    try {
        console.log(`Загрузка заявок, страница: ${page}`);
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
            params.append('typeAction', currentFilters.actionType);
        }

        if (currentFilters.role !== 'ALL') {
            params.append('requestedRole', currentFilters.role);
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
            console.error('Ошибка сервера:', errorText);
            throw new Error(`Ошибка сервера: ${response.status} - ${errorText}`);
        }

        const responseData = await response.json();
        console.log('Получены данные от API:', responseData);

        // ВАЖНО: Анализируем структуру ответа
        let requests = [];
        let totalElements = 0;
        let currentPageNum = 0;
        let totalPagesNum = 1;

        // Если ответ - массив (прямой список заявок)
        if (Array.isArray(responseData)) {
            requests = responseData;
            totalElements = requests.length;
            totalPagesNum = Math.ceil(totalElements / pageSize);
        }
        // Если ответ - объект с пагинацией (стандартный Spring Page)
        else if (responseData && typeof responseData === 'object') {
            console.log('Ответ - объект. Ищем данные в:', Object.keys(responseData));

            // Проверяем различные возможные имена полей
            if (responseData.content && Array.isArray(responseData.content)) {
                requests = responseData.content;
                totalElements = responseData.totalElements || responseData.total || requests.length;
                totalPagesNum = responseData.totalPages || Math.ceil(totalElements / pageSize);
                currentPageNum = responseData.number || responseData.page || page;
            }
            // Проверяем другие возможные имена полей
            else if (responseData.requests && Array.isArray(responseData.requests)) {
                requests = responseData.requests;
                totalElements = responseData.total || requests.length;
                totalPagesNum = Math.ceil(totalElements / pageSize);
            }
            else if (responseData.data && Array.isArray(responseData.data)) {
                requests = responseData.data;
                totalElements = responseData.total || requests.length;
                totalPagesNum = Math.ceil(totalElements / pageSize);
            }
            else if (responseData.items && Array.isArray(responseData.items)) {
                requests = responseData.items;
                totalElements = responseData.total || requests.length;
                totalPagesNum = Math.ceil(totalElements / pageSize);
            }
            else {
                // Ищем первый массив в объекте
                for (const key in responseData) {
                    if (Array.isArray(responseData[key])) {
                        requests = responseData[key];
                        totalElements = requests.length;
                        totalPagesNum = Math.ceil(totalElements / pageSize);
                        console.log(`Найден массив в поле "${key}"`);
                        break;
                    }
                }
            }
        }

        console.log('Обработанные данные:', {
            requestsCount: requests.length,
            totalElements,
            currentPageNum,
            totalPagesNum,
            firstRequest: requests[0] || 'Нет заявок'
        });

        // Рендерим таблицу
        renderRequestsTable(requests);

        // Обновляем пагинацию
        if (totalElements > 0) {
            updatePagination(totalElements, currentPageNum, totalPagesNum);
        }

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
    console.log('renderRequestsTable вызвана, количество заявок:', requests?.length);

    const tbody = document.getElementById('requestsList');
    if (!tbody) {
        console.error('Элемент tbody (requestsList) не найден!');
        return;
    }

    if (!requests || requests.length === 0) {
        console.log('Нет заявок для отображения');
        tbody.innerHTML = `
            <tr>
                <td colspan="7" class="loading">
                    <div style="display: flex; flex-direction: column; align-items: center; gap: 10px; padding: 40px;">
                        <div style="font-size: 48px;">📭</div>
                        <h3 style="margin: 0; color: #666;">Заявок не найдено</h3>
                        <p style="margin: 0; color: #999; text-align: center;">
                            Попробуйте изменить фильтры или загрузить заново
                        </p>
                        <button class="btn btn-primary" onclick="loadRequests(0)" style="margin-top: 20px;">
                            Обновить список
                        </button>
                    </div>
                </td>
            </tr>
        `;
        return;
    }

    // Проверяем структуру первой заявки
    const firstRequest = requests[0];
    console.log('Структура первой заявки:', firstRequest);
    console.log('ID:', firstRequest.id, 'Тип ID:', typeof firstRequest.id);
    console.log('Поля заявки:', Object.keys(firstRequest));

    // Рендерим строки с безопасным доступом к данным
    tbody.innerHTML = requests.map(request => {
        try {
            // Безопасное получение данных
            const requestId = request.id || request.requestId || 'N/A';
            const status = request.status || 'PENDING';
            const typeAction = request.typeAction || request.actionType || 'ENHANCE';
            const requestedRole = request.requestedRole || request.role || 'USER';

            // Получаем данные пользователя (может быть вложенным объектом)
            let userInfo = {};
            if (request.user && typeof request.user === 'object') {
                userInfo = request.user;
            }

            const userId = request.userId || userInfo.id || 'N/A';
            const userName = request.name || userInfo.name || userInfo.username || 'Не указано';
            const userEmail = request.email || userInfo.email || 'Не указан';
            const currentRole = userInfo.role || 'USER';

            const message = request.message || 'Без описания';
            const createdAt = request.createdAt || request.createAt || request.created_date || 'Не указана';

            const statusClass = getStatusClass(status);
            const statusText = getStatusText(status);
            const requestType = getRequestTypeText(typeAction, requestedRole);
            const requestTypeClass = typeAction === 'ENHANCE' ? 'type-upgrade' : 'type-downgrade';

            const formattedDate = formatDate(createdAt);

            return `
                <tr>
                    <td class="request-id">#${requestId}</td>
                    <td>
                        <div class="user-info">
                            <strong>${escapeHtml(userName)}</strong>
                            <small>ID: ${userId}</small>
                        </div>
                    </td>
                    <td>${escapeHtml(userEmail)}</td>
                    <td>
                        <span class="request-type ${requestTypeClass}">
                            ${escapeHtml(requestType)}
                        </span>
                        <br>
                        <small>Текущая роль: ${getRoleText(currentRole)}</small>
                    </td>
                    <td>
                        <span class="status-badge ${statusClass}">${statusText}</span>
                    </td>
                    <td>${formattedDate}</td>
                    <td>
                        <div class="action-buttons">
                            <button class="btn-table btn-table-primary" 
                                    onclick="viewRequestDetails(${requestId})"
                                    title="Просмотр деталей">
                                👁️ Подробно
                            </button>
                            
                            ${status === 'PENDING' ? `
                                ${typeAction === 'ENHANCE' ? `
                                    <button class="btn-table btn-table-success" 
                                            onclick="approveRequest(${requestId})"
                                            title="Одобрить повышение">
                                        ⬆️ Повысить
                                    </button>
                                ` : `
                                    <button class="btn-table btn-table-warning" 
                                            onclick="downgradeRequest(${requestId})"
                                            title="Одобрить понижение">
                                        ⬇️ Понизить
                                    </button>
                                `}
                                <button class="btn-table btn-table-danger" 
                                        onclick="rejectRequest(${requestId})"
                                        title="Отклонить заявку">
                                    ❌ Отклонить
                                </button>
                            ` : ''}
                        </div>
                    </td>
                </tr>
            `;
        } catch (error) {
            console.error('Ошибка рендеринга строки:', error, request);
            return `<tr><td colspan="7" style="color: red;">Ошибка отображения данных</td></tr>`;
        }
    }).join('');
}

// ============ ДЕТАЛИ ЗАЯВКИ ============

async function viewRequestDetails(requestId) {
    console.log('viewRequestDetails вызвана с ID:', requestId);

    try {
        showLoading(true);

        // Проверяем ID
        if (!requestId || requestId === 'undefined') {
            throw new Error('Неверный идентификатор заявки');
        }

        const url = `${API_ENDPOINTS.GET_REQUEST_BY_ID}/${requestId}`;
        console.log('Запрос деталей заявки:', url);

        const response = await fetch(url, {
            method: 'GET',
            headers: {
                'Accept': 'application/json',
                'Content-Type': 'application/json'
            },
            credentials: 'include'
        });

        console.log('Статус ответа деталей:', response.status);

        if (!response.ok) {
            const errorText = await response.text();
            console.error('Ошибка деталей:', errorText);
            throw new Error(`Ошибка ${response.status}: ${errorText}`);
        }

        const request = await response.json();
        console.log('Детали заявки получены:', request);

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
    try {
        // Безопасное получение данных
        const requestId = request.id || request.requestId || 'N/A';
        const userId = request.userId || (request.user && request.user.id) || 'N/A';
        const userName = request.name || (request.user && request.user.name) || 'Не указано';
        const userEmail = request.email || (request.user && request.user.email) || 'Не указан';
        const currentRole = (request.user && request.user.role) || 'USER';
        const requestedRole = request.requestedRole || request.role || 'USER';
        const status = request.status || 'PENDING';
        const typeAction = request.typeAction || request.actionType || 'ENHANCE';
        const message = request.message || 'Описание отсутствует';
        const createdAt = request.createdAt || request.createAt || 'Не указана';

        // Основная информация
        document.getElementById('modalRequestId').textContent = requestId;
        document.getElementById('modalUserId').textContent = userId;
        document.getElementById('modalUserName').textContent = userName;
        document.getElementById('modalUserEmail').textContent = userEmail;
        document.getElementById('modalCurrentRole').textContent = getRoleText(currentRole);
        document.getElementById('modalRequestedRole').textContent = getRoleText(requestedRole);

        // Статус
        const statusElement = document.getElementById('modalStatus');
        statusElement.textContent = getStatusText(status);
        statusElement.className = `info-value status-badge status-${status.toLowerCase()}`;

        document.getElementById('modalCreatedAt').textContent = formatDate(createdAt);
        document.getElementById('modalDescription').textContent = message;

        // Действия
        const actionsSection = document.getElementById('modalActions');
        const actionsInfo = document.getElementById('actionsInfo');
        const actionsButtons = document.getElementById('actionsButtons');

        if (status === 'PENDING') {
            actionsSection.style.display = 'block';

            if (typeAction === 'ENHANCE') {
                actionsInfo.innerHTML = `
                    <p><strong>⚠️ Эта заявка ожидает рассмотрения</strong></p>
                    <p>Пользователь запрашивает <strong>повышение роли</strong>.</p>
                    <p><strong>Текущая роль:</strong> ${getRoleText(currentRole)}</p>
                    <p><strong>Запрашиваемая роль:</strong> ${getRoleText(requestedRole)}</p>
                `;

                actionsButtons.innerHTML = `
                    <button class="btn btn-success" onclick="approveRequest(${requestId})">
                        ⬆️ Одобрить повышение
                    </button>
                    <button class="btn btn-danger" onclick="rejectRequest(${requestId})">
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
                    <p><strong>Текущая роль:</strong> ${getRoleText(currentRole)}</p>
                    <p><strong>Станет:</strong> ${getRoleText('USER')}</p>
                `;

                actionsButtons.innerHTML = `
                    <button class="btn btn-warning" onclick="downgradeRequest(${requestId})">
                        ⬇️ Одобрить понижение
                    </button>
                    <button class="btn btn-danger" onclick="rejectRequest(${requestId})">
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
    } catch (error) {
        console.error('Ошибка заполнения модального окна:', error);
        showNotification('❌ Ошибка отображения деталей заявки', 'error');
    }
}

// ============ ОПЕРАЦИИ С ЗАЯВКАМИ ============

async function approveRequest(requestId) {
    console.log('approveRequest вызвана с параметром:', requestId, 'Тип:', typeof requestId);

    // Проверяем ID
    if (!requestId || requestId === 'undefined' || requestId === 'null' || requestId === 'N/A') {
        console.error('ERROR: requestId is invalid!', requestId);
        showNotification('❌ Ошибка: неверный идентификатор заявки', 'error');
        return;
    }

    // Преобразуем в число для безопасности
    const id = Number(requestId);
    if (isNaN(id) || id <= 0) {
        console.error('ERROR: requestId is not a valid number!', requestId);
        showNotification('❌ Ошибка: некорректный идентификатор заявки', 'error');
        return;
    }

    if (!confirm('Вы уверены, что хотите одобрить повышение роли?')) return;

    try {
        const url = `${API_ENDPOINTS.APPROVE_REQUEST}/${id}/approve`;
        console.log('Отправка запроса на одобрение:', url);

        const response = await fetch(url, {
            method: 'POST',
            headers: {
                'Accept': 'application/json',
                'Content-Type': 'application/json'
            },
            credentials: 'include'
        });

        console.log('Ответ на одобрение:', response.status);

        if (!response.ok) {
            let errorMessage = `Ошибка ${response.status}`;
            try {
                const errorData = await response.json();
                errorMessage = errorData.error || errorData.message || errorMessage;
            } catch (e) {
                // Не удалось распарсить JSON
            }
            throw new Error(errorMessage);
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
    console.log('downgradeRequest вызвана с параметром:', requestId, 'Тип:', typeof requestId);

    if (!requestId || requestId === 'undefined' || requestId === 'null' || requestId === 'N/A') {
        console.error('ERROR: requestId is invalid!', requestId);
        showNotification('❌ Ошибка: неверный идентификатор заявки', 'error');
        return;
    }

    const id = Number(requestId);
    if (isNaN(id) || id <= 0) {
        console.error('ERROR: requestId is not a valid number!', requestId);
        showNotification('❌ Ошибка: некорректный идентификатор заявки', 'error');
        return;
    }

    if (!confirm('Вы уверены, что хотите одобрить понижение роли?')) return;

    try {
        const url = `${API_ENDPOINTS.DOWNGRADE_REQUEST}/${id}/downgrade`;
        console.log('Отправка запроса на понижение:', url);

        const response = await fetch(url, {
            method: 'POST',
            headers: {
                'Accept': 'application/json',
                'Content-Type': 'application/json'
            },
            credentials: 'include'
        });

        console.log('Ответ на понижение:', response.status);

        if (!response.ok) {
            let errorMessage = `Ошибка ${response.status}`;
            try {
                const errorData = await response.json();
                errorMessage = errorData.error || errorData.message || errorMessage;
            } catch (e) {}
            throw new Error(errorMessage);
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
    console.log('rejectRequest вызвана с параметром:', requestId, 'Тип:', typeof requestId);

    if (!requestId || requestId === 'undefined' || requestId === 'null' || requestId === 'N/A') {
        console.error('ERROR: requestId is invalid!', requestId);
        showNotification('❌ Ошибка: неверный идентификатор заявки', 'error');
        return;
    }

    const id = Number(requestId);
    if (isNaN(id) || id <= 0) {
        console.error('ERROR: requestId is not a valid number!', requestId);
        showNotification('❌ Ошибка: некорректный идентификатор заявки', 'error');
        return;
    }

    if (!confirm('Вы уверены, что хотите отклонить заявку?')) return;

    try {
        const url = `${API_ENDPOINTS.REJECT_REQUEST}/${id}/reject`;
        console.log('Отправка запроса на отклонение:', url);

        const response = await fetch(url, {
            method: 'POST',
            headers: {
                'Accept': 'application/json',
                'Content-Type': 'application/json'
            },
            credentials: 'include'
        });

        console.log('Ответ на отклонение:', response.status);

        if (!response.ok) {
            let errorMessage = `Ошибка ${response.status}`;
            try {
                const errorData = await response.json();
                errorMessage = errorData.error || errorData.message || errorMessage;
            } catch (e) {}
            throw new Error(errorMessage);
        }

        showNotification('❌ Заявка отклонена', 'warning');
        closeModal();
        loadRequests(currentPage);

    } catch (error) {
        console.error('Ошибка отклонения:', error);
        showNotification(`❌ Ошибка: ${error.message}`, 'error');
    }
}

// ============ ВСПОМОГАТЕЛЬНЫЕ ФУНКЦИИ ============

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

function setupModals() {
    document.addEventListener('click', function(e) {
        if (e.target.classList.contains('modal')) {
            closeModal();
            closeConfirmModal();
        }
    });

    document.addEventListener('keydown', function(e) {
        if (e.key === 'Escape') {
            closeModal();
            closeConfirmModal();
        }
    });

    document.querySelectorAll('.modal-close').forEach(btn => {
        btn.addEventListener('click', function() {
            const modal = this.closest('.modal');
            if (modal) modal.style.display = 'none';
        });
    });
}

function setupButtons() {
    const refreshBtn = document.querySelector('.filter-actions .btn-primary');
    if (refreshBtn) {
        refreshBtn.addEventListener('click', function() {
            loadRequests(currentPage);
        });
    }

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
                    <div style="display: flex; flex-direction: column; align-items: center; gap: 10px; padding: 40px;">
                        <div class="spinner" style="width: 40px; height: 40px; border: 4px solid #f3f3f3; border-top: 4px solid #3498db; border-radius: 50%; animation: spin 1s linear infinite;"></div>
                        <p style="margin: 0; color: #666;">Загрузка заявок...</p>
                    </div>
                    <style>
                        @keyframes spin {
                            0% { transform: rotate(0deg); }
                            100% { transform: rotate(360deg); }
                        }
                    </style>
                </td>
            </tr>
        `;
    }
}

function updatePagination(totalItems, currentPage, totalPages) {
    const pagination = document.getElementById('pagination');
    if (!pagination) {
        console.warn('Элемент пагинации не найден');
        return;
    }

    if (totalItems <= pageSize) {
        pagination.innerHTML = `
            <div class="pagination-info">
                Всего заявок: ${totalItems}
            </div>
        `;
        return;
    }

    let paginationHtml = '';

    const startItem = currentPage * pageSize + 1;
    const endItem = Math.min((currentPage + 1) * pageSize, totalItems);

    paginationHtml += `
        <button class="pagination-btn ${currentPage === 0 ? 'disabled' : ''}" 
                onclick="loadRequests(${currentPage - 1})"
                ${currentPage === 0 ? 'disabled' : ''}>
            ← Назад
        </button>
    `;

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

    paginationHtml += `
        <button class="pagination-btn ${currentPage >= totalPages - 1 ? 'disabled' : ''}" 
                onclick="loadRequests(${currentPage + 1})"
                ${currentPage >= totalPages - 1 ? 'disabled' : ''}>
            Вперед →
        </button>
    `;

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
    } else {
        console.warn('Элемент для отображения количества заявок не найден');
    }
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

function showNotification(message, type = 'success') {
    const notification = document.getElementById('notification');
    const notificationText = document.getElementById('notificationText');

    if (!notification || !notificationText) return;

    notificationText.textContent = message;
    notification.className = `notification ${type}`;
    notification.style.display = 'flex';

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

function escapeHtml(text) {
    if (!text) return '';
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
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
window.debugApiResponse = debugApiResponse;

console.log('Admin script loaded and ready');