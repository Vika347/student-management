package ru.itmo.studentmanagement.mapper;

import ru.itmo.studentmanagement.dto.StudentDTO;
import ru.itmo.studentmanagement.entity.Student;

// Преобразование Student в StudentDTO и обратно
public class StudentMapper {

    // Из сущности Student в DTO для ответа клиенту
    public static StudentDTO toDTO(Student student) {
        if (student == null) {
            return null;
        }

        StudentDTO dto = new StudentDTO();

        dto.setId(student.getId());
        dto.setFullName(student.getFullName());
        dto.setGroup(student.getGroup());
        dto.setIsuId(student.getIsuId());
        dto.setDormNumber(student.getDormNumber());
        dto.setRoomNumber(student.getRoomNumber());
        dto.setSettlementDate(student.getSettlementDate());
        dto.setIsForeigner(student.getIsForeigner());
        dto.setNotes(student.getNotes());

        return dto;
    }

    // Из DTO в сущность Student для сохранения в базе
    public static Student toEntity(StudentDTO dto) {
        if (dto == null) {
            return null;
        }

        Student student = new Student();

        student.setFullName(dto.getFullName());
        student.setGroup(dto.getGroup());
        student.setIsuId(dto.getIsuId());
        student.setDormNumber(dto.getDormNumber());
        student.setRoomNumber(dto.getRoomNumber());
        student.setSettlementDate(dto.getSettlementDate());
        student.setIsForeigner(dto.getIsForeigner());
        student.setNotes(dto.getNotes());

        return student;
    }
}
