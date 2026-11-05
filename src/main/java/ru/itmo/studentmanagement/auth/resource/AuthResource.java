package ru.itmo.studentmanagement.auth.resource;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import ru.itmo.studentmanagement.auth.jwt.JwtService;
import ru.itmo.studentmanagement.auth.service.AuthService;
import ru.itmo.studentmanagement.user.dto.AuthResponse;
import ru.itmo.studentmanagement.user.dto.LoginRequest;
import ru.itmo.studentmanagement.user.dto.RefreshRequest;
import ru.itmo.studentmanagement.user.dto.RegisterRequest;
import ru.itmo.studentmanagement.user.entity.User;
import ru.itmo.studentmanagement.user.service.UserService;
import jakarta.ws.rs.NotAuthorizedException;
import jakarta.validation.Valid;
import jakarta.ws.rs.WebApplicationException;


@Path("/auth")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class AuthResource {

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
        try {
            return Response.ok(authService.login(request)).build();
        } catch (NotAuthorizedException e) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("{\"error\":{\"message\":\"Неверный логин или пароль\"}}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }
    }

    @POST
    @Path("/refresh")
    public Response refresh(@Valid RefreshRequest request) {

        String refreshToken = request.getRefreshToken();

        if (!jwtService.validateRefreshToken(refreshToken)) {
            throw new WebApplicationException(
                    "Недействительный refresh-токен", 401
            );
        }

        String username = jwtService.getUsernameFromToken(refreshToken);

        User user = userService.findByUsername(username);

        if (user == null) {
            throw new WebApplicationException(
                    "Недействительный refresh-токен", 401
            );
        }

        String newAccessToken = jwtService.generateAccessToken(user);

        AuthResponse response = new AuthResponse(
                newAccessToken,
                refreshToken,
                user.getUsername(),
                user.getRole().name()
        );

        return Response.ok(response).build();
    }
}
