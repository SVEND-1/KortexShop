document.addEventListener('DOMContentLoaded', function() {
    console.log('Страница авторизации загружена');

    const loginForm = document.querySelector('.loginForm');

    if (loginForm) {
        loginForm.addEventListener('submit', async function(e) {
            e.preventDefault(); // отменяем стандартный сабмит формы

            // Валидация
            if (!validateLoginForm()) {
                return false;
            }

            // Сбор данных формы
            const email = document.querySelector('input[type="email"]').value;
            const password = document.querySelector('input[type="password"]').value;

            try {
                const response = await fetch('http://localhost:8080/api/auth/login', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    credentials: 'include', // для куки
                    body: JSON.stringify({ email, password }) // отправка JSON
                });

                const data = await response.json();

                if (response.ok) {
                    alert(data.message); // Успешный вход
                    if (data.redirectUrl) {
                        window.location.href = data.redirectUrl;
                    }
                } else {
                    alert(data.message || 'Ошибка входа');
                }

            } catch (err) {
                console.error(err);
                alert('Ошибка соединения с сервером');
            }
        });
    }
});

// Валидация
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
