// Запуск страниц
document.addEventListener("DOMContentLoaded", function () {

    // Проверяем, авторизован ли пользователь
    if (!sessionStorage.getItem("accessToken")) {
        window.location.replace("login.html");
        return;
    }

    // Показываем кнопки добавления только администратору
    document.querySelectorAll("[data-admin-only]").forEach(function (element) {
        element.style.display = getUserRole() === "ADMIN" ? "" : "none";
    });

    const table = document.getElementById("student-table-body");
    const form = document.getElementById("student-form");
    const profile = document.getElementById("profile-content");

    if (table !== null) {
        showStudentsTable();
        startFilters();
    }

    if (profile !== null) {
        showStudentProfile();
    }

    if (form !== null) {
        startForm();
    }
});


// Запуск обработчиков фильтров
function startFilters() {
    const applyBtn = document.getElementById("btn-filter-apply");
    const resetBtn = document.getElementById("btn-filter-reset");
    const toggleBtn = document.getElementById("btn-filter-toggle");

    if (applyBtn !== null) {
        applyBtn.addEventListener("click", function () {
            showStudentsTable();
        });
    }

    if (resetBtn !== null) {
        resetBtn.addEventListener("click", function () {
            clearFilters();
            showStudentsTable();
        });
    }

    if (toggleBtn !== null) {
        toggleBtn.addEventListener("click", toggleAdvancedFilters);
    }
}


// Запуск формы создания или редактирования
function startForm() {

    // Только ADMIN может изменять студентов
    if (getUserRole() !== "ADMIN") {
        alert("Недостаточно прав для изменения студентов");
        window.location.href = "index.html";
        return;
    }

    const form = document.getElementById("student-form");

    const params = new URLSearchParams(window.location.search);
    const id = params.get("id");

    // Если есть ID, загружаем данные студента
    if (id !== null) {
        fillForm(id);
    }

    // Обрабатываем отправку формы
    form.addEventListener("submit", async function (e) {
        e.preventDefault();

        if (validateForm() === false) {
            return;
        }

        await saveForm(id);
    });
}


// Создание или обновление студента
async function saveForm(id) {

    // Дополнительная проверка роли
    if (getUserRole() !== "ADMIN") {
        alert("Недостаточно прав для изменения студентов");
        return;
    }

    // Получаем данные из validation.js
    const student = readForm();

    try {
        if (id === null) {
            // Создание нового студента
            await createStudent(student);
        } else {
            // Обновление существующего студента
            await updateStudent(id, student);
        }

        // Возвращаемся к списку студентов
        window.location.href = "index.html";

    } catch (err) {
        // Показываем ошибки Java backend
        showServerErrors(err);
    }
}


// Заполнение формы данными существующего студента
async function fillForm(id) {
    let student;

    try {
        student = await getStudentById(id);
    } catch (err) {
        alert("Не удалось загрузить данные студента");
        window.location.href = "index.html";
        return;
    }

    if (student === null) {
        alert("Студент не найден");
        window.location.href = "index.html";
        return;
    }

    document.getElementById("form-student-id").value = student.id;
    document.getElementById("form-fullname").value = student.fullName;
    document.getElementById("form-group").value = student.group;
    document.getElementById("form-isuid").value = student.isuId;
    document.getElementById("form-dorm").value = student.dormNumber;
    document.getElementById("form-room").value = student.roomNumber;
    document.getElementById("form-movein").value = student.settlementDate;
    document.getElementById("form-foreigner").checked = student.isForeigner;
    document.getElementById("form-notes").value = student.notes ?? "";
}
