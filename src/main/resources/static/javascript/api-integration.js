// api-integration.js
document.addEventListener('DOMContentLoaded', function() {
    console.log('API интеграция загружена');

    // ========== ИНИЦИАЛИЗАЦИЯ КОНСТАНТ И ПЕРЕМЕННЫХ ==========
    const RESEND_TIMEOUT = 60; // 60 секунд до возможности повторной отправки
    let resendTimer;

    // ========== РЕГИСТРАЦИЯ ==========
    const registerForm = document.getElementById('registerForm');
    if (registerForm) {
        console.log('Найдена форма регистрации');
        // Удаляем все существующие обработчики и добавляем наш
        registerForm.replaceWith(registerForm.cloneNode(true));
        const newRegisterForm = document.getElementById('registerForm');
        newRegisterForm.addEventListener('submit', async function(e) {
            e.preventDefault();
            e.stopPropagation(); // Предотвращаем всплытие
            await handleRegistration(e);
        });
    }

    // ========== ЛОГИН ==========
    const loginForm = document.getElementById('loginForm');
    if (loginForm) {
        console.log('Найдена форма логина');
        loginForm.addEventListener('submit', async function(e) {
            e.preventDefault();
            await handleLogin(e);
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

    // ========== ВВОД КОДА В ПОЛЯХ ВВОДА ==========
    const codeInputs = document.querySelectorAll('.code-input');
    if (codeInputs.length > 0) {
        console.log('Найдены поля для ввода кода');
        initializeCodeInputs();
    }

    // ========== ЛОГАУТ ==========
    const logoutBtn = document.getElementById('logoutBtn');
    if (logoutBtn) {
        console.log('Найдена кнопка выхода');
        logoutBtn.addEventListener('click', async function(e) {
            e.preventDefault();
            await handleLogout();
        });
    }

    // ========== ТАЙМЕР ДЛЯ ПОВТОРНОЙ ОТПРАВКИ ==========
    const urlParams = new URLSearchParams(window.location.search);
    const type = urlParams.get('type');
    if (type === 'registration' || type === 'reset') {
        startResendTimer(RESEND_TIMEOUT);
    }

    // Проверка авторизации при загрузке
    checkAuth();
});

// ========== ОСНОВНЫЕ ФУНКЦИИ ==========

// 1. Обработка регистрации
async function handleRegistration(e) {
    try {
        console.log('Обработка регистрации...');

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

        console.log('Отправка данных для регистрации:', { ...userData, password: '***' });

        // Показываем индикатор загрузки
        showLoading(true);

        // Шаг 1: Отправка email и получение кода
        const response = await fetch('/api/auth/register/send-code', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(userData)
        });

        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }

        const result = await response.json();
        console.log('Ответ сервера:', result);

        if (result.success) {
            showSuccessMessage(result.message || 'Код подтверждения отправлен на email');

            // Сохраняем данные в sessionStorage
            sessionStorage.setItem('pendingRegistration', JSON.stringify({
                email: userData.email,
                name: userData.name,
                password: userData.password,
                registrationId: result.registrationId
            }));

            // Ждем 1 секунду и переходим на страницу ввода кода
            setTimeout(() => {
                window.location.href = '/codeEmail?type=registration';
            }, 1000);

        } else {
            showErrorMessage(result.message || 'Не удалось отправить код');
        }

    } catch (error) {
        console.error('Ошибка при регистрации:', error);
        showErrorMessage('Произошла ошибка при отправке данных');
    } finally {
        showLoading(false);
    }
}

// 2. Обработка логина
async function handleLogin(e) {
    try {
        console.log('Обработка логина...');

        const form = e.target;
        const formData = new FormData(form);

        const loginData = {
            email: formData.get('email'),
            password: formData.get('password')
        };

        // Валидация
        if (!loginData.email || !loginData.password) {
            showErrorMessage('Заполните все поля');
            return;
        }

        if (!validateEmail(loginData.email)) {
            showErrorMessage('Введите корректный email');
            return;
        }

        console.log('Отправка данных для входа:', { ...loginData, password: '***' });
        showLoading(true);

        const response = await fetch('/api/auth/login', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            credentials: 'include',
            body: JSON.stringify(loginData)
        });

        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }

        const result = await response.json();
        console.log('Ответ сервера:', result);

        if (result.success) {
            showSuccessMessage(result.message || 'Вход выполнен успешно');

            setTimeout(() => {
                if (result.redirectUrl) {
                    window.location.href = result.redirectUrl;
                } else {
                    window.location.href = '/';
                }
            }, 1000);

        } else {
            showErrorMessage(result.message || 'Неверный email или пароль');
        }

    } catch (error) {
        console.error('Ошибка при входе:', error);
        showErrorMessage('Произошла ошибка при входе');
    } finally {
        showLoading(false);
    }
}

