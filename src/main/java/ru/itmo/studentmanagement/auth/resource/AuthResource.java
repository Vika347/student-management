package ru.itmo.studentmanagement.auth.resource;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.CookieParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;

import ru.itmo.studentmanagement.auth.jwt.JwtService;
import ru.itmo.studentmanagement.auth.service.AuthService;
import ru.itmo.studentmanagement.exception.ApiErrorResponse;
import ru.itmo.studentmanagement.user.dto.AuthResponse;
import ru.itmo.studentmanagement.user.dto.LoginRequest;
import ru.itmo.studentmanagement.user.dto.RegisterRequest;
import ru.itmo.studentmanagement.user.entity.User;
import ru.itmo.studentmanagement.user.service.UserService;

import java.util.Map;

@Path("/auth")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class AuthResource {

    private static final String ACCESS_PATH = "/student-management/api";
    private static final String REFRESH_PATH = "/student-management/api/auth";

    @Inject
    private AuthService authService;

    @Inject
    private UserService userService;

    @Inject
    private JwtService jwtService;

    @POST
    @Path("/register")
    public Response register(@Valid RegisterRequest request) {

        User user = userService.register(
                request.getUsername(),
                request.getEmail(),
                request.getPassword()
        );

        return Response.status(Response.Status.CREATED)
                .entity(user.getUsername())
                .build();
    }

    @POST
    @Path("/login")
    public Response login(@Valid LoginRequest request) {
        AuthResponse result = authService.login(request);

        return Response.ok(Map.of(
                        "username", result.getUsername(),
                        "role", result.getRole()
                ))
                .cookie(createCookie("access_token", result.getAccessToken(), ACCESS_PATH, 15 * 60))
                .cookie(createCookie("refresh_token", result.getRefreshToken(), REFRESH_PATH, 7 * 24 * 60 * 60))
                .build();
    }

    @POST
    @Path("/refresh")
    @Consumes(MediaType.WILDCARD)
    public Response refresh(@CookieParam("refresh_token") String refreshToken) {

        if (refreshToken == null || !jwtService.validateRefreshToken(refreshToken)) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(new ApiErrorResponse("Недействительный refresh-токен"))
                    .build();
        }

        String username = jwtService.getUsernameFromToken(refreshToken);
        User user = userService.findByUsername(username);

        if (user == null) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(new ApiErrorResponse("Пользователь не найден"))
                    .build();
        }

        String newAccessToken = jwtService.generateAccessToken(user);

        return Response.ok(Map.of(
                        "username", user.getUsername(),
                        "role", user.getRole().name()
                ))
                .cookie(createCookie("access_token", newAccessToken, ACCESS_PATH, 15 * 60))
                .build();
    }

    @POST
    @Path("/logout")
    @Consumes(MediaType.WILDCARD)
    public Response logout() {
        return Response.noContent()
                .cookie(deleteCookie("access_token", ACCESS_PATH))
                .cookie(deleteCookie("refresh_token", REFRESH_PATH))
                .build();
    }

    private NewCookie createCookie(String name, String value, String path, int maxAge) {
        return new NewCookie.Builder(name)
                .value(value)
                .path(path)
                .maxAge(maxAge)
                .httpOnly(true)
                .secure(Boolean.parseBoolean(System.getenv("COOKIE_SECURE")))
                .sameSite(NewCookie.SameSite.STRICT)
                .build();
    }

    private NewCookie deleteCookie(String name, String path) {
        return new NewCookie.Builder(name)
                .value("")
                .path(path)
                .maxAge(0)
                .httpOnly(true)
                .secure(Boolean.parseBoolean(System.getenv("COOKIE_SECURE")))
                .sameSite(NewCookie.SameSite.STRICT)
                .build();
    }
}
