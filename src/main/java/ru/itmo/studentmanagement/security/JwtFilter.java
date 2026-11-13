package ru.itmo.studentmanagement.security;

import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;

import ru.itmo.studentmanagement.auth.jwt.JwtService;
import ru.itmo.studentmanagement.exception.ApiErrorResponse;

import java.io.IOException;

@Provider
@Priority(Priorities.AUTHENTICATION)
public class JwtFilter implements ContainerRequestFilter {

    @Inject
    private JwtService jwtService;

    @Override
    public void filter(ContainerRequestContext requestContext)
            throws IOException {

        String path = requestContext.getUriInfo().getPath();
        String method = requestContext.getMethod();

        // Пропускаем предварительные CORS-запросы
        if ("OPTIONS".equals(method)) {
            return;
        }

        // Публичные маршруты
        if (path.equals("auth/login")
                || path.equals("auth/register")
                || path.equals("auth/refresh")) {
            return;
        }

        String header = requestContext.getHeaderString("Authorization");

        if (header == null || !header.startsWith("Bearer ")) {
            sendError(requestContext, "Отсутствует токен авторизации");
            return;
        }

        String token = header.substring(7).trim();

        if (token.isEmpty()) {
            sendError(requestContext, "Пустой токен авторизации");
            return;
        }

        if (!jwtService.validateAccessToken(token)) {
            sendError(requestContext, "Недействительный токен");
        }
    }

    private void sendError(ContainerRequestContext context, String message) {
        context.abortWith(
                Response.status(Response.Status.UNAUTHORIZED)
                        .type(MediaType.APPLICATION_JSON)
                        .entity(new ApiErrorResponse(message))
                        .build()
        );
    }
}
