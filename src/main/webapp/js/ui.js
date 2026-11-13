// Отображение на странице

async function showStudentsTable() {
    const filters = readFilters();
    const tableBody = document.getElementById("student-table-body");
    const emptyState = document.getElementById("empty-state");

    if (tableBody === null) return;

    let students;
    try {
        students = await getAllStudents(filters);
    } catch (err) {
        if (err.status === 401) return;
        alert(err.message || "Не удалось загрузить список");
        return;
    }

    tableBody.innerHTML = "";

    if (!students || students.length === 0) {
        emptyState.style.display = "block";
        showFilterSummary(0);
        return;
    }

    emptyState.style.display = "none";
    showFilterSummary(students.length);

    for (let i = 0; i < students.length; i++) {
        const student = students[i];
        const row = document.createElement("tr");

        // Кнопки изменения/удаления — только для ADMIN
        let actions =
            "<a href='student.html?id=" + student.id +
            "' class='btn btn--small'>Посмотреть</a>";

        if (isAdmin()) {
            actions +=
                " <a href='form.html?id=" + student.id +
                "' class='btn btn--small'>Изменить</a>" +
                " <button class='btn btn--danger btn--small' " +
                "onclick='removeStudent(" + student.id + ")'>Удалить</button>";
        }

        row.innerHTML =
            "<td>" + student.id + "</td>" +
            "<td>" + student.fullName + "</td>" +
            "<td>" + student.group + "</td>" +
            "<td>" + student.isuId + "</td>" +
            "<td>" + student.dormNumber + "</td>" +
            "<td>" + student.roomNumber + "</td>" +
            "<td>" + (student.isForeigner ? "Да" : "Нет") + "</td>" +
            "<td><div class='actions'>" + actions + "</div></td>";

        tableBody.appendChild(row);
    }
}

async function removeStudent(id) {
    if (!confirm("Удалить этого студента?")) return;

    try {
        await deleteStudent(id);
        await showStudentsTable();
    } catch (err) {
        if (err.status === 401) return;
        alert(err.message || "Не удалось удалить");
    }
}

async function showStudentProfile() {
    const profile = document.getElementById("profile-content");
    if (profile === null) return;

    const params = new URLSearchParams(window.location.search);
    const id = params.get("id");

    if (!id) {
        profile.innerHTML = "<p>Студент не найден</p>";
        return;
    }

    let student;
    try {
        student = await getStudentById(id);
    } catch (err) {
        if (err.status === 401) return;
        profile.innerHTML = "<p>Студент не найден</p>";
        return;
    }

    if (student === null) {
        profile.innerHTML = "<p>Студент не найден</p>";
        return;
    }

    const foreigner = student.isForeigner ? "Да" : "Нет";

    profile.innerHTML =
        "<p><span class='label'>ФИО:</span><span class='value'>" +
        student.fullName + "</span></p>" +
        "<p><span class='label'>Группа:</span><span class='value'>" +
        student.group + "</span></p>" +
        "<p><span class='label'>ИСУ ID:</span><span class='value'>" +
        student.isuId + "</span></p>" +
        "<p><span class='label'>Общежитие:</span><span class='value'>" +
        student.dormNumber + "</span></p>" +
        "<p><span class='label'>Комната:</span><span class='value'>" +
        student.roomNumber + "</span></p>" +
        "<p><span class='label'>Дата заселения:</span><span class='value'>" +
        student.settlementDate + "</span></p>" +
        "<p><span class='label'>Иностранец:</span><span class='value'>" +
        foreigner + "</span></p>" +
        "<p><span class='label'>Заметки:</span><span class='value'>" +
        (student.notes || "") + "</span></p>";
}

function readFilters() {
    function val(id) {
        const el = document.getElementById(id);
        return el ? el.value.trim() : "";
    }

    return {
        group:          val("filter-group").toUpperCase(),
        dormNumber:     val("filter-dorm"),          // было dormitory
        fullName:       val("filter-name"),
        isuId:          val("filter-isu"),
        roomNumber:     val("filter-room"),
        settlementDate: val("filter-date"),
        isForeigner:    val("filter-foreigner")
    };
}

function showFilterSummary(count) {
    const el = document.getElementById("filters-summary");
    if (el === null) return;

    if (count === 0) {
        el.textContent = "Ничего не найдено";
    } else if (count === 1) {
        el.textContent = "Найден 1 студент";
    } else {
        el.textContent = "Найдено студентов: " + count;
    }
}

function clearFilters() {
    const ids = [
        "filter-group", "filter-dorm", "filter-name",
        "filter-isu", "filter-room", "filter-date", "filter-foreigner"
    ];

    for (let i = 0; i < ids.length; i++) {
        const el = document.getElementById(ids[i]);
        if (el !== null) el.value = "";
    }

    const summary = document.getElementById("filters-summary");
    if (summary !== null) summary.textContent = "";
}

function toggleAdvancedFilters() {
    const block = document.getElementById("filters-advanced");
    const btn = document.getElementById("btn-filter-toggle");
    if (block === null || btn === null) return;

    if (block.hasAttribute("hidden")) {
        block.removeAttribute("hidden");
        btn.textContent = "⚙️ Скрыть фильтр";
    } else {
        block.setAttribute("hidden", "");
        btn.textContent = "⚙️ Расширенный фильтр";
    }
}