// 3. Обработка запроса на восстановление пароля
async function handleForgotPassword() {
    try {
        console.log('Обработка восстановления пароля...');

        const emailInput = document.getElementById('forgotPasswordEmail');
        const email = emailInput ? emailInput.value : document.querySelector('input[type="email"]')?.value;

        // Валидация email
        if (!email || !validateEmail(email)) {
            showErrorMessage('Введите корректный email');
            if (emailInput) emailInput.focus();
            return;
        }

        console.log('Запрос восстановления пароля для:', email);
        showLoading(true);

        const response = await fetch(`/api/auth/password/forgot?email=${encodeURIComponent(email)}`, {
            method: 'POST'
        });

        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }

        const result = await response.json();
        console.log('Ответ сервера:', result);

        if (result.success) {
            showSuccessMessage(result.message || 'Код для сброса пароля отправлен на email');

            // Сохраняем в sessionStorage
            sessionStorage.setItem('pendingPasswordReset', JSON.stringify({
                email: email,
                resetId: result.resetId
            }));

            // Переходим на страницу ввода кода
            setTimeout(() => {
                window.location.href = '/codeEmail?type=reset';
            }, 1000);

        } else {
            showErrorMessage(result.message || 'Пользователь не найден');
        }

    } catch (error) {
        console.error('Ошибка при запросе восстановления пароля:', error);
        showErrorMessage('Произошла ошибка при отправке запроса');
    } finally {
        showLoading(false);
    }
}

// 4. Обработка подтверждения кода
async function handleCodeConfirmation() {
    try {
        console.log('Обработка подтверждения кода...');

        // Получаем код из полей ввода
        const codeInputs = document.querySelectorAll('.code-input');
        let code = '';

        codeInputs.forEach(input => {
            code += input.value;
        });

        // Проверяем, что все 6 цифр введены
        if (code.length !== 6) {
            showErrorMessage('Введите все 6 цифр кода');
            return;
        }

        // Определяем тип операции
        const urlParams = new URLSearchParams(window.location.search);
        const type = urlParams.get('type');

        showLoading(true);

        if (type === 'registration') {
            // Получаем данные из sessionStorage
            const storedData = sessionStorage.getItem('pendingRegistration');
            if (!storedData) {
                showErrorMessage('Сессия истекла. Пожалуйста, начните заново.');
                setTimeout(() => {
                    window.location.href = '/register';
                }, 2000);
                return;
            }

            const data = JSON.parse(storedData);
            const registrationId = data.registrationId;

            console.log('Подтверждение регистрации:', { registrationId, code });

            const response = await fetch('/api/auth/register/verify', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    registrationId: registrationId,
                    code: code
                })
            });

            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            const result = await response.json();
            console.log('Ответ подтверждения:', result);

            if (result.success) {
                showSuccessMessage(result.message || 'Регистрация успешно завершена!');
                sessionStorage.removeItem('pendingRegistration');

                setTimeout(() => {
                    if (result.redirectUrl) {
                        window.location.href = result.redirectUrl;
                    } else {
                        window.location.href = '/';
                    }
                }, 1000);

            } else {
                showErrorMessage(result.message || 'Неверный код');
            }

        } else if (type === 'reset') {
            // Логика для сброса пароля
            const storedData = sessionStorage.getItem('pendingPasswordReset');
            if (!storedData) {
                showErrorMessage('Сессия истекла. Пожалуйста, начните заново.');
                setTimeout(() => {
                    window.location.href = '/forgotPassword';
                }, 2000);
                return;
            }

            const data = JSON.parse(storedData);
            const resetId = data.resetId;

            console.log('Подтверждение кода сброса пароля:', { resetId, code });

            const response = await fetch(`/api/auth/password/verify?resetId=${encodeURIComponent(resetId)}&code=${encodeURIComponent(code)}`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                }
            });

            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            const result = await response.json();
            console.log('Ответ сервера:', result);

            if (result.success) {
                showSuccessMessage(result.message || 'Код подтвержден!');

                // Обновляем resetId в sessionStorage на случай, если он изменился
                sessionStorage.setItem('pendingPasswordReset', JSON.stringify({
                    ...data,
                    resetId: result.resetId || resetId
                }));

                setTimeout(() => {
                    window.location.href = '/recoveryPassword';
                }, 1000);

            } else {
                showErrorMessage(result.message || 'Неверный код');
            }
        } else {
            showErrorMessage('Неизвестный тип операции');
        }

    } catch (error) {
        console.error('Ошибка при подтверждении кода:', error);
        showErrorMessage('Произошла ошибка при проверке кода');
    } finally {
        showLoading(false);
    }
}

