package ru.itmo.studentmanagement.exception;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.util.logging.Level;
import java.util.logging.Logger;
//Обработка непредвиденных ошибок и логирование
@Provider
public class InternalExceptionMapper
        implements ExceptionMapper<Exception> {

    private static final Logger LOGGER =
            Logger.getLogger(InternalExceptionMapper.class.getName());

    @Override
    public Response toResponse(Exception exception) {

        LOGGER.log(
                Level.SEVERE,
                "Внутренняя ошибка сервера",
                exception
        );

        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .type(MediaType.APPLICATION_JSON)
                .entity(new ApiErrorResponse("Внутренняя ошибка сервера"))
                .build();
    }
}

