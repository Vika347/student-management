package ru.itmo.studentmanagement.exception;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.util.Map;

// Обрабатывает ошибки в данных запроса (HTTP 400)
@Provider
public class IllegalArgumentExceptionMapper
        implements ExceptionMapper<IllegalArgumentException> {

    // Возвращает JSON с описанием ошибки
    @Override
    public Response toResponse(IllegalArgumentException exception) {

        return Response.status(Response.Status.BAD_REQUEST)
                .type(MediaType.APPLICATION_JSON)
                .entity(Map.of(
                        "error", Map.of(
                                "message", exception.getMessage()
                        )
                ))
                .build();
    }
}
