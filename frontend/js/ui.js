// Отображение на странице

// Показать всех студентов в таблице на index.html
async function showStudentsTable() {
    const tableBody = document.getElementById("student-table-body");
    const emptyState = document.getElementById("empty-state");

    if (tableBody === null) {
        return;
    }

    const filters = readFilters();
    let students;

    try {
        students = await getAllStudents(filters);
    } catch (err) {
        tableBody.innerHTML = "";

        if (emptyState !== null) {
            emptyState.style.display = "block";
            emptyState.textContent =
                err.message || "Не удалось загрузить студентов";
        }

        showFilterSummary(0);
        return;
    }

    tableBody.innerHTML = "";

    if (students.length === 0) {
        if (emptyState !== null) {
            emptyState.style.display = "block";
        }

        showFilterSummary(0);
        return;
    }

    if (emptyState !== null) {
        emptyState.style.display = "none";
    }

    showFilterSummary(students.length);

    // Определяем роль текущего пользователя
    const isAdmin = getUserRole() === "ADMIN";

    for (let i = 0; i < students.length; i++) {
        const student = students[i];

        const row = document.createElement("tr");

        row.innerHTML =
            "<td>" + student.id + "</td>" +
            "<td>" + escapeHtml(student.fullName) + "</td>" +
            "<td>" + escapeHtml(student.group) + "</td>" +
            "<td>" + student.isuId + "</td>" +
            "<td>" + student.dormNumber + "</td>" +
            "<td>" + student.roomNumber + "</td>" +
            "<td>" + (student.isForeigner ? "Да" : "Нет") + "</td>" +
            "<td>" +
            "<a href='student.html?id=" + student.id +
            "' class='btn btn--small'>Посмотреть</a> " +
            (isAdmin
                ? "<a href='form.html?id=" + student.id +
                "' class='btn btn--small'>Изменить</a> " +
                "<button class='btn btn--danger btn--small' " +
                "onclick='removeStudent(" + student.id +
                ")'>Удалить</button>"
                : "") +
            "</td>";

        tableBody.appendChild(row);
    }
}


// Экранирование текста перед вставкой в HTML
function escapeHtml(value) {
    const el = document.createElement("span");
    el.textContent = value == null ? "" : String(value);
    return el.innerHTML;
}


// Удаление студента
async function removeStudent(id) {

    // Только администратор может удалять студентов
    if (getUserRole() !== "ADMIN") {
        alert("Недостаточно прав для удаления студента");
        return;
    }

    const answer = confirm("Удалить этого студента?");

    if (answer === false) {
        return;
    }

    try {
        await deleteStudent(id);
        await showStudentsTable();
    } catch (err) {
        alert(err.message || "Не удалось удалить");
    }
}


// Показать досье студента
async function showStudentProfile() {
    const profile = document.getElementById("profile-content");

    if (profile === null) {
        return;
    }

    const params = new URLSearchParams(window.location.search);
    const id = params.get("id");

    let student;

    try {
        student = await getStudentById(id);
    } catch (err) {
        profile.textContent =
            err.message || "Не удалось загрузить студента";
        return;
    }

    if (student === null) {
        profile.textContent = "Студент не найден";
        return;
    }

    const foreigner = student.isForeigner ? "Да" : "Нет";

    profile.innerHTML =
        "<p><strong>ФИО:</strong> " +
        escapeHtml(student.fullName) + "</p>" +

        "<p><strong>Группа:</strong> " +
        escapeHtml(student.group) + "</p>" +

        "<p><strong>ИСУ ID:</strong> " +
        student.isuId + "</p>" +

        "<p><strong>Общежитие:</strong> " +
        student.dormNumber + "</p>" +

        "<p><strong>Комната:</strong> " +
        student.roomNumber + "</p>" +

        "<p><strong>Дата заселения:</strong> " +
        escapeHtml(student.settlementDate) + "</p>" +

        "<p><strong>Иностранец:</strong> " +
        foreigner + "</p>" +

        "<p><strong>Заметки:</strong> " +
        escapeHtml(student.notes) + "</p>";
}


// Собрать значения фильтров с панели
function readFilters() {
    function val(id) {
        const el = document.getElementById(id);
        return el ? el.value.trim() : "";
    }

    return {
        group: val("filter-group").toUpperCase(),
        dormNumber: val("filter-dorm"),
        fullName: val("filter-name"),
        isuId: val("filter-isu"),
        roomNumber: val("filter-room"),
        settlementDate: val("filter-date"),
        isForeigner: val("filter-foreigner")
    };
}


// Показать количество найденных студентов
function showFilterSummary(count) {
    const el = document.getElementById("filters-summary");

    if (el === null) {
        return;
    }

    if (count === 0) {
        el.textContent = "Ничего не найдено";
    } else if (count === 1) {
        el.textContent = "Найден 1 студент";
    } else {
        el.textContent = "Найдено студентов: " + count;
    }
}


// Сбросить все поля фильтров
function clearFilters() {
    const ids = [
        "filter-group",
        "filter-dorm",
        "filter-name",
        "filter-isu",
        "filter-room",
        "filter-date",
        "filter-foreigner"
    ];

    for (let i = 0; i < ids.length; i++) {
        const el = document.getElementById(ids[i]);

        if (el !== null) {
            el.value = "";
        }
    }

    const summary = document.getElementById("filters-summary");

    if (summary !== null) {
        summary.textContent = "";
    }
}


// Раскрыть или скрыть расширенный блок фильтров
function toggleAdvancedFilters() {
    const block = document.getElementById("filters-advanced");
    const btn = document.getElementById("btn-filter-toggle");

    if (block === null || btn === null) {
        return;
    }

    if (block.hasAttribute("hidden")) {
        block.removeAttribute("hidden");
        btn.textContent = "⚙️ Скрыть фильтр";
    } else {
        block.setAttribute("hidden", "");
        btn.textContent = "⚙️ Расширенный фильтр";
    }
}
