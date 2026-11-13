package ru.itmo.studentmanagement.auth.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.NotAuthorizedException;
import org.mindrot.jbcrypt.BCrypt;

import ru.itmo.studentmanagement.auth.jwt.JwtService;
import ru.itmo.studentmanagement.user.dto.AuthResponse;
import ru.itmo.studentmanagement.user.dto.LoginRequest;
import ru.itmo.studentmanagement.user.entity.User;
import ru.itmo.studentmanagement.user.repository.UserRepository;

@ApplicationScoped
public class AuthService {

    @Inject
    private UserRepository userRepository;

    @Inject
    private JwtService jwtService;

    public AuthResponse login(LoginRequest request) {

        if (request == null
                || request.getUsername() == null
                || request.getUsername().isBlank()
                || request.getPassword() == null
                || request.getPassword().isBlank()) {

            throw new NotAuthorizedException("Bearer");
        }

        User user = userRepository.findByUsername(request.getUsername());

        if (user == null
                || !BCrypt.checkpw(request.getPassword(), user.getPassword())) {
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
