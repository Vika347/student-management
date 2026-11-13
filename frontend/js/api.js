// Адрес Java backend (Payara)
const API_BASE = "http://localhost:8081/student-management/api";


// Собирает query-строку из объекта фильтров.
// Пустые значения пропускает.
function buildQueryString(params) {
    const parts = [];

    for (const key in params) {
        const value = params[key];

        if (value !== "" && value !== null && value !== undefined) {
            parts.push(
                encodeURIComponent(key) + "=" +
                encodeURIComponent(value)
            );
        }
    }

    if (parts.length === 0) {
        return "";
    }

    return "?" + parts.join("&");
}


// Базовая функция для любого запроса.
// Добавляет JWT-токен и обрабатывает ответ сервера.
async function apiRequest(url, options) {
    options = options || {};

    const fetchOptions = {
        method: options.method || "GET",
        headers: {
            "Content-Type": "application/json"
        }
    };

    // Передаём access token, если пользователь авторизован
    const token = sessionStorage.getItem("accessToken");

    if (token) {
        fetchOptions.headers["Authorization"] = "Bearer " + token;
    }

    if (options.body !== undefined) {
        fetchOptions.body = options.body;
    }

    const response = await fetch(url, fetchOptions);

    // 204 No Content — успешное удаление
    if (response.status === 204) {
        return null;
    }

    // Пытаемся прочитать JSON
    let data = null;

    try {
        data = await response.json();
    } catch (e) {
        data = null;
    }

    // Обработка ошибок сервера
    if (!response.ok) {
        let message = "Ошибка сервера (" + response.status + ")";

        if (data !== null &&
            data.error !== undefined &&
            data.error.message !== undefined) {
            message = data.error.message;
        }

        const err = new Error(message);
        err.status = response.status;
        err.details = (
            data !== null &&
            data.error !== undefined &&
            data.error.details !== undefined
        ) ? data.error.details : {};

        throw err;
    }

    return data;
}


// Регистрация пользователя
function registerUser(username, email, password) {
    return apiRequest(API_BASE + "/auth/register", {
        method: "POST",
        body: JSON.stringify({
            username: username,
            email: email,
            password: password
        })
    });
}


// Вход пользователя
async function loginUser(username, password) {
    const data = await apiRequest(API_BASE + "/auth/login", {
        method: "POST",
        body: JSON.stringify({
            username: username,
            password: password
        })
    });

    // Сохраняем токены и данные пользователя
    sessionStorage.setItem("accessToken", data.accessToken);
    sessionStorage.setItem("refreshToken", data.refreshToken);
    sessionStorage.setItem("username", data.username);
    sessionStorage.setItem("role", data.role);

    return data;
}


// Выход из системы
function logoutUser() {
    sessionStorage.removeItem("accessToken");
    sessionStorage.removeItem("refreshToken");
    sessionStorage.removeItem("username");
    sessionStorage.removeItem("role");
}


// Текущая роль пользователя
function getUserRole() {
    return sessionStorage.getItem("role");
}


// Список студентов (с фильтрами или без)
async function getStudentsFromApi(filters = {}) {
    const clean = {};

    for (const key in filters) {
        const value = filters[key];

        if (value !== "" &&
            value !== null &&
            value !== undefined) {
            clean[key] = value;
        }
    }

    const count = Object.keys(clean).length;

    // Мало фильтров — обычный GET
    if (count <= 2) {
        return apiRequest(
            API_BASE + "/students" + buildQueryString(clean)
        );
    }

    // Много фильтров — QUERY с JSON-телом
    return apiRequest(API_BASE + "/students", {
        method: "QUERY",
        body: JSON.stringify(clean)
    });
}


// Один студент по ID
function getStudentFromApi(id) {
    return apiRequest(API_BASE + "/students/" + id);
}


// Создание студента
function createStudentOnApi(studentData) {
    return apiRequest(API_BASE + "/students", {
        method: "POST",
        body: JSON.stringify(studentData)
    });
}


// Частичное обновление студента
function updateStudentOnApi(id, studentData) {
    return apiRequest(API_BASE + "/students/" + id, {
        method: "PATCH",
        body: JSON.stringify(studentData)
    });
}


// Удаление студента
function deleteStudentOnApi(id) {
    return apiRequest(API_BASE + "/students/" + id, {
        method: "DELETE"
    });
}
