// codeFromEmail.js - управление полями ввода кода
document.addEventListener('DOMContentLoaded', function() {
    console.log('Страница ввода кода загружена');
    
    const codeInputs = document.querySelectorAll('.code-input');
    
    // Фокус на первом поле
    if (codeInputs.length > 0) {
        codeInputs[0].focus();
    }
    
    // Обработка ввода в полях кода
    codeInputs.forEach((input, index) => {
        input.addEventListener('input', function(e) {
            // Разрешаем только цифры
            this.value = this.value.replace(/\D/g, '');
            
            // Если ввели цифру, переходим к следующему полю
            if (this.value.length === 1 && index < codeInputs.length - 1) {
                codeInputs[index + 1].focus();
            }
        });
        
        // Обработка клавиш Backspace и Delete
        input.addEventListener('keydown', function(e) {
            if ((e.key === 'Backspace' || e.key === 'Delete') && this.value === '' && index > 0) {
                codeInputs[index - 1].focus();
            }
        });
        
        // Обработка вставки (paste)
        input.addEventListener('paste', function(e) {
            e.preventDefault();
            const pasteData = e.clipboardData.getData('text');
            const digits = pasteData.replace(/\D/g, '').split('');
            
            // Заполняем поля вставленными цифрами
            digits.forEach((digit, digitIndex) => {
                if (index + digitIndex < codeInputs.length) {
                    codeInputs[index + digitIndex].value = digit;
                }
            });
            
            // Фокус на последнее заполненное поле
            const lastIndex = Math.min(index + digits.length, codeInputs.length - 1);
            codeInputs[lastIndex].focus();
        });
    });
    
    // Запуск таймера
    startTimer();
});

// Таймер для повторной отправки кода
function startTimer() {
    const timerElement = document.getElementById('timer');
    if (!timerElement) return;
    
    let timeLeft = 60;
    
    const timerInterval = setInterval(() => {
        timeLeft--;
        
        if (timeLeft <= 0) {
            clearInterval(timerInterval);
            timerElement.textContent = '00:00';
            document.getElementById('resendCodeBtn').disabled = false;
            document.getElementById('resendCodeBtn').style.opacity = '1';
        } else {
            const minutes = Math.floor(timeLeft / 60);
            const seconds = timeLeft % 60;
            timerElement.textContent = `${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}`;
        }
    }, 1000);
}

function resetTimer() {
    const timerElement = document.getElementById('timer');
    if (timerElement) {
        timerElement.textContent = '00:60';
        document.getElementById('resendCodeBtn').disabled = true;
        document.getElementById('resendCodeBtn').style.opacity = '0.5';
        startTimer();
    }
}