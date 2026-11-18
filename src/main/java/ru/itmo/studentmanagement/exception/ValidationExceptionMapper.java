package ru.itmo.studentmanagement.exception;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.util.LinkedHashMap;
import java.util.Map;
//Обработка ошибок Bean Validation
@Provider
public class ValidationExceptionMapper
        implements ExceptionMapper<ConstraintViolationException> {

    @Override
    public Response toResponse(ConstraintViolationException exception) {

        Map<String, String> details = new LinkedHashMap<>();

        for (ConstraintViolation<?> violation
                : exception.getConstraintViolations()) {

            String path = violation.getPropertyPath().toString();
            String field = path.substring(path.lastIndexOf('.') + 1);

            details.merge(
                    field,
                    violation.getMessage(),
                    (oldMessage, newMessage) -> oldMessage + "; " + newMessage
            );
        }

        return Response.status(Response.Status.BAD_REQUEST)
                .type(MediaType.APPLICATION_JSON)
                .entity(new ApiErrorResponse("Ошибка валидации", details))
                .build();
    }
}
