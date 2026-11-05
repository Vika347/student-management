package ru.itmo.studentmanagement.exception;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.util.LinkedHashMap;
import java.util.Map;

// Обрабатывает ошибки валидации полей студента (HTTP 400)
@Provider
public class ValidationExceptionMapper
        implements ExceptionMapper<ConstraintViolationException> {

    // Собираем названия полей и сообщения об ошибках
    @Override
    public Response toResponse(ConstraintViolationException exception) {

        Map<String, String> details = new LinkedHashMap<>();

        for (ConstraintViolation<?> violation
                : exception.getConstraintViolations()) {

            String path = violation.getPropertyPath().toString();
            String field = path.substring(path.lastIndexOf('.') + 1);

            details.put(field, violation.getMessage());
        }

        // Формируем описание ошибки
        Map<String, Object> error = new LinkedHashMap<>();
        error.put("message", "Ошибка валидации");
        error.put("details", details);

        // Создаём JSON-ответ
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("error", error);

        return Response.status(Response.Status.BAD_REQUEST)
                .type(MediaType.APPLICATION_JSON)
                .entity(response)
                .build();
    }
}
