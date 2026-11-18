// Проверка формы данных

function readForm() {
    const form = document.getElementById("student-form");
    if (!form) return null;

    return {
        fullName: document.getElementById("form-fullname").value.trim(),
        group: document.getElementById("form-group").value.trim().toUpperCase(),
        isuId: Number(document.getElementById("form-isuid").value),
        dormNumber: Number(document.getElementById("form-dorm").value),
        roomNumber: Number(document.getElementById("form-room").value),
        settlementDate: document.getElementById("form-movein").value,
        isForeigner: document.getElementById("form-foreigner").checked,
        notes: document.getElementById("form-notes").value.trim()
    };
}


// Возвращает ID студента при редактировании
function readFormId() {
    const el = document.getElementById("form-student-id");
    if (!el) return null;

    const value = el.value.trim();
    return value ? Number(value) : null;
}


// Очищает сообщения об ошибках
function clearErrors() {
    const errors = document.querySelectorAll(".form-error");

    for (let i = 0; i < errors.length; i++) {
        errors[i].textContent = "";
    }
}


// Показывает ошибку под конкретным полем
function showError(fieldKey, message) {
    const el = document.getElementById("error-" + fieldKey);
    if (el) el.textContent = message;
}


// Показывает ошибки, полученные от Java backend
function showServerErrors(err) {
    clearErrors();

    const details = err.details || {};

    const fieldMap = {
        fullName: "fullname",
        group: "group",
        isuId: "isuid",
        dormNumber: "dorm",
        roomNumber: "room",
        settlementDate: "movein",
        notes: "notes"
    };

    let shown = false;

    for (const serverField in fieldMap) {
        if (details[serverField]) {
            let msg = details[serverField];

            if (Array.isArray(msg)) {
                msg = msg.join(", ");
            }

            showError(fieldMap[serverField], String(msg));
            shown = true;
        }
    }

    if (!shown) {
        showError("fullname", err.message || "Ошибка сохранения");
    }
}


// Проверка формы перед отправкой
function validateForm() {
    const fullName = document.getElementById("form-fullname");
    const group = document.getElementById("form-group");
    const isuId = document.getElementById("form-isuid");
    const dorm = document.getElementById("form-dorm");
    const room = document.getElementById("form-room");
    const moveIn = document.getElementById("form-movein");
    const notes = document.getElementById("form-notes");

    clearErrors();

    let isValid = true;


    // ФИО
    if (!fullName.checkValidity()) {
        showError("fullname", "Введите корректное ФИО");
        isValid = false;
    }


    // Группа
    if (!group.checkValidity()) {
        showError("group", "Введите группу в формате P3224");
        isValid = false;
    }


    // ИСУ
    if (!isuId.checkValidity()) {
        showError(
            "isuid",
            "ИСУ ID должен быть целым числом от 100000 до 999999"
        );
        isValid = false;
    }


    // Общежитие
    if (!dorm.checkValidity()) {
        showError(
            "dorm",
            "Номер общежития должен быть целым числом от 1 до 10"
        );
        isValid = false;
    }


    // Комната
    if (!room.checkValidity()) {
        showError(
            "room",
            "Номер комнаты должен быть целым числом от 100 до 999"
        );
        isValid = false;
    }


    // Дата заселения
    if (!moveIn.checkValidity()) {
        showError(
            "movein",
            "Введите корректную дату заселения"
        );
        isValid = false;
    } else if (moveIn.value !== "") {
        const today = new Date();
        today.setHours(0, 0, 0, 0);

        const moveInDate = new Date(
            moveIn.value + "T00:00:00"
        );

        if (moveInDate > today) {
            showError(
                "movein",
                "Дата заселения не может быть в будущем"
            );
            isValid = false;
        }
    }


    // Заметки: запрещённые символы
    const forbiddenSymbols = ["/", "\\", "*", "\"", "<", ">"];

    for (let i = 0; i < forbiddenSymbols.length; i++) {
        if (notes.value.includes(forbiddenSymbols[i])) {
            showError(
                "notes",
                'В заметках нельзя использовать / \\ * " < >'
            );
            isValid = false;
            break;
        }
    }


    // Заметки: не больше 500 символов
    if (notes.value.length > 500) {
        showError(
            "notes",
            "В заметках должно быть не больше 500 символов"
        );
        isValid = false;
    }


    return isValid;
}
