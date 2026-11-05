package ru.itmo.studentmanagement.repository;

import jakarta.persistence.TypedQuery;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import ru.itmo.studentmanagement.entity.Student;
import java.util.List;
import ru.itmo.studentmanagement.exception.BadRequestException;

// Работа со студентами в базе данных через JPA
@Stateless
public class StudentRepository {

    // EntityManager выполняет операции с базой данных
    @PersistenceContext(unitName = "StudentPU")
    private EntityManager entityManager;

    // Сохранение нового студента
    public void save(Student student) {
        entityManager.persist(student);
        entityManager.flush();
    }

    // Получение всех студентов
    public List<Student> findAll() {
        return entityManager
                .createQuery("SELECT s FROM Student s", Student.class)
                .getResultList();
    }

    // Поиск студента по ID
    public Student findById(Long id) {
        return entityManager.find(Student.class, id);
    }

    // Обновление данных студента
    public Student update(Student student) {
        return entityManager.merge(student);
    }

    // Удаление студента
    public void delete(Student student) {
        entityManager.remove(entityManager.merge(student));
    }

    // Поиск по номеру ИСУ
    public Student findByIsuId(Integer isuId) {
        return entityManager.createQuery(
                        "SELECT s FROM Student s WHERE s.isuId = :isuId",
                        Student.class
                )
                .setParameter("isuId", isuId)
                .getResultStream()
                .findFirst()
                .orElse(null);
    }


   // Получение студентов с поиском, фильтрами, сортировкой и пагинацией
    public List<Student> findByFilters(
            String group,
            Integer dormNumber,
            String sortBy,
            String sortOrder,
            Integer page,
            Integer size,
            String search) {

        // Начало JPQL-запроса
        String jpql = "SELECT s FROM Student s WHERE 1=1";

        // Поиск по ФИО без учёта регистра
        if (search != null && !search.isBlank()) {
            jpql += " AND LOWER(s.fullName) LIKE :search";
        }

        // Фильтрация по группе
        if (group != null) {
            jpql += " AND s.group = :group";
        }

        // Фильтрация по номеру общежития
        if (dormNumber != null) {
            jpql += " AND s.dormNumber = :dormNumber";
        }

        // Поля, по которым разрешена сортировка
        List<String> allowedSortFields = List.of(
                "id",
                "fullName",
                "group",
                "isuId",
                "dormNumber",
                "roomNumber",
                "settlementDate"
        );

        if (sortBy == null) {
            sortBy = "id";
        }

        if (!allowedSortFields.contains(sortBy)) {
            throw new BadRequestException(
                    "Недопустимое поле сортировки: " + sortBy
            );
        }

        if (sortOrder == null) {
            sortOrder = "asc";
        }

        if (!sortOrder.equalsIgnoreCase("asc")
                && !sortOrder.equalsIgnoreCase("desc")) {
            throw new BadRequestException(
                    "Направление сортировки должно быть asc или desc"
            );
        }

        // Добавляем сортировку к запросу
        jpql += " ORDER BY s." + sortBy + " " + sortOrder.toUpperCase();

        // Создаём запрос JPA
        TypedQuery<Student> query =
                entityManager.createQuery(jpql, Student.class);

        // Подставляем значения параметров
        if (search != null && !search.isBlank()) {
            query.setParameter("search", "%" + search.toLowerCase() + "%");
        }

        if (group != null) {
            query.setParameter("group", group);
        }

        if (dormNumber != null) {
            query.setParameter("dormNumber", dormNumber);
        }

        // Проверяем номер страницы и её размер
        if (page == null) {
            page = 1;
        }

        if (size == null) {
            size = 10;
        }

        if (page < 1) {
            throw new BadRequestException("Номер страницы должен быть больше 0");
        }

        if (size < 1 || size > 100) {
            throw new BadRequestException("Размер страницы должен быть от 1 до 100");
        }

        long offset = ((long) page - 1) * size;

        if (offset > Integer.MAX_VALUE) {
            throw new BadRequestException("Слишком большой номер страницы");
        }
        // Ограничиваем количество возвращаемых студентов
        query.setFirstResult((int) offset);
        query.setMaxResults(size);

        return query.getResultList();

    }

}
