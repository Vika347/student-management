package ru.itmo.studentmanagement.user.repository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

import ru.itmo.studentmanagement.user.entity.User;

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

        } catch (NoResultException e) {
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

        } catch (NoResultException e) {
            return null;
        }
    }

    @Transactional
    public void save(User user) {
        entityManager.persist(user);
    }
}
