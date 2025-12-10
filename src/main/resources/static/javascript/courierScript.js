// Тестовые данные заказов
let testOrders = [
    {
        id: 1001,
        status: 'available',
        customerName: 'Анна Петрова',
        customerPhone: '+7 (911) 111-11-11',
        customerEmail: 'anna@example.com',
        address: 'ул. Ленина, д. 15, кв. 42',
        totalPrice: '2,450 руб.'
    },
    {
        id: 1002,
        status: 'pending',
        customerName: 'Сергей Иванов',
        customerPhone: '+7 (922) 222-22-22',
        customerEmail: 'sergey@example.com',
        address: 'пр. Победы, д. 28, кв. 15',
        totalPrice: '5,790 руб.'
    },
    {
        id: 1003,
        status: 'in_progress',
        customerName: 'Мария Сидорова',
        customerPhone: '+7 (933) 333-33-33',
        customerEmail: 'maria@example.com', 
        address: 'пр. Мира, д. 88, кв. 17',
        totalPrice: '1,230 руб.'
    },
    {
        id: 1004,
        status: 'available',
        customerName: 'Дмитрий Козлов',
        customerPhone: '+7 (944) 444-44-44',
        customerEmail: 'dmitry@example.com',
        address: 'ул. Садовая, д. 5, кв. 9',
        totalPrice: '8,990 руб.'
    }
];

let currentFilter = 'all';

// Скрипт для панели курьера
document.addEventListener('DOMContentLoaded', function() {
    console.log('Панель курьера загружена');
    loadCourierData();
    loadOrders();
});

// Загрузка данных курьера
function loadCourierData() {
    // Здесь будет запрос к Spring API
    // fetch('/api/courier/profile')
    
    const courierData = {
        name: 'Иван Иванов',
        avatar: 'images/avatar.jpg',
        stats: {
            active: 2,
            available: 2,
            total: 4
        }
    };
    
    document.getElementById('courierName').textContent = courierData.name;
    document.getElementById('courierAvatar').src = courierData.avatar;
    updateStats();
}

// Обновление статистики
function updateStats() {
    const activeOrders = testOrders.filter(order => 
        order.status === 'pending' || order.status === 'in_progress'
    ).length;
    
    const availableOrders = testOrders.filter(order => 
        order.status === 'available'
    ).length;

    document.getElementById('activeOrders').textContent = activeOrders;
    document.getElementById('availableOrders').textContent = availableOrders;
    document.getElementById('totalOrders').textContent = testOrders.length;
}

// Загрузка заказов
function loadOrders(filter = 'all') {
    currentFilter = filter;
    
    const ordersContainer = document.getElementById('ordersContainer');
    const ordersTitle = document.getElementById('ordersTitle');
    
    // Показываем загрузку
    ordersContainer.innerHTML = '<div class="loading">Загрузка заказов...</div>';
    
    // Имитация загрузки с сервера
    setTimeout(() => {
        // Здесь будет запрос к Spring API
        // fetch(`/api/courier/orders?filter=${filter}`)
        
        let filteredOrders = testOrders;
        
        if (filter === 'my') {
            filteredOrders = testOrders.filter(order => 
                order.status === 'pending' || order.status === 'in_progress'
            );
        } else if (filter === 'available') {
            filteredOrders = testOrders.filter(order => order.status === 'available');
        }
        
        // Обновляем заголовок
        const titles = {
            'all': 'Все заказы',
            'my': 'Мои заказы', 
            'available': 'Доступные заказы'
        };
        ordersTitle.textContent = titles[filter] || 'Все заказы';
        
        if (filteredOrders.length === 0) {
            ordersContainer.innerHTML = `
                <div class="no-orders">
                    <div class="no-orders-icon">📦</div>
                    <h4>Заказов нет</h4>
                    <p>В данный момент нет доступных заказов</p>
                </div>
            `;
            return;
        }
        
        // Очищаем контейнер
        ordersContainer.innerHTML = '';
        
        // Добавляем заказы
        filteredOrders.forEach(order => {
            const orderElement = createOrderElement(order);
            ordersContainer.appendChild(orderElement);
        });
        
        updateStats();
    }, 500);
}

