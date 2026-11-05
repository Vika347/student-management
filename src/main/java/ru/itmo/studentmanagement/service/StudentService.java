package ru.itmo.studentmanagement.service;

import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;

import ru.itmo.studentmanagement.exception.ConflictException;
import java.time.format.DateTimeParseException;
import jakarta.json.JsonNumber;
import jakarta.json.JsonValue;
import java.time.LocalDate;
import ru.itmo.studentmanagement.dto.StudentDTO;
import ru.itmo.studentmanagement.entity.Student;
import ru.itmo.studentmanagement.mapper.StudentMapper;
import ru.itmo.studentmanagement.repository.StudentRepository;
import java.util.List;
import java.util.stream.Collectors;
import jakarta.json.JsonObject;
import jakarta.inject.Inject;
import jakarta.validation.Validator;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.util.Set;
import java.util.logging.Logger;
import java.util.logging.Level;

@Stateless
public class StudentService {

    @EJB
    private StudentRepository studentRepository;

    private static final Logger logger =
            Logger.getLogger(StudentService.class.getName());

    @Inject
    private Validator validator;

    // Создание студента с проверкой уникальности ИСУ
    public StudentDTO createStudent(StudentDTO dto) {

        Student existingStudent = studentRepository.findByIsuId(dto.getIsuId());

        if (existingStudent != null) {
            throw new ConflictException("Номер ИСУ уже занят");
        }

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

        return StudentMapper.toDTO(student);
    }


    // Удаление студента по ID
    public boolean deleteStudent(Long id) {

        Student student = studentRepository.findById(id);

        if (student == null) {
            logger.log(Level.WARNING,
                    "Попытка удалить несуществующего студента с ID: {0}", id);
            return false;
        }

        studentRepository.delete(student);

        logger.log(Level.INFO,
                "Удалён студент с ID: {0}", id);

        return true;
    }

    // Получение целого числа из JSON с проверкой типа
    private int getIntegerField(JsonObject updates, String field) {

        if (updates.get(field).getValueType() != JsonValue.ValueType.NUMBER) {
            throw new IllegalArgumentException(
                    "Поле " + field + " должно быть числом"
            );
        }

        JsonNumber number = updates.getJsonNumber(field);

        if (!number.isIntegral()) {
            throw new IllegalArgumentException(
                    "Поле " + field + " должно быть целым числом"
            );
        }

        try {
            return number.intValueExact();
        } catch (ArithmeticException e) {
            throw new IllegalArgumentException(
                    "Поле " + field + " содержит слишком большое число"
            );
        }
    }

    // Получение строки из JSON с проверкой типа
    private String getStringField(JsonObject updates, String field) {

        if (updates.get(field).getValueType() != JsonValue.ValueType.STRING) {
            throw new IllegalArgumentException(
                    "Поле " + field + " должно быть строкой"
            );
        }

        return updates.getString(field);
    }

    // Получение логического значения из JSON
    private boolean getBooleanField(JsonObject updates, String field) {

        JsonValue.ValueType type = updates.get(field).getValueType();

        if (type != JsonValue.ValueType.TRUE &&
                type != JsonValue.ValueType.FALSE) {

            throw new IllegalArgumentException(
                    "Поле " + field + " должно быть логическим значением"
            );
        }

        return updates.getBoolean(field);
    }

    // Получение и проверка даты из JSON
    private LocalDate getDateField(JsonObject updates, String field) {

        String value = getStringField(updates, field);

        try {
            return LocalDate.parse(value);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(
                    "Поле " + field + " должно содержать дату в формате ГГГГ-ММ-ДД"
            );
        }
    }


    // Частичное обновление студента через PATCH
    public StudentDTO updateStudent(Long id, JsonObject updates) {

        Student student = studentRepository.findById(id);

        if (student == null) {
            return null;
        }

        if (updates == null || updates.isEmpty()) {
            throw new IllegalArgumentException(
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

                    Student existingStudent = studentRepository.findByIsuId(newIsuId);

                    if (existingStudent != null && !existingStudent.getId().equals(id)) {
                        throw new ConflictException("Номер ИСУ уже занят");
                    }

                    student.setIsuId(newIsuId);
                }
                case "settlementDate" ->
                        student.setSettlementDate(getDateField(updates, field));

                case "isForeigner" ->
                        student.setIsForeigner(getBooleanField(updates, field));

                default ->
                        throw new IllegalArgumentException(
                                "Неизвестное поле: " + field
                        );
            }
        }
        // Проверяем студента после изменения полей
        Set<ConstraintViolation<Student>> violations = validator.validate(student);

        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }
        // Сохраняем изменения
        Student updatedStudent = studentRepository.update(student);

        logger.log(Level.INFO,
                "Частично обновлён студент с ID: {0}", id);

        return StudentMapper.toDTO(updatedStudent);
    }


    // Полное обновление данных студента через PUT
    public StudentDTO updateStudentFully(Long id, StudentDTO dto) {

        Student student = studentRepository.findById(id);

        if (student == null) {
            return null;
        }

        Student existingStudent = studentRepository.findByIsuId(dto.getIsuId());

        if (existingStudent != null && !existingStudent.getId().equals(id)) {
            throw new ConflictException("Номер ИСУ уже занят");
        }

        student.setFullName(dto.getFullName());
        student.setGroup(dto.getGroup());
        student.setIsuId(dto.getIsuId());
        student.setDormNumber(dto.getDormNumber());
        student.setRoomNumber(dto.getRoomNumber());
        student.setSettlementDate(dto.getSettlementDate());
        student.setIsForeigner(dto.getIsForeigner());
        student.setNotes(dto.getNotes());

        Set<ConstraintViolation<Student>> violations = validator.validate(student);

        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }

        Student updatedStudent = studentRepository.update(student);

        logger.log(Level.INFO,
                "Полностью обновлён студент с ID: {0}", id);

        return StudentMapper.toDTO(updatedStudent);
    }



    // Получение студентов с фильтрацией по всем полям,
    // поиском, сортировкой и пагинацией
    public List<StudentDTO> getStudentsByFilters(
            Long id,
            String fullName,
            String group,
            Integer isuId,
            Integer dormNumber,
            Integer roomNumber,
            String settlementDate,
            Boolean isForeigner,
            String notes,
            String search,
            String sortBy,
            String sortOrder,
            Integer page,
            Integer size) {

        // Проверяем формат даты, если пользователь её указал
        LocalDate parsedDate = null;

        if (settlementDate != null && !settlementDate.isBlank()) {
            try {
                parsedDate = LocalDate.parse(settlementDate);
            } catch (DateTimeParseException e) {
                throw new IllegalArgumentException(
                        "settlementDate должна быть в формате ГГГГ-ММ-ДД"
                );
            }
        }

        List<Student> students = studentRepository.findByFilters(
                id,
                fullName,
                group,
                isuId,
                dormNumber,
                roomNumber,
                parsedDate,
                isForeigner,
                notes,
                search,
                sortBy,
                sortOrder,
                page,
                size
        );

        return students.stream()
                .map(StudentMapper::toDTO)
                .toList();
    }



}
