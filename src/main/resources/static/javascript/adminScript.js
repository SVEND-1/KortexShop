// Конфигурация API для админ-панели
const API_BASE_URL = 'http://localhost:8080';
const API_ENDPOINTS = {
    GET_ALL_REQUESTS: '/api/admin/role-request',
    GET_REQUEST_DETAILS: '/api/admin/role-request',
    APPROVE_REQUEST: '/api/admin/role-request',
    REJECT_REQUEST: '/api/admin/role-request',
    DOWNGRADE_REQUEST: '/api/admin/role-request'
};

// Константы
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
function getHeaders(contentType = 'application/json') {
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
document.addEventListener('DOMContentLoaded', function() {
    console.log('Панель администратора загружена');

    loadRequests();

    const statusFilter = document.getElementById('statusFilter');
    const typeFilter = document.getElementById('requestTypeFilter');

    if (statusFilter) {
        statusFilter.addEventListener('change', function() {
            loadRequests();
        });
    }

    if (typeFilter) {
        typeFilter.addEventListener('change', function() {
            loadRequests();
        });
    }

    const refreshBtn = document.getElementById('refreshBtn');
    if (refreshBtn) {
        refreshBtn.addEventListener('click', function() {
            loadRequests();
        });
    }

    const clearBtn = document.getElementById('clearBtn');
    if (clearBtn) {
        clearBtn.addEventListener('click', function() {
            clearFilters();
        });
    }

    document.addEventListener('keydown', function(e) {
        if (e.key === 'Escape') {
            closeModal();
            closeConfirmModal();
        }
    });

    const modals = document.querySelectorAll('.modal');
    modals.forEach(modal => {
        modal.addEventListener('click', function(e) {
            if (e.target === this) {
                if (this.id === 'requestModal') {
                    closeModal();
                } else if (this.id === 'confirmModal') {
                    closeConfirmModal();
                }
            }
        });
    });
});

// Загрузка списка заявок
async function loadRequests() {
    const statusFilter = document.getElementById('statusFilter');
    const typeFilter = document.getElementById('requestTypeFilter');

    const statusValue = statusFilter ? statusFilter.value : 'ALL';
    const actionTypeValue = typeFilter ? typeFilter.value : 'ALL';

    showLoading(true);

    try {
        let url = `${API_BASE_URL}${API_ENDPOINTS.GET_ALL_REQUESTS}`;
        const params = [];

        if (statusValue !== 'ALL') {
            params.push(`status=${statusValue}`);
        }

        if (actionTypeValue !== 'ALL') {
            params.push(`typeAction=${actionTypeValue}`);
        }

        params.push(`pageSize=50`);
        params.push(`pageNumber=0`);

        if (params.length > 0) {
            url += `?${params.join('&')}`;
        }

        console.log('Запрос заявок на:', url);

        const response = await fetch(url, {
            method: 'GET',
            headers: getHeaders()
        });

        if (response.ok) {
            const data = await response.json();
            console.log('Получены заявки:', data);

            if (Array.isArray(data)) {
                renderRequestsTable(data);
                updateRequestsCount(data.length);
            } else {
                console.warn('Данные пришли не в виде массива:', data);
                renderRequestsTable([]);
                updateRequestsCount(0);
            }

        } else if (response.status === 403) {
            showNotification('❌ У вас нет доступа к админ панели', 'error');
            setTimeout(() => {
                window.location.href = 'profile.html';
            }, 2000);
        } else if (response.status === 404) {
            showNotification('⚠️ Заявок пока нет', 'info');
            renderRequestsTable([]);
            updateRequestsCount(0);
        } else {
            const errorText = await response.text();
            throw new Error(`HTTP error! status: ${response.status}, message: ${errorText}`);
        }
    } catch (error) {
        console.error('Ошибка загрузки заявок:', error);

        renderRequestsTable(testData);
        updateRequestsCount(testData.length);

        showNotification('⚠️ Бэкенд недоступен, отображаются тестовые данные', 'warning');
    } finally {
        showLoading(false);
    }
}

// Отрисовка таблицы заявок
function renderRequestsTable(requests) {
    const tbody = document.getElementById('requestsList');

    if (!tbody) return;

    if (!requests || requests.length === 0) {
        tbody.innerHTML = `
            <tr>
                <td colspan="7" class="loading">
                    📭 Нет заявок, соответствующих фильтрам
                </td>
            </tr>
        `;
        return;
    }

    requests.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt));

    tbody.innerHTML = requests.map(request => {
        const needsDowngrade = request.typeAction === 'REMOVE';

        return `
        <tr>
            <td>${request.id}</td>
            <td>
                <div class="user-info">
                    <strong>${request.user?.name || 'Не указано'}</strong>
                    <small>ID: ${request.user?.id || 'N/A'}</small>
                </div>
            </td>
            <td>${request.user?.email || 'Не указан'}</td>
            <td>
                <span class="request-type type-${request.typeAction === 'ENHANCE' ? 'upgrade' : 'downgrade'}">
                    ${getRequestTypeText(request.typeAction, request.requestedRole)}
                </span>
            </td>
            <td>
                <span class="status-badge status-${request.status.toLowerCase()}">
                    ${getStatusText(request.status)}
                </span>
            </td>
            <td>${formatDate(request.createdAt)}</td>
            <td>
                <div class="action-buttons">
                    <button class="btn-table btn-table-primary" 
                            onclick="showRequestDetails(${request.id})"
                            title="Подробнее о заявке">
                        👁️ Подробно
                    </button>
                    
                    ${request.status === 'PENDING' ? `
                        ${needsDowngrade ? `
                            <button class="btn-table btn-table-warning" 
                                    onclick="downgradeRequest(${request.id})"
                                    title="Одобрить понижение">
                                ⬇️ Понизить
                            </button>
                        ` : `
                            <button class="btn-table btn-table-success" 
                                    onclick="approveRequest(${request.id})"
                                    title="Одобрить повышение">
                                ⬆️ Повысить
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
    `}).join('');
}

