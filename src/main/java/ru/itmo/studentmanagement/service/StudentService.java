package ru.itmo.studentmanagement.service;

import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.json.JsonObject;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import jakarta.ws.rs.BadRequestException;

import ru.itmo.studentmanagement.dto.StudentDTO;
import ru.itmo.studentmanagement.entity.Student;
import ru.itmo.studentmanagement.exception.ConflictException;
import ru.itmo.studentmanagement.filtr.StudentFilterCriteria;
import ru.itmo.studentmanagement.mapper.StudentMapper;
import ru.itmo.studentmanagement.repository.StudentRepository;
import ru.itmo.studentmanagement.service.parser.StudentFilterParser;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static ru.itmo.studentmanagement.service.parser.StudentJsonFieldParser.*;

// Сервис с бизнес-логикой работы со студентами
@Stateless
public class StudentService {

    @EJB
    private StudentRepository studentRepository;

    @Inject
    private Validator validator;

    private static final Logger logger =
            Logger.getLogger(StudentService.class.getName());

    // Создание студента с проверкой уникальности ИСУ
    public StudentDTO createStudent(StudentDTO dto) {

        checkIsuIdUnique(dto.getIsuId(), null);

        Student student = StudentMapper.toEntity(dto);

        studentRepository.save(student);

        logger.log(Level.INFO, "Создан студент с ID: {0}", student.getId());

        return StudentMapper.toDTO(student);
    }

    // Получение всех студентов
    public List<StudentDTO> getAllStudents() {

        return studentRepository.findAll()
                .stream()
                .map(StudentMapper::toDTO)
                .collect(Collectors.toList());
    }

    // Получение студента по ID
    public StudentDTO getStudentById(Long id) {

        Student student = studentRepository.findById(id);

        if (student == null) {
            return null;
        }

        return StudentMapper.toDTO(student);
    }

    // Удаление студента по ID
    public boolean deleteStudent(Long id) {

        Student student = studentRepository.findById(id);

        if (student == null) {
            logger.log(
                    Level.WARNING,
                    "Попытка удалить несуществующего студента с ID: {0}",
                    id
            );

            return false;
        }

        studentRepository.delete(student);

        logger.log(Level.INFO, "Удалён студент с ID: {0}", id);

        return true;
    }

    // Частичное обновление через PATCH
    public StudentDTO updateStudent(Long id, JsonObject updates) {

        Student student = studentRepository.findById(id);

        if (student == null) {
            return null;
        }

        if (updates == null || updates.isEmpty()) {
            throw new BadRequestException(
                    "Необходимо указать хотя бы одно поле для обновления"
            );
        }

        for (String field : updates.keySet()) {

            switch (field) {

                case "fullName" ->
                        student.setFullName(getStringField(updates, field));

                case "group" ->
                        student.setGroup(getStringField(updates, field));

                case "notes" ->
                        student.setNotes(getStringField(updates, field));

                case "dormNumber" ->
                        student.setDormNumber(getIntegerField(updates, field));

                case "roomNumber" ->
                        student.setRoomNumber(getIntegerField(updates, field));

                case "isuId" -> {

                    Integer newIsuId = getIntegerField(updates, field);

                    checkIsuIdUnique(newIsuId, id);

                    student.setIsuId(newIsuId);
                }

                case "settlementDate" ->
                        student.setSettlementDate(getDateField(updates, field));

                case "isForeigner" ->
                        student.setIsForeigner(getBooleanField(updates, field));

                default ->
                        throw new BadRequestException(
                                "Неизвестное поле: " + field
                        );
            }
        }

        // Проверка данных после обновления
        validateStudent(student);

        // Сохранение изменений
        Student updatedStudent = studentRepository.update(student);

        logger.log(
                Level.INFO,
                "Частично обновлён студент с ID: {0}",
                id
        );

        return StudentMapper.toDTO(updatedStudent);
    }

    // Полное обновление студента через PUT
    public StudentDTO updateStudentFully(Long id, StudentDTO dto) {

        Student student = studentRepository.findById(id);

        if (student == null) {
            return null;
        }

        checkIsuIdUnique(dto.getIsuId(), id);

        student.setFullName(dto.getFullName());
        student.setGroup(dto.getGroup());
        student.setIsuId(dto.getIsuId());
        student.setDormNumber(dto.getDormNumber());
        student.setRoomNumber(dto.getRoomNumber());
        student.setSettlementDate(dto.getSettlementDate());
        student.setIsForeigner(dto.getIsForeigner());
        student.setNotes(dto.getNotes());

        // Проверка всех полей
        validateStudent(student);

        Student updatedStudent = studentRepository.update(student);

        logger.log(Level.INFO, "Полностью обновлён студент с ID: {0}", id);

        return StudentMapper.toDTO(updatedStudent);
    }

    // Общая фильтрация
    public List<StudentDTO> getStudentsByFilters(
            StudentFilterCriteria filter) {

        // Проверка формата даты
        LocalDate parsedDate = null;

        String settlementDate = filter.settlementDate();

        if (settlementDate != null && !settlementDate.isBlank()) {

            try {
                parsedDate = LocalDate.parse(settlementDate);

            } catch (DateTimeParseException e) {
                throw new BadRequestException(
                        "settlementDate должна быть в формате ГГГГ-ММ-ДД"
                );
            }
        }

        // Фильтрация
        List<Student> students = studentRepository.findByFilters(
                filter,
                parsedDate
        );

        return students.stream()
                .map(StudentMapper::toDTO)
                .toList();
    }


    // Получение студентов через QUERY с JSON-фильтрами
    public List<StudentDTO> queryStudents(JsonObject filters) {

        // Проверка и преобразование JSON-параметров
        StudentFilterParser parser = new StudentFilterParser(filters);

        // Используем общую фильтрацию
        return getStudentsByFilters(parser.parse());
    }


    private void checkIsuIdUnique(Integer isuId, Long currentStudentId) {

        Student existingStudent = studentRepository.findByIsuId(isuId);

        if (existingStudent != null &&
                (currentStudentId == null ||
                        !existingStudent.getId().equals(currentStudentId))) {

            throw new ConflictException("Номер ИСУ уже занят");
        }
    }

    private void validateStudent(Student student) {

        Set<ConstraintViolation<Student>> violations =
                validator.validate(student);

        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }
    }
}