// 5. Обработка сброса пароля
async function handlePasswordReset() {
    try {
        console.log('Обработка сброса пароля...');

        const passwordInput = document.getElementById('recoveryPassword');
        const confirmPasswordInput = document.getElementById('recoveryConfirmPassword');

        if (!passwordInput || !confirmPasswordInput) {
            showErrorMessage('Элементы формы не найдены');
            return;
        }

        const password = passwordInput.value;
        const confirmPassword = confirmPasswordInput.value;

        // Валидация
        if (password.length < 6) {
            showErrorMessage('Пароль должен содержать не менее 6 символов');
            passwordInput.focus();
            return;
        }

        if (password !== confirmPassword) {
            showErrorMessage('Пароли не совпадают');
            confirmPasswordInput.focus();
            return;
        }

        // Получаем данные из sessionStorage
        const storedData = sessionStorage.getItem('pendingPasswordReset');
        if (!storedData) {
            showErrorMessage('Сессия истекла. Пожалуйста, начните заново.');
            setTimeout(() => {
                window.location.href = '/forgotPassword';
            }, 2000);
            return;
        }

        const data = JSON.parse(storedData);
        const resetId = data.resetId;

        console.log('Сброс пароля:', { resetId });

        showLoading(true);

        const requestData = {
            resetId: resetId,
            newPassword: password,
            confirmPassword: confirmPassword
        };

        const response = await fetch('/api/auth/password/reset', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(requestData)
        });

        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }

        const result = await response.json();
        console.log('Ответ сервера:', result);

        if (result.success) {
            showSuccessMessage(result.message || 'Пароль успешно изменен!');

            // Очищаем sessionStorage
            sessionStorage.removeItem('pendingPasswordReset');

            setTimeout(() => {
                if (result.redirectUrl) {
                    window.location.href = result.redirectUrl;
                } else {
                    window.location.href = '/login';
                }
            }, 1000);

        } else {
            showErrorMessage(result.message || 'Не удалось изменить пароль');
        }

    } catch (error) {
        console.error('Ошибка при сбросе пароля:', error);
        showErrorMessage('Произошла ошибка при изменении пароля');
    } finally {
        showLoading(false);
    }
}

// 6. Повторная отправка кода
async function handleResendCode() {
    try {
        console.log('Повторная отправка кода...');

        const urlParams = new URLSearchParams(window.location.search);
        const type = urlParams.get('type');

        let endpoint, storageKey, paramName;

        if (type === 'registration') {
            endpoint = '/api/auth/register/resend-code';
            storageKey = 'pendingRegistration';
            paramName = 'registrationId';
        } else if (type === 'reset') {
            // Для сброса пароля повторно вызываем forgotPassword
            endpoint = '/api/auth/password/forgot';
            storageKey = 'pendingPasswordReset';
            paramName = 'email';
        } else {
            showErrorMessage('Неизвестный тип операции');
            return;
        }

        // Получаем данные из sessionStorage
        const storedData = sessionStorage.getItem(storageKey);
        if (!storedData) {
            showErrorMessage('Сессия истекла. Пожалуйста, начните заново.');
            setTimeout(() => {
                window.location.href = type === 'registration' ? '/register' : '/forgotPassword';
            }, 2000);
            return;
        }

        const data = JSON.parse(storedData);
        let requestUrl;

        showLoading(true);

        if (type === 'registration') {
            const registrationId = data.registrationId;
            console.log(`Повторная отправка кода для регистрации:`, registrationId);
            requestUrl = `${endpoint}?${paramName}=${registrationId}`;

            const response = await fetch(requestUrl, {
                method: 'POST'
            });

            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            const result = await response.json();
            console.log('Ответ сервера:', result);

            if (result.success) {
                showSuccessMessage(result.message || 'Новый код отправлен на email');
                // Запускаем таймер через замыкание
                startResendTimer(60);
            } else {
                showErrorMessage(result.message || 'Не удалось отправить код');
            }

        } else {
            // Для сброса пароля
            const email = data.email;
            console.log(`Повторная отправка кода для сброса пароля:`, email);
            requestUrl = `${endpoint}?email=${encodeURIComponent(email)}`;

            const response = await fetch(requestUrl, {
                method: 'POST'
            });

            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            const result = await response.json();
            console.log('Ответ сервера:', result);

            if (result.success) {
                showSuccessMessage(result.message || 'Новый код отправлен на email');

                // Обновляем resetId в sessionStorage
                sessionStorage.setItem(storageKey, JSON.stringify({
                    email: email,
                    resetId: result.resetId
                }));

                // Запускаем таймер через замыкание
                startResendTimer(60);
            } else {
                showErrorMessage(result.message || 'Не удалось отправить код');
            }
        }

    } catch (error) {
        console.error('Ошибка при повторной отправке кода:', error);
        showErrorMessage('Произошла ошибка при отправке кода');
    } finally {
        showLoading(false);
    }
}

