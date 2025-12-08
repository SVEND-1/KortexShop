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
            // Пробуем получить с сервера
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
        
        // Если сервер не отвечает, используем локальные данные
        return Object.values(this.products);
    }

    // Генерация нового ID
    generateNewId() {
        const ids = Object.keys(this.products).map(Number);
        return ids.length > 0 ? Math.max(...ids) + 1 : 1;
    }

    // Добавить новый товар (только локально для совместимости)
    addProduct(productData) {
        const newId = this.generateNewId();
        const newProduct = {
            id: newId,
            ...productData,
            categoryName: this.getCategoryName(productData.category)
        };
        
        this.products[newId] = newProduct;
        this.saveProducts();
        this.dispatchProductUpdate();
        
        return newId;
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

    // Умные характеристики для разных категорий
    getDefaultFeatures(category, productName) {
        const featuresMap = {
            'ELECTRONICS': [
                'Высокое качество',
                'Гарантия 1 год',
                'Оригинальная продукция'
            ],
            'CLOTHING': [
                'Качественные материалы',
                'Размерная сетка соответствует',
                'Удобная носка'
            ],
            'BOOKS': [
                'Качественная печать',
                'Твердый переплет',
                'Быстрая доставка'
            ],
            'FOOD': [
                'Свежий продукт',
                'Сертификат качества',
                'Длительный срок хранения'
            ],
            'SPORTS': [
                'Прочные материалы',
                'Безопасность использования',
                'Подходит для тренировок'
            ],
            'HOME': [
                'Качественные материалы',
                'Легкий уход',
                'Долговечность'
            ],
            'BEAUTY': [
                'Гипоаллергенно',
                'Натуральные компоненты',
                'Эффективный результат'
            ]
        };
        
        return featuresMap[category] || ['Качественный товар', 'Гарантия качества', 'Быстрая доставка'];
    }

    // Событие обновления товаров
    dispatchProductUpdate() {
        window.dispatchEvent(new CustomEvent('productsUpdated'));
    }
}

// Создаем глобальный экземпляр
window.productManager = new ProductManager();