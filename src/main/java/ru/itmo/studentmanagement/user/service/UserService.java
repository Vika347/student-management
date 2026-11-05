package ru.itmo.studentmanagement.user.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.mindrot.jbcrypt.BCrypt;
import ru.itmo.studentmanagement.user.entity.Role;
import ru.itmo.studentmanagement.user.entity.User;
import ru.itmo.studentmanagement.user.repository.UserRepository;
import jakarta.ws.rs.WebApplicationException;
import ru.itmo.studentmanagement.exception.ConflictException;

@ApplicationScoped
public class UserService {

    @Inject
    private UserRepository userRepository;

    public User register(String username, String email, String password) {

        if (userRepository.findByUsername(username) != null) {
            throw new ConflictException("Пользователь с таким логином уже существует");
        }

        if (userRepository.findByEmail(email) != null) {
            throw new ConflictException("Пользователь с таким email уже существует");
        }

        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());

        User user = new User(username, email, hashedPassword, Role.USER);

        userRepository.save(user);

        return user;
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username);
    }
}
