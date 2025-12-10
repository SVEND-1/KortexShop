// Скрипт для корзины
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
    cartItemsContainer.innerHTML = cartItems.map(item => `
        <div class="cart-item" data-product-id="${item.id}">
            <div class="item-image">
                <img src="${window.imageUploader.getImage(item.id)}" alt="${item.name}" 
                     onerror="this.src='images/product-img.png'">
            </div>
            <div class="item-details">
                <h3 class="item-name">${item.name}</h3>
                <p class="item-description">${window.productManager.getProductById(item.id)?.description || 'Описание товара'}</p>
                <div class="item-price">${formatPrice(item.price)}</div>
            </div>
            <div class="item-controls">
                <!-- Форма для изменения количества -->
                <div class="quantity-controls">
                    <button type="button" class="quantity-btn minus-btn" onclick="updateQuantity(${item.id}, ${item.quantity - 1})">-</button>
                    <span class="quantity">${item.quantity}</span>
                    <button type="button" class="quantity-btn plus-btn" onclick="updateQuantity(${item.id}, ${item.quantity + 1})">+</button>
                </div>
                
                <!-- Кнопка удаления -->
                <button type="button" class="remove-btn" onclick="removeFromCart(${item.id})">
                    Удалить
                </button>
            </div>
        </div>
    `).join('');

    // Обновляем итоги
    updateCartSummary();

    // Делаем товары кликабельными
    makeCartItemsClickable();
}

function updateCartSummary() {
    const totalItems = window.cartManager.getTotalItems();
    const totalPrice = window.cartManager.getTotalPrice();

    document.getElementById('totalItemsText').textContent = `Товары (${totalItems})`;
    document.getElementById('totalPrice').textContent = formatPrice(totalPrice);
    document.getElementById('finalTotal').textContent = formatPrice(totalPrice);
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
    window.cartManager.updateQuantity(productId, newQuantity);
}

function removeFromCart(productId) {
    if (confirm('Удалить товар из корзины?')) {
        window.cartManager.removeFromCart(productId);
    }
}

function checkout() {
    const order = window.cartManager.checkout();
    if (order) {
        alert(`Заказ оформлен! Номер заказа: ${order.id}\nСумма: ${formatPrice(order.total)}`);
        window.location.href = '/profile'; // Переходим в профиль чтобы увидеть историю
    } else {
        alert('Корзина пуста!');
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
    const product = window.productManager.getProductById(productId);
    if (product) {
        localStorage.setItem('currentProduct', JSON.stringify(product));
        window.location.href = '/productForm';
    }
}

function formatPrice(price) {
    return new Intl.NumberFormat('ru-RU', {
        style: 'currency',
        currency: 'RUB',
        minimumFractionDigits: 0
    }).format(price);
}