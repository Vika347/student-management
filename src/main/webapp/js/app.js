document.addEventListener("DOMContentLoaded", function () {
    const loginForm    = document.getElementById("login-form");
    const registerForm = document.getElementById("register-form");

    if (loginForm)    { initLoginPage();    return; }
    if (registerForm) { initRegisterPage(); return; }

    // Остальные страницы — только для авторизованных
    if (!requireAuth()) return;
    initNavbar();

    const table   = document.getElementById("student-table-body");
    const form    = document.getElementById("student-form");
    const profile = document.getElementById("profile-content");

    if (table)   { showStudentsTable(); startFilters(); }
    if (profile) { showStudentProfile(); }
    if (form)    { startForm(); }
});

// ---- login ----
function initLoginPage() {
    if (isAuthenticated()) { window.location.href = "index.html"; return; }

    document.getElementById("login-form")
        .addEventListener("submit", async function (e) {
            e.preventDefault();
            const errBox = document.getElementById("login-error");
            errBox.textContent = "";

            try {
                const data = await loginRequest(
                    document.getElementById("login-username").value.trim(),
                    document.getElementById("login-password").value
                );
                saveAuth(data);
                window.location.href = "index.html";
            } catch (err) {
                errBox.textContent = err.message || "Ошибка входа";
            }
        });
}

// ---- register ----
function initRegisterPage() {
    if (isAuthenticated()) { window.location.href = "index.html"; return; }

    document.getElementById("register-form")
        .addEventListener("submit", async function (e) {
            e.preventDefault();
            const errBox = document.getElementById("register-error");
            errBox.textContent = "";

            try {
                await registerRequest(
                    document.getElementById("reg-username").value.trim(),
                    document.getElementById("reg-email").value.trim(),
                    document.getElementById("reg-password").value
                );
                window.location.href = "login.html";
            } catch (err) {
                errBox.textContent = err.message || "Ошибка регистрации";
            }
        });
}

// ---- navbar ----
function initNavbar() {
    const userEl = document.getElementById("navbar-user");
    if (userEl) {
        userEl.textContent = "👤 " + getUsername() + " (" + getRole() + ")";
    }
    const logoutBtn = document.getElementById("logout-btn");
    if (logoutBtn) {
        logoutBtn.addEventListener("click", function (e) {
            e.preventDefault();
            logout();
        });
    }
    // Прячем "Добавить" для не-админов
    if (!isAdmin()) {
        const addLink = document.getElementById("navbar-add-link");
        if (addLink) addLink.style.display = "none";
        const addBtn = document.getElementById("add-student-btn");
        if (addBtn) addBtn.style.display = "none";
    }
}

// ---- фильтры ----
function startFilters() {
    const applyBtn  = document.getElementById("btn-filter-apply");
    const resetBtn  = document.getElementById("btn-filter-reset");
    const toggleBtn = document.getElementById("btn-filter-toggle");

    if (applyBtn)  applyBtn.addEventListener("click", showStudentsTable);
    if (resetBtn)  resetBtn.addEventListener("click", () => { clearFilters(); showStudentsTable(); });
    if (toggleBtn) toggleBtn.addEventListener("click", toggleAdvancedFilters);
}

// ---- форма ----
function startForm() {
    if (!isAdmin()) {
        alert("Недостаточно прав для изменения студентов");
        window.location.href = "index.html";
        return;
    }

    const form = document.getElementById("student-form");
    const id = new URLSearchParams(window.location.search).get("id");
    if (id !== null) fillForm(id);

    form.addEventListener("submit", async function (e) {
        e.preventDefault();
        if (validateForm() === false) return;
        await saveForm(id);
    });
}

async function saveForm(id) {
    const student = {
        fullName:       document.getElementById("form-fullname").value,
        group:          document.getElementById("form-group").value,
        isuId:          Number(document.getElementById("form-isuid").value),
        dormNumber:     Number(document.getElementById("form-dorm").value),
        roomNumber:     Number(document.getElementById("form-room").value),
        settlementDate: document.getElementById("form-movein").value,
        isForeigner:    document.getElementById("form-foreigner").checked,
        notes:          document.getElementById("form-notes").value
    };

    try {
        if (id === null) await createStudent(student);
        else             await updateStudent(id, student);
        window.location.href = "index.html";
    } catch (err) {
        if (err.status === 401) return;  // уже редиректнуло
        showServerErrors(err);
    }
}

async function fillForm(id) {
    let student;
    try { student = await getStudentById(id); }
    catch (err) {
        alert("Студент не найден");
        window.location.href = "index.html";
        return;
    }
    if (student === null) { window.location.href = "index.html"; return; }

    document.getElementById("form-student-id").value = student.id;
    document.getElementById("form-fullname").value    = student.fullName;
    document.getElementById("form-group").value       = student.group;
    document.getElementById("form-isuid").value       = student.isuId;
    document.getElementById("form-dorm").value        = student.dormNumber;
    document.getElementById("form-room").value        = student.roomNumber;
    document.getElementById("form-movein").value      = student.settlementDate;
    document.getElementById("form-foreigner").checked = student.isForeigner;
    document.getElementById("form-notes").value       = student.notes;
}