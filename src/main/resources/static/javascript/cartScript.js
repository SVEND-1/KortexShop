// cartScript.js - renders cartForm.html DOM using server DTO (format A)
// Relies on cartManager singleton being available and initialized.
// Uses element IDs present in cartForm.html: cartItems, totalItemsText, totalPrice, finalTotal, emptyCart, cartWithItems

async function initCartPage() {
    // wait for initial load
    if (!window.cartManager) {
        console.error('cartManager not found');
        return;
    }
    await window.cartManager.ready();
    renderCart();
}

// Render logic
function renderCart() {
    const items = window.cartManager.getItems();
    const total = window.cartManager.getTotal();

    const cartItemsContainer = document.getElementById('cartItems');
    const emptyCart = document.getElementById('emptyCart');
    const cartWithItems = document.getElementById('cartWithItems');
    const totalItemsText = document.getElementById('totalItemsText');
    const totalPrice = document.getElementById('totalPrice');
    const finalTotal = document.getElementById('finalTotal');

    if (!cartItemsContainer || !emptyCart || !cartWithItems) {
        console.error('Cart DOM elements missing');
        return;
    }

    if (!items || items.length === 0) {
        emptyCart.style.display = 'block';
        cartWithItems.style.display = 'none';
        if (totalItemsText) totalItemsText.textContent = 'Товары (0)';
        if (totalPrice) totalPrice.textContent = formatPrice(0);
        if (finalTotal) finalTotal.textContent = formatPrice(0);
        cartItemsContainer.innerHTML = '';
        return;
    }

    emptyCart.style.display = 'none';
    cartWithItems.style.display = 'block';

    // Render each item
    cartItemsContainer.innerHTML = items.map(item => {
        const imagePath = item.image ? `/uploads/images/${item.image}` : '/images/product-img.png';
        const name = item.productName || item.name || 'Товар';
        const price = (typeof item.price === 'number' ? item.price : Number(item.price || 0));
        const quantity = (typeof item.quantity === 'number' ? item.quantity : Number(item.quantity || 1));
        const subtotal = price * quantity;
        return `
        <div class="cart-item" data-item-id="${item.id}">
            <div class="item-image">
                <img src="${imagePath}" alt="${name}" onerror="this.onerror=null;this.src='/images/product-img.png'" style="width:100px;height:100px;object-fit:cover;border-radius:8px;">
            </div>
            <div class="item-details">
                <h3 class="item-name">${escapeHtml(name)}</h3>
                <p class="item-description">${escapeHtml((item.description||'').substring(0,120))}${(item.description && item.description.length>120)?'...':''}</p>
                <div class="item-price">${formatPrice(price)}</div>
                <div class="item-subtotal">Итого: ${formatPrice(subtotal)}</div>
            </div>
            <div class="item-controls">
                <div class="quantity-controls">
                    <button type="button" class="quantity-btn minus-btn" data-action="decrease" data-id="${item.id}">-</button>
                    <span class="quantity">${quantity}</span>
                    <button type="button" class="quantity-btn plus-btn" data-action="increase" data-id="${item.id}">+</button>
                </div>
                <button type="button" class="remove-btn" data-action="remove" data-id="${item.id}">Удалить</button>
            </div>
        </div>`;
    }).join('');

    // Update summary
    const uniqueCount = window.cartManager.getUniqueCount();
    if (totalItemsText) totalItemsText.textContent = `Товары (${uniqueCount})`;
    if (totalPrice) totalPrice.textContent = formatPrice(total);
    if (finalTotal) finalTotal.textContent = formatPrice(total);

    // Attach event listeners (delegation)
    attachCartEvents();
}

// event delegation for cart items
function attachCartEvents() {
    const container = document.getElementById('cartItems');
    if (!container) return;
    container.querySelectorAll('[data-action]').forEach(btn => {
        btn.removeEventListener('click', cartActionHandler);
        btn.addEventListener('click', cartActionHandler);
    });
}

async function cartActionHandler(e) {
    e.preventDefault();
    const el = e.currentTarget;
    const action = el.getAttribute('data-action');
    const id = el.getAttribute('data-id');
    if (!action || !id) return;
    try {
        if (action === 'increase') {
            await window.cartManager.increase(id);
        } else if (action === 'decrease') {
            await window.cartManager.decrease(id);
        } else if (action === 'remove') {
            if (!confirm('Удалить товар из корзины?')) return;
            await window.cartManager.remove(id);
        }
    } catch (err) {
        console.error('Cart action failed', err);
        alert('Не удалось выполнить действие. Попробуйте позже.');
    }
}

// helper: price formatting and safe HTML escape
function formatPrice(price) {
    try {
        return new Intl.NumberFormat('ru-RU', { style: 'currency', currency: 'RUB', minimumFractionDigits: 0 }).format(price);
    } catch (e) {
        return price + ' ₽';
    }
}
function escapeHtml(unsafe) {
    return String(unsafe).replace(/[&<>"']/g, function(m){ return ({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#039;'})[m]; });
}

// init
document.addEventListener('DOMContentLoaded', initCartPage);

// expose helpers for other scripts
window.cartPage = { renderCart, initCartPage, formatPrice };
