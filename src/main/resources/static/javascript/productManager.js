// productManager.js - для совместимости с бекендом

class ProductManager {
    constructor() {
        this.storageKey = 'productsDatabase';
        this.loadProducts();
    }

    // Загрузка из localStorage (для совместимости)
    loadProducts() {
        const saved = localStorage.getItem(this.storageKey);
        if (saved) {
            this.products = JSON.parse(saved);
        } else {
            this.products = {};
        }
    }

    // Сохранение в localStorage
    saveProducts() {
        localStorage.setItem(this.storageKey, JSON.stringify(this.products));
    }

    // Получить товар по ID (в первую очередь с сервера)
    async getProductById(id) {
        try {
            const response = await fetch(`/api/products/${id}`);
            if (response.ok) {
                return await response.json();
            }
        } catch (error) {
            console.error('Ошибка при запросе к серверу:', error);
        }

        // Если сервер не отвечает, используем локальные данные
        return this.products[id];
    }

    // Получить все товары (в первую очередь с сервера)
    async getAllProducts() {
        try {
            const response = await fetch('/api/products');
            if (response.ok) {
                const data = await response.json();
                return data.content || data;
            }
        } catch (error) {
            console.error('Ошибка при запросе к серверу:', error);
        }

        return Object.values(this.products);
    }

    // Получить товары продавца
    async getSellerProducts() {
        try {
            const token = localStorage.getItem('authToken');
            if (!token) {
                throw new Error('Пользователь не авторизован');
            }

            const response = await fetch('/api/sellers/products', {
                headers: {
                    'Authorization': `Bearer ${token}`,
                    'Content-Type': 'application/json'
                }
            });

            if (!response.ok) {
                throw new Error(`Ошибка HTTP: ${response.status}`);
            }

            return await response.json();
        } catch (error) {
            console.error('Ошибка при получении товаров продавца:', error);
            return [];
        }
    }

    // Добавить товар (через API продавца)
    async addProduct(productData, imageFile) {
        try {
            const token = localStorage.getItem('authToken');
            if (!token) {
                throw new Error('Пользователь не авторизован');
            }

            const formData = new FormData();
            formData.append('name', productData.name);
            formData.append('price', productData.price);
            formData.append('count', productData.count);
            formData.append('category', productData.category);
            formData.append('description', productData.description);
            formData.append('brand', productData.brand || '');

            if (imageFile) {
                formData.append('imageFile', imageFile);
            }

            const response = await fetch('/api/sellers/products', {
                method: 'POST',
                headers: {
                    'Authorization': `Bearer ${token}`
                },
                body: formData
            });

            if (!response.ok) {
                const errorData = await response.json();
                throw new Error(errorData.error || `Ошибка HTTP: ${response.status}`);
            }

            return await response.json();
        } catch (error) {
            throw new Error(error.message);
        }
    }

    // Обновить товар
    async updateProduct(productId, productData, imageFile = null) {
        try {
            const token = localStorage.getItem('authToken');
            if (!token) {
                throw new Error('Пользователь не авторизован');
            }

            const formData = new FormData();
            formData.append('name', productData.name);
            formData.append('price', productData.price);
            formData.append('count', productData.count);
            formData.append('category', productData.category);
            formData.append('description', productData.description);
            formData.append('brand', productData.brand || '');

            if (imageFile) {
                formData.append('imageFile', imageFile);
            }

            const response = await fetch(`/api/sellers/products/${productId}`, {
                method: 'PUT',
                headers: {
                    'Authorization': `Bearer ${token}`
                },
                body: formData
            });

            if (!response.ok) {
                const errorData = await response.json();
                throw new Error(errorData.error || `Ошибка HTTP: ${response.status}`);
            }

            return await response.json();
        } catch (error) {
            throw new Error(error.message);
        }
    }

    // Удалить товар
    async deleteProduct(productId) {
        try {
            const token = localStorage.getItem('authToken');
            if (!token) {
                throw new Error('Пользователь не авторизован');
            }

            const response = await fetch(`/api/sellers/products/${productId}`, {
                method: 'DELETE',
                headers: {
                    'Authorization': `Bearer ${token}`,
                    'Content-Type': 'application/json'
                }
            });

            if (!response.ok) {
                const errorData = await response.json();
                throw new Error(errorData.error || `Ошибка HTTP: ${response.status}`);
            }

            return await response.json();
        } catch (error) {
            throw new Error(error.message);
        }
    }

    // Получить название категории
    getCategoryName(categoryKey) {
        const categories = {
            "ELECTRONICS": "Электроника",
            "CLOTHING": "Одежда",
            "BOOKS": "Книги",
            "FOOD": "Еда",
            "SPORTS": "Спорт товары",
            "HOME": "Товары для дома",
            "BEAUTY": "Красота",
            "OTHER": "Другое"
        };
        return categories[categoryKey] || 'Другое';
    }
}

// Создаем глобальный экземпляр
window.productManager = new ProductManager();