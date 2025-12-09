// sellerScript.js - скрипт для панели продавца с AJAX к Spring бекенду

// Конфигурация API
const API_BASE_URL = '/api/sellers';

class SellerPanel {
    constructor() {
        this.currentProductId = null;
        this.products = [];
        this.init();
    }

    async init() {
        console.log('Панель продавца инициализирована');

        this.checkAuth();
        this.initTabs();
        this.initFileInputs();
        this.setupForms();
        this.setupEventListeners();

        // Загружаем товары продавца
        await this.loadSellerProducts();
    }

    // Проверка авторизации
    checkAuth() {
        const token = localStorage.getItem('authToken');
        if (!token) {
            this.showError('Для доступа к панели продавца необходимо авторизоваться');
            setTimeout(() => {
                window.location.href = '/login';
            }, 2000);
            return false;
        }
        return true;
    }

    // Инициализация табов
    initTabs() {
        const tabButtons = document.querySelectorAll('.tab-button');
        const tabContents = document.querySelectorAll('.tab-content');

        tabButtons.forEach(button => {
            button.addEventListener('click', async () => {
                const tabId = button.dataset.tab;

                // Убираем активный класс у всех кнопок и контента
                tabButtons.forEach(btn => btn.classList.remove('active'));
                tabContents.forEach(content => content.classList.remove('active'));

                // Добавляем активный класс текущей кнопке
                button.classList.add('active');

                // Показываем соответствующий контент
                if (tabId === 'edit') {
                    document.getElementById('edit-tab').classList.add('active');
                    document.getElementById('edit-form-container').classList.remove('active');
                    await this.loadSellerProducts();
                } else if (tabId === 'add') {
                    document.getElementById('add-tab').classList.add('active');
                }
            });
        });
    }

    // Инициализация загрузки файлов
    initFileInputs() {
        this.initFileInput('add-fileInput', 'add-fileName', 'add-image-preview');
        this.initFileInput('edit-fileInput', 'edit-fileName', 'edit-image-preview');
    }

    initFileInput(inputId, fileNameId, previewId) {
        const fileInput = document.getElementById(inputId);
        const fileName = document.getElementById(fileNameId);
        const preview = document.getElementById(previewId);

        if (fileInput && fileName) {
            fileInput.addEventListener('change', () => {
                if (fileInput.files.length > 0) {
                    const file = fileInput.files[0];

                    if (!file.type.startsWith('image/')) {
                        this.showError('Пожалуйста, выберите файл изображения');
                        fileInput.value = '';
                        fileName.textContent = 'Файл не выбран';
                        return;
                    }

                    fileName.textContent = file.name;

                    // Показываем превью
                    if (preview) {
                        const reader = new FileReader();
                        reader.onload = (e) => {
                            preview.innerHTML = `
                                <p>Предпросмотр:</p>
                                <img src="${e.target.result}" alt="Превью" style="max-width: 200px; max-height: 200px; border-radius: 8px;">
                            `;
                        };
                        reader.readAsDataURL(file);
                    }
                } else {
                    fileName.textContent = 'Файл не выбран';
                    if (preview) {
                        preview.innerHTML = '';
                    }
                }
            });
        }
    }

    // Настройка форм
    setupForms() {
        this.setupAddProductForm();
        this.setupEditProductForm();
    }

    // Настройка формы добавления товара
    setupAddProductForm() {
        const form = document.getElementById('add-product-form');
        if (!form) return;

        form.addEventListener('submit', async (e) => {
            e.preventDefault();

            if (!this.validateAddForm()) {
                return;
            }

            try {
                await this.createProduct(form);
            } catch (error) {
                this.showError(`Ошибка при добавлении товара: ${error.message}`);
            }
        });
    }

    // Настройка формы редактирования товара
    setupEditProductForm() {
        const form = document.getElementById('edit-product-form');
        const cancelBtn = document.getElementById('cancel-edit');
        const deleteBtn = document.getElementById('delete-product');

        if (!form) return;

        form.addEventListener('submit', async (e) => {
            e.preventDefault();

            if (!this.currentProductId) {
                this.showError('Не выбран товар для редактирования');
                return;
            }

            if (!this.validateEditForm()) {
                return;
            }

            try {
                await this.updateProduct(this.currentProductId, form);
            } catch (error) {
                this.showError(`Ошибка при обновлении товара: ${error.message}`);
            }
        });

        if (cancelBtn) {
            cancelBtn.addEventListener('click', () => this.showProductsList());
        }

        if (deleteBtn) {
            deleteBtn.addEventListener('click', () => {
                if (this.currentProductId) {
                    this.deleteProduct(this.currentProductId);
                }
            });
        }
    }

