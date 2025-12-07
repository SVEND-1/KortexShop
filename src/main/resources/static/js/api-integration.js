// api-integration.js
document.addEventListener('DOMContentLoaded', function() {
    console.log('API интеграция загружена');
    
    // Настройка базового URL для API
    const API_BASE_URL = '/api';
    
    // ========== РЕГИСТРАЦИЯ ==========
    const registerForm = document.getElementById('registerForm');
    if (registerForm) {
        console.log('Найдена форма регистрации');
        registerForm.addEventListener('submit', async function(e) {
            e.preventDefault();
            await handleRegistration(e);
        });
    }
    
    // ========== ВОССТАНОВЛЕНИЕ ПАРОЛЯ ==========
    const forgotPasswordBtn = document.getElementById('continueBtn');
    if (forgotPasswordBtn) {
        console.log('Найдена кнопка восстановления пароля');
        forgotPasswordBtn.addEventListener('click', async function() {
            await handleForgotPassword();
        });
    }
    
    // ========== ПОДТВЕРЖДЕНИЕ КОДА ==========
    const codeConfirmBtn = document.getElementById('continueBtn1');
    if (codeConfirmBtn) {
        console.log('Найдена кнопка подтверждения кода');
        codeConfirmBtn.addEventListener('click', async function() {
            await handleCodeConfirmation();
        });
    }
    
    // ========== СОХРАНЕНИЕ НОВОГО ПАРОЛЯ ==========
    const savePasswordBtn = document.getElementById('continueBtn2');
    if (savePasswordBtn) {
        console.log('Найдена кнопка сохранения пароля');
        savePasswordBtn.addEventListener('click', async function() {
            await handlePasswordReset();
        });
    }
    
    // ========== ПОВТОРНАЯ ОТПРАВКА КОДА ==========
    const resendCodeBtn = document.getElementById('resendCodeBtn');
    if (resendCodeBtn) {
        console.log('Найдена кнопка повторной отправки кода');
        resendCodeBtn.addEventListener('click', async function() {
            await handleResendCode();
        });
    }
});

// ========== ОСНОВНЫЕ ФУНКЦИИ ==========

// 1. Обработка регистрации
async function handleRegistration(e) {
    try {
        // Валидация формы
        if (!validateRegistrationForm()) {
            return;
        }

        const form = e.target;
        const formData = new FormData(form);

        const userData = {
            email: formData.get('email'),
            password: formData.get('password'),
            name: formData.get('name')
        };

        console.log('Отправка данных для регистрации:', userData);

        // Шаг 1: Отправка email и получение кода
        const response = await fetch('/api/auth/register/send-code', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(userData)
        });

        const result = await response.json();

        if (response.ok) {
            // Сохраняем registrationId и данные пользователя в localStorage
            localStorage.setItem('registrationId', result.registrationId);
            localStorage.setItem('pendingEmail', userData.email);
            localStorage.setItem('pendingName', userData.name);
            localStorage.setItem('pendingPassword', userData.password);

            alert('Код подтверждения отправлен на email. Переходим к вводу кода.');

            // Переходим на страницу ввода кода
            window.location.href = 'codeFromEmailForm.html?type=registration';
        } else {
            alert('Ошибка: ' + (result.message || 'Не удалось отправить код'));
        }

    } catch (error) {
        console.error('Ошибка при регистрации:', error);
        alert('Произошла ошибка при отправке данных');
    }
}

// 2. Обработка запроса на восстановление пароля
async function handleForgotPassword() {
    try {
        const emailInput = document.getElementById('forgotPasswordEmail');
        const email = emailInput.value;
        
        // Валидация email
        if (!email || !validateEmail(email)) {
            alert('Введите корректный email');
            emailInput.focus();
            return;
        }
        
        console.log('Запрос восстановления пароля для:', email);
        
        const response = await fetch(`/api/auth/password/forgot?email=${encodeURIComponent(email)}`, {
            method: 'POST'
        });
        
        const result = await response.json();
        
        if (response.ok) {
            // Сохраняем resetId в localStorage
            localStorage.setItem('resetId', result.resetId);
            localStorage.setItem('resetEmail', email);
            
            alert('Код для сброса пароля отправлен на email');
            
            // Переходим на страницу ввода кода
            window.location.href = 'codeFromEmailForm.html?type=reset';
        } else {
            alert('Ошибка: ' + (result.message || 'Пользователь не найден'));
        }
        
    } catch (error) {
        console.error('Ошибка при запросе восстановления пароля:', error);
        alert('Произошла ошибка при отправке запроса');
    }
}

