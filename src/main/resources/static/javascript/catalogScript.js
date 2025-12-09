// catalogScript.js - для работы с Spring бекендом

document.addEventListener('DOMContentLoaded', function() {
    console.log('Каталог товаров загружен');
    initializeCatalog();
});

function initializeCatalog() {
    loadProducts();
    setupFilters();
    setupSearch();
}

// Загрузка товаров с сервера
async function loadProducts() {
    try {
        const productsGrid = document.getElementById('productsGrid');
        productsGrid.innerHTML = `
            <div class="loading" style="grid-column: 1 / -1; text-align: center; padding: 50px; color: #666;">
                <div class="spinner" style="border: 4px solid #f3f3f3; border-top: 4px solid #3498db; border-radius: 50%; width: 50px; height: 50px; animation: spin 2s linear infinite; margin: 0 auto 20px;"></div>
                <p>Загрузка товаров...</p>
            </div>
        `;
        
        // Добавляем стили для анимации
        const style = document.createElement('style');
        style.textContent = `
            @keyframes spin {
                0% { transform: rotate(0deg); }
                100% { transform: rotate(360deg); }
            }
        `;
        document.head.appendChild(style);
        
        // Получаем параметры фильтрации
        const category = document.getElementById('categoryFilter')?.value || '';
        const searchTerm = document.getElementById('search-input')?.value || '';
        const sortBy = document.getElementById('sortFilter')?.value || '';
        
        // Формируем URL с параметрами
        let url = '/api/products';
        const params = new URLSearchParams();
        
        if (category) params.append('category', category);
        if (searchTerm) params.append('query', searchTerm);
        if (sortBy) params.append('sort', sortBy);
        
        if (params.toString()) {
            url += '?' + params.toString();
        }
        
        console.log('Запрашиваем URL:', url);
        
        // Делаем запрос к бекенду
        const response = await fetch(url, {
            method: 'GET',
            headers: {
                'Accept': 'application/json',
                'Content-Type': 'application/json'
            }
        });
        
        if (!response.ok) {
            throw new Error(`Ошибка HTTP: ${response.status}`);
        }
        
        const products = await response.json();
        console.log('Получены товары:', products);
        
        // Если товары получены как объект с полями products (Page)
        if (products.content) {
            displayProducts(products.content);
        } else {
            displayProducts(products);
        }
        
    } catch (error) {
        console.error('Ошибка при загрузке товаров:', error);
        showError('Не удалось загрузить товары. Попробуйте позже.');
    }
}

// Отображение товаров
function displayProducts(products) {
    const productsGrid = document.getElementById('productsGrid');
    
    if (!products || products.length === 0) {
        productsGrid.innerHTML = `
            <div class="empty-catalog" style="grid-column: 1 / -1; text-align: center; padding: 60px 20px; background: #f8f9fa; border-radius: 12px; border: 2px dashed #dee2e6;">
                <div class="empty-catalog-icon" style="font-size: 64px; margin-bottom: 20px;">📦</div>
                <h3 style="margin-bottom: 10px; color: #333;">Товары не найдены</h3>
                <p style="color: #666; margin-bottom: 20px;">Попробуйте изменить параметры поиска или фильтрации</p>
            </div>
        `;
        return;
    }
    
    productsGrid.innerHTML = products.map(product => `
        <div class="product-card" data-product-id="${product.id}" style="cursor: pointer;">
            <div class="product-image">
                <img src="${product.image || 'images/product-img.png'}" 
                     alt="${product.name}" 
                     style="width: 100%; height: 200px; object-fit: cover; border-radius: 8px;"
                     onerror="this.src='images/product-img.png'">
            </div>
            <div class="product-info" style="padding: 15px;">
                <h3 class="product-name" style="margin: 0 0 10px 0; font-size: 16px; color: #333;">${product.name}</h3>
                <span class="product-category" style="display: inline-block; background: #e8f4fd; color: #3182ce; padding: 3px 8px; border-radius: 12px; font-size: 12px; margin-bottom: 10px;">
                    ${product.category || 'Без категории'}
                </span>
                <p class="product-description" style="color: #666; font-size: 14px; margin-bottom: 15px; height: 40px; overflow: hidden;">
                    ${(product.description || 'Описание отсутствует').substring(0, 80)}...
                </p>
                <div class="product-price" style="font-size: 18px; font-weight: bold; color: #2d3748; margin-bottom: 15px;">
                    ${formatPrice(product.price || 0)}
                </div>
                <div class="product-actions" style="display: flex; gap: 10px;">
                    <button class="btn-add-to-cart" onclick="event.stopPropagation(); addToCart(${product.id})" 
                            style="flex: 1; padding: 10px; background: #3182ce; color: white; border: none; border-radius: 6px; cursor: pointer; font-size: 14px;">
                        В корзину
                    </button>
                    <button class="btn-view-details" onclick="event.stopPropagation(); viewProductDetails(${product.id})"
                            style="flex: 1; padding: 10px; background: #e2e8f0; color: #4a5568; border: none; border-radius: 6px; cursor: pointer; font-size: 14px;">
                        Подробнее
                    </button>
                </div>
            </div>
        </div>
    `).join('');
    
    makeProductCardsClickable();
}

