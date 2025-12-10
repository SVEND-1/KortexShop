// sellerScript.js

document.addEventListener('DOMContentLoaded', function() {
    // Элементы DOM
    const addTabContent = document.getElementById('add-tab');
    const editTabContent = document.getElementById('edit-tab');
    const editFormContainer = document.getElementById('edit-form-container');
    const productsContainer = document.getElementById('products-container');
    const tabButtons = document.querySelectorAll('.tab-button');
    const authLinks = document.querySelector('.auth-links');

    // Форма добавления товара
    const addProductForm = document.getElementById('add-product-form');
    const addSubmitBtn = document.getElementById('add-submit-btn');
    const addImageFile = document.getElementById('add-imageFile');
    const addFileName = document.getElementById('add-fileName');

    // Форма редактирования товара
    const editProductForm = document.getElementById('edit-product-form');
    const updateSubmitBtn = document.getElementById('update-submit-btn');
    const cancelEditBtn = document.getElementById('cancel-edit');
    const deleteProductBtn = document.getElementById('delete-product');
    const editImageFile = document.getElementById('edit-imageFile');
    const editFileName = document.getElementById('edit-fileName');
    const currentImage = document.getElementById('current-image');
    const currentImagePreview = document.getElementById('current-image-preview');

    // Текущий редактируемый товар
    let currentEditingProductId = null;

    // Базовый URL для API
    const API_BASE_URL = '/api/sellers';

    // Инициализация
    init();

    function init() {
        // Инициализация табов
        initTabs();

        // Инициализация обработчиков событий
        initEventHandlers();

        // Загрузка товаров продавца
        loadSellerProducts();
    }

    function initTabs() {
        tabButtons.forEach(button => {
            button.addEventListener('click', () => {
                const tabId = button.getAttribute('data-tab');

                // Обновление активных кнопок
                tabButtons.forEach(btn => btn.classList.remove('active'));
                button.classList.add('active');

                // Показ соответствующего контента
                if (tabId === 'add') {
                    addTabContent.classList.add('active');
                    editTabContent.classList.remove('active');
                    editFormContainer.style.display = 'none';
                    authLinks.style.display = 'flex';
                } else if (tabId === 'edit') {
                    addTabContent.classList.remove('active');
                    editTabContent.classList.add('active');
                    editFormContainer.style.display = 'none';
                    authLinks.style.display = 'flex';
                    loadSellerProducts();
                }
            });
        });
    }

    function initEventHandlers() {
        // Обработка выбора файла для добавления товара
        addImageFile.addEventListener('change', function() {
            if (this.files && this.files[0]) {
                addFileName.textContent = this.files[0].name;
            } else {
                addFileName.textContent = 'Файл не выбран';
            }
        });

        // Обработка выбора файла для редактирования товара
        editImageFile.addEventListener('change', function() {
            if (this.files && this.files[0]) {
                editFileName.textContent = this.files[0].name;
            } else {
                editFileName.textContent = 'Файл не выбран';
            }
        });

        // Добавление товара
        addSubmitBtn.addEventListener('click', addProduct);

        // Сохранение изменений товара
        updateSubmitBtn.addEventListener('click', updateProduct);

        // Отмена редактирования
        cancelEditBtn.addEventListener('click', cancelEdit);

        // Удаление товара
        deleteProductBtn.addEventListener('click', deleteProduct);

        // Сброс формы при клике на кнопку "Очистить"
        addProductForm.querySelector('.reset-btn').addEventListener('click', function() {
            addFileName.textContent = 'Файл не выбран';
        });
    }

    // Загрузка товаров продавца
    async function loadSellerProducts() {
        try {
            productsContainer.innerHTML = '<p class="loading-text">Загрузка товаров...</p>';

            const response = await fetch(`${API_BASE_URL}/products`, {
                method: 'GET',
                headers: {
                    'Content-Type': 'application/json'
                },
                credentials: 'include' // Важно для сессий Spring Security
            });

            // Проверяем статус ответа
            if (response.status === 401) {
                // Неавторизован - перенаправляем на логин
                showNotification('Требуется авторизация', 'error');
                setTimeout(() => {
                    window.location.href = '/login?redirect=/seller';
                }, 2000);
                return;
            }

            if (response.status === 403) {
                // Доступ запрещен (нет прав продавца)
                showNotification('У вас нет прав для доступа к панели продавца', 'error');
                setTimeout(() => {
                    window.location.href = '/profile';
                }, 2000);
                return;
            }

            if (!response.ok) {
                throw new Error(`Ошибка HTTP: ${response.status}`);
            }

            const products = await response.json();

            if (products.length === 0) {
                productsContainer.innerHTML = `
                    <div class="no-products">
                        <p>У вас пока нет товаров</p>
                    </div>
                `;
                return;
            }

            renderProducts(products);
        } catch (error) {
            console.error('Ошибка при загрузке товаров:', error);
            productsContainer.innerHTML = `
                <div class="error-message">
                    <p>Ошибка при загрузке товаров: ${error.message}</p>
                    <button onclick="loadSellerProducts()" class="retry-btn">Повторить попытку</button>
                </div>
            `;
        }
    }

    // Отображение товаров
    function renderProducts(products) {
        productsContainer.innerHTML = '';

        // Проверяем, является ли ответ массивом
        if (!Array.isArray(products)) {
            console.error('Ожидался массив товаров, получено:', products);
            productsContainer.innerHTML = `
                <div class="error-message">
                    <p>Ошибка формата данных</p>
                </div>
            `;
            return;
        }

        products.forEach(product => {
            const productCard = createProductCard(product);
            productsContainer.appendChild(productCard);
        });
    }

    // Создание карточки товара
    function createProductCard(product) {
        const card = document.createElement('div');
        card.className = 'product-card';
        card.dataset.id = product.id;

        // Категория на русском
        const categoryMap = {
            'ELECTRONICS': 'Электроника',
            'CLOTHING': 'Одежда',
            'BOOKS': 'Книги',
            'FOOD': 'Еда',
            'SPORTS': 'Спорт товары',
            'HOME': 'Товары для дома',
            'BEAUTY': 'Красота',
            'OTHER': 'Другое'
        };

        const categoryName = categoryMap[product.category] || product.category;

        // Изображение товара (если есть)
        const imageUrl = product.image ? `/uploads/images/${product.image}` : '/images/no-image.png';

        card.innerHTML = `
            <div class="product-image">
                <img src="${imageUrl}" alt="${product.name}" 
                     onerror="this.onerror=null; this.src='/images/no-image.png'">
            </div>
            <div class="product-info">
                <h3 class="product-name">${product.name}</h3>
                <p class="product-price">${product.price.toFixed(2)} ₽</p>
                <p class="product-category">Категория: ${categoryName}</p>
                <p class="product-count">На складе: ${product.count} шт.</p>
                <p class="product-description">${product.description || 'Описание отсутствует'}</p>
            </div>
            <div class="product-actions">
                <button class="edit-btn" data-id="${product.id}">Редактировать</button>
                <button class="delete-btn" data-id="${product.id}">Удалить</button>
            </div>
        `;

        // Добавляем обработчики событий для кнопок
        card.querySelector('.edit-btn').addEventListener('click', () => editProduct(product.id));
        card.querySelector('.delete-btn').addEventListener('click', () => confirmDeleteProduct(product.id));

        return card;
    }

    // Добавление нового товара
    async function addProduct() {
        // Сбор данных формы
        const name = document.getElementById('add-name').value.trim();
        const price = parseFloat(document.getElementById('add-price').value);
        const count = parseInt(document.getElementById('add-count').value);
        const category = document.getElementById('add-category').value;
        const description = document.getElementById('add-description').value.trim();
        const imageFile = addImageFile.files[0];

        // Валидация
        if (!validateProductData({ name, price, count, category, description })) {
            return;
        }

        // Подготовка FormData
        const formData = new FormData();
        formData.append('name', name);
        formData.append('price', price);
        formData.append('count', count);
        formData.append('category', category);
        formData.append('description', description);

        if (imageFile) {
            formData.append('imageFile', imageFile);
        } else {
            showNotification('Выберите изображение товара', 'error');
            return;
        }

        try {
            addSubmitBtn.disabled = true;
            addSubmitBtn.textContent = 'Добавление...';

            const response = await fetch(`${API_BASE_URL}/products`, {
                method: 'POST',
                body: formData,
                credentials: 'include'
            });

            if (response.status === 401) {
                showNotification('Сессия истекла. Пожалуйста, войдите снова.', 'error');
                setTimeout(() => {
                    window.location.href = '/login?redirect=/seller';
                }, 2000);
                return;
            }

            if (!response.ok) {
                const errorData = await response.json();
                throw new Error(errorData.error || 'Ошибка при создании товара');
            }

            const result = await response.json();

            showNotification('Товар успешно добавлен!', 'success');

            // Очистка формы
            addProductForm.reset();
            addFileName.textContent = 'Файл не выбран';

            // Переход на вкладку редактирования и обновление списка
            document.querySelector('[data-tab="edit"]').click();

        } catch (error) {
            console.error('Ошибка при добавлении товара:', error);
            showNotification(`Ошибка: ${error.message}`, 'error');
        } finally {
            addSubmitBtn.disabled = false;
            addSubmitBtn.textContent = 'Добавить товар';
        }
    }

    // Редактирование товара
    async function editProduct(productId) {
        try {
            const response = await fetch(`${API_BASE_URL}/products/${productId}`, {
                method: 'GET',
                headers: {
                    'Content-Type': 'application/json'
                },
                credentials: 'include'
            });

            if (response.status === 401) {
                showNotification('Сессия истекла. Пожалуйста, войдите снова.', 'error');
                setTimeout(() => {
                    window.location.href = '/login?redirect=/seller';
                }, 2000);
                return;
            }

            if (!response.ok) {
                const errorData = await response.json();
                throw new Error(errorData.error || 'Ошибка загрузки товара');
            }

            const product = await response.json();
            currentEditingProductId = productId;

            // Заполнение формы редактирования
            document.getElementById('edit-id').value = product.id;
            document.getElementById('edit-name').value = product.name;
            document.getElementById('edit-price').value = product.price;
            document.getElementById('edit-count').value = product.count;
            document.getElementById('edit-category').value = product.category;
            document.getElementById('edit-description').value = product.description || '';

            // Отображение текущего изображения
            if (product.image) {
                const imageUrl = `/uploads/images/${product.image}`;
                currentImage.src = imageUrl;
                currentImage.style.display = 'block';
                currentImagePreview.style.display = 'block';

                // Добавляем обработчик ошибок для изображения
                currentImage.onerror = function() {
                    this.src = '/images/no-image.png';
                };
            } else {
                currentImage.style.display = 'none';
                currentImagePreview.style.display = 'none';
            }

            // Сброс выбора нового файла
            editImageFile.value = '';
            editFileName.textContent = 'Файл не выбран';

            // Переключение на форму редактирования
            editTabContent.classList.remove('active');
            editFormContainer.style.display = 'block';
            authLinks.style.display = 'none';

        } catch (error) {
            console.error('Ошибка при загрузке товара для редактирования:', error);
            showNotification(`Ошибка: ${error.message}`, 'error');
        }
    }

    // Обновление товара
    async function updateProduct() {
        const id = document.getElementById('edit-id').value;
        const name = document.getElementById('edit-name').value.trim();
        const price = parseFloat(document.getElementById('edit-price').value);
        const count = parseInt(document.getElementById('edit-count').value);
        const category = document.getElementById('edit-category').value;
        const description = document.getElementById('edit-description').value.trim();
        const imageFile = editImageFile.files[0];

        // Валидация
        if (!validateProductData({ name, price, count, category, description })) {
            return;
        }

        // Подготовка FormData
        const formData = new FormData();
        formData.append('name', name);
        formData.append('price', price);
        formData.append('count', count);
        formData.append('category', category);
        formData.append('description', description);

        if (imageFile) {
            formData.append('imageFile', imageFile);
        }

        try {
            updateSubmitBtn.disabled = true;
            updateSubmitBtn.textContent = 'Сохранение...';

            const response = await fetch(`${API_BASE_URL}/products/${id}`, {
                method: 'PUT',
                body: formData,
                credentials: 'include'
            });

            if (response.status === 401) {
                showNotification('Сессия истекла. Пожалуйста, войдите снова.', 'error');
                setTimeout(() => {
                    window.location.href = '/login?redirect=/seller';
                }, 2000);
                return;
            }

            if (!response.ok) {
                const errorData = await response.json();
                throw new Error(errorData.error || 'Ошибка при обновлении товара');
            }

            const result = await response.json();

            showNotification('Товар успешно обновлен!', 'success');

            // Возврат к списку товаров и обновление
            cancelEdit();
            loadSellerProducts();

        } catch (error) {
            console.error('Ошибка при обновлении товара:', error);
            showNotification(`Ошибка: ${error.message}`, 'error');
        } finally {
            updateSubmitBtn.disabled = false;
            updateSubmitBtn.textContent = 'Сохранить изменения';
        }
    }

    // Отмена редактирования
    function cancelEdit() {
        currentEditingProductId = null;
        editFormContainer.style.display = 'none';
        editTabContent.classList.add('active');
        authLinks.style.display = 'flex';
        editProductForm.reset();
        currentImage.style.display = 'none';
        currentImagePreview.style.display = 'none';
    }

    // Подтверждение удаления товара
    function confirmDeleteProduct(productId) {
        if (confirm('Вы уверены, что хотите удалить этот товар?')) {
            deleteProductConfirmed(productId);
        }
    }

    // Удаление товара (из формы редактирования)
    async function deleteProduct() {
        const productId = currentEditingProductId;

        if (!productId || !confirm('Вы уверены, что хотите удалить этот товар?')) {
            return;
        }

        await deleteProductConfirmed(productId);
    }

    // Удаление товара (подтвержденное)
    async function deleteProductConfirmed(productId) {
        try {
            deleteProductBtn.disabled = true;
            deleteProductBtn.textContent = 'Удаление...';

            const response = await fetch(`${API_BASE_URL}/products/${productId}`, {
                method: 'DELETE',
                headers: {
                    'Content-Type': 'application/json'
                },
                credentials: 'include'
            });

            if (response.status === 401) {
                showNotification('Сессия истекла. Пожалуйста, войдите снова.', 'error');
                setTimeout(() => {
                    window.location.href = '/login?redirect=/seller';
                }, 2000);
                return;
            }

            if (!response.ok) {
                const errorData = await response.json();
                throw new Error(errorData.error || 'Ошибка при удалении товара');
            }

            const result = await response.json();

            showNotification('Товар успешно удален!', 'success');

            // Если удаляем из формы редактирования
            if (currentEditingProductId === productId) {
                cancelEdit();
            }

            // Обновление списка товаров
            loadSellerProducts();

        } catch (error) {
            console.error('Ошибка при удалении товара:', error);
            showNotification(`Ошибка: ${error.message}`, 'error');
        } finally {
            deleteProductBtn.disabled = false;
            deleteProductBtn.textContent = 'Удалить товар';
        }
    }

    // Валидация данных товара
    function validateProductData(data) {
        const { name, price, count, category } = data;

        if (!name || name.length < 2) {
            showNotification('Название товара должно содержать минимум 2 символа', 'error');
            return false;
        }

        if (isNaN(price) || price <= 0) {
            showNotification('Цена должна быть числом больше 0', 'error');
            return false;
        }

        if (isNaN(count) || count < 0) {
            showNotification('Количество должно быть числом не меньше 0', 'error');
            return false;
        }

        if (!category) {
            showNotification('Выберите категорию товара', 'error');
            return false;
        }

        return true;
    }

    // Показать уведомление
    function showNotification(message, type = 'info') {
        // Удаляем предыдущие уведомления
        const existingNotification = document.querySelector('.notification');
        if (existingNotification) {
            existingNotification.remove();
        }

        // Создаем новое уведомление
        const notification = document.createElement('div');
        notification.className = `notification ${type}`;
        notification.textContent = message;

        // Стили для уведомления
        notification.style.cssText = `
            position: fixed;
            top: 20px;
            right: 20px;
            padding: 15px 20px;
            border-radius: 5px;
            color: white;
            font-weight: bold;
            z-index: 1000;
            opacity: 0;
            transform: translateY(-20px);
            transition: opacity 0.3s, transform 0.3s;
        `;

        if (type === 'success') {
            notification.style.backgroundColor = '#4CAF50';
        } else if (type === 'error') {
            notification.style.backgroundColor = '#f44336';
        } else {
            notification.style.backgroundColor = '#2196F3';
        }

        document.body.appendChild(notification);

        // Анимация появления
        setTimeout(() => {
            notification.style.opacity = '1';
            notification.style.transform = 'translateY(0)';
        }, 10);

        // Автоматическое скрытие
        setTimeout(() => {
            notification.style.opacity = '0';
            notification.style.transform = 'translateY(-20px)';
            setTimeout(() => notification.remove(), 300);
        }, 3000);
    }

    // Глобальные функции для использования в HTML
    window.loadSellerProducts = loadSellerProducts;
    window.editProduct = editProduct;
});