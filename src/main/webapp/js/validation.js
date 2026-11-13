// Проверка формы студента и вывод серверных ошибок

function readForm() {
    const form = document.getElementById('student-form');
    if (!form) return null;

    return {
        fullName:       document.getElementById('form-fullname').value.trim(),
        group:          document.getElementById('form-group').value.trim().toUpperCase(),
        isuId:          Number(document.getElementById('form-isuid').value),
        dormNumber:     Number(document.getElementById('form-dorm').value),
        roomNumber:     Number(document.getElementById('form-room').value),
        settlementDate: document.getElementById('form-movein').value,
        isForeigner:    document.getElementById('form-foreigner').checked,
        notes:          document.getElementById('form-notes').value.trim()
    };
}

function clearErrors() {
    const errors = document.querySelectorAll('.form-error');
    for (let i = 0; i < errors.length; i++) errors[i].textContent = '';
}

function showError(fieldKey, message) {
    const el = document.getElementById('error-' + fieldKey);
    if (el) el.textContent = message;
}

// Показывает ошибки, пришедшие с сервера.
function showServerErrors(err) {
    clearErrors();

    const details = err.details || {};

    const fieldMap = {
        fullName:               'fullname',
        group:                  'group',
        isuId:                  'isuid',
        dormNumber:             'dorm',
        roomNumber:             'room',
        settlementDate:         'movein',
        notes:                  'notes',
        isSettlementYearValid:  'movein',
        isSettlementNotTooOld:  'movein',
        isNotesWordCountValid:  'notes'
    };

    let shown = false;
    for (const serverField in fieldMap) {
        if (details[serverField]) {
            let msg = details[serverField];
            if (Array.isArray(msg)) msg = msg.join(', ');
            showError(fieldMap[serverField], String(msg));
            shown = true;
        }
    }

    if (!shown) {
        if (err.status === 409) {
            showError('isuid', err.message || "Номер ИСУ уже занят");
        } else {
            showError('fullname', err.message || "Ошибка сохранения");
        }
    }
}

function validateForm() {
    const fullName = document.getElementById("form-fullname");
    const group    = document.getElementById("form-group");
    const isuId    = document.getElementById("form-isuid");
    const dorm     = document.getElementById("form-dorm");
    const room     = document.getElementById("form-room");
    const moveIn   = document.getElementById("form-movein");
    const notes    = document.getElementById("form-notes");

    clearErrors();
    let isValid = true;

    // ФИО
    if (fullName.checkValidity() === false) {
        document.getElementById("error-fullname").textContent =
            "Введите корректное ФИО";
        isValid = false;
    }

    // Группа
    if (group.checkValidity() === false) {
        document.getElementById("error-group").textContent =
            "Введите группу в формате P3224";
        isValid = false;
    }

    // ИСУ
    if (isuId.checkValidity() === false) {
        document.getElementById("error-isuid").textContent =
            "ИСУ ID должен быть целым числом от 100000 до 999999";
        isValid = false;
    }

    // Общежитие
    if (dorm.checkValidity() === false) {
        document.getElementById("error-dorm").textContent =
            "Номер общежития должен быть целым числом от 1 до 10";
        isValid = false;
    }

    // Комната
    if (room.checkValidity() === false) {
        document.getElementById("error-room").textContent =
            "Номер комнаты должен быть целым числом от 100 до 999";
        isValid = false;
    }

    // Дата заселения
    if (moveIn.checkValidity() === false) {
        document.getElementById("error-movein").textContent =
            "Дата заселения должна быть в диапазоне 2018–2035";
        isValid = false;
    }

    // Не старше 10 лет
    if (moveIn.value !== "") {
        const moveInDate = new Date(moveIn.value);
        const tenYearsAgo = new Date();
        tenYearsAgo.setFullYear(new Date().getFullYear() - 10);

        if (moveInDate < tenYearsAgo) {
            document.getElementById("error-movein").textContent =
                "Студент не может жить в общежитии больше 10 лет";
            isValid = false;
        }

        if (moveInDate > new Date()) {
            document.getElementById("error-movein").textContent =
                "Дата заселения не может быть в будущем";
            isValid = false;
        }
    }

    // Заметки — запрещённые символы
    const forbiddenSymbols = ["/", "\\", "\"", "'", "<", ">"];
    for (let i = 0; i < forbiddenSymbols.length; i++) {
        if (notes.value.includes(forbiddenSymbols[i])) {
            document.getElementById("error-notes").textContent =
                "В заметках нельзя использовать / \\ \" ' < >";
            isValid = false;
            break;
        }
    }

    // Заметки — не больше 500 символов
    if (notes.value.length > 500) {
        document.getElementById("error-notes").textContent =
            "В заметках должно быть не больше 500 символов";
        isValid = false;
    }

    // Заметки — не больше 50 слов
    const notesText = notes.value.trim();
    if (notesText !== "") {
        const words = notesText.split(/\s+/);
        if (words.length > 50) {
            document.getElementById("error-notes").textContent =
                "В заметках должно быть не больше 50 слов";
            isValid = false;
        }
    }

    return isValid;
}