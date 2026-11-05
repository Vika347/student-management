package ru.itmo.studentmanagement.auth.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.mindrot.jbcrypt.BCrypt;
import ru.itmo.studentmanagement.auth.jwt.JwtService;
import ru.itmo.studentmanagement.user.dto.AuthResponse;
import ru.itmo.studentmanagement.user.dto.LoginRequest;
import ru.itmo.studentmanagement.user.entity.User;
import ru.itmo.studentmanagement.user.repository.UserRepository;
import ru.itmo.studentmanagement.user.service.UserService;
import jakarta.ws.rs.NotAuthorizedException;

@ApplicationScoped
public class AuthService {

    @Inject
    private UserRepository userRepository;

    @Inject
    private UserService userService;

    @Inject
    private JwtService jwtService;

    public AuthResponse login(LoginRequest request) {

        User user = userRepository.findByUsername(request.getUsername());

        if (user == null) {
            throw new NotAuthorizedException("Bearer");
        }

        if (!BCrypt.checkpw(request.getPassword(), user.getPassword())) {
            throw new NotAuthorizedException("Bearer");
        }

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        return new AuthResponse(
                accessToken,
                refreshToken,
                user.getUsername(),
                user.getRole().name()
        );
    }
}
