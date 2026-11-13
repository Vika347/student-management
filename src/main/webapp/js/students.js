// CRUD-обёртки над REST API

function getAllStudents(filters) {
    return getStudentsFromApi(filters || {});
}

function getStudentById(id) {
    return getStudentFromApi(id);
}

function createStudent(studentData) {
    return createStudentOnApi(studentData);
}

function updateStudent(id, studentData) {
    return updateStudentOnApi(id, studentData);
}

function deleteStudent(id) {
    return deleteStudentOnApi(id);
}