// 7. Обработка выхода
async function handleLogout() {
    try {
        console.log('Выход из системы...');
        showLoading(true);

        const response = await fetch('/api/auth/logout', {
            method: 'POST'
        });

        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }

        const result = await response.json();
        console.log('Ответ сервера:', result);

        if (result.success) {
            showSuccessMessage(result.message || 'Выход выполнен успешно');

            setTimeout(() => {
                window.location.href = '/login';
            }, 1000);

        } else {
            showErrorMessage(result.message || 'Ошибка при выходе');
        }

    } catch (error) {
        console.error('Ошибка при выходе:', error);
        showErrorMessage('Произошла ошибка при выходе');
    } finally {
        showLoading(false);
    }
}

// ========== ВСПОМОГАТЕЛЬНЫЕ ФУНКЦИИ ==========

// Валидация формы регистрации
function validateRegistrationForm() {
    const nameInput = document.querySelector('input[name="name"]');
    const emailInput = document.getElementById('email');
    const passwordInput = document.getElementById('password');
    const confirmPasswordInput = document.getElementById('confirmPassword');
    const personalDataCheckbox = document.getElementById('personalData');

    if (!nameInput || !nameInput.value.trim()) {
        showErrorMessage('Введите имя');
        if (nameInput) nameInput.focus();
        return false;
    }

    if (nameInput.value.trim().length < 2) {
        showErrorMessage('Имя должно содержать минимум 2 символа');
        nameInput.focus();
        return false;
    }

    if (!emailInput || !validateEmail(emailInput.value)) {
        showErrorMessage('Введите корректный email');
        if (emailInput) emailInput.focus();
        return false;
    }

    if (!passwordInput || passwordInput.value.length < 6) {
        showErrorMessage('Пароль должен содержать не менее 6 символов');
        if (passwordInput) passwordInput.focus();
        return false;
    }

    if (!confirmPasswordInput || passwordInput.value !== confirmPasswordInput.value) {
        showErrorMessage('Пароли не совпадают');
        if (confirmPasswordInput) confirmPasswordInput.focus();
        return false;
    }

    if (!personalDataCheckbox || !personalDataCheckbox.checked) {
        showErrorMessage('Необходимо согласие с правилами и политикой конфиденциальности');
        return false;
    }

    return true;
}

// Валидация email
function validateEmail(email) {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return emailRegex.test(email);
}

// Инициализация полей ввода кода
function initializeCodeInputs() {
    const codeInputs = document.querySelectorAll('.code-input');

    codeInputs.forEach((input, index) => {
        // Ограничение ввода только цифр
        input.addEventListener('input', function(e) {
            this.value = this.value.replace(/\D/g, '');

            // Автопереход к следующему полю
            if (this.value.length === 1 && index < codeInputs.length - 1) {
                codeInputs[index + 1].focus();
            }
        });

        // Обработка клавиш Backspace и Delete
        input.addEventListener('keydown', function(e) {
            if ((e.key === 'Backspace' || e.key === 'Delete') && this.value.length === 0 && index > 0) {
                codeInputs[index - 1].focus();
            }
        });

        // Обработка вставки из буфера обмена
        input.addEventListener('paste', function(e) {
            e.preventDefault();
            const pastedText = (e.clipboardData || window.clipboardData).getData('text');

            if (pastedText.length === 6 && /^\d+$/.test(pastedText)) {
                for (let i = 0; i < 6; i++) {
                    if (codeInputs[i]) {
                        codeInputs[i].value = pastedText[i];
                    }
                }
                if (codeInputs[5]) {
                    codeInputs[5].focus();
                }
            }
        });
    });
}

// Запуск таймера для повторной отправки
function startResendTimer(timeoutSeconds = 60) {
    const resendBtn = document.getElementById('resendCodeBtn');
    const timerElement = document.getElementById('timer');

    if (!resendBtn || !timerElement) return;

    // Очищаем предыдущий таймер
    if (window.resendTimer) {
        clearInterval(window.resendTimer);
    }

    let timeLeft = timeoutSeconds;
    resendBtn.disabled = true;
    resendBtn.classList.add('disabled');

    window.resendTimer = setInterval(() => {
        timeLeft--;

        const minutes = Math.floor(timeLeft / 60);
        const seconds = timeLeft % 60;
        timerElement.textContent = `${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}`;

        if (timeLeft <= 0) {
            clearInterval(window.resendTimer);
            resendBtn.disabled = false;
            resendBtn.classList.remove('disabled');
            timerElement.textContent = 'Отправить код';
        }
    }, 1000);
}

