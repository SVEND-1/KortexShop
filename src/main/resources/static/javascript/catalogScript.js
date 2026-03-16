// catalogScript.js - для работы с Spring бекендом (jQuery версия)

// Глобальные переменные для пагинации
let currentPage = 0;
let totalPages = 1;
let totalElements = 0;
let pageSize = 12; // Товаров на странице

$(document).ready(function() {
    console.log('Каталог товаров загружен');
    initializeCatalog();
    // Загружаем товары при старте
    loadProducts(0);
});

function initializeCatalog() {
    setupFilters();
    setupSearch();
    setupPaginationListeners();
}

// Загрузка товаров с сервера с пагинацией
async function loadProducts(page = 0) {
    try {
        const $productsGrid = $('#productsGrid');
        $productsGrid.html(`
            <div class="loading-container">
                <div class="loading-spinner"></div>
                <p class="loading-text">Загрузка товаров...</p>
            </div>
        `);

        // Получаем параметры фильтрации
        const category = $('#categoryFilter').val() || '';
        const searchTerm = $('#search-input').val() || '';

        // Формируем URL с параметрами
        let url = '/api/products';
        const params = new URLSearchParams();

        // Параметры пагинации
        params.append('page', page);
        params.append('size', pageSize);

        // Параметры фильтрации
        if (category) params.append('category', category);
        if (searchTerm) params.append('query', searchTerm);
        // sort параметр НЕ передаем, так как его нет на бекенде

        if (params.toString()) {
            url += '?' + params.toString();
        }

        console.log('Запрашиваем URL:', url);

        // Делаем запрос к бекенду
        const response = await fetch(url, {
            method: 'GET',
            headers: {
                'Accept': 'application/json'
            }
        });

        if (!response.ok) {
            const errorText = await response.text();
            console.error('Ошибка ответа:', errorText);
            throw new Error(`Ошибка HTTP: ${response.status}`);
        }

        const data = await response.json();
        console.log('Получены данные:', data);

        // Проверяем структуру ответа
        if (data.content && Array.isArray(data.content)) {
            // Данные в формате Page
            const products = data.content;
            currentPage = data.page || data.number || 0;
            totalPages = data.totalPages || 1;
            totalElements = data.totalElements || 0;

            console.log(`Пагинация: страница ${currentPage + 1} из ${totalPages}, всего товаров: ${totalElements}`);

            // Отображаем товары
            displayProducts(products);

            // Обновляем информацию о странице
            updatePageInfo();

            // Обновляем пагинацию
            updatePagination();

        } else if (Array.isArray(data)) {
            // Просто массив товаров
            displayProducts(data);
            console.log('Пагинация не поддерживается сервером');
            $('.pagination-container').hide();

        } else {
            console.warn('Неизвестная структура данных:', data);
            showError('Неверный формат данных от сервера');
        }

    } catch (error) {
        console.error('Ошибка при загрузке товаров:', error);
        showError('Не удалось загрузить товары. Попробуйте позже.');
    }
}

// Отображение товаров
function displayProducts(products) {
    const $productsGrid = $('#productsGrid');

    if (!products || products.length === 0) {
        $productsGrid.html(`
            <div class="empty-catalog">
                <div class="empty-catalog-icon">📦</div>
                <h3>Товары не найдены</h3>
                <p>Попробуйте изменить параметры поиска или фильтрации</p>
                <button onclick="loadProducts(0)" class="btn-reset-filters">
                    Сбросить фильтры
                </button>
            </div>
        `);

        // Скрываем пагинацию, если нет товаров
        $('.pagination-container').hide();
        return;
    }

    // Показываем контейнер пагинации
    $('.pagination-container').show();

    const productsHtml = products.map(product => {
        // Формируем URL изображения
        const imageUrl = product.image
            ? `/uploads/images/${product.image}`
            : '/images/product-img.png';

        // Форматируем категорию
        let categoryName = product.category || 'Без категории';
        if (product.category === 'ELECTRONICS') categoryName = 'Электроника';
        else if (product.category === 'CLOTHING') categoryName = 'Одежда';
        else if (product.category === 'BOOKS') categoryName = 'Книги';
        else if (product.category === 'FOOD') categoryName = 'Еда';
        else if (product.category === 'SPORTS') categoryName = 'Спорт товары';
        else if (product.category === 'HOME') categoryName = 'Товары для дома';
        else if (product.category === 'BEAUTY') categoryName = 'Красота';
        else if (product.category === 'OTHER') categoryName = 'Другое';

        // Проверяем наличие
        const inStock = product.count > 0;
        const stockText = inStock ? `В наличии: ${product.count}` : 'Нет в наличии';
        const stockClass = inStock ? 'in-stock' : 'out-of-stock';

        return `
        <div class="product-card" data-product-id="${product.id}">
            <div class="product-image">
                <img src="${imageUrl}" 
                     alt="${product.name || 'Товар'}" 
                     onerror="this.onerror=null; this.src='/images/product-img.png'">
            </div>
            <div class="product-info">
                <h3 class="product-name">
                    ${product.name || 'Без названия'}
                </h3>
                
                <div class="product-meta">
                    <span class="product-category">
                        ${categoryName}
                    </span>
                </div>
                
                <p class="product-description">
                    ${(product.description || 'Описание отсутствует')}
                </p>
                
                <div class="product-price-row">
                    <div class="product-price">
                        ${formatPrice(product.price || 0)}
                    </div>
                    
                    <div class="product-stock ${stockClass}">
                        ${stockText}
                    </div>
                </div>
                
                <div class="product-actions">
                    <button class="btn-add-to-cart" 
                            onclick="event.stopPropagation(); addToCartViaAPI(${product.id})" 
                            ${!inStock ? 'disabled' : ''}>
                        ${inStock ? 'В корзину' : 'Нет в наличии'}
                    </button>
                    
                    <button class="btn-view-details" 
                            onclick="event.stopPropagation(); viewProductDetails(${product.id})">
                        Подробнее
                    </button>
                </div>
            </div>
        </div>
        `;
    }).join('');

    $productsGrid.html(productsHtml);
    makeProductCardsClickable();
}

