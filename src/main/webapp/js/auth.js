// Хранение JWT в localStorage
const AUTH_KEYS = {
    access:   "sm_access",
    refresh:  "sm_refresh",
    username: "sm_username",
    role:     "sm_role"
};

function saveAuth(data) {
    localStorage.setItem(AUTH_KEYS.access,   data.accessToken);
    localStorage.setItem(AUTH_KEYS.refresh,  data.refreshToken);
    localStorage.setItem(AUTH_KEYS.username, data.username);
    localStorage.setItem(AUTH_KEYS.role,     data.role);
}

function clearAuth() {
    Object.values(AUTH_KEYS).forEach(k => localStorage.removeItem(k));
}

function getAccessToken()  { return localStorage.getItem(AUTH_KEYS.access); }
function getRefreshToken() { return localStorage.getItem(AUTH_KEYS.refresh); }
function getUsername()     { return localStorage.getItem(AUTH_KEYS.username); }
function getRole()         { return localStorage.getItem(AUTH_KEYS.role); }

function isAuthenticated() { return !!getAccessToken(); }
function isAdmin()         { return getRole() === "ADMIN"; }

// Редирект на login, если не авторизован. Возвращает true, если можно идти дальше.
function requireAuth() {
    if (!isAuthenticated()) {
        window.location.href = "login.html";
        return false;
    }
    return true;
}

function logout() {
    clearAuth();
    window.location.href = "login.html";
}