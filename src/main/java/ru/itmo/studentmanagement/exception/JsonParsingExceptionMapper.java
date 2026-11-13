package ru.itmo.studentmanagement.exception;

import jakarta.json.stream.JsonParsingException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class JsonParsingExceptionMapper
        implements ExceptionMapper<JsonParsingException> {

    @Override
    public Response toResponse(JsonParsingException exception) {

        return Response.status(Response.Status.BAD_REQUEST)
                .type(MediaType.APPLICATION_JSON)
                .entity(new ApiErrorResponse("Некорректный формат JSON"))
                .build();
    }
}