// Настройка слушателей для пагинации
function setupPaginationListeners() {
    const $prevBtn = $('#prevPage');
    const $nextBtn = $('#nextPage');

    $prevBtn.on('click', function() {
        if (currentPage > 0) {
            loadProducts(currentPage - 1);
        }
    });

    $nextBtn.on('click', function() {
        if (currentPage < totalPages - 1) {
            loadProducts(currentPage + 1);
        }
    });
}

// Обновление информации о странице
function updatePageInfo() {
    const $shownItems = $('#shownItems');
    const $totalItems = $('#totalItems');

    if ($shownItems.length && $totalItems.length) {
        const startItem = currentPage * pageSize + 1;
        const endItem = Math.min((currentPage + 1) * pageSize, totalElements);

        $shownItems.text(`${startItem}-${endItem}`);
        $totalItems.text(totalElements);
    }
}

// Обновление пагинации
function updatePagination() {
    const $prevBtn = $('#prevPage');
    const $nextBtn = $('#nextPage');
    const $paginationNumbers = $('#paginationNumbers');

    // Обновляем кнопки "Назад" и "Вперед"
    $prevBtn.prop('disabled', currentPage === 0);
    $nextBtn.prop('disabled', currentPage === totalPages - 1 || totalPages === 0);

    // Генерируем номера страниц
    if ($paginationNumbers.length && totalPages > 1) {
        let pageNumbers = '';
        const maxVisiblePages = 7;

        let startPage = Math.max(0, currentPage - Math.floor(maxVisiblePages / 2));
        let endPage = Math.min(totalPages - 1, startPage + maxVisiblePages - 1);

        // Корректируем, если мы в начале
        if (endPage - startPage + 1 < maxVisiblePages) {
            startPage = Math.max(0, endPage - maxVisiblePages + 1);
        }

        // Первая страница
        if (startPage > 0) {
            pageNumbers += `
                <button class="pagination-number" onclick="loadProducts(0)">1</button>
                ${startPage > 1 ? '<span class="pagination-ellipsis">...</span>' : ''}
            `;
        }

        // Страницы
        for (let i = startPage; i <= endPage; i++) {
            pageNumbers += `
                <button class="pagination-number ${i === currentPage ? 'active' : ''}" 
                        onclick="loadProducts(${i})">
                    ${i + 1}
                </button>
            `;
        }

        // Последняя страница
        if (endPage < totalPages - 1) {
            pageNumbers += `
                ${endPage < totalPages - 2 ? '<span class="pagination-ellipsis">...</span>' : ''}
                <button class="pagination-number" onclick="loadProducts(${totalPages - 1})">
                    ${totalPages}
                </button>
            `;
        }

        $paginationNumbers.html(pageNumbers);
    }
}

// Форматирование цены
function formatPrice(price) {
    if (typeof price === 'string') {
        price = parseFloat(price);
    }
    return new Intl.NumberFormat('ru-RU', {
        style: 'currency',
        currency: 'RUB',
        minimumFractionDigits: 0,
        maximumFractionDigits: 2
    }).format(price || 0);
}

// Настройка фильтров
function setupFilters() {
    const $categoryFilter = $('#categoryFilter');

    if ($categoryFilter.length) {
        // Убедимся, что категории уже загружены
        if ($categoryFilter.find('option').length <= 1) {
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

            // Добавляем опции, если их нет
            categories.forEach(cat => {
                if (!$categoryFilter.find(`option[value="${cat.value}"]`).length) {
                    $categoryFilter.append($('<option>', {
                        value: cat.value,
                        text: cat.name
                    }));
                }
            });
        }

        $categoryFilter.on('change', function() {
            currentPage = 0;
            loadProducts(0);
        });
    }

    // Убираем сортировку, так как её нет на бекенде
    const $sortFilter = $('#sortFilter');
    if ($sortFilter.length) {
        $sortFilter.hide(); // Скрываем фильтр сортировки
    }
}

