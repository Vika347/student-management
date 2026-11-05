
package ru.itmo.studentmanagement.repository;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;

import ru.itmo.studentmanagement.entity.Student;
import ru.itmo.studentmanagement.exception.BadRequestException;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

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

    // Получение студентов с фильтрацией, поиском, сортировкой и пагинацией
    public List<Student> findByFilters(
            Long id,
            String fullName,
            String group,
            Integer isuId,
            Integer dormNumber,
            Integer roomNumber,
            LocalDate settlementDate,
            Boolean isForeigner,
            String notes,
            String search,
            String sortBy,
            String sortOrder,
            Integer page,
            Integer size) {

        // Начало JPQL-запроса
        StringBuilder jpql = new StringBuilder(
                "SELECT s FROM Student s WHERE 1=1"
        );

        // Фильтрация по ID
        if (id != null) {
            jpql.append(" AND s.id = :id");
        }

        // Фильтрация по ФИО без учёта регистра
        if (fullName != null && !fullName.isBlank()) {
            jpql.append(" AND LOWER(s.fullName) LIKE :fullName");
        }

        // Фильтрация по группе
        if (group != null) {
            jpql.append(" AND s.group = :group");
        }

        // Фильтрация по номеру ИСУ
        if (isuId != null) {
            jpql.append(" AND s.isuId = :isuId");
        }

        // Фильтрация по номеру общежития
        if (dormNumber != null) {
            jpql.append(" AND s.dormNumber = :dormNumber");
        }

        // Фильтрация по номеру комнаты
        if (roomNumber != null) {
            jpql.append(" AND s.roomNumber = :roomNumber");
        }

        // Фильтрация по дате заселения
        if (settlementDate != null) {
            jpql.append(" AND s.settlementDate = :settlementDate");
        }

        // Фильтрация по статусу иностранного студента
        if (isForeigner != null) {
            jpql.append(" AND s.isForeigner = :isForeigner");
        }

        // Фильтрация по примечаниям
        if (notes != null && !notes.isBlank()) {
            jpql.append(" AND LOWER(s.notes) LIKE :notes");
        }

        // Поиск по ФИО
        if (search != null && !search.isBlank()) {
            jpql.append(" AND LOWER(s.fullName) LIKE :search");
        }

        // Поля, по которым разрешена сортировка
        List<String> allowedSortFields = List.of(
                "id",
                "fullName",
                "group",
                "isuId",
                "dormNumber",
                "roomNumber",
                "settlementDate",
                "isForeigner",
                "notes"
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

        // Добавляем сортировку к JPQL-запросу
        jpql.append(" ORDER BY s.")
                .append(sortBy)
                .append(" ")
                .append(sortOrder.toUpperCase(Locale.ROOT));

        // Стабильный порядок при одинаковых значениях поля сортировки
        if (!sortBy.equals("id")) {
            jpql.append(", s.id ASC");
        }

        // Создаём JPA-запрос
        TypedQuery<Student> query =
                entityManager.createQuery(jpql.toString(), Student.class);

        // Подставляем значения параметров
        if (id != null) {
            query.setParameter("id", id);
        }

        if (fullName != null && !fullName.isBlank()) {
            query.setParameter(
                    "fullName",
                    "%" + fullName.toLowerCase(Locale.ROOT) + "%"
            );
        }

        if (group != null) {
            query.setParameter("group", group);
        }

        if (isuId != null) {
            query.setParameter("isuId", isuId);
        }

        if (dormNumber != null) {
            query.setParameter("dormNumber", dormNumber);
        }

        if (roomNumber != null) {
            query.setParameter("roomNumber", roomNumber);
        }

        if (settlementDate != null) {
            query.setParameter("settlementDate", settlementDate);
        }

        if (isForeigner != null) {
            query.setParameter("isForeigner", isForeigner);
        }

        if (notes != null && !notes.isBlank()) {
            query.setParameter(
                    "notes",
                    "%" + notes.toLowerCase(Locale.ROOT) + "%"
            );
        }

        if (search != null && !search.isBlank()) {
            query.setParameter(
                    "search",
                    "%" + search.toLowerCase(Locale.ROOT) + "%"
            );
        }

        // Проверяем номер страницы
        if (page == null) {
            page = 1;
        }

        // Проверяем размер страницы
        if (size == null) {
            size = 10;
        }

        if (page < 1) {
            throw new BadRequestException(
                    "Номер страницы должен быть больше 0"
            );
        }

        if (size < 1 || size > 100) {
            throw new BadRequestException(
                    "Размер страницы должен быть от 1 до 100"
            );
        }

        // Рассчитываем количество пропускаемых записей
        long offset = ((long) page - 1) * size;

        if (offset > Integer.MAX_VALUE) {
            throw new BadRequestException(
                    "Слишком большой номер страницы"
            );
        }

        // Ограничиваем количество возвращаемых студентов
        query.setFirstResult((int) offset);
        query.setMaxResults(size);

        return query.getResultList();
    }
}

