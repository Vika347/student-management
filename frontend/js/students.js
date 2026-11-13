//CRUD студентов

//Получить всех студентов (при необходимости — с фильтрами)
function getAllStudents(filters) {
    return getStudentsFromApi(filters || {});
}

//Получить студента по id
function getStudentById(id) {
    return getStudentFromApi(id);
}


//Возвращаем созданного студента
function createStudent(studentData) {
    return createStudentOnApi(studentData);
}

//Заменить данные студента
function updateStudent(id, studentData) {
    return updateStudentOnApi(id, studentData);
}

//Удаление
function deleteStudent(id) {
    return deleteStudentOnApi(id);
}