// Настройка фильтров
function setupFilters() {
    const categoryFilter = document.getElementById('categoryFilter');
    
    // Статические категории (можно заменить на запрос к API)
    const categories = [
        { value: 'ELECTRONICS', name: 'Электроника' },
        { value: 'CLOTHING', name: 'Одежда' },
        { value: 'BOOKS', name: 'Книги' },
        { value: 'FOOD', name: 'Еда' },
        { value: 'SPORTS', name: 'Спорт товары' },
        { value: 'HOME', name: 'Товары для дома' },
        { value: 'BEAUTY', name: 'Красота' },
        { value: 'OTHER', name: 'Другое' }
    ];
    
    if (categoryFilter) {
        categoryFilter.innerHTML = '<option value="">Все категории</option>' +
            categories.map(cat => 
                `<option value="${cat.value}">${cat.name}</option>`
            ).join('');
        
        categoryFilter.addEventListener('change', loadProducts);
    }
    
    const sortFilter = document.getElementById('sortFilter');
    if (sortFilter) {
        sortFilter.addEventListener('change', loadProducts);
    }
}

// Настройка поиска
function setupSearch() {
    const searchForm = document.querySelector('.search-form');
    const searchInput = document.getElementById('search-input');
    
    if (searchForm) {
        searchForm.addEventListener('submit', function(e) {
            e.preventDefault();
            loadProducts();
        });
    }
    
    if (searchInput) {
        let searchTimeout;
        searchInput.addEventListener('input', function() {
            clearTimeout(searchTimeout);
            searchTimeout = setTimeout(() => loadProducts(), 500);
        });
    }
}

// Добавление в корзину
async function addToCart(productId) {
    try {
        // Получаем данные о товаре с сервера
        const response = await fetch(`/api/products/${productId}`);
        if (!response.ok) {
            throw new Error('Товар не найден');
        }
        
        const product = await response.json();
        
        // Проверяем, загружен ли cartManager
        if (window.cartManager) {
            if (window.cartManager.addToCart(productId)) {
                showNotification(`Товар "${product.name}" добавлен в корзину!`);
            }
        } else {
            // Запасной вариант - localStorage
            const cart = JSON.parse(localStorage.getItem('cart')) || [];
            const existingItem = cart.find(item => item.id === productId);
            
            if (existingItem) {
                existingItem.quantity += 1;
            } else {
                cart.push({
                    id: product.id,
                    name: product.name,
                    price: product.price,
                    image: product.image || 'images/product-img.png',
                    quantity: 1
                });
            }
            
            localStorage.setItem('cart', JSON.stringify(cart));
            showNotification(`Товар "${product.name}" добавлен в корзину!`);
        }
    } catch (error) {
        console.error('Ошибка при добавлении в корзину:', error);
        showNotification('Не удалось добавить товар в корзину');
    }
}

// Просмотр деталей товара
function viewProductDetails(productId) {
    window.location.href = `/api/products/?id=${productId}`;
}

// Делаем карточки товаров кликабельными
function makeProductCardsClickable() {
    const productCards = document.querySelectorAll('.product-card');
    productCards.forEach(card => {
        card.addEventListener('click', function(e) {
            if (!e.target.closest('button')) {
                const productId = this.getAttribute('data-product-id');
                viewProductDetails(parseInt(productId));
            }
        });
    });
}

// Форматирование цены
function formatPrice(price) {
    return new Intl.NumberFormat('ru-RU', {
        style: 'currency',
        currency: 'RUB',
        minimumFractionDigits: 0
    }).format(price);
}

// Показ уведомлений
function showNotification(message) {
    // Удаляем старые уведомления
    const oldNotifications = document.querySelectorAll('.notification');
    oldNotifications.forEach(n => n.remove());
    
    const notification = document.createElement('div');
    notification.className = 'notification';
    notification.style.cssText = `
        position: fixed;
        top: 20px;
        right: 20px;
        background: #28a745;
        color: white;
        padding: 15px 20px;
        border-radius: 8px;
        box-shadow: 0 4px 12px rgba(0,0,0,0.15);
        z-index: 1000;
        transform: translateX(100%);
        opacity: 0;
        transition: transform 0.3s ease, opacity 0.3s ease;
    `;
    notification.textContent = message;
    
    document.body.appendChild(notification);
    
    setTimeout(() => {
        notification.style.transform = 'translateX(0)';
        notification.style.opacity = '1';
    }, 10);
    
    setTimeout(() => {
        notification.style.transform = 'translateX(100%)';
        notification.style.opacity = '0';
        setTimeout(() => notification.remove(), 300);
    }, 3000);
}

// Показ ошибки
function showError(message) {
    const productsGrid = document.getElementById('productsGrid');
    productsGrid.innerHTML = `
        <div class="error-message" style="grid-column: 1 / -1; text-align: center; padding: 50px; background: #fff5f5; border: 2px solid #fed7d7; border-radius: 12px;">
            <div class="error-icon" style="font-size: 48px; margin-bottom: 20px;">⚠️</div>
            <h3 style="margin-bottom: 10px; color: #c53030;">Ошибка загрузки</h3>
            <p style="color: #718096; margin-bottom: 20px;">${message}</p>
            <button onclick="loadProducts()" class="btn-retry" style="padding: 10px 30px; background: #3182ce; color: white; border: none; border-radius: 6px; cursor: pointer; font-size: 16px;">
                Попробовать снова
            </button>
        </div>
    `;
}

// Экспортируем функции для использования в других файлах
window.loadProducts = loadProducts;
window.addToCart = addToCart;
window.viewProductDetails = viewProductDetails;