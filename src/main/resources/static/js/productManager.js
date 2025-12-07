// Единый менеджер товаров для всего сайта

class ProductManager {
    constructor() {
        this.storageKey = 'productsDatabase';
        this.loadProducts();
    }

    // Загрузка товаров из localStorage
    loadProducts() {
        const saved = localStorage.getItem(this.storageKey);
        if (saved) {
            this.products = JSON.parse(saved);
        } else {
            // НАЧАЛЬНЫЕ ТОВАРЫ - ПУСТО!
            this.products = {};
            this.saveProducts();
        }
    }

    // Сохранение товаров в localStorage
    saveProducts() {
        localStorage.setItem(this.storageKey, JSON.stringify(this.products));
    }

    // Получить все товары
    getAllProducts() {
        return Object.values(this.products);
    }

    // Получить товар по ID
    getProductById(id) {
        return this.products[id];
    }

    // Добавить новый товар
    addProduct(productData) {
        const newId = this.generateNewId();
        const newProduct = {
            id: newId,
            ...productData,
            categoryName: this.getCategoryName(productData.category),
            // Добавляем дефолтные характеристики в зависимости от категории
            features: this.getDefaultFeatures(productData.category, productData.name)
        };
        
        this.products[newId] = newProduct;
        this.saveProducts();
        this.dispatchProductUpdate();
        
        return newId;
    }

    // Генерация нового ID
    generateNewId() {
        const ids = Object.keys(this.products).map(Number);
        return ids.length > 0 ? Math.max(...ids) + 1 : 1;
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

// Создаем глобальный экземпляр менеджера
window.productManager = new ProductManager();