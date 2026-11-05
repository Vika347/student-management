package ru.itmo.studentmanagement.exception;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.util.Map;

// Обрабатывает конфликт данных и возвращает HTTP 409
@Provider
public class ConflictExceptionMapper
        implements ExceptionMapper<ConflictException> {

    // Формирует JSON-ответ с описанием ошибки
    @Override
    public Response toResponse(ConflictException exception) {
        return Response.status(Response.Status.CONFLICT)
                .type(MediaType.APPLICATION_JSON)
                .entity(Map.of(
                        "error", Map.of(
                                "message", exception.getMessage()
                        )
                ))
                .build();
    }
}
