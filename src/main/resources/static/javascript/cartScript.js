// cartScript.js - исправленная версия с перенаправлением на checkout

document.addEventListener('DOMContentLoaded', function() {
    console.log('Страница корзины загружена');
    initializeCart();

    // Слушаем обновления корзины
    window.addEventListener('cartUpdated', function() {
        displayCartItems();
    });
});

function initializeCart() {
    displayCartItems();
    setupCartEvents();
}

function displayCartItems() {
    const cartItems = window.cartManager.getCartItems();
    const cartItemsContainer = document.getElementById('cartItems');
    const emptyCart = document.getElementById('emptyCart');
    const cartWithItems = document.getElementById('cartWithItems');

    if (cartItems.length === 0) {
        emptyCart.style.display = 'block';
        cartWithItems.style.display = 'none';
        return;
    }

    emptyCart.style.display = 'none';
    cartWithItems.style.display = 'block';

    // Отображаем товары
    cartItemsContainer.innerHTML = cartItems.map(item => {
        // Формируем правильный путь к изображению
        const imagePath = item.image
            ? `/uploads/images/${item.image}`
            : (window.imageUploader ? window.imageUploader.getImage(item.id) : '/images/product-img.png');

        // Получаем описание товара
        const description = window.productManager?.getProductById(item.id)?.description ||
            item.description || 'Описание товара';

        return `
        <div class="cart-item" data-product-id="${item.id}">
            <div class="item-image">
                <img src="${imagePath}" alt="${item.name}" 
                     onerror="this.onerror=null; this.src='/images/product-img.png'"
                     style="width: 100px; height: 100px; object-fit: cover; border-radius: 8px;">
            </div>
            <div class="item-details">
                <h3 class="item-name">${item.name}</h3>
                <p class="item-description" style="color: #666; font-size: 14px; margin: 5px 0;">
                    ${description.substring(0, 100)}${description.length > 100 ? '...' : ''}
                </p>
                <div class="item-price" style="font-size: 18px; font-weight: bold; color: #2d3748;">
                    ${formatPrice(item.price)}
                </div>
                <div class="item-subtotal" style="color: #718096; font-size: 14px;">
                    Итого: ${formatPrice(item.price * item.quantity)}
                </div>
            </div>
            <div class="item-controls">
                <!-- Форма для изменения количества -->
                <div class="quantity-controls" style="display: flex; align-items: center; gap: 10px;">
                    <button type="button" class="quantity-btn minus-btn" 
                            onclick="event.stopPropagation(); updateQuantity(${item.id}, ${item.quantity - 1})"
                            style="padding: 5px 15px; background: #e2e8f0; border: none; border-radius: 4px; cursor: pointer;">
                        -
                    </button>
                    <span class="quantity" style="font-weight: bold;">${item.quantity}</span>
                    <button type="button" class="quantity-btn plus-btn" 
                            onclick="event.stopPropagation(); updateQuantity(${item.id}, ${item.quantity + 1})"
                            style="padding: 5px 15px; background: #e2e8f0; border: none; border-radius: 4px; cursor: pointer;">
                        +
                    </button>
                </div>
                
                <!-- Кнопка удаления -->
                <button type="button" class="remove-btn" 
                        onclick="event.stopPropagation(); removeFromCart(${item.id})"
                        style="padding: 8px 16px; background: #fed7d7; color: #c53030; border: none; border-radius: 6px; cursor: pointer; margin-top: 10px;">
                    Удалить
                </button>
            </div>
        </div>
        `;
    }).join('');

    // Обновляем итоги
    updateCartSummary();

    // Делаем товары кликабельными
    makeCartItemsClickable();
}

function updateCartSummary() {
    const cartItems = window.cartManager.getCartItems();
    const totalItems = cartItems.reduce((sum, item) => sum + item.quantity, 0);
    const totalPrice = cartItems.reduce((sum, item) => sum + (item.price * item.quantity), 0);

    const totalItemsElement = document.getElementById('totalItemsText');
    const totalPriceElement = document.getElementById('totalPrice');
    const finalTotalElement = document.getElementById('finalTotal');

    if (totalItemsElement) {
        totalItemsElement.textContent = `Товары (${totalItems})`;
    }
    if (totalPriceElement) {
        totalPriceElement.textContent = formatPrice(totalPrice);
    }
    if (finalTotalElement) {
        finalTotalElement.textContent = formatPrice(totalPrice);
    }
}

function setupCartEvents() {
    // Обработчик оформления заказа
    const checkoutBtn = document.getElementById('checkoutBtn');
    if (checkoutBtn) {
        checkoutBtn.addEventListener('click', function(e) {
            e.preventDefault();
            checkout();
        });
    }
}

function updateQuantity(productId, newQuantity) {
    if (newQuantity < 1) {
        if (confirm('Удалить товар из корзины?')) {
            window.cartManager.removeFromCart(productId);
        }
        return;
    }

    // Обновляем количество через cartManager
    if (window.cartManager && window.cartManager.updateQuantity) {
        window.cartManager.updateQuantity(productId, newQuantity);
    } else {
        // Fallback: обновляем localStorage напрямую
        const cart = JSON.parse(localStorage.getItem('cart')) || [];
        const itemIndex = cart.findIndex(item => item.id == productId);

        if (itemIndex !== -1) {
            cart[itemIndex].quantity = newQuantity;
            localStorage.setItem('cart', JSON.stringify(cart));
            displayCartItems();
        }
    }
}

function removeFromCart(productId) {
    if (confirm('Удалить товар из корзины?')) {
        if (window.cartManager && window.cartManager.removeFromCart) {
            window.cartManager.removeFromCart(productId);
        } else {
            // Fallback: удаляем из localStorage
            let cart = JSON.parse(localStorage.getItem('cart')) || [];
            cart = cart.filter(item => item.id != productId);
            localStorage.setItem('cart', JSON.stringify(cart));
            displayCartItems();
        }
    }
}

function checkout() {
    try {
        // Получаем текущую корзину
        const cart = JSON.parse(localStorage.getItem('cart')) || [];

        if (cart.length === 0) {
            alert('Корзина пуста! Добавьте товары для оформления заказа.');
            return;
        }

        // Просто перенаправляем на страницу оформления заказа
        window.location.href = '/checkout';

    } catch (error) {
        console.error('Ошибка при переходе к оформлению:', error);
        alert('Произошла ошибка при переходе к оформлению заказа. Попробуйте позже.');
    }
}

function makeCartItemsClickable() {
    const cartItems = document.querySelectorAll('.cart-item');
    cartItems.forEach(item => {
        item.addEventListener('click', function(e) {
            if (!e.target.closest('button')) {
                const productId = this.getAttribute('data-product-id');
                viewProductDetails(parseInt(productId));
            }
        });
    });
}

function viewProductDetails(productId) {
    window.location.href = `/product/${productId}`;
}

function formatPrice(price) {
    return new Intl.NumberFormat('ru-RU', {
        style: 'currency',
        currency: 'RUB',
        minimumFractionDigits: 0
    }).format(price);
}

// Экспортируем функции для использования
window.cartPage = {
    displayCartItems,
    updateCartSummary,
    updateQuantity,
    removeFromCart,
    checkout,
    formatPrice
};