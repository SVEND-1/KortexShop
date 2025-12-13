// mainFormScript.js - добавьте эти функции

document.addEventListener('DOMContentLoaded', function() {
    // Проверка статуса пользователя
    checkUserStatus();

    // Инициализация других функций...
});

async function checkUserStatus() {
    try {
        const response = await fetch('/api/auth/status', {
            method: 'GET',
            credentials: 'include'
        });

        if (response.ok) {
            const data = await response.json();
            updateNavigation(data);
        }
    } catch (error) {
        console.error('Ошибка проверки статуса:', error);
    }
}

function updateNavigation(userData) {
    const loginLink = document.querySelector('a[href="/login"]');
    const profileLink = document.querySelector('a[href="/profile"]');

    if (userData.authenticated && loginLink) {
        loginLink.style.display = 'none';

        // Показываем ссылку на выход
        const logoutLink = document.createElement('a');
        logoutLink.href = '/';
        logoutLink.className = 'nav-link';
        logoutLink.innerHTML = '<img src="images/logout-img.png" alt="Выйти" class="nav-icon"> Выйти';
        logoutLink.addEventListener('click', async (e) => {
            e.preventDefault();
            await logout();
        });

        loginLink.parentNode.insertBefore(logoutLink, loginLink.nextSibling);
    }

    if (profileLink && userData.username) {
        const profileText = profileLink.querySelector('.profile-text');
        if (profileText) {
            profileText.textContent = userData.username;
        }
    }
}

async function logout() {
    try {
        const response = await fetch('/logout', {
            method: 'POST',
            credentials: 'include'
        });

        if (response.ok) {
            window.location.href = '/';
        }
    } catch (error) {
        console.error('Ошибка при выходе:', error);
        window.location.href = '/';
    }
}