// Показать сообщение об успехе
function showSuccessMessage(message) {
    showMessage(message, 'success');
}

// Показать сообщение об ошибке
function showErrorMessage(message) {
    showMessage(message, 'error');
}

// Показать сообщение
function showMessage(message, type) {
    // Удаляем старые сообщения
    const oldMessages = document.querySelectorAll('.alert-message');
    oldMessages.forEach(msg => msg.remove());

    // Создаем новое сообщение
    const alertDiv = document.createElement('div');
    alertDiv.className = `alert-message alert-${type}`;
    alertDiv.textContent = message;
    alertDiv.style.cssText = `
        position: fixed;
        top: 20px;
        right: 20px;
        padding: 15px 20px;
        border-radius: 5px;
        color: white;
        font-weight: 500;
        z-index: 9999;
        animation: slideIn 0.3s ease-out;
        ${type === 'success' ? 'background-color: #4CAF50;' : 'background-color: #f44336;'}
        box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
    `;

    document.body.appendChild(alertDiv);

    // Автоматически скрываем через 5 секунд
    setTimeout(() => {
        if (alertDiv.parentNode) {
            alertDiv.style.animation = 'slideOut 0.3s ease-out';
            setTimeout(() => {
                if (alertDiv.parentNode) {
                    alertDiv.parentNode.removeChild(alertDiv);
                }
            }, 300);
        }
    }, 5000);

    // Добавляем CSS анимации
    if (!document.getElementById('alert-animations')) {
        const style = document.createElement('style');
        style.id = 'alert-animations';
        style.textContent = `
            @keyframes slideIn {
                from {
                    transform: translateX(100%);
                    opacity: 0;
                }
                to {
                    transform: translateX(0);
                    opacity: 1;
                }
            }
            @keyframes slideOut {
                from {
                    transform: translateX(0);
                    opacity: 1;
                }
                to {
                    transform: translateX(100%);
                    opacity: 0;
                }
            }
        `;
        document.head.appendChild(style);
    }
}

// Показать/скрыть индикатор загрузки
function showLoading(show) {
    let loader = document.getElementById('global-loader');

    if (show) {
        if (!loader) {
            loader = document.createElement('div');
            loader.id = 'global-loader';
            loader.style.cssText = `
                position: fixed;
                top: 0;
                left: 0;
                width: 100%;
                height: 100%;
                background-color: rgba(255, 255, 255, 0.8);
                display: flex;
                justify-content: center;
                align-items: center;
                z-index: 9998;
                backdrop-filter: blur(2px);
            `;

            const spinner = document.createElement('div');
            spinner.style.cssText = `
                width: 50px;
                height: 50px;
                border: 5px solid #f3f3f3;
                border-top: 5px solid #3498db;
                border-radius: 50%;
                animation: spin 1s linear infinite;
            `;

            loader.appendChild(spinner);

            // Добавляем CSS анимацию
            if (!document.getElementById('loader-animation')) {
                const style = document.createElement('style');
                style.id = 'loader-animation';
                style.textContent = `
                    @keyframes spin {
                        0% { transform: rotate(0deg); }
                        100% { transform: rotate(360deg); }
                    }
                `;
                document.head.appendChild(style);
            }

            document.body.appendChild(loader);
        }
    } else {
        if (loader && loader.parentNode) {
            loader.parentNode.removeChild(loader);
        }
    }
}

// Проверка авторизации при загрузке страницы
function checkAuth() {
    const cookies = document.cookie.split(';');
    const jwtCookie = cookies.find(cookie => cookie.trim().startsWith('jwtToken='));

    if (jwtCookie) {
        console.log('Пользователь авторизован');
        // Можно обновить интерфейс для авторизованного пользователя
        const authElements = document.querySelectorAll('.auth-only');
        const guestElements = document.querySelectorAll('.guest-only');

        authElements.forEach(el => {
            if (el) el.style.display = 'block';
        });
        guestElements.forEach(el => {
            if (el) el.style.display = 'none';
        });
    }
}

// Очистка при разгрузке страницы
window.addEventListener('beforeunload', function() {
    if (window.resendTimer) {
        clearInterval(window.resendTimer);
    }
});