// Показать детали заявки
async function showRequestDetails(requestId) {
    try {
        const response = await fetch(
            `${API_BASE_URL}${API_ENDPOINTS.GET_REQUEST_DETAILS}/${requestId}`,
            {
                method: 'GET',
                headers: getHeaders()
            }
        );

        if (response.ok) {
            const request = await response.json();
            currentRequestId = requestId;
            fillRequestModal(request);
            document.getElementById('requestModal').style.display = 'flex';
        } else {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
    } catch (error) {
        console.error('Ошибка загрузки деталей заявки:', error);
        showNotification('❌ Ошибка загрузки деталей заявки', 'error');
    }
}

// Заполнение модального окна данными
function fillRequestModal(request) {
    document.getElementById('modalRequestId').textContent = request.id;
    document.getElementById('modalUserId').textContent = request.user?.id || 'N/A';
    document.getElementById('modalUserName').textContent = request.user?.name || 'Не указано';
    document.getElementById('modalUserEmail').textContent = request.user?.email || 'Не указан';
    document.getElementById('modalCurrentRole').textContent = getRoleText(request.user?.role || 'CUSTOMER');
    document.getElementById('modalRequestedRole').textContent = getRoleText(request.requestedRole);
    document.getElementById('modalStatus').textContent = getStatusText(request.status);
    document.getElementById('modalStatus').className = `info-value status-badge status-${request.status.toLowerCase()}`;
    document.getElementById('modalCreatedAt').textContent = formatDate(request.createdAt);
    document.getElementById('modalDescription').textContent = request.message || 'Описание отсутствует';

    const typeActionText = request.typeAction === 'ENHANCE' ? 'Повышение роли' : 'Снятие с роли';
    document.getElementById('modalRequestType').textContent = typeActionText;

    const actionsInfo = document.getElementById('actionsInfo');
    const actionsButtons = document.getElementById('actionsButtons');

    if (request.status === 'PENDING') {
        actionsInfo.innerHTML = `
            <p><strong>⚠️ Эта заявка ожидает рассмотрения</strong></p>
            <p>Пользователь запрашивает <strong>${typeActionText.toLowerCase()}</strong>.</p>
            <p><strong>Текущая роль:</strong> ${getRoleText(request.user?.role || 'CUSTOMER')}</p>
            <p><strong>Запрашиваемая роль:</strong> ${getRoleText(request.requestedRole)}</p>
            <p><strong>ID пользователя:</strong> ${request.user?.id || 'N/A'}</p>
        `;

        const buttonType = request.typeAction === 'ENHANCE' ? 'approveRequest' : 'downgradeRequest';
        const buttonText = request.typeAction === 'ENHANCE' ? '✅ Одобрить повышение' : '✅ Одобрить понижение';

        actionsButtons.innerHTML = `
            <button class="btn btn-success" onclick="${buttonType}(${request.id})">
                ${buttonText}
            </button>
            <button class="btn btn-danger" onclick="rejectRequest(${request.id})">
                ❌ Отклонить заявку
            </button>
            <button class="btn btn-secondary" onclick="closeModal()">
                Закрыть
            </button>
        `;
    } else if (request.status === 'APPROVED') {
        actionsInfo.innerHTML = `
            <p><strong>✅ Эта заявка была одобрена</strong></p>
            <p><strong>Тип:</strong> ${typeActionText}</p>
            <p><strong>Результат:</strong> Пользователь теперь ${getRoleText(request.requestedRole).toLowerCase()}</p>
            <p><strong>ID пользователя:</strong> ${request.user?.id || 'N/A'}</p>
        `;
        actionsButtons.innerHTML = `
            <button class="btn btn-secondary" onclick="closeModal()">
                Закрыть
            </button>
        `;
    } else {
        actionsInfo.innerHTML = `
            <p><strong>❌ Эта заявка была отклонена</strong></p>
            <p><strong>Тип:</strong> ${typeActionText}</p>
            <p><strong>Статус:</strong> Отклонено администратором</p>
            <p><strong>ID пользователя:</strong> ${request.user?.id || 'N/A'}</p>
        `;
        actionsButtons.innerHTML = `
            <button class="btn btn-secondary" onclick="closeModal()">
                Закрыть
            </button>
        `;
    }
}

// Одобрить заявку (для повышения)
async function approveRequest(requestId) {
    showConfirmModal(
        'Подтверждение одобрения',
        '<div style="padding: 15px 0;">' +
        '<p style="font-size: 1.2rem; margin-bottom: 10px; color: #333; font-weight: 500;">' +
        'Вы уверены, что хотите одобрить повышение роли?' +
        '</p>' +
        '</div>',
        async () => {
            try {
                const response = await fetch(
                    `${API_BASE_URL}${API_ENDPOINTS.APPROVE_REQUEST}/${requestId}/approve`,
                    {
                        method: 'POST',
                        headers: getHeaders()
                    }
                );

                const responseText = await response.text();

                if (response.ok) {
                    showNotification('✅ Заявка на повышение успешно одобрена', 'success');
                    closeModal();
                    loadRequests();
                } else {
                    throw new Error(responseText || `HTTP error! status: ${response.status}`);
                }
            } catch (error) {
                console.error('Ошибка одобрения заявки:', error);
                showNotification(`❌ Ошибка: ${error.message}`, 'error');
            }
        },
        '✅ Одобрить повышение'
    );
}

// Одобрить понижение
async function downgradeRequest(requestId) {
    showConfirmModal(
        'Подтверждение понижения',
        '<div style="padding: 15px 0;">' +
        '<p style="font-size: 1.2rem; margin-bottom: 10px; color: #333; font-weight: 500;">' +
        'Вы уверены, что хотите одобрить понижение роли?' +
        '</p>' +
        '</div>',
        async () => {
            try {
                const response = await fetch(
                    `${API_BASE_URL}${API_ENDPOINTS.DOWNGRADE_REQUEST}/${requestId}/downgrade`,
                    {
                        method: 'POST',
                        headers: getHeaders()
                    }
                );

                const responseText = await response.text();

                if (response.ok) {
                    showNotification('✅ Заявка на понижение успешно одобрена', 'success');
                    closeModal();
                    loadRequests();
                } else {
                    throw new Error(responseText || `HTTP error! status: ${response.status}`);
                }
            } catch (error) {
                console.error('Ошибка понижения:', error);
                showNotification(`❌ Ошибка: ${error.message}`, 'error');
            }
        },
        '✅ Одобрить понижение'
    );
}

// Отклонить заявку
async function rejectRequest(requestId) {
    showConfirmModal(
        'Подтверждение отклонения',
        '<div style="padding: 15px 0;">' +
        '<p style="font-size: 1.2rem; margin-bottom: 10px; color: #333; font-weight: 500;">' +
        'Вы уверены, что хотите отклонить эту заявку?' +
        '</p>' +
        '</div>',
        async () => {
            try {
                const response = await fetch(
                    `${API_BASE_URL}${API_ENDPOINTS.REJECT_REQUEST}/${requestId}/reject`,
                    {
                        method: 'POST',
                        headers: getHeaders()
                    }
                );

                const responseText = await response.text();

                if (response.ok) {
                    showNotification('❌ Заявка отклонена', 'warning');
                    closeModal();
                    loadRequests();
                } else {
                    throw new Error(responseText || `HTTP error! status: ${response.status}`);
                }
            } catch (error) {
                console.error('Ошибка отклонения заявки:', error);
                showNotification(`❌ Ошибка: ${error.message}`, 'error');
            }
        },
        '❌ Отклонить'
    );
}

// Модальное окно подтверждения
function showConfirmModal(title, message, callback, confirmText = "Подтвердить") {
    document.getElementById('confirmTitle').textContent = title;
    document.getElementById('confirmMessage').innerHTML = message;

    const confirmButton = document.getElementById('confirmButton');
    confirmButton.textContent = confirmText;
    confirmButton.onclick = function() {
        callback();
        closeConfirmModal();
    };

    document.getElementById('confirmModal').style.display = 'flex';
}

function closeConfirmModal() {
    document.getElementById('confirmModal').style.display = 'none';
}

// Закрыть основное модальное окно
function closeModal() {
    document.getElementById('requestModal').style.display = 'none';
    currentRequestId = null;
}

// Обновление счетчика заявок
function updateRequestsCount(count) {
    const countElement = document.getElementById('requestsCount');
    if (countElement) {
        countElement.textContent = `Всего заявок: ${count}`;

        countElement.style.transform = 'scale(1.1)';
        setTimeout(() => {
            countElement.style.transform = 'scale(1)';
        }, 300);
    }
}

// Очистка фильтров
function clearFilters() {
    const statusFilter = document.getElementById('statusFilter');
    const typeFilter = document.getElementById('requestTypeFilter');

    if (statusFilter) statusFilter.value = 'ALL';
    if (typeFilter) typeFilter.value = 'ALL';

    loadRequests();
}

// Показать загрузку
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

// Показать уведомление
function showNotification(message, type = 'success') {
    const notification = document.getElementById('notification');
    const notificationText = document.getElementById('notificationText');

    if (!notification || !notificationText) return;

    notificationText.textContent = message;
    notification.className = `notification ${type}`;
    notification.style.display = 'flex';

    setTimeout(() => {
        hideNotification();
    }, 5000);
}

// Скрыть уведомление
function hideNotification() {
    const notification = document.getElementById('notification');
    if (notification) {
        notification.style.display = 'none';
    }
}

// Вспомогательные функции
function getRequestTypeText(typeAction, requestedRole) {
    if (typeAction === 'ENHANCE') {
        if (requestedRole === 'SELLER') return 'Стать продавцом';
        if (requestedRole === 'COURIER') return 'Стать курьером';
        return 'Повышение';
    } else if (typeAction === 'REMOVE') {
        return 'Снятие с роли';
    }
    return typeAction;
}

function getStatusText(status) {
    const statusMap = {
        'PENDING': 'Ожидает',
        'APPROVED': 'Одобрено',
        'REJECTED': 'Отклонено'
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

