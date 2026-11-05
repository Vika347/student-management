package ru.itmo.studentmanagement.user.repository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import ru.itmo.studentmanagement.user.entity.User;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class UserRepository {

    @PersistenceContext(unitName = "StudentPU")
    private EntityManager entityManager;

    public User findByUsername(String username) {
        try {
            return entityManager.createQuery(
                            "SELECT u FROM User u WHERE u.username = :username",
                            User.class
                    )
                    .setParameter("username", username)
                    .getSingleResult();
        } catch (Exception e) {
            return null;
        }
    }

    public User findByEmail(String email) {
        try {
            return entityManager.createQuery(
                            "SELECT u FROM User u WHERE u.email = :email",
                            User.class
                    )
                    .setParameter("email", email)
                    .getSingleResult();
        } catch (Exception e) {
            return null;
        }
    }

    // Сохранение пользователя в базе данных
    @Transactional
    public void save(User user) {
        entityManager.persist(user);
    }
}
