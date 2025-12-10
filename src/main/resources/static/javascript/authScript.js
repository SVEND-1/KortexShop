// authScript.js - только валидация
document.addEventListener('DOMContentLoaded', function() {
    console.log('Страница авторизации загружена');
    
    // Фронтенд валидация при отправке формы
    const registerForm = document.getElementById('registerForm');
    if (registerForm) {
        registerForm.addEventListener('submit', function(e) {
            if (!validateRegistrationForm()) {
                e.preventDefault();
                return false;
            }
        });
    }
    
    const loginForm = document.querySelector('.loginForm');
    if (loginForm && !registerForm) {
        loginForm.addEventListener('submit', function(e) {
            if (!validateLoginForm()) {
                e.preventDefault();
                return false;
            }
        });
    }
});

// Функции валидации
function validateRegistrationForm() {
    const name = document.querySelector('input[name="name"]');
    const email = document.getElementById('email');
    const password = document.getElementById('password');
    const confirmPassword = document.getElementById('confirmPassword');
    const personalData = document.getElementById('personalData');
    
    if (!name.value.trim()) {
        showError('Введите имя');
        name.focus();
        return false;
    }
    
    if (name.value.trim().length < 2) {
        showError('Имя должно содержать минимум 2 символа');
        name.focus();
        return false;
    }
    
    if (!validateEmail(email.value)) {
        showError('Введите корректный email');
        email.focus();
        return false;
    }
    
    if (password.value.length < 6) {
        showError('Пароль должен содержать не менее 6 символов');
        password.focus();
        return false;
    }
    
    if (password.value !== confirmPassword.value) {
        showError('Пароли не совпадают');
        confirmPassword.focus();
        return false;
    }
    
    if (!personalData.checked) {
        showError('Необходимо согласие с правилами и политикой конфиденциальности');
        return false;
    }
    
    return true;
}

function validateLoginForm() {
    const email = document.querySelector('input[type="email"]');
    const password = document.querySelector('input[type="password"]');
    
    if (!validateEmail(email.value)) {
        showError('Введите корректный email');
        email.focus();
        return false;
    }
    
    if (!password.value) {
        showError('Введите пароль');
        password.focus();
        return false;
    }
    
    return true;
}

function validateEmail(email) {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return emailRegex.test(email);
}

function showError(message) {
    alert('Ошибка: ' + message);
}