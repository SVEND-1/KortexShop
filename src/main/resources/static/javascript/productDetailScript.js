// productDetailScript.js - для страницы товара

document.addEventListener('DOMContentLoaded', async function() {
    // Получаем ID товара из URL
    const urlParams = new URLSearchParams(window.location.search);
    const productId = urlParams.get('id');
    
    if (!productId) {
        showError('Товар не найден');
        return;
    }
    
    await loadProductDetails(productId);
});

async function loadProductDetails(productId) {
    try {
        showLoading();
        
        const response = await fetch(`/api/products/${productId}`);
        
        if (!response.ok) {
            throw new Error('Товар не найден');
        }
        
        const product = await response.json();
        displayProductDetails(product);
        
    } catch (error) {
        console.error('Ошибка загрузки товара:', error);
        showError('Не удалось загрузить информацию о товаре');
    }
}

function displayProductDetails(product) {
    // Заполняем данные на странице
    document.getElementById('product-title').textContent = product.name;
    document.getElementById('product-price').textContent = formatPrice(product.price);
    document.getElementById('product-description').textContent = product.description || 'Описание отсутствует';
    document.getElementById('product-category').textContent = product.category || 'Без категории';
    document.getElementById('product-brand').textContent = product.brand || 'Не указан';
    document.getElementById('product-seller').textContent = product.seller || 'Не указан';
    document.getElementById('product-count').textContent = product.count || 0;
    
    // Заполняем характеристики
    const featuresList = document.getElementById('product-features');
    if (product.features && Array.isArray(product.features)) {
        featuresList.innerHTML = product.features.map(feature => 
            `<li>${feature}</li>`
        ).join('');
    } else {
        featuresList.innerHTML = '<li>Характеристики не указаны</li>';
    }
    
    // Устанавливаем изображение
    const productImage = document.getElementById('product-image');
    if (product.image) {
        productImage.src = product.image;
        productImage.onerror = function() {
            this.src = 'images/product-img.png';
        };
    }
    
    // Настраиваем кнопку добавления в корзину
    const addToCartBtn = document.getElementById('add-to-cart-btn');
    if (addToCartBtn) {
        addToCartBtn.onclick = function() {
            if (window.cartManager) {
                window.cartManager.addToCart(product.id, 1);
                showNotification('Товар добавлен в корзину!');
            } else {
                showNotification('Корзина временно недоступна');
            }
        };
    }
    
    // Показываем контент
    document.querySelector('.product-detail-content').style.display = 'block';
}

function showLoading() {
    const content = document.querySelector('.product-detail-content');
    if (content) {
        content.innerHTML = `
            <div style="text-align: center; padding: 50px;">
                <div style="border: 4px solid #f3f3f3; border-top: 4px solid #3498db; border-radius: 50%; width: 50px; height: 50px; animation: spin 2s linear infinite; margin: 0 auto 20px;"></div>
                <p>Загрузка товара...</p>
            </div>
        `;
        
        const style = document.createElement('style');
        style.textContent = `
            @keyframes spin {
                0% { transform: rotate(0deg); }
                100% { transform: rotate(360deg); }
            }
        `;
        document.head.appendChild(style);
    }
}

function showError(message) {
    const content = document.querySelector('.product-detail-content');
    if (content) {
        content.innerHTML = `
            <div style="text-align: center; padding: 50px;">
                <div style="font-size: 48px; margin-bottom: 20px;">⚠️</div>
                <h3>Ошибка</h3>
                <p style="margin-bottom: 20px;">${message}</p>
                <button onclick="window.history.back()" style="padding: 10px 20px; background: #3182ce; color: white; border: none; border-radius: 6px; cursor: pointer;">
                    Вернуться назад
                </button>
            </div>
        `;
    }
}

function formatPrice(price) {
    return new Intl.NumberFormat('ru-RU', {
        style: 'currency',
        currency: 'RUB',
        minimumFractionDigits: 0
    }).format(price);
}

function showNotification(message) {
    const notification = document.createElement('div');
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