    // Настройка обработчиков событий
    setupEventListeners() {
        // Поиск товаров
        const searchInput = document.getElementById('product-search');
        if (searchInput) {
            searchInput.addEventListener('input', () => this.filterProducts(searchInput.value));
        }
    }

    // Загрузка товаров продавца
    async loadSellerProducts() {
        const container = document.getElementById('products-container');
        if (!container) return;

        try {
            container.innerHTML = '<div class="loading-spinner">Загрузка товаров...</div>';

            const response = await fetch(`${API_BASE_URL}/products`, {
                method: 'GET',
                headers: {
                    'Authorization': `Bearer ${localStorage.getItem('authToken')}`,
                    'Content-Type': 'application/json'
                }
            });

            if (!response.ok) {
                throw new Error(`Ошибка HTTP: ${response.status}`);
            }

            const result = await response.json();

            if (result.error) {
                throw new Error(result.error);
            }

            this.products = Array.isArray(result) ? result : [];
            this.displayProducts(this.products);

        } catch (error) {
            console.error('Ошибка при загрузке товаров:', error);
            container.innerHTML = `
                <div class="error-message">
                    <p>Не удалось загрузить товары: ${error.message}</p>
                    <button onclick="sellerPanel.loadSellerProducts()" class="btn-retry">Попробовать снова</button>
                </div>
            `;
        }
    }

    // Отображение товаров
    displayProducts(products) {
        const container = document.getElementById('products-container');
        if (!container) return;

        if (!products || products.length === 0) {
            container.innerHTML = `
                <div class="no-products">
                    <p>У вас пока нет товаров</p>
                    <button class="tab-button" data-tab="add">Добавить первый товар</button>
                </div>
            `;
            return;
        }

        container.innerHTML = products.map(product => this.createProductCard(product)).join('');
    }

    // Создание карточки товара
    createProductCard(product) {
        const imageUrl = product.image ? `/uploads/images/${product.image}` : 'images/product-img.png';
        const statusClass = product.count > 0 ? 'in-stock' : 'out-of-stock';
        const statusText = product.count > 0 ? 'В наличии' : 'Нет в наличии';

        return `
            <div class="product-card" data-id="${product.id}">
                <div class="product-image">
                    <img src="${imageUrl}" alt="${product.name}" 
                         onerror="this.src='images/product-img.png'">
                </div>
                <div class="product-info">
                    <div class="product-header">
                        <h3 class="product-name">${product.name}</h3>
                        <span class="product-price">${this.formatPrice(product.price)}</span>
                    </div>
                    <div class="product-details">
                        <div class="product-detail">
                            <span>Категория:</span> ${this.getCategoryName(product.category)}
                        </div>
                        <div class="product-detail">
                            <span>Бренд:</span> ${product.brand || 'Не указан'}
                        </div>
                        <div class="product-detail">
                            <span>Количество:</span> ${product.count} шт.
                        </div>
                        <div class="product-detail">
                            <span>Статус:</span> <span class="status ${statusClass}">${statusText}</span>
                        </div>
                    </div>
                    <p class="product-description">${product.description ? product.description.substring(0, 100) + (product.description.length > 100 ? '...' : '') : 'Описание отсутствует'}</p>
                    <div class="product-actions">
                        <button class="edit-btn" onclick="sellerPanel.editProduct(${product.id})">Редактировать</button>
                        <button class="delete-btn" onclick="sellerPanel.confirmDelete(${product.id}, '${product.name.replace(/'/g, "\\'")}')">Удалить</button>
                    </div>
                </div>
            </div>
        `;
    }

    // Фильтрация товаров
    filterProducts(searchTerm) {
        const filtered = this.products.filter(product =>
            product.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
            (product.description && product.description.toLowerCase().includes(searchTerm.toLowerCase())) ||
            (product.brand && product.brand.toLowerCase().includes(searchTerm.toLowerCase()))
        );
        this.displayProducts(filtered);
    }

