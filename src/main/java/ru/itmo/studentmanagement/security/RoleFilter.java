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

        if (path.startsWith("auth")) {
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

        // Проверяем, изменяет ли пользователь данные студентов
        boolean isStudentModification =
                path.startsWith("students")
                        && !requestContext.getMethod().equals("GET");

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
