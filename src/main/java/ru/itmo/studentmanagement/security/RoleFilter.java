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

import java.io.IOException;
import java.util.Map;

// Проверяет права пользователя после проверки JWT
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

        // Пропускаем авторизацию и CORS preflight
        if (path.startsWith("auth") || method.equals("OPTIONS")) {
            return;
        }

        String authorizationHeader =
                requestContext.getHeaderString("Authorization");

        if (authorizationHeader == null
                || !authorizationHeader.startsWith("Bearer ")) {
            return;
        }

        String token = authorizationHeader.substring(7);
        String role = jwtService.getRoleFromToken(token);

        // Методы, изменяющие данные студентов
        boolean isStudentModification =
                path.startsWith("students")
                        && (method.equals("POST")
                        || method.equals("PUT")
                        || method.equals("PATCH")
                        || method.equals("DELETE"));

        // Изменять студентов может только ADMIN
        if ((path.startsWith("admin") || isStudentModification)
                && !"ADMIN".equals(role)) {

            requestContext.abortWith(
                    Response.status(403)
                            .type(MediaType.APPLICATION_JSON)
                            .entity(Map.of(
                                    "error", Map.of("message", "Недостаточно прав")
                            ))
                            .build()
            );
        }
    }
}
