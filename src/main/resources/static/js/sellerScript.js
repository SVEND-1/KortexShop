// Скрипт для формы продавца
document.addEventListener('DOMContentLoaded', function() {
    console.log('Панель продавца загружена');
    initTabs();
    initFileInput();
    initEditFileInput();
    setupProductForm();
    setupEditProductForm();
    loadProductsForEditing();
    setupSearch();
});

// Инициализация табов
function initTabs() {
    const tabButtons = document.querySelectorAll('.tab-button');
    const tabContents = document.querySelectorAll('.tab-content');
    
    tabButtons.forEach(button => {
        button.addEventListener('click', function() {
            const tabId = this.getAttribute('data-tab');
            
            // Убираем активный класс у всех кнопок и контента
            tabButtons.forEach(btn => btn.classList.remove('active'));
            tabContents.forEach(content => content.classList.remove('active'));
            
            // Добавляем активный класс текущей кнопке
            this.classList.add('active');
            
            // Показываем соответствующий контент
            if (tabId === 'edit') {
                document.getElementById('edit-tab').classList.add('active');
                loadProductsForEditing();
            } else if (tabId === 'add') {
                document.getElementById('add-tab').classList.add('active');
            }
        });
    });
}

// Инициализация загрузки файлов для добавления
function initFileInput() {
    const fileInput = document.getElementById('fileInput');
    const fileName = document.getElementById('fileName');
    
    if (fileInput && fileName) {
        fileInput.addEventListener('change', function() {
            if (this.files.length > 0) {
                const file = this.files[0];
                if (file.type.startsWith('image/')) {
                    fileName.textContent = file.name;
                } else {
                    alert('Пожалуйста, выберите файл изображения');
                    this.value = '';
                    fileName.textContent = 'Файл не выбран';
                }
            } else {
                fileName.textContent = 'Файл не выбран';
            }
        });
    }
}

// Инициализация загрузки файлов для редактирования
function initEditFileInput() {
    const fileInput = document.getElementById('edit-fileInput');
    const fileName = document.getElementById('edit-fileName');
    
    if (fileInput && fileName) {
        fileInput.addEventListener('change', function() {
            if (this.files.length > 0) {
                const file = this.files[0];
                if (file.type.startsWith('image/')) {
                    fileName.textContent = file.name;
                } else {
                    alert('Пожалуйста, выберите файл изображения');
                    this.value = '';
                    fileName.textContent = 'Файл не выбран';
                }
            } else {
                fileName.textContent = 'Файл не выбран';
            }
        });
    }
}

// Настройка формы добавления товара
function setupProductForm() {
    const productForm = document.getElementById('add-product-form');
    const fileInput = document.getElementById('fileInput');
    
    if (!productForm) return;
    
    productForm.addEventListener('submit', async function(e) {
        e.preventDefault();
        
        if (!validateProductForm()) {
            return;
        }
        
        const formData = new FormData(this);
        const productData = {
            name: formData.get('name'),
            price: parseFloat(formData.get('price')),
            quantity: parseInt(formData.get('quantity')),
            category: formData.get('category'),
            description: formData.get('description'),
            brand: formData.get('brand') || 'Мой магазин',
            seller: 'Текущий пользователь',
            image: 'images/product-img.png' // временное изображение
        };
        
        try {
            // AJAX запрос к Spring бекенду
            const response = await fetch('/api/products', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(productData)
            });
            
            if (!response.ok) {
                throw new Error('Ошибка сервера');
            }
            
            const result = await response.json();
            
            // Если есть изображение, загружаем его отдельно
            if (fileInput.files.length > 0) {
                await uploadProductImage(result.id, fileInput.files[0]);
            }
            
            showSuccessMessage(`Товар "${productData.name}" успешно добавлен!`);
            this.reset();
            document.getElementById('fileName').textContent = 'Файл не выбран';
            
            // Обновляем список товаров
            loadProductsForEditing();
            
        } catch (error) {
            showErrorMessage('Ошибка при добавлении товара: ' + error.message);
        }
    });
}

