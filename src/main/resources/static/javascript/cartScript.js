// cartScript.js - отображение корзины с правильным расчетом суммы

let cartManager = null;

async function initCartPage() {
    if (!window.cartManager) {
        console.error('cartManager не найден');
        return;
    }

    cartManager = window.cartManager;
    await cartManager.ready();

    renderCart();

    window.addEventListener('cartUpdated', function(event) {
        console.log('Событие cartUpdated, обновляем корзину');
        renderCart();
    });
}

function renderCart() {
    if (!cartManager) return;

    const items = cartManager.getItems();
    // Используем правильную сумму из cartManager
    const total = cartManager.getTotal();

    console.log('Рендер корзины:', { itemsCount: items.length, total: total });

    // Детальный лог каждого товара
    items.forEach(item => {
        console.log(`Товар: ${item.productName}, цена: ${item.price}, кол-во: ${item.quantity}, сумма: ${item.price * item.quantity}`);
    });

    const cartItemsContainer = document.getElementById('cartItems');
    const emptyCart = document.getElementById('emptyCart');
    const cartWithItems = document.getElementById('cartWithItems');
    const totalItemsText = document.getElementById('totalItemsText');
    const totalPrice = document.getElementById('totalPrice');
    const finalTotal = document.getElementById('finalTotal');
    const checkoutBtn = document.getElementById('checkoutBtn');

    if (!items || items.length === 0) {
        if (emptyCart) emptyCart.style.display = 'block';
        if (cartWithItems) cartWithItems.style.display = 'none';
        if (totalItemsText) totalItemsText.textContent = 'Товары (0)';
        if (totalPrice) totalPrice.textContent = formatPrice(0);
        if (finalTotal) finalTotal.textContent = formatPrice(0);
        if (cartItemsContainer) cartItemsContainer.innerHTML = '';
        if (checkoutBtn) {
            checkoutBtn.disabled = true;
            checkoutBtn.style.opacity = '0.6';
        }
        return;
    }

    if (emptyCart) emptyCart.style.display = 'none';
    if (cartWithItems) cartWithItems.style.display = 'block';
    if (checkoutBtn) {
        checkoutBtn.disabled = false;
        checkoutBtn.style.opacity = '1';
    }

    // Отрисовываем товары с правильной суммой
    if (cartItemsContainer) {
        const itemsHtml = items.map(item => {
            const imagePath = item.image ? `/uploads/images/${item.image}` : '/images/product-img.png';
            const name = item.productName || 'Товар';
            const price = Number(item.price) || 0;
            const quantity = Number(item.quantity) || 1;
            const subtotal = price * quantity; // ПРАВИЛЬНЫЙ расчет суммы товара

            return `
            <div class="cart-item" data-item-id="${item.id}">
                <div class="item-image">
                    <img src="${imagePath}" alt="${name}" 
                         onerror="this.onerror=null;this.src='/images/product-img.png'">
                </div>
                <div class="item-details">
                    <h3 class="item-name">${escapeHtml(name)}</h3>
                    <div class="item-price">${formatPrice(price)}</div>
                    <div class="item-subtotal">Итого: ${formatPrice(subtotal)}</div>
                </div>
                <div class="item-controls">
                    <div class="quantity-controls">
                        <button type="button" class="quantity-btn minus-btn" 
                                data-action="decrease" data-id="${item.id}"
                                ${quantity <= 1 ? 'disabled' : ''}>-</button>
                        <span class="quantity">${quantity}</span>
                        <button type="button" class="quantity-btn plus-btn" 
                                data-action="increase" data-id="${item.id}">+</button>
                    </div>
                    <button type="button" class="remove-btn" 
                            data-action="remove" data-id="${item.id}">Удалить</button>
                </div>
            </div>`;
        }).join('');

        cartItemsContainer.innerHTML = itemsHtml;
    }

    // Обновляем итоговые суммы
    const uniqueCount = items.length;
    if (totalItemsText) totalItemsText.textContent = `Товары (${uniqueCount})`;
    if (totalPrice) totalPrice.textContent = formatPrice(total);
    if (finalTotal) finalTotal.textContent = formatPrice(total);

    attachCartEvents();
}

function attachCartEvents() {
    const container = document.getElementById('cartItems');
    if (!container) return;

    // Обработчики для кнопок
    document.querySelectorAll('[data-action]').forEach(btn => {
        btn.removeEventListener('click', handleCartClick);
        btn.addEventListener('click', handleCartClick);
    });
}

async function handleCartClick(event) {
    const button = event.target.closest('[data-action]');
    if (!button) return;

    event.preventDefault();

    const action = button.dataset.action;
    const itemId = button.dataset.id;

    if (!action || !itemId) return;

    button.disabled = true;

    try {
        if (action === 'increase') {
            await cartManager.increase(itemId);
        } else if (action === 'decrease') {
            await cartManager.decrease(itemId);
        } else if (action === 'remove') {
            if (!confirm('Удалить товар из корзины?')) {
                button.disabled = false;
                return;
            }
            await cartManager.remove(itemId);
        }

    } catch (error) {
        console.error('Ошибка:', error);
        alert('Не удалось выполнить действие: ' + error.message);
        button.disabled = false;
    }
}

function formatPrice(price) {
    return new Intl.NumberFormat('ru-RU', {
        style: 'currency',
        currency: 'RUB',
        minimumFractionDigits: 0,
        maximumFractionDigits: 0
    }).format(price || 0);
}

function escapeHtml(text) {
    if (!text) return '';
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

document.addEventListener('DOMContentLoaded', initCartPage);