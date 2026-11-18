package ru.itmo.studentmanagement.exception;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
//Преобразование конфликта в HTTP 409
@Provider
public class ConflictExceptionMapper
        implements ExceptionMapper<ConflictException> {

    @Override
    public Response toResponse(ConflictException exception) {
        return Response.status(Response.Status.CONFLICT)
                .type(MediaType.APPLICATION_JSON)
                .entity(new ApiErrorResponse(exception.getMessage()))
                .build();
    }
}

