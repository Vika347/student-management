package ru.itmo.studentmanagement.exception;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class WebApplicationExceptionMapper
        implements ExceptionMapper<WebApplicationException> {

    @Override
    public Response toResponse(WebApplicationException exception) {

        int status = exception.getResponse().getStatus();

        String message = switch (status) {
            case 400 -> "Некорректный запрос";
            case 401 -> "Требуется авторизация";
            case 403 -> "Доступ запрещён";
            case 404 -> "Ресурс не найден";
            case 405 -> "Метод не поддерживается";
            case 415 -> "Неподдерживаемый формат данных";
            case 500 -> "Внутренняя ошибка сервера";
            default -> "Ошибка обработки запроса";
        };

        return Response.status(status)
                .type(MediaType.APPLICATION_JSON)
                .entity(new ApiErrorResponse(message))
                .build();
    }
}
