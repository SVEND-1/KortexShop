// cartScript.js - отображение корзины с плавным обновлением

let cartManager = null;
let isUpdating = false;

async function initCartPage() {
    if (!window.cartManager) {
        console.error('cartManager не найден');
        return;
    }

    cartManager = window.cartManager;
    await cartManager.ready();

    await renderCart();

    // Слушаем событие обновления корзины
    window.addEventListener('cartUpdated', async function(event) {
        console.log('Событие cartUpdated, обновляем корзину');
        if (!isUpdating) {
            await renderCart();
        }
    });
}

async function renderCart() {
    if (!cartManager || isUpdating) return;

    isUpdating = true;

    // Сохраняем позицию скролла
    const scrollPosition = window.scrollY;

    try {
        const items = cartManager.getItems();
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
                const subtotal = price * quantity;

                return `
                <div class="cart-item" data-item-id="${item.id}">
                    <div class="item-image">
                        <img src="${imagePath}" alt="${name}" 
                             onerror="this.onerror=null;this.src='/images/product-img.png'">
                    </div>
                    <div class="item-details">
                        <h3 class="item-name">${escapeHtml(name)}</h3>
                        <div class="item-price">${formatPrice(price)}</div>
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

        // Восстанавливаем позицию скролла
        window.scrollTo(0, scrollPosition);

    } catch (error) {
        console.error('Ошибка при рендере:', error);
    } finally {
        isUpdating = false;
    }
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

    // Блокируем кнопку и показываем загрузку
    button.disabled = true;
    const originalText = button.textContent;
    if (action === 'increase' || action === 'decrease') {
        button.textContent = '...';
    }

    try {
        if (action === 'increase') {
            await cartManager.increase(itemId);
        } else if (action === 'decrease') {
            await cartManager.decrease(itemId);
        } else if (action === 'remove') {
            if (!confirm('Удалить товар из корзины?')) {
                button.disabled = false;
                button.textContent = originalText;
                return;
            }
            await cartManager.remove(itemId);
        }

        // Обновляем отображение без перезагрузки
        await renderCart();

    } catch (error) {
        console.error('Ошибка:', error);
        alert('Не удалось выполнить действие: ' + error.message);
        button.disabled = false;
        button.textContent = originalText;
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