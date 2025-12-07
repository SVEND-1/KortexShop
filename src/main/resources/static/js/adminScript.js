// Конфигурация API для админ-панели
const API_BASE_URL = 'http://localhost:8080';
const API_ENDPOINTS = {
    GET_ALL_REQUESTS: '/api/admin/role-requests',
    GET_REQUEST_DETAILS: '/api/admin/role-requests',
    APPROVE_REQUEST: '/api/admin/role-requests',
    REJECT_REQUEST: '/api/admin/role-requests'
};

// Константы
const REQUEST_STATUS = {
    PENDING: 'PENDING',
    APPROVED: 'APPROVED',
    REJECTED: 'REJECTED'
};

const TYPE_ACTION = {
    ENHANCE: 'ENHANCE',
    REMOVE: 'REMOVE'
};

// Глобальные переменные
let currentPage = 1;
let totalPages = 1;
let currentRequestId = null;

// Получить заголовки с токеном
function getHeaders() {
    const token = localStorage.getItem('authToken');
    const headers = {
        'Content-Type': 'application/json'
    };
    
    if (token) {
        headers['Authorization'] = `Bearer ${token}`;
    }
    
    return headers;
}

// Инициализация
document.addEventListener('DOMContentLoaded', function() {
    console.log('Панель администратора загружена');
    
    // Загрузка заявок при загрузке страницы
    loadRequests();
    
    // Настройка обработчиков фильтров
    const statusFilter = document.getElementById('statusFilter');
    const typeFilter = document.getElementById('requestTypeFilter');
    
    if (statusFilter) {
        statusFilter.addEventListener('change', function() {
            currentPage = 1;
            loadRequests();
        });
    }
    
    if (typeFilter) {
        typeFilter.addEventListener('change', function() {
            currentPage = 1;
            loadRequests();
        });
    }
    
    // Закрытие модальных окон по клику вне их
    const requestModal = document.getElementById('requestModal');
    const confirmModal = document.getElementById('confirmModal');
    
    if (requestModal) {
        requestModal.addEventListener('click', function(e) {
            if (e.target === this) {
                closeModal();
            }
        });
    }
    
    if (confirmModal) {
        confirmModal.addEventListener('click', function(e) {
            if (e.target === this) {
                closeConfirmModal();
            }
        });
    }
    
    // ESC для закрытия модальных окон
    document.addEventListener('keydown', function(e) {
        if (e.key === 'Escape') {
            closeModal();
            closeConfirmModal();
        }
    });
});

