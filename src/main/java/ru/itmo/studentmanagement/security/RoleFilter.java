package ru.itmo.studentmanagement.security;

import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.Cookie;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;

import ru.itmo.studentmanagement.auth.jwt.JwtService;
import ru.itmo.studentmanagement.exception.ApiErrorResponse;

import java.io.IOException;

@Provider
@Priority(Priorities.AUTHORIZATION)
public class RoleFilter implements ContainerRequestFilter {

    @Inject
    private JwtService jwtService;

    @Override
    public void filter(ContainerRequestContext requestContext)
            throws IOException {

        String path = requestContext.getUriInfo().getPath();
        String method = requestContext.getMethod();

        // Публичные маршруты авторизации и CORS preflight
        if ("OPTIONS".equals(method)
                || path.equals("auth/login")
                || path.equals("auth/register")
                || path.equals("auth/refresh")
                || path.equals("auth/logout")) {
            return;
        }

        // Проверяем, требует ли запрос роли ADMIN
        boolean isStudentModification =
                (path.equals("students") || path.startsWith("students/"))
                        && ("POST".equals(method)
                        || "PUT".equals(method)
                        || "PATCH".equals(method)
                        || "DELETE".equals(method));

        boolean requiresAdmin =
                path.equals("admin")
                        || path.startsWith("admin/")
                        || isStudentModification;

        // Для остальных запросов дополнительная проверка роли не нужна
        if (!requiresAdmin) {
            return;
        }

        Cookie cookie = requestContext.getCookies().get("access_token");

        if (cookie == null || cookie.getValue() == null || cookie.getValue().isBlank()) {
            sendError(requestContext,
                    Response.Status.UNAUTHORIZED,
                    "Требуется авторизация");
            return;
        }

        String token = cookie.getValue();

        if (!jwtService.validateAccessToken(token)) {
            sendError(requestContext,
                    Response.Status.UNAUTHORIZED,
                    "Недействительный токен");
            return;
        }

        String role = jwtService.getRoleFromToken(token);

        if (!"ADMIN".equals(role)) {
            sendError(requestContext,
                    Response.Status.FORBIDDEN,
                    "Недостаточно прав");
        }
    }

    private void sendError(
            ContainerRequestContext context,
            Response.Status status,
            String message) {

        context.abortWith(
                Response.status(status)
                        .type(MediaType.APPLICATION_JSON)
                        .entity(new ApiErrorResponse(message))
                        .build()
        );
    }
}