// Настройка формы редактирования товара
function setupEditProductForm() {
    const editForm = document.getElementById('edit-product-form');
    const cancelBtn = document.getElementById('cancel-edit');
    const deleteBtn = document.getElementById('delete-product');
    
    if (!editForm) return;
    
    // Отправка формы редактирования
    editForm.addEventListener('submit', async function(e) {
        e.preventDefault();
        
        if (!validateEditProductForm()) {
            return;
        }
        
        const productId = document.getElementById('edit-id').value;
        const formData = new FormData(this);
        const productData = {
            id: productId,
            name: formData.get('name'),
            price: parseFloat(formData.get('price')),
            quantity: parseInt(formData.get('quantity')),
            category: formData.get('category'),
            description: formData.get('description'),
            brand: formData.get('brand') || 'Мой магазин'
        };
        
        try {
            // AJAX запрос к Spring бекенду
            const response = await fetch(`/api/products/${productId}`, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(productData)
            });
            
            if (!response.ok) {
                throw new Error('Ошибка сервера');
            }
            
            // Если выбран новый файл изображения
            const fileInput = document.getElementById('edit-fileInput');
            if (fileInput.files.length > 0) {
                await uploadProductImage(productId, fileInput.files[0]);
            }
            
            showSuccessMessage(`Товар "${productData.name}" успешно обновлен!`);
            
            // Возвращаемся к списку товаров
            showProductsList();
            loadProductsForEditing();
            
        } catch (error) {
            showErrorMessage('Ошибка при обновлении товара: ' + error.message);
        }
    });
    
    // Кнопка отмены
    cancelBtn.addEventListener('click', showProductsList);
    
    // Кнопка удаления
    deleteBtn.addEventListener('click', async function() {
        const productId = document.getElementById('edit-id').value;
        const productName = document.getElementById('edit-name').value;
        
        if (!confirm(`Вы уверены, что хотите удалить товар "${productName}"?`)) {
            return;
        }
        
        try {
            // AJAX запрос к Spring бекенду
            const response = await fetch(`/api/products/${productId}`, {
                method: 'DELETE'
            });
            
            if (!response.ok) {
                throw new Error('Ошибка сервера');
            }
            
            showSuccessMessage(`Товар "${productName}" успешно удален!`);
            
            // Возвращаемся к списку товаров
            showProductsList();
            loadProductsForEditing();
            
        } catch (error) {
            showErrorMessage('Ошибка при удалении товара: ' + error.message);
        }
    });
}

// Загрузка товаров для редактирования
async function loadProductsForEditing() {
    const productsContainer = document.getElementById('products-container');
    
    if (!productsContainer) return;
    
    productsContainer.innerHTML = '<p class="loading-text">Загрузка товаров...</p>';
    
    try {
        // AJAX запрос к Spring бекенду
        const response = await fetch('/api/products/seller');
        
        if (!response.ok) {
            throw new Error('Ошибка загрузки товаров');
        }
        
        const products = await response.json();
        
        if (products.length === 0) {
            productsContainer.innerHTML = `
                <div class="no-products">
                    <p>У вас пока нет товаров</p>
                    <button class="tab-button" data-tab="add">Добавить первый товар</button>
                </div>
            `;
            return;
        }
        
        displayProductsList(products);
        
    } catch (error) {
        productsContainer.innerHTML = `
            <div class="no-products">
                <p>Ошибка загрузки товаров: ${error.message}</p>
            </div>
        `;
    }
}

// Отображение списка товаров
function displayProductsList(products) {
    const productsContainer = document.getElementById('products-container');
    const searchTerm = document.getElementById('product-search')?.value.toLowerCase() || '';
    
    const filteredProducts = products.filter(product => 
        product.name.toLowerCase().includes(searchTerm)
    );
    
    if (filteredProducts.length === 0) {
        productsContainer.innerHTML = '<p class="no-products">Товары не найдены</p>';
        return;
    }
    
    productsContainer.innerHTML = '';
    
    filteredProducts.forEach(product => {
        const productElement = createProductElement(product);
        productsContainer.appendChild(productElement);
    });
}

// Создание элемента товара
function createProductElement(product) {
    const div = document.createElement('div');
    div.className = 'product-item';
    div.dataset.id = product.id;
    
    div.innerHTML = `
        <div class="product-header">
            <h3 class="product-name">${product.name}</h3>
            <span class="product-price">${product.price.toFixed(2)} ₽</span>
        </div>
        <div class="product-details">
            <div class="product-detail">
                <span>Категория:</span> ${product.categoryName || product.category}
            </div>
            <div class="product-detail">
                <span>Бренд:</span> ${product.brand || 'Не указан'}
            </div>
            <div class="product-detail">
                <span>Количество:</span> ${product.quantity} шт.
            </div>
            <div class="product-detail">
                <span>Статус:</span> ${product.quantity > 0 ? 'В наличии' : 'Нет в наличии'}
            </div>
        </div>
        <p class="product-description">${product.description.substring(0, 100)}${product.description.length > 100 ? '...' : ''}</p>
        <div class="product-actions">
            <button class="edit-btn" onclick="editProduct(${product.id})">Редактировать</button>
            <button class="delete-btn" onclick="deleteProductPrompt(${product.id}, '${product.name}')">Удалить</button>
        </div>
    `;
    
    return div;
}

