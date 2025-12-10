// mainFormScript.js - скрипт для главной страницы

document.addEventListener('DOMContentLoaded', function() {
    console.log('Главная страница загружена');

    // Обработчик корзины
    const cartLink = document.querySelector('a[href="/cart"]');
    if (cartLink) {
        cartLink.addEventListener('click', function(e) {
            e.preventDefault();
            console.log('Переход в корзину');
            window.location.href = '/cart';
        });
    }

    // Обработчик профиля - исправлено (была ссылка на корзину)
    const profileLink = document.querySelector('a[href="/profile"]');
    if (profileLink) {
        profileLink.addEventListener('click', function(e) {
            e.preventDefault();
            console.log('Переход в профиль');
            window.location.href = '/profile';
        });
    }

    // Обработчик входа
    const loginLink = document.querySelector('a[href="/login"]');
    if (loginLink) {
        loginLink.addEventListener('click', function(e) {
            e.preventDefault();
            console.log('Переход на страницу входа');
            window.location.href = '/login';
        });
    }

    // Обновляем счетчик корзины
    updateCartCount();

    // Слушаем обновления корзины
    window.addEventListener('cartUpdated', updateCartCount);
});

// Обновление счетчика товаров в корзине
function updateCartCount() {
    const cartLink = document.querySelector('a[href="/cart"]');
    if (cartLink) {
        let totalItems = 0;

        if (window.cartManager && typeof window.cartManager.getTotalItems === 'function') {
            totalItems = window.cartManager.getTotalItems();
        } else {
            // Проверяем localStorage
            const cart = JSON.parse(localStorage.getItem('cart')) || [];
            totalItems = cart.reduce((sum, item) => sum + (item.quantity || 1), 0);
        }

        // Удаляем старый счетчик
        const oldCounter = cartLink.querySelector('.cart-counter');
        if (oldCounter) {
            oldCounter.remove();
        }

        if (totalItems > 0) {
            const counter = document.createElement('span');
            counter.className = 'cart-counter';
            counter.style.cssText = `
                position: absolute;
                top: -8px;
                right: -8px;
                background: #e53e3e;
                color: white;
                border-radius: 50%;
                width: 20px;
                height: 20px;
                font-size: 12px;
                display: flex;
                align-items: center;
                justify-content: center;
                font-weight: bold;
            `;
            counter.textContent = totalItems > 9 ? '9+' : totalItems;

            cartLink.style.position = 'relative';
            cartLink.appendChild(counter);
        }
    }
}

// Функция для проверки авторизации
function checkAuth() {
    const token = localStorage.getItem('authToken');
    const userData = localStorage.getItem('userData');

    if (token && userData) {
        // Пользователь авторизован
        const loginLink = document.querySelector('a[href="/login"]');
        if (loginLink) {
            loginLink.innerHTML = `
                <img src="/images/join-img.png" alt="Выйти" class="nav-icon">
                Выйти
            `;
            loginLink.href = '#';
            loginLink.onclick = function(e) {
                e.preventDefault();
                localStorage.removeItem('authToken');
                localStorage.removeItem('userData');
                window.location.reload();
            };
        }
    }
}

// Экспортируем функции
window.checkAuth = checkAuth;
window.updateCartCount = updateCartCount;