// Настройка поиска
function setupSearch() {
    const $searchForm = $('.search-form');
    const $searchInput = $('#search-input');

    $searchForm.on('submit', function(e) {
        e.preventDefault();
        currentPage = 0;
        loadProducts(0);
    });

    if ($searchInput.length) {
        let searchTimeout;
        $searchInput.on('input', function() {
            clearTimeout(searchTimeout);
            searchTimeout = setTimeout(() => {
                currentPage = 0;
                loadProducts(0);
            }, 500);
        });
    }
}

async function addToCartViaAPI(productId) {
    try {
        const response = await fetch(`/api/carts/items?productId=${productId}`, {
            method: "POST",
            credentials: "include", // важно!
            headers: { "Accept": "application/json" }
        });

        // 🔥 ПРОВЕРКА: Spring Security отредиректил на /login
        if (response.redirected || response.url.endsWith("/login")) {
            window.location.href = "/login";
            return;
        }

        // 🔥 ПРОВЕРКА: на случай, если вернулся HTML, а не JSON
        const contentType = response.headers.get("content-type");
        if (contentType && contentType.includes("text/html")) {
            // Значит, мы получили HTML страницы логина
            window.location.href = "/login";
            return;
        }

        if (!response.ok) {
            const err = await response.json().catch(() => ({}));
            throw new Error(err.error || "Ошибка при добавлении товара");
        }

        showNotification("Товар добавлен в корзину!");
        await updateCartCounter();

    } catch (e) {
        console.error(e);
        showNotification("Ошибка: " + e.message);
    }
}

// Обновление счетчика корзины
async function updateCartCounter() {
    try {
        const response = await fetch('/api/carts/me', {
            method: 'GET',
            headers: {
                'Accept': 'application/json'
            }
        });

        if (response.ok) {
            const cartData = await response.json();
            const totalItems = cartData.totalItems || 0;

            // Обновляем счетчик в шапке
            const $cartLink = $('a[href="/cart"]');
            if ($cartLink.length) {
                // Удаляем старый счетчик
                $cartLink.find('.cart-counter').remove();

                if (totalItems > 0) {
                    const $counter = $('<span>', {
                        class: 'cart-counter',
                        text: totalItems > 9 ? '9+' : totalItems
                    });

                    $cartLink.css('position', 'relative').append($counter);
                }
            }
        }
    } catch (error) {
        console.error('Ошибка при обновлении счетчика корзины:', error);
    }
}

// Просмотр деталей товара
function viewProductDetails(productId) {
    window.location.href = `/productForm?id=${productId}`;
}

// Делаем карточки товаров кликабельными
function makeProductCardsClickable() {
    $('.product-card').on('click', function(e) {
        if (!$(e.target).closest('button').length) {
            const productId = $(this).data('product-id');
            viewProductDetails(parseInt(productId));
        }
    });
}

// Показ уведомлений
function showNotification(message) {
    // Удаляем старые уведомления
    $('.notification').remove();

    const $notification = $('<div>', {
        class: 'notification',
        css: {
            position: 'fixed',
            top: '20px',
            right: '20px',
            background: '#28a745',
            color: 'white',
            padding: '15px 20px',
            borderRadius: '8px',
            boxShadow: '0 4px 12px rgba(0,0,0,0.15)',
            zIndex: 1000,
            transform: 'translateX(100%)',
            opacity: 0,
            transition: 'transform 0.3s ease, opacity 0.3s ease'
        },
        text: message
    });

    $('body').append($notification);

    setTimeout(() => {
        $notification.css({
            transform: 'translateX(0)',
            opacity: 1
        });
    }, 10);

    setTimeout(() => {
        $notification.css({
            transform: 'translateX(100%)',
            opacity: 0
        });
        setTimeout(() => $notification.remove(), 300);
    }, 3000);
}

// Показ ошибки
function showError(message) {
    $('#productsGrid').html(`
        <div class="error-message">
            <div class="error-icon">⚠️</div>
            <h3>Ошибка загрузки</h3>
            <p>${message}</p>
            <button onclick="loadProducts(0)" class="btn-retry">
                Попробовать снова
            </button>
        </div>
    `);

    // Скрываем пагинацию при ошибке
    $('.pagination-container').hide();
}

// Экспортируем функции
window.loadProducts = loadProducts;
window.addToCartViaAPI = addToCartViaAPI;
window.viewProductDetails = viewProductDetails;
window.formatPrice = formatPrice;
window.updateCartCounter = updateCartCounter;