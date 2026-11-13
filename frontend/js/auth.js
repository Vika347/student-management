// Авторизация и регистрация пользователей

document.addEventListener("DOMContentLoaded", function () {

    const loginForm = document.getElementById("login-form");
    const registerForm = document.getElementById("register-form");

    // Вход
    if (loginForm !== null) {
        loginForm.addEventListener("submit", async function (event) {
            event.preventDefault();

            const username = document.getElementById("login-username").value.trim();
            const password = document.getElementById("login-password").value;
            const errorElement = document.getElementById("auth-error");

            errorElement.textContent = "";

            try {
                await loginUser(username, password);

                // После входа открываем список студентов
                window.location.href = "index.html";

            } catch (err) {
                errorElement.textContent =
                    err.message || "Не удалось войти";
            }
        });
    }

    // Регистрация
    if (registerForm !== null) {
        registerForm.addEventListener("submit", async function (event) {
            event.preventDefault();

            const username = document.getElementById("register-username").value.trim();
            const email = document.getElementById("register-email").value.trim();
            const password = document.getElementById("register-password").value;
            const errorElement = document.getElementById("auth-error");

            errorElement.textContent = "";

            try {
                await registerUser(username, email, password);

                // После регистрации предлагаем войти
                window.location.href = "login.html";

            } catch (err) {
                errorElement.textContent =
                    err.message || "Не удалось зарегистрироваться";
            }
        });
    }
});
