// Базовый путь REST API (под контекстом приложения)
const API_BASE = "api";

function buildQueryString(params) {
    const parts = [];
    for (const key in params) {
        const value = params[key];
        if (value !== "" && value !== null && value !== undefined) {
            parts.push(encodeURIComponent(key) + "=" + encodeURIComponent(value));
        }
    }
    return parts.length ? "?" + parts.join("&") : "";
}

async function apiRequest(url, options) {
    options = options || {};

    const fetchOptions = {
        method: options.method || "GET",
        headers: { "Content-Type": "application/json" }
    };

    if (options.auth !== false) {
        const token = getAccessToken();
        if (token) fetchOptions.headers["Authorization"] = "Bearer " + token;
    }

    if (options.body !== undefined) fetchOptions.body = options.body;

    const response = await fetch(url, fetchOptions);

    if (response.status === 401) {
        const path = window.location.pathname;
        if (!path.endsWith("/login.html") && !path.endsWith("/register.html")) {
            clearAuth();
            window.location.href = "login.html";
        }
        const err = new Error("Не авторизован");
        err.status = 401;
        throw err;
    }

    if (response.status === 204) return null;

    let data = null;
    try { data = await response.json(); } catch (e) { data = null; }

    if (!response.ok) {
        let message = "Ошибка сервера (" + response.status + ")";
        let details = {};
        if (data && data.error) {
            if (data.error.message) message = data.error.message;
            if (data.error.details) details = data.error.details;
        }
        const err = new Error(message);
        err.status = response.status;
        err.details = details;
        throw err;
    }

    return data;
}

// ---- AUTH ----
function loginRequest(username, password) {
    return apiRequest(API_BASE + "/auth/login", {
        method: "POST",
        auth: false,
        body: JSON.stringify({ username, password })
    });
}

function registerRequest(username, email, password) {
    return apiRequest(API_BASE + "/auth/register", {
        method: "POST",
        auth: false,
        body: JSON.stringify({ username, email, password })
    });
}

// ---- STUDENTS ----
function getStudentsFromApi(filters) {
    const clean = {};
    for (const key in filters) {
        const v = filters[key];
        if (v !== "" && v !== null && v !== undefined) clean[key] = v;
    }
    // Бэкенд по умолчанию size=10 — просим 100, чтобы увидеть всех
    if (!clean.size) clean.size = 100;

    return apiRequest(API_BASE + "/students" + buildQueryString(clean));
}

function getStudentFromApi(id) {
    return apiRequest(API_BASE + "/students/" + id);
}

function createStudentOnApi(studentData) {
    return apiRequest(API_BASE + "/students", {
        method: "POST",
        body: JSON.stringify(studentData)
    });
}

function updateStudentOnApi(id, studentData) {
    return apiRequest(API_BASE + "/students/" + id, {
        method: "PUT",
        body: JSON.stringify(studentData)
    });
}

function deleteStudentOnApi(id) {
    return apiRequest(API_BASE + "/students/" + id, { method: "DELETE" });
}