// 3. Обработка подтверждения кода
// 3. Обработка подтверждения кода
async function handleCodeConfirmation() {
    try {
        console.log('=== НАЧАЛО ПОДТВЕРЖДЕНИЯ КОДА ===');

        // Получаем код из полей ввода
        const codeInputs = document.querySelectorAll('.code-input');
        let code = '';

        codeInputs.forEach(input => {
            code += input.value;
        });

        // Проверяем, что все 6 цифр введены
        if (code.length !== 6) {
            alert('Введите все 6 цифр кода');
            return;
        }

        // Форматируем код в XXX-XXX
        const formattedCode = code.substring(0, 3)  + code.substring(3, 6);
        console.log('Сформированный код:', formattedCode);

        // Определяем тип операции (регистрация или сброс пароля)
        const urlParams = new URLSearchParams(window.location.search);
        const type = urlParams.get('type');
        console.log('Тип операции:', type);

        let endpoint, idKey;

        if (type === 'registration') {
            endpoint = '/api/auth/register/verify';
            idKey = 'registrationId';
        } else {
            endpoint = '/api/auth/password/verify';
            idKey = 'resetId';
        }

        const operationId = localStorage.getItem(idKey);
        console.log('Operation ID:', operationId);

        if (!operationId) {
            alert('Сессия истекла. Пожалуйста, начните заново.');
            window.location.href = type === 'registration' ? 'registerForm.html' : 'forgotPasswordForm.html';
            return;
        }

        console.log(`Отправка запроса на ${endpoint}`, {
            operationId,
            code: formattedCode
        });

        const response = await fetch(`${endpoint}?${idKey}=${operationId}&code=${formattedCode}`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            }
        });

        console.log('Статус ответа:', response.status);

        const result = await response.json();
        console.log('Результат:', result);

        if (response.ok && result.success) {
            alert(result.message || 'Код подтвержден успешно!');

            if (type === 'registration') {
                // Регистрация завершена
                localStorage.removeItem('registrationId');
                localStorage.removeItem('pendingEmail');
                localStorage.removeItem('pendingName');
                localStorage.removeItem('pendingPassword');

                // Переходим на главную
                window.location.href = result.redirectUrl || '/';

            } else {
                // Сброс пароля - переходим к установке нового пароля
                window.location.href = 'recoveryPasswordForm.html';
            }
        } else {
            alert('Ошибка: ' + (result.message || 'Неверный код'));
        }

    } catch (error) {
        console.error('Ошибка при подтверждении кода:', error);
        alert('Произошла ошибка при проверке кода: ' + error.message);
    }
}

// 4. Обработка сброса пароля
async function handlePasswordReset() {
    try {
        const password = document.getElementById('recoveryPassword').value;
        const confirmPassword = document.getElementById('recoveryConfirmPassword').value;
        
        // Валидация
        if (password.length < 6) {
            alert('Пароль должен содержать не менее 6 символов');
            return;
        }
        
        if (password !== confirmPassword) {
            alert('Пароли не совпадают');
            return;
        }
        
        const resetId = localStorage.getItem('resetId');
        
        if (!resetId) {
            alert('Сессия истекла. Пожалуйста, начните заново.');
            window.location.href = 'forgotPasswordForm.html';
            return;
        }
        
        console.log('Сброс пароля:', { resetId, newPassword: password });
        
        const response = await fetch(`/api/auth/password/reset?resetId=${resetId}&newPassword=${encodeURIComponent(password)}&confirmPassword=${encodeURIComponent(confirmPassword)}`, {
            method: 'POST'
        });
        
        const result = await response.text();
        
        if (response.ok) {
            // Очищаем localStorage
            localStorage.removeItem('resetId');
            localStorage.removeItem('resetEmail');
            
            alert('Пароль успешно изменен! Теперь вы можете войти с новым паролем.');
            window.location.href = 'loginForm.html';
        } else {
            alert('Ошибка: ' + (result.message || 'Не удалось изменить пароль'));
        }
        
    } catch (error) {
        console.error('Ошибка при сбросе пароля:', error);
        alert('Произошла ошибка при изменении пароля');
    }
}

// 5. Повторная отправка кода
async function handleResendCode() {
    try {
        const urlParams = new URLSearchParams(window.location.search);
        const type = urlParams.get('type');
        
        let endpoint, idKey;
        
        if (type === 'registration') {
            endpoint = '/api/auth/register/resend-code';
            idKey = 'registrationId';
        } else {
            // Для сброса пароля нужно начать процесс заново
            alert('Для повторной отправки кода начните процесс восстановления пароля заново.');
            window.location.href = 'forgotPasswordForm.html';
            return;
        }
        
        const operationId = localStorage.getItem(idKey);
        
        if (!operationId) {
            alert('Сессия истекла. Пожалуйста, начните заново.');
            window.location.href = type === 'registration' ? 'registerForm.html' : 'forgotPasswordForm.html';
            return;
        }
        
        console.log(`Повторная отправка кода для ${type}:`, operationId);
        
        const response = await fetch(`${endpoint}?${idKey}=${operationId}`, {
            method: 'POST'
        });
        
        const result = await response.text();
        
        if (response.ok) {
            alert('Новый код отправлен на email');
            resetTimer(); // Сброс таймера (если есть)
        } else {
            alert('Ошибка: ' + (result.message || 'Не удалось отправить код'));
        }
        
    } catch (error) {
        console.error('Ошибка при повторной отправке кода:', error);
        alert('Произошла ошибка при отправке кода');
    }
}

// ========== ВСПОМОГАТЕЛЬНЫЕ ФУНКЦИИ ==========

// Функции валидации из authScript.js
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

function validateEmail(email) {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return emailRegex.test(email);
}

function showError(message) {
    alert('Ошибка: ' + message);
}

function resetTimer() {
    // Функция для сброса таймера (если нужно)
    const timerElement = document.getElementById('timer');
    if (timerElement) {
        timerElement.textContent = '00:60';
    }
}