    // Создание товара
    async createProduct(form) {
        const formData = new FormData(form);

        try {
            const response = await fetch(`${API_BASE_URL}/products`, {
                method: 'POST',
                headers: {
                    'Authorization': `Bearer ${localStorage.getItem('authToken')}`
                },
                body: formData
            });

            if (!response.ok) {
                const errorData = await response.json();
                throw new Error(errorData.error || `Ошибка HTTP: ${response.status}`);
            }

            const result = await response.json();

            this.showSuccess(`Товар "${result.product?.name || 'товар'}" успешно создан!`);
            form.reset();
            document.getElementById('add-fileName').textContent = 'Файл не выбран';
            document.getElementById('add-image-preview').innerHTML = '';

            // Переключаемся на вкладку с товарами
            document.querySelector('[data-tab="edit"]').click();

        } catch (error) {
            throw new Error(error.message);
        }
    }

    // Редактирование товара
    async editProduct(productId) {
        try {
            const product = await this.getProduct(productId);
            if (!product) {
                this.showError('Товар не найден');
                return;
            }

            this.currentProductId = productId;
            this.showEditForm(product);

        } catch (error) {
            this.showError(`Ошибка при загрузке товара: ${error.message}`);
        }
    }

    // Получение товара по ID
    async getProduct(productId) {
        const response = await fetch(`${API_BASE_URL}/products/${productId}`, {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${localStorage.getItem('authToken')}`,
                'Content-Type': 'application/json'
            }
        });

        if (!response.ok) {
            throw new Error(`Ошибка HTTP: ${response.status}`);
        }

        const result = await response.json();

        if (result.error) {
            throw new Error(result.error);
        }

        return result;
    }

    // Показ формы редактирования
    showEditForm(product) {
        // Заполняем форму
        document.getElementById('edit-id').value = product.id;
        document.getElementById('edit-name').value = product.name;
        document.getElementById('edit-price').value = product.price;
        document.getElementById('edit-brand').value = product.brand || '';
        document.getElementById('edit-count').value = product.count;
        document.getElementById('edit-category').value = product.category;
        document.getElementById('edit-description').value = product.description || '';

        // Показываем текущее изображение
        const imageContainer = document.getElementById('current-image-container');
        const currentImage = document.getElementById('current-image');

        if (product.image) {
            currentImage.src = `/uploads/images/${product.image}`;
            currentImage.onerror = function() {
                this.src = 'images/product-img.png';
            };
            imageContainer.style.display = 'block';
        } else {
            imageContainer.style.display = 'none';
        }

        // Сбрасываем поле выбора файла
        document.getElementById('edit-fileInput').value = '';
        document.getElementById('edit-fileName').textContent = 'Файл не выбран';
        document.getElementById('edit-image-preview').innerHTML = '';

        // Показываем форму редактирования
        document.getElementById('edit-tab').classList.remove('active');
        document.getElementById('edit-form-container').classList.add('active');
    }

    // Обновление товара
    async updateProduct(productId, form) {
        const formData = new FormData(form);

        try {
            const response = await fetch(`${API_BASE_URL}/products/${productId}`, {
                method: 'PUT',
                headers: {
                    'Authorization': `Bearer ${localStorage.getItem('authToken')}`
                },
                body: formData
            });

            if (!response.ok) {
                const errorData = await response.json();
                throw new Error(errorData.error || `Ошибка HTTP: ${response.status}`);
            }

            const result = await response.json();

            this.showSuccess(`Товар "${result.product?.name || 'товар'}" успешно обновлен!`);

            // Возвращаемся к списку товаров
            this.showProductsList();
            await this.loadSellerProducts();

        } catch (error) {
            throw new Error(error.message);
        }
    }

    // Подтверждение удаления
    confirmDelete(productId, productName) {
        if (confirm(`Вы уверены, что хотите удалить товар "${productName}"?`)) {
            this.deleteProduct(productId);
        }
    }

    // Удаление товара
    async deleteProduct(productId) {
        try {
            const response = await fetch(`${API_BASE_URL}/products/${productId}`, {
                method: 'DELETE',
                headers: {
                    'Authorization': `Bearer ${localStorage.getItem('authToken')}`,
                    'Content-Type': 'application/json'
                }
            });

            if (!response.ok) {
                const errorData = await response.json();
                throw new Error(errorData.error || `Ошибка HTTP: ${response.status}`);
            }

            const result = await response.json();

            this.showSuccess(result.message || 'Товар успешно удален!');

            // Обновляем список товаров
            await this.loadSellerProducts();

        } catch (error) {
            this.showError(`Ошибка при удалении товара: ${error.message}`);
        }
    }

    // Показать список товаров
    showProductsList() {
        document.getElementById('edit-form-container').classList.remove('active');
        document.getElementById('edit-tab').classList.add('active');
        this.currentProductId = null;
    }

    // Валидация формы добавления
    validateAddForm() {
        const name = document.getElementById('add-name').value.trim();
        const price = parseFloat(document.getElementById('add-price').value);
        const count = parseInt(document.getElementById('add-count').value);
        const category = document.getElementById('add-category').value;
        const description = document.getElementById('add-description').value.trim();
        const imageFile = document.getElementById('add-fileInput').files[0];

        if (!name) {
            this.showError('Введите название товара');
            return false;
        }

        if (isNaN(price) || price <= 0) {
            this.showError('Введите корректную цену (больше 0)');
            return false;
        }

        if (isNaN(count) || count < 0) {
            this.showError('Введите корректное количество');
            return false;
        }

        if (!category) {
            this.showError('Выберите категорию товара');
            return false;
        }

        if (!description) {
            this.showError('Введите описание товара');
            return false;
        }

        if (!imageFile) {
            this.showError('Выберите изображение товара');
            return false;
        }

        return true;
    }

    // Валидация формы редактирования
    validateEditForm() {
        const name = document.getElementById('edit-name').value.trim();
        const price = parseFloat(document.getElementById('edit-price').value);
        const count = parseInt(document.getElementById('edit-count').value);
        const category = document.getElementById('edit-category').value;
        const description = document.getElementById('edit-description').value.trim();

        if (!name) {
            this.showError('Введите название товара');
            return false;
        }

        if (isNaN(price) || price <= 0) {
            this.showError('Введите корректную цену (больше 0)');
            return false;
        }

        if (isNaN(count) || count < 0) {
            this.showError('Введите корректное количество');
            return false;
        }

        if (!category) {
            this.showError('Выберите категорию товара');
            return false;
        }

        if (!description) {
            this.showError('Введите описание товара');
            return false;
        }

        return true;
    }

    // Вспомогательные методы
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

    formatPrice(price) {
        return new Intl.NumberFormat('ru-RU', {
            style: 'currency',
            currency: 'RUB',
            minimumFractionDigits: 0
        }).format(price);
    }

    showSuccess(message) {
        this.showNotification(message, 'success');
    }

    showError(message) {
        this.showNotification(message, 'error');
    }

    showNotification(message, type = 'info') {
        // Удаляем старые уведомления
        const oldNotifications = document.querySelectorAll('.notification');
        oldNotifications.forEach(n => n.remove());

        const notification = document.createElement('div');
        notification.className = `notification ${type}`;
        notification.innerHTML = `
            <div class="notification-content">
                <span class="notification-icon">${type === 'success' ? '✅' : type === 'error' ? '❌' : 'ℹ️'}</span>
                <span>${message}</span>
            </div>
        `;

        document.body.appendChild(notification);

        // Анимация появления
        setTimeout(() => {
            notification.style.transform = 'translateX(0)';
            notification.style.opacity = '1';
        }, 10);

        // Автоматическое скрытие
        setTimeout(() => {
            notification.style.transform = 'translateX(100%)';
            notification.style.opacity = '0';
            setTimeout(() => notification.remove(), 300);
        }, 3000);
    }
}

// Инициализация при загрузке страницы
document.addEventListener('DOMContentLoaded', () => {
    window.sellerPanel = new SellerPanel();
});

// Глобальные функции для использования в HTML
window.editProduct = (productId) => window.sellerPanel?.editProduct(productId);
window.confirmDelete = (productId, productName) => window.sellerPanel?.confirmDelete(productId, productName);