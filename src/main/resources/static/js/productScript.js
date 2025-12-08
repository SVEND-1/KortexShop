// Скрипт для страницы товара
document.addEventListener('DOMContentLoaded', function() {
    console.log('Страница товара загружена');
    
    // Инициализация счетчика количества
    initQuantitySelector();
    
    // Инициализация формы добавления в корзину
    initAddToCartForm();
});

// Управление количеством товара
function initQuantitySelector() {
    const minusBtn = document.querySelector('.quantity-btn.minus');
    const plusBtn = document.querySelector('.quantity-btn.plus');
    const quantityElement = document.querySelector('.quantity');
    const quantityInput = document.getElementById('quantityInput');
    
    let quantity = 1;
    
    if (minusBtn && plusBtn && quantityElement && quantityInput) {
        minusBtn.addEventListener('click', function() {
            if (quantity > 1) {
                quantity--;
                updateQuantity();
            }
        });
        
        plusBtn.addEventListener('click', function() {
            quantity++;
            updateQuantity();
        });
        
        function updateQuantity() {
            quantityElement.textContent = quantity;
            quantityInput.value = quantity;
        }
    }
}

// Обработка добавления в корзину
function initAddToCartForm() {
    const addToCartForm = document.querySelector('.add-to-cart-form');
    const successMessage = document.getElementById('successMessage');
    const errorMessage = document.getElementById('errorMessage');
    
    if (addToCartForm) {
        addToCartForm.addEventListener('submit', function(e) {
            e.preventDefault();
            
            const submitButton = this.querySelector('button[type="submit"]');
            const originalText = submitButton.innerHTML;
            
            // Показываем состояние загрузки
            submitButton.innerHTML = '🔄 Добавляем...';
            submitButton.disabled = true;
            
            // Скрываем предыдущие сообщения
            successMessage.style.display = 'none';
            errorMessage.style.display = 'none';
            
            // Имитируем отправку на сервер (заглушка)
            setTimeout(() => {
                // В реальном проекте здесь будет fetch запрос
                const isSuccess = Math.random() > 0.2; // 80% успеха для демо
                
                if (isSuccess) {
                    successMessage.style.display = 'block';
                    submitButton.innerHTML = '✅ Добавлено!';
                } else {
                    errorMessage.style.display = 'block';
                    submitButton.innerHTML = '❌ Ошибка';
                }
                
                // Возвращаем исходное состояние через 2 секунды
                setTimeout(() => {
                    submitButton.innerHTML = originalText;
                    submitButton.disabled = false;
                    if (isSuccess) {
                        successMessage.style.display = 'none';
                    }
                }, 2000);
                
            }, 1000);
        });
    }
}