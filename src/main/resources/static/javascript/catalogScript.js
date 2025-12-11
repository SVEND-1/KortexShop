// catalogScript.js - для работы с Spring бекендом

document.addEventListener('DOMContentLoaded', function() {
    console.log('Каталог товаров загружен');
    initializeCatalog();
});

function initializeCatalog() {
    setupFilters();
    setupSearch();
    // Загрузка товаров будет вызвана из HTML
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

        // Формируем URL с параметрами (УБИРАЕМ sort из запроса к бекенду)
        let url = '/api/products';
        const params = new URLSearchParams();

        if (category) params.append('category', category);
        if (searchTerm) params.append('query', searchTerm);

        if (params.toString()) {
            url += '?' + params.toString();
        }

        console.log('Запрашиваем URL:', url);

        // Делаем запрос к бекенду - ВАЖНО: убираем Content-Type для GET запроса
        const response = await fetch(url, {
            method: 'GET',
            headers: {
                'Accept': 'application/json'
            }
        });

        if (!response.ok) {
            throw new Error(`Ошибка HTTP: ${response.status}`);
        }

        const data = await response.json();
        console.log('Получены данные:', data);

        // Если товары получены как объект с полями products (Page)
        if (data.content) {
            displayProducts(data.content);
        } else {
            displayProducts(data);
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

    // Применяем сортировку на клиенте
    const sortedProducts = applySorting(products);

    // Сохраняем товары для сортировки
    localStorage.setItem('lastProducts', JSON.stringify(sortedProducts));

    productsGrid.innerHTML = sortedProducts.map(product => {
        // Формируем URL изображения
        const imageUrl = product.image
            ? `/uploads/images/${product.image}`
            : '/images/product-img.png';

        // Форматируем категорию (если есть displayName)
        let categoryName = product.category || 'Без категории';
        if (product.category && typeof product.category === 'object' && product.category.displayName) {
            categoryName = product.category.displayName;
        } else if (product.category === 'ELECTRONICS') categoryName = 'Электроника';
        else if (product.category === 'CLOTHING') categoryName = 'Одежда';
        else if (product.category === 'BOOKS') categoryName = 'Книги';
        else if (product.category === 'FOOD') categoryName = 'Еда';
        else if (product.category === 'SPORTS') categoryName = 'Спорт товары';
        else if (product.category === 'HOME') categoryName = 'Товары для дома';
        else if (product.category === 'BEAUTY') categoryName = 'Красота';
        else if (product.category === 'OTHER') categoryName = 'Другое';

        return `
        <div class="product-card" data-product-id="${product.id}" style="
            border: 1px solid #e2e8f0; 
            border-radius: 12px; 
            overflow: hidden; 
            background: white;
            transition: transform 0.2s, box-shadow 0.2s;
            cursor: pointer;
        ">
            <div class="product-image" style="height: 200px; overflow: hidden;">
                <img src="${imageUrl}" 
                     alt="${product.name || 'Товар'}" 
                     style="width: 100%; height: 100%; object-fit: cover;"
                     onerror="this.onerror=null; this.src='/images/product-img.png'">
            </div>
            <div class="product-info" style="padding: 16px;">
                <h3 class="product-name" style="
                    margin: 0 0 8px 0; 
                    font-size: 18px; 
                    font-weight: 600; 
                    color: #2d3748;
                    line-height: 1.4;
                ">
                    ${product.name || 'Без названия'}
                </h3>
                
                <div class="product-meta" style="margin-bottom: 12px;">
                    <span class="product-category" style="
                        display: inline-block;
                        background: #e8f4fd;
                        color: #3182ce;
                        padding: 4px 10px;
                        border-radius: 12px;
                        font-size: 12px;
                        font-weight: 500;
                    ">
                        ${categoryName}
                    </span>
                </div>
                
                <p class="product-description" style="
                    color: #718096; 
                    font-size: 14px; 
                    margin: 0 0 16px 0;
                    line-height: 1.5;
                    height: 42px;
                    overflow: hidden;
                    text-overflow: ellipsis;
                    display: -webkit-box;
                    -webkit-line-clamp: 2;
                    -webkit-box-orient: vertical;
                ">
                    ${(product.description || 'Описание отсутствует')}
                </p>
                
                <div class="product-price-row" style="
                    display: flex;
                    justify-content: space-between;
                    align-items: center;
                    margin-bottom: 16px;
                ">
                    <div class="product-price" style="
                        font-size: 22px; 
                        font-weight: bold; 
                        color: #2d3748;
                    ">
                        ${formatPrice(product.price || 0)}
                    </div>
                    
                    <div class="product-stock" style="
                        font-size: 14px;
                        color: ${(product.count > 0 ? '#38a169' : '#e53e3e')};
                        font-weight: 500;
                    ">
                        ${product.count > 0 ? `В наличии: ${product.count}` : 'Нет в наличии'}
                    </div>
                </div>
                
                <div class="product-actions" style="display: flex; gap: 10px;">
                    <button class="btn-add-to-cart" 
                            onclick="event.stopPropagation(); addToCart(${product.id})" 
                            style="
                                flex: 1; 
                                padding: 12px;
                                background: ${product.count > 0 ? '#3182ce' : '#cbd5e0'}; 
                                color: white; 
                                border: none; 
                                border-radius: 8px; 
                                cursor: ${product.count > 0 ? 'pointer' : 'not-allowed'};
                                font-size: 14px;
                                font-weight: 600;
                                transition: background 0.2s;
                            "
                            ${product.count <= 0 ? 'disabled' : ''}>
                        ${product.count > 0 ? 'В корзину' : 'Нет в наличии'}
                    </button>
                    
                    <button class="btn-view-details" 
                            onclick="event.stopPropagation(); viewProductDetails(${product.id})"
                            style="
                                flex: 1; 
                                padding: 12px;
                                background: #f7fafc; 
                                color: #4a5568; 
                                border: 1px solid #e2e8f0;
                                border-radius: 8px; 
                                cursor: pointer; 
                                font-size: 14px;
                                font-weight: 600;
                                transition: background 0.2s;
                            ">
                        Подробнее
                    </button>
                </div>
            </div>
        </div>
        `;
    }).join('');

    // Добавляем CSS для hover эффектов
    if (!document.querySelector('#product-card-styles')) {
        const style = document.createElement('style');
        style.id = 'product-card-styles';
        style.textContent = `
            .product-card:hover {
                transform: translateY(-4px);
                box-shadow: 0 10px 25px rgba(0,0,0,0.1);
            }
            .product-card:hover .product-name {
                color: #3182ce;
            }
            .btn-add-to-cart:hover:not(:disabled) {
                background: #2c5282 !important;
            }
            .btn-view-details:hover {
                background: #e2e8f0 !important;
            }
        `;
        document.head.appendChild(style);
    }

    makeProductCardsClickable();
}

// Обновленная функция formatPrice
function formatPrice(price) {
    if (typeof price === 'string') {
        price = parseFloat(price);
    }
    return new Intl.NumberFormat('ru-RU', {
        style: 'currency',
        currency: 'RUB',
        minimumFractionDigits: price % 1 === 0 ? 0 : 2,
        maximumFractionDigits: 2
    }).format(price || 0);
}
// Применение сортировки на клиенте
function applySorting(products) {
    const sortFilter = document.getElementById('sortFilter');
    const sortBy = sortFilter ? sortFilter.value : '';

    if (!sortBy) return products;

    return [...products].sort((a, b) => {
        switch(sortBy) {
            case 'name':
                return (a.name || '').localeCompare(b.name || '');
            case 'price_asc':
                return (a.price || 0) - (b.price || 0);
            case 'price_desc':
                return (b.price || 0) - (a.price || 0);
            default:
                return 0;
        }
    });
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
        sortFilter.addEventListener('change', function() {
            // Перезагружаем и сортируем товары
            const productsGrid = document.getElementById('productsGrid');
            const productCards = Array.from(productsGrid.querySelectorAll('.product-card'));

            if (productCards.length > 0) {
                // Сортируем на клиенте
                const products = productCards.map(card => {
                    return {
                        id: card.getAttribute('data-product-id'),
                        element: card
                    };
                });

                // Здесь можно применить сортировку
                displayProducts(JSON.parse(localStorage.getItem('lastProducts') || '[]'));
            }
        });
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

// Добавление в корзину (упрощенная версия)
async function addToCart(productId) {
    try {
        // Получаем данные о товаре с сервера - используем относительный путь
        const response = await fetch(`/api/products/${productId}`);
        if (!response.ok) {
            throw new Error('Товар не найден');
        }

        const product = await response.json();

        // Простая корзина через localStorage
        const cart = JSON.parse(localStorage.getItem('cart')) || [];
        const existingItem = cart.find(item => item.id == productId);

        if (existingItem) {
            existingItem.quantity += 1;
        } else {
            cart.push({
                id: product.id,
                name: product.name,
                price: product.price,
                image: product.image || '/images/product-img.png',
                quantity: 1
            });
        }

        localStorage.setItem('cart', JSON.stringify(cart));

        // Обновляем счетчик
        updateCartCount();

        // Показываем уведомление
        showNotification(`Товар "${product.name}" добавлен в корзину!`);

        // Отправляем событие обновления корзины
        window.dispatchEvent(new Event('cartUpdated'));

    } catch (error) {
        console.error('Ошибка при добавлении в корзину:', error);
        showNotification('Не удалось добавить товар в корзину');
    }
}

// Просмотр деталей товара
function viewProductDetails(productId) {
    // Используем относительный путь
    window.location.href = `/api/products/${productId}`;
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
window.formatPrice = formatPrice;