// Создание элемента заказа
function createOrderElement(order) {
    const template = document.getElementById('orderTemplate');
    const clone = template.content.cloneNode(true);
    const orderCard = clone.querySelector('.order-card');
    
    // Заполняем данные
    orderCard.setAttribute('data-order-id', order.id);
    orderCard.querySelector('.order-id').textContent = order.id;
    orderCard.querySelector('.customer-name').textContent = order.customerName;
    orderCard.querySelector('.customer-phone').textContent = order.customerPhone;
    orderCard.querySelector('.customer-address').textContent = order.address;
    
    // Устанавливаем статус
    const statusElement = orderCard.querySelector('.order-status');
    statusElement.textContent = getStatusText(order.status);
    statusElement.className = `order-status ${order.status}`;
    
    // Настраиваем кнопки в зависимости от статуса
    setupOrderButtons(orderCard, order.status, order.id);
    
    return orderCard;
}

// Настройка кнопок заказа
function setupOrderButtons(orderCard, status, orderId) {
    const acceptBtn = orderCard.querySelector('.accept-btn');
    const startBtn = orderCard.querySelector('.start-btn');
    const completeBtn = orderCard.querySelector('.complete-btn');
    
    // Сначала скрываем все кнопки действий
    acceptBtn.style.display = 'none';
    startBtn.style.display = 'none';
    completeBtn.style.display = 'none';
    
    // Показываем нужные кнопки в зависимости от статуса
    switch(status) {
        case 'available':
            acceptBtn.style.display = 'block';
            break;
        case 'pending':
            startBtn.style.display = 'block';
            break;
        case 'in_progress':
            completeBtn.style.display = 'block';
            break;
    }
}

// Установка фильтра
function setFilter(filter) {
    const filterButtons = document.querySelectorAll('.filter-btn');
    
    // Обновляем активную кнопку
    filterButtons.forEach(btn => {
        btn.classList.remove('active');
    });
    event.target.classList.add('active');
    
    // Загружаем заказы с новым фильтром
    loadOrders(filter);
}

// Получение текста статуса
function getStatusText(status) {
    const statusMap = {
        'pending': 'Ожидает доставки',
        'available': 'Доступен',
        'in_progress': 'В доставке',
        'completed': 'Завершен',
        'cancelled': 'Отменен'
    };
    return statusMap[status] || 'Неизвестно';
}

// Принять заказ
function acceptOrder(button) {
    const orderCard = button.closest('.order-card');
    const orderId = orderCard.getAttribute('data-order-id');
    
    if (!confirm(`Принять заказ #${orderId}?`)) return;

    // Обновляем статус в тестовых данных
    testOrders = testOrders.map(order => 
        order.id == orderId ? {...order, status: 'pending'} : order
    );

    alert(`Заказ #${orderId} принят!`);
    loadOrders(currentFilter);
}

// Начать доставку
function startDelivery(button) {
    const orderCard = button.closest('.order-card');
    const orderId = orderCard.getAttribute('data-order-id');
    
    if (!confirm(`Начать доставку заказа #${orderId}?`)) return;

    testOrders = testOrders.map(order => 
        order.id == orderId ? {...order, status: 'in_progress'} : order
    );

    alert(`Доставка заказа #${orderId} начата!`);
    loadOrders(currentFilter);
}

// Завершить доставку
function completeOrder(button) {
    const orderCard = button.closest('.order-card');
    const orderId = orderCard.getAttribute('data-order-id');
    
    if (!confirm(`Завершить доставку заказа #${orderId}?`)) return;

    testOrders = testOrders.map(order => 
        order.id == orderId ? {...order, status: 'completed'} : order
    );

    alert(`Заказ #${orderId} завершен!`);
    loadOrders(currentFilter);
}

// Показать детали заказа
function showOrderDetails(button) {
    const orderCard = button.closest('.order-card');
    const orderId = orderCard.getAttribute('data-order-id');
    const order = testOrders.find(o => o.id == orderId);
    
    if (order) {
        alert(`Детали заказа #${orderId}:\n\n` +
              `Клиент: ${order.customerName}\n` +
              `Телефон: ${order.customerPhone}\n` +
              `Email: ${order.customerEmail}\n` +
              `Адрес: ${order.address}\n` +
              `Сумма: ${order.totalPrice}\n` +
              `Статус: ${getStatusText(order.status)}`);
    }
}

// Для Spring бэкенда - пример AJAX запросов:
/*
function acceptOrder(button) {
    const orderCard = button.closest('.order-card');
    const orderId = orderCard.getAttribute('data-order-id');
    
    fetch('/api/courier/orders/accept', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({ orderId: orderId })
    })
    .then(response => response.json())
    .then(data => {
        alert('Заказ принят!');
        loadOrders(currentFilter);
    })
    .catch(error => {
        console.error('Error:', error);
        alert('Ошибка при принятии заказа');
    });
}
*/