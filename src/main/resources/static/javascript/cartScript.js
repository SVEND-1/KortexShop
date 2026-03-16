// cartScript.js - отображение корзины с сервера (jQuery версия)
// Ждем cartManager и обновляем при событиях

let cartManager = null;

async function initCartPage() {
    // Ждем пока cartManager загрузится
    if (!window.cartManager) {
        console.error('cartManager не найден');
        return;
    }

    cartManager = window.cartManager;
    await cartManager.ready();

    // Первоначальная отрисовка
    renderCart();

    // Слушаем события обновления корзины (jQuery способ)
    $(window).on('cartUpdated', function(event, cartData) {
        renderCart();
    });
}

function renderCart() {
    if (!cartManager) return;

    const items = cartManager.getItems();
    const total = cartManager.getTotal();

    const $cartItemsContainer = $('#cartItems');
    const $emptyCart = $('#emptyCart');
    const $cartWithItems = $('#cartWithItems');
    const $totalItemsText = $('#totalItemsText');
    const $totalPrice = $('#totalPrice');
    const $finalTotal = $('#finalTotal');
    const $checkoutBtn = $('#checkoutBtn');

    if (!items || items.length === 0) {
        // Показываем пустую корзину
        $emptyCart.show();
        $cartWithItems.hide();

        if ($totalItemsText.length) $totalItemsText.text('Товары (0)');
        if ($totalPrice.length) $totalPrice.text(formatPrice(0));
        if ($finalTotal.length) $finalTotal.text(formatPrice(0));
        if ($cartItemsContainer.length) $cartItemsContainer.empty();

        // Деактивируем кнопку оформления
        if ($checkoutBtn.length) {
            $checkoutBtn.prop('disabled', true).css('opacity', '0.6');
        }
        return;
    }

    // Показываем корзину с товарами
    $emptyCart.hide();
    $cartWithItems.show();

    // Активируем кнопку оформления
    if ($checkoutBtn.length) {
        $checkoutBtn.prop('disabled', false).css('opacity', '1');
    }

    // Отрисовываем товары
    if ($cartItemsContainer.length) {
        const itemsHtml = items.map(item => {
            const imagePath = item.image ? `/uploads/images/${item.image}` : '/images/product-img.png';
            const name = item.productName || item.name || 'Товар';
            const price = Number(item.price) || 0;
            const quantity = Number(item.quantity) || 1;
            const subtotal = price * quantity;

            return `
            <div class="cart-item" data-item-id="${item.id}">
                <div class="item-image">
                    <img src="${imagePath}" alt="${name}" 
                         onerror="this.onerror=null;this.src='/images/product-img.png'">
                </div>
                <div class="item-details">
                    <h3 class="item-name">${escapeHtml(name)}</h3>
                    <p class="item-description">${escapeHtml(item.description || '').substring(0,120)}${(item.description && item.description.length > 120) ? '...' : ''}</p>
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

        $cartItemsContainer.html(itemsHtml);
    }

    // Обновляем итоги
    const uniqueCount = cartManager.getUniqueCount();
    if ($totalItemsText.length) $totalItemsText.text(`Товары (${uniqueCount})`);
    if ($totalPrice.length) $totalPrice.text(formatPrice(total));
    if ($finalTotal.length) $finalTotal.text(formatPrice(total));

    // Вешаем обработчики событий
    attachCartEvents();
}

// Вешаем обработчики на кнопки
function attachCartEvents() {
    const $container = $('#cartItems');
    if (!$container.length) return;

    // Удаляем старые обработчики и вешаем новые
    $container.off('click', handleCartClick);
    $container.on('click', handleCartClick);
}

// Обработчик кликов по кнопкам в корзине
async function handleCartClick(event) {
    const $button = $(event.target).closest('[data-action]');
    if (!$button.length) return;

    event.preventDefault();

    const action = $button.data('action');
    const itemId = $button.data('id');

    if (!action || !itemId) return;

    // Блокируем кнопку на время запроса
    $button.prop('disabled', true);

    try {
        if (action === 'increase') {
            await cartManager.increase(itemId);
        } else if (action === 'decrease') {
            await cartManager.decrease(itemId);
        } else if (action === 'remove') {
            if (!confirm('Удалить товар из корзины?')) {
                $button.prop('disabled', false);
                return;
            }
            await cartManager.remove(itemId);
        }

        // КОРЗИНА ОБНОВИТСЯ АВТОМАТИЧЕСКИ ЧЕРЕЗ СОБЫТИЕ cartUpdated
        // которое отправляет cartManager после успешного запроса

    } catch (error) {
        console.error('Ошибка:', error);
        alert('Не удалось выполнить действие: ' + error.message);
        $button.prop('disabled', false);
    }
}

// Форматирование цены
function formatPrice(price) {
    return new Intl.NumberFormat('ru-RU', {
        style: 'currency',
        currency: 'RUB',
        minimumFractionDigits: 0
    }).format(price || 0);
}

// Безопасное HTML
function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

// Запускаем при загрузке страницы (jQuery способ)
$(document).ready(initCartPage);