// Загрузка списка заявок
async function loadRequests() {
    const statusFilter = document.getElementById('statusFilter');
    const typeFilter = document.getElementById('requestTypeFilter');
    
    const statusValue = statusFilter ? statusFilter.value : 'ALL';
    const typeValue = typeFilter ? typeFilter.value : 'ALL';
    
    // Показать индикатор загрузки
    showLoading(true);
    
    try {
        // Строим URL с параметрами
        let url = `${API_BASE_URL}${API_ENDPOINTS.GET_ALL_REQUESTS}`;
        const params = [];
        
        if (statusValue !== 'ALL') params.push(`status=${statusValue}`);
        if (typeValue !== 'ALL') params.push(`typeAction=${typeValue}`);
        
        if (params.length > 0) {
            url += `?${params.join('&')}`;
        }
        
        console.log('Запрос на:', url);
        
        const response = await fetch(url, {
            method: 'GET',
            headers: getHeaders()
        });
        
        if (response.ok) {
            const data = await response.json();
            console.log('Получены заявки:', data);
            
            renderRequestsTable(data);
            updateRequestsCount(data.length);
            updatePagination(1); // В вашем API нет пагинации, используем одну страницу
            
        } else if (response.status === 403) {
            showNotification('❌ У вас нет доступа к админ панели', 'error');
            setTimeout(() => {
                window.location.href = 'profile.html';
            }, 2000);
        } else {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
    } catch (error) {
        console.error('Ошибка загрузки заявок:', error);
        
        // Тестовые данные для демонстрации
        const testData = getTestData();
        renderRequestsTable(testData);
        updateRequestsCount(testData.length);
        updatePagination(1);
        
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
    
    // Сортируем по дате (новые сверху)
    requests.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt));
    
    tbody.innerHTML = requests.map(request => `
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
                        <button class="btn-table btn-table-success" 
                                onclick="approveRequest(${request.id})"
                                title="Одобрить заявку">
                            ✅ Одобрить
                        </button>
                        <button class="btn-table btn-table-danger" 
                                onclick="rejectRequest(${request.id})"
                                title="Отклонить заявку">
                            ❌ Отклонить
                        </button>
                    ` : ''}
                </div>
            </td>
        </tr>
    `).join('');
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
    // Основная информация
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
    
    // Тип действия
    const typeActionText = request.typeAction === 'ENHANCE' ? 'Повышение роли' : 'Снятие с роли';
    document.getElementById('modalRequestType').textContent = typeActionText;
    
    // Настройка действий
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
        
        actionsButtons.innerHTML = `
            <button class="btn btn-success" onclick="approveRequest(${request.id})">
                <span>✅</span> Одобрить заявку
            </button>
            <button class="btn btn-danger" onclick="rejectRequest(${request.id})">
                <span>❌</span> Отклонить заявку
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

// Одобрить заявку
async function approveRequest(requestId) {
    showConfirmModal(
        'Подтверждение одобрения',
        '<div style="padding: 15px 0;">' +
        '<p style="font-size: 1.2rem; margin-bottom: 10px; color: #333; font-weight: 500;">' +
        'Вы уверены, что хотите одобрить эту заявку?' +
        '</p>' +
        '<p style="color: #666; font-size: 0.95rem; margin: 0;">' +
        'Пользователь получит расширенные права и возможности.' +
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
                    showNotification('✅ Заявка успешно одобрена', 'success');
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
        '✅ Одобрить'
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
        '<p style="color: #666; font-size: 0.95rem; margin: 0;">' +
        'Это действие нельзя будет отменить.' +
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

// Обновление пагинации
function updatePagination(total) {
    totalPages = total;
    const pagination = document.getElementById('pagination');
    
    if (!pagination) return;
    
    if (totalPages <= 1) {
        pagination.innerHTML = '';
        return;
    }
    
    let html = '';
    
    // Кнопка "Назад"
    html += `
        <button class="pagination-btn" 
                ${currentPage === 1 ? 'disabled' : ''}
                onclick="changePage(${currentPage - 1})">
            ◀
        </button>
    `;
    
    // Номера страниц
    const start = Math.max(1, currentPage - 2);
    const end = Math.min(totalPages, start + 4);
    
    for (let i = start; i <= end; i++) {
        html += `
            <button class="pagination-btn ${i === currentPage ? 'active' : ''}"
                    onclick="changePage(${i})">
                ${i}
            </button>
        `;
    }
    
    // Кнопка "Вперед"
    html += `
        <button class="pagination-btn" 
                ${currentPage === totalPages ? 'disabled' : ''}
                onclick="changePage(${currentPage + 1})">
            ▶
        </button>
    `;
    
    // Информация
    html += `
        <span class="pagination-info">
            Страница ${currentPage} из ${totalPages}
        </span>
    `;
    
    pagination.innerHTML = html;
}

// Смена страницы
function changePage(page) {
    if (page < 1 || page > totalPages || page === currentPage) return;
    
    currentPage = page;
    loadRequests();
    
    // Прокрутка к верху таблицы
    const requestsSection = document.querySelector('.requests-section');
    if (requestsSection) {
        requestsSection.scrollIntoView({ 
            behavior: 'smooth' 
        });
    }
}

// Обновление счетчика заявок
function updateRequestsCount(count) {
    const countElement = document.getElementById('requestsCount');
    if (countElement) {
        countElement.textContent = `Всего заявок: ${count}`;
        
        // Анимация обновления
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
    
    currentPage = 1;
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
    
    // Автоматическое скрытие через 5 секунд
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

// Тестовые данные (если бэкенд недоступен)
function getTestData() {
    return [
        {
            id: 1,
            user: { id: 1001, name: "Иван Петров", email: "ivan@example.com", role: "CUSTOMER" },
            requestedRole: "SELLER",
            typeAction: "ENHANCE",
            message: "Хочу продавать электронику, есть опыт в этой сфере более 3 лет. Имею свой небольшой склад и готов предоставить качественный сервис.",
            status: "PENDING",
            createdAt: "2024-01-15T10:30:00"
        },
        {
            id: 2,
            user: { id: 1002, name: "Мария Сидорова", email: "maria@example.com", role: "COURIER" },
            requestedRole: "CUSTOMER",
            typeAction: "REMOVE",
            message: "Хочу вернуться к роли покупателя, так как больше не могу заниматься доставкой из-за личных обстоятельств.",
            status: "PENDING",
            createdAt: "2024-01-14T14:20:00"
        },
        {
            id: 3,
            user: { id: 1003, name: "Алексей Иванов", email: "alex@example.com", role: "CUSTOMER" },
            requestedRole: "COURIER",
            typeAction: "ENHANCE",
            message: "Имею свой автомобиль и свободное время. Готов работать в любое время суток. Опыт работы курьером 2 года.",
            status: "APPROVED",
            createdAt: "2024-01-13T09:15:00"
        },
        {
            id: 4,
            user: { id: 1004, name: "Дмитрий Смирнов", email: "dmitry@example.com", role: "SELLER" },
            requestedRole: "CUSTOMER",
            typeAction: "REMOVE",
            message: "Не могу больше заниматься продажами из-за занятости на основной работе.",
            status: "REJECTED",
            createdAt: "2024-01-12T16:45:00"
        }
    ];
}

// Переключение меню действий
function toggleActionMenu(requestId) {
    const menus = document.querySelectorAll('.dropdown-menu');
    menus.forEach(menu => {
        if (menu.id !== `menu-${requestId}`) {
            menu.style.display = 'none';
        }
    });
    
    const menu = document.getElementById(`menu-${requestId}`);
    if (menu) {
        menu.style.display = menu.style.display === 'block' ? 'none' : 'block';
        
        // Закрытие меню при клике вне его
        document.addEventListener('click', function closeMenu(e) {
            if (!e.target.closest('.action-dropdown')) {
                menu.style.display = 'none';
                document.removeEventListener('click', closeMenu);
            }
        });
    }
}