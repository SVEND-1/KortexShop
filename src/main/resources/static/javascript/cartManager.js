// cartManager.js - менеджер корзины

class CartManager {
    constructor() {
        this.storageKey = 'cart';
        this.ordersKey = 'ordersHistory';
        this.loadCart();
    }

    loadCart() {
        const saved = localStorage.getItem(this.storageKey);
        this.cart = saved ? JSON.parse(saved) : [];
    }

    saveCart() {
        localStorage.setItem(this.storageKey, JSON.stringify(this.cart));
    }

    // Добавить в корзину
    async addToCart(productId, quantity = 1) {
        // Сначала получаем данные о товаре
        let product;
        
        if (window.productManager) {
            product = await window.productManager.getProductById(productId);
        } else {
            // Пробуем получить с сервера
            try {
                const response = await fetch(`/api/products/${productId}`);
                if (response.ok) {
                    product = await response.json();
                }
            } catch (error) {
                console.error('Ошибка при получении товара:', error);
            }
        }
        
        if (!product) {
            console.error('Товар не найден');
            return false;
        }

        const existingItem = this.cart.find(item => item.id === productId);
        
        if (existingItem) {
            existingItem.quantity += quantity;
        } else {
            this.cart.push({
                id: product.id,
                name: product.name,
                price: product.price,
                image: product.image || 'images/product-img.png',
                quantity: quantity
            });
        }
        
        this.saveCart();
        this.dispatchCartUpdate();
        return true;
    }

    // Удалить из корзины
    removeFromCart(productId) {
        this.cart = this.cart.filter(item => item.id !== productId);
        this.saveCart();
        this.dispatchCartUpdate();
    }

    // Изменить количество
    updateQuantity(productId, newQuantity) {
        const item = this.cart.find(item => item.id === productId);
        if (item) {
            if (newQuantity <= 0) {
                this.removeFromCart(productId);
            } else {
                item.quantity = newQuantity;
                this.saveCart();
                this.dispatchCartUpdate();
            }
        }
    }

    // Очистить корзину
    clearCart() {
        this.cart = [];
        this.saveCart();
        this.dispatchCartUpdate();
    }

    // Получить все товары в корзине
    getCartItems() {
        return this.cart;
    }

    // Получить общую сумму
    getTotalPrice() {
        return this.cart.reduce((total, item) => total + (item.price * item.quantity), 0);
    }

    // Получить общее количество товаров
    getTotalItems() {
        return this.cart.reduce((total, item) => total + item.quantity, 0);
    }

    // Оформить заказ
    checkout() {
        if (this.cart.length === 0) return false;

        const order = {
            id: Date.now(),
            date: new Date().toLocaleDateString('ru-RU'),
            items: [...this.cart],
            total: this.getTotalPrice(),
            status: 'completed'
        };

        // Сохраняем в историю заказов
        this.saveOrder(order);
        
        // Очищаем корзину
        this.clearCart();
        
        return order;
    }

    // Сохранить заказ в историю
    saveOrder(order) {
        let orders = JSON.parse(localStorage.getItem(this.ordersKey)) || [];
        orders.unshift(order);
        localStorage.setItem(this.ordersKey, JSON.stringify(orders));
    }

    // Получить историю заказов
    getOrdersHistory() {
        return JSON.parse(localStorage.getItem(this.ordersKey)) || [];
    }

    // Событие обновления корзины
    dispatchCartUpdate() {
        window.dispatchEvent(new CustomEvent('cartUpdated'));
    }
}

// Создаем глобальный экземпляр
window.cartManager = new CartManager();