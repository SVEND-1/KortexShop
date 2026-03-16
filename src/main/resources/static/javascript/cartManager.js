// cartManager.js - Исправленная версия для твоего CartController (jQuery версия)

class CartManager {
    constructor() {
        this.cart = { items: [], total: 0 };
        this._ready = this.refreshFromServer().catch(e => {
            console.error('Ошибка инициализации корзины:', e);
        });
    }

    async ready() {
        return this._ready;
    }

    // Загружаем корзину через /api/carts/me
    async refreshFromServer() {
        try {
            const response = await fetch('/api/carts/me', {
                method: 'GET',
                headers: {
                    'Accept': 'application/json',
                    'Cache-Control': 'no-cache'
                },
                credentials: 'include'
            });

            if (!response.ok) {
                if (response.status === 401 || response.status === 403) {
                    console.log('Пользователь не авторизован, корзина пустая');
                    this.cart = { items: [], total: 0 };
                    $(window).trigger('cartUpdated', [this.cart]);
                    return this.cart;
                }
                throw new Error(`HTTP ${response.status}`);
            }

            const data = await response.json();

            // ВАЖНО: адаптируем данные под формат, который ждет фронт
            // Проверяем структуру ответа
            console.log('Данные корзины с сервера:', data);

            if (data.items && Array.isArray(data.items)) {
                // Формат 1: есть поле items
                this.cart = {
                    items: data.items.map(item => ({
                        id: item.id,
                        productId: item.productId,
                        productName: item.productName || item.name,
                        price: item.price,
                        quantity: item.quantity,
                        description: item.description,
                        image: item.image
                    })),
                    total: data.total || 0
                };
            } else if (data.cartItems && Array.isArray(data.cartItems)) {
                // Формат 2: есть поле cartItems (из /me-create)
                this.cart = {
                    items: data.cartItems.map(item => ({
                        id: item.id,
                        productId: item.productId,
                        productName: item.productName || item.name,
                        price: item.price,
                        quantity: item.quantity,
                        description: item.description,
                        image: item.image
                    })),
                    total: data.totalPrice || data.total || 0
                };
            } else {
                // Неизвестный формат, пробуем адаптировать
                this.cart = {
                    items: Array.isArray(data) ? data : [],
                    total: data.total || 0
                };
            }

            $(window).trigger('cartUpdated', [this.cart]);

            return this.cart;

        } catch (error) {
            console.error('Ошибка загрузки корзины:', error);
            this.cart = { items: [], total: 0 };
            $(window).trigger('cartUpdated', [this.cart]);
            return this.cart;
        }
    }

    // Добавить товар (без quantity параметра, как в твоем API)
    async addItem(productId) {
        try {
            const response = await fetch(`/api/carts/items?productId=${productId}`, {
                method: 'POST',
                headers: {
                    'Accept': 'application/json'
                },
                credentials: 'include'
            });

            if (!response.ok) {
                const errorData = await response.json().catch(() => ({}));
                throw new Error(errorData.error || `Ошибка ${response.status}`);
            }

            // Обновляем корзину после добавления
            return this.refreshFromServer();

        } catch (error) {
            console.error('Ошибка добавления товара:', error);
            throw error;
        }
    }

    // Увеличить количество
    async increase(itemId) {
        try {
            const response = await fetch(`/api/carts/items/${itemId}/increase`, {
                method: 'PATCH',
                headers: {
                    'Accept': 'application/json'
                },
                credentials: 'include'
            });

            if (!response.ok) {
                const errorData = await response.json().catch(() => ({}));
                throw new Error(errorData.error || `Ошибка ${response.status}`);
            }

            return this.refreshFromServer();

        } catch (error) {
            console.error('Ошибка увеличения количества:', error);
            throw error;
        }
    }

    // Уменьшить количество
    async decrease(itemId) {
        try {
            const response = await fetch(`/api/carts/items/${itemId}/decrease`, {
                method: 'PATCH',
                headers: {
                    'Accept': 'application/json'
                },
                credentials: 'include'
            });

            if (!response.ok) {
                const errorData = await response.json().catch(() => ({}));
                throw new Error(errorData.error || `Ошибка ${response.status}`);
            }

            return this.refreshFromServer();

        } catch (error) {
            console.error('Ошибка уменьшения количества:', error);
            throw error;
        }
    }

    // Удалить товар
    async remove(itemId) {
        try {
            const response = await fetch(`/api/carts/items/${itemId}`, {
                method: 'DELETE',
                headers: {
                    'Accept': 'application/json'
                },
                credentials: 'include'
            });

            if (!response.ok) {
                const errorData = await response.json().catch(() => ({}));
                throw new Error(errorData.error || `Ошибка ${response.status}`);
            }

            return this.refreshFromServer();

        } catch (error) {
            console.error('Ошибка удаления товара:', error);
            throw error;
        }
    }

    // Геттеры для фронта
    getItems() {
        return this.cart.items || [];
    }

    getTotal() {
        return this.cart.total || 0;
    }

    getUniqueCount() {
        return (this.cart.items || []).length;
    }
}

// Создаем глобальный экземпляр
window.cartManager = new CartManager();