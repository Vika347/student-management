package ru.itmo.studentmanagement.exception;

import jakarta.ejb.ApplicationException;

// Исключение(номер ИСУ уже занят другим студентом (HTTP 409))
@ApplicationException(rollback = true)
public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }
}