// Редактирование товара
async function editProduct(productId) {
    try {
        // AJAX запрос к Spring бекенду
        const response = await fetch(`/api/products/${productId}`);
        
        if (!response.ok) {
            throw new Error('Ошибка загрузки товара');
        }
        
        const product = await response.json();
        
        // Заполняем форму редактирования
        document.getElementById('edit-id').value = product.id;
        document.getElementById('edit-name').value = product.name;
        document.getElementById('edit-price').value = product.price;
        document.getElementById('edit-brand').value = product.brand || '';
        document.getElementById('edit-quantity').value = product.quantity;
        document.getElementById('edit-category').value = product.category;
        document.getElementById('edit-description').value = product.description;
        
        // Отображаем текущее изображение
        const previewContainer = document.getElementById('current-image-preview');
        if (product.image && product.image !== 'images/product-img.png') {
            previewContainer.innerHTML = `
                <p>Текущее изображение:</p>
                <img src="${product.image}" alt="${product.name}" class="current-image">
            `;
        } else {
            previewContainer.innerHTML = '<p>Текущее изображение не загружено</p>';
        }
        
        // Сбрасываем выбор файла
        document.getElementById('edit-fileInput').value = '';
        document.getElementById('edit-fileName').textContent = 'Файл не выбран';
        
        // Показываем форму редактирования
        document.getElementById('edit-tab').style.display = 'none';
        document.getElementById('edit-form-container').style.display = 'block';
        
    } catch (error) {
        showErrorMessage('Ошибка при загрузке товара: ' + error.message);
    }
}

// Удаление товара (подтверждение)
function deleteProductPrompt(productId, productName) {
    if (!confirm(`Вы уверены, что хотите удалить товар "${productName}"?`)) {
        return;
    }
    deleteProduct(productId);
}

// Удаление товара
async function deleteProduct(productId) {
    try {
        // AJAX запрос к Spring бекенду
        const response = await fetch(`/api/products/${productId}`, {
            method: 'DELETE'
        });
        
        if (!response.ok) {
            throw new Error('Ошибка удаления товара');
        }
        
        showSuccessMessage('Товар успешно удален!');
        loadProductsForEditing();
        
    } catch (error) {
        showErrorMessage('Ошибка при удалении товара: ' + error.message);
    }
}

// Загрузка изображения товара
async function uploadProductImage(productId, imageFile) {
    const formData = new FormData();
    formData.append('image', imageFile);
    
    try {
        const response = await fetch(`/api/products/${productId}/image`, {
            method: 'POST',
            body: formData
        });
        
        if (!response.ok) {
            throw new Error('Ошибка загрузки изображения');
        }
        
        return await response.json();
    } catch (error) {
        console.error('Ошибка загрузки изображения:', error);
        return null;
    }
}

// Показать список товаров
function showProductsList() {
    document.getElementById('edit-form-container').style.display = 'none';
    document.getElementById('edit-tab').style.display = 'block';
}

// Настройка поиска
function setupSearch() {
    const searchInput = document.getElementById('product-search');
    if (searchInput) {
        searchInput.addEventListener('input', () => {
            // Перезагружаем товары с учетом поиска
            loadProductsForEditing();
        });
    }
}

// Валидация формы добавления
function validateProductForm() {
    return validateForm('add-product-form');
}

// Валидация формы редактирования
function validateEditProductForm() {
    return validateForm('edit-product-form');
}

// Общая валидация формы
function validateForm(formId) {
    const form = document.getElementById(formId);
    const name = form.querySelector('input[name="name"]');
    const price = form.querySelector('input[name="price"]');
    const quantity = form.querySelector('input[name="quantity"]');
    const category = form.querySelector('select[name="category"]');
    const description = form.querySelector('textarea[name="description"]');
    
    if (!name.value.trim()) {
        showErrorMessage('Введите название товара');
        name.focus();
        return false;
    }
    
    if (price.value <= 0) {
        showErrorMessage('Цена должна быть больше 0');
        price.focus();
        return false;
    }
    
    if (quantity.value < 0) {
        showErrorMessage('Количество не может быть отрицательным');
        quantity.focus();
        return false;
    }
    
    if (!category.value) {
        showErrorMessage('Выберите категорию товара');
        category.focus();
        return false;
    }
    
    if (!description.value.trim()) {
        showErrorMessage('Введите описание товара');
        description.focus();
        return false;
    }
    
    return true;
}

// Уведомления
function showSuccessMessage(message) {
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
    `;
    notification.textContent = message;
    document.body.appendChild(notification);
    setTimeout(() => notification.remove(), 3000);
}

function showErrorMessage(message) {
    alert('❌ ' + message);
}

// Сделаем функции глобальными для использования в onclick
window.editProduct = editProduct;
window.deleteProductPrompt = deleteProductPrompt;