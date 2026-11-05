package ru.itmo.studentmanagement.security;

import jakarta.inject.Inject;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import ru.itmo.studentmanagement.auth.jwt.JwtService;
import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.core.MediaType;
import java.util.Map;
import java.io.IOException;

// Проверяет JWT перед доступом к API
@Provider
@Priority(Priorities.AUTHENTICATION)
public class JwtFilter implements ContainerRequestFilter {

    @Inject
    private JwtService jwtService;

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {

        String path = requestContext.getUriInfo().getPath();

        if (path.startsWith("auth")) {
            return;
        }

        String header = requestContext.getHeaderString("Authorization");

        if (header == null || !header.startsWith("Bearer ")) {
            sendError(requestContext, "Отсутствует токен авторизации");
            return;
        }

        String token = header.substring(7);

        if (!jwtService.validateAccessToken(token)) {
            sendError(requestContext, "Недействительный токен");
        }
    }

    private void sendError(ContainerRequestContext context, String message) {
        context.abortWith(
                Response.status(401)
                        .type(MediaType.APPLICATION_JSON)
                        .entity(Map.of("error", Map.of("message", message)))
                        .build()
        );
    }
}
