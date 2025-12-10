// productNavigation.js - для навигации по товарам

// Функция для открытия страницы товара
function openProductInfo(productId) {
    console.log('Открываем товар ID:', productId);
    window.location.href = `productForm.html?id=${productId}`;
}

// Функция для получения данных о товаре
async function getProductData(productId) {
    try {
        // Сначала пробуем получить с сервера
        const response = await fetch(`/api/products/${productId}`);
        if (response.ok) {
            return await response.json();
        }
    } catch (error) {
        console.error('Ошибка при запросе к серверу:', error);
    }

    // Если сервер не отвечает, пробуем локально
    if (window.productManager) {
        return window.productManager.getProductById(productId);
    }

    return null;
}

// Автоматически делаем все товары кликабельными
document.addEventListener('DOMContentLoaded', function() {
    makeAllProductsClickable();
});

// Функция для автоматического добавления обработчиков клика
function makeAllProductsClickable() {
    // Для товаров в корзине
    const cartItems = document.querySelectorAll('.cart-item');
    cartItems.forEach(item => {
        const productId = item.getAttribute('data-product-id');
        if (productId) {
            item.style.cursor = 'pointer';
            item.addEventListener('click', function(e) {
                if (!e.target.closest('button') && !e.target.closest('form')) {
                    openProductInfo(parseInt(productId));
                }
            });
        }
    });

    // Для товаров в истории заказов
    const orderItems = document.querySelectorAll('.order-item');
    orderItems.forEach(item => {
        const productId = item.getAttribute('data-product-id');
        if (productId) {
            item.style.cursor = 'pointer';
            item.addEventListener('click', function(e) {
                if (!e.target.closest('button')) {
                    openProductInfo(parseInt(productId));
                }
            });
        }
    });

    // Для товаров на главной странице
    const productItems = document.querySelectorAll('.product-item');
    productItems.forEach(item => {
        const productId = item.getAttribute('data-product-id');
        if (productId) {
            item.style.cursor = 'pointer';
            item.addEventListener('click', function() {
                openProductInfo(parseInt(productId));
            });
        }
    });

    // Для карточек товаров в каталоге
    const productCards = document.querySelectorAll('.product-card');
    productCards.forEach(card => {
        const productId = card.getAttribute('data-product-id');
        if (productId) {
            card.style.cursor = 'pointer';
            card.addEventListener('click', function(e) {
                if (!e.target.closest('button')) {
                    openProductInfo(parseInt(productId));
                }
            });
        }
    });
}

// Экспортируем функции
window.openProductInfo = openProductInfo;
window.getProductData = getProductData;