package ru.itmo.studentmanagement.exception;

import jakarta.ejb.ApplicationException;

// Исключение для некорректных данных запроса (HTTP 400)
@ApplicationException(rollback = true)
public class BadRequestException extends IllegalArgumentException {

    public BadRequestException(String message) {
        super(message);
    }
}
