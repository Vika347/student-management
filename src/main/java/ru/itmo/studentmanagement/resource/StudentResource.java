package ru.itmo.studentmanagement.resource;

import jakarta.ejb.EJB;
import jakarta.json.JsonNumber;
import jakarta.json.JsonObject;
import jakarta.json.JsonString;
import jakarta.json.JsonValue;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import ru.itmo.studentmanagement.filtr.StudentFilterCriteria;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import ru.itmo.studentmanagement.dto.StudentDTO;
import ru.itmo.studentmanagement.service.StudentService;

import java.util.List;
import java.util.Set;

// REST API для работы со студентами
@Path("/students")
@Tag(
        name = "Students",
        description = "Управление студентами: создание, получение, обновление, удаление, поиск и фильтрация"
)
public class StudentResource {

    // Сервис с основной логикой работы со студентами
    @EJB
    private StudentService studentService;

    // Разрешённые поля сортировки
    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
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

    // Проверка параметров до обращения к EJB
    private void validateParameters(
            String sortBy,
            String sortOrder,
            Integer page,
            Integer size) {

        if (page != null && page < 1) {
            throw new BadRequestException(
                    "Номер страницы должен быть больше 0"
            );
        }

        if (size != null && (size < 1 || size > 100)) {
            throw new BadRequestException(
                    "Размер страницы должен быть от 1 до 100"
            );
        }

        int actualPage = page == null ? 1 : page;
        int actualSize = size == null ? 10 : size;

        long offset = ((long) actualPage - 1) * actualSize; //количество записей, которые нужно пропустить перед получением текущей страницы

        if (offset > Integer.MAX_VALUE) {
            throw new BadRequestException(
                    "Слишком большой номер страницы"
            );
        }

        if (sortBy != null && !ALLOWED_SORT_FIELDS.contains(sortBy)) {
            throw new BadRequestException(
                    "Недопустимое поле сортировки: " + sortBy
            );
        }

        if (sortOrder != null
                && !sortOrder.equalsIgnoreCase("asc")
                && !sortOrder.equalsIgnoreCase("desc")) {
            throw new BadRequestException(
                    "Направление сортировки должно быть asc или desc"
            );
        }
    }

    // Чтение целочисленного параметра из JSON для QUERY
    private Integer readIntegerFilter(JsonObject filters, String name) {
        if (!filters.containsKey(name) || filters.isNull(name)) {
            return null;
        }

        JsonValue value = filters.get(name);

        try {
            if (value instanceof JsonNumber number) { //одновременно проверяет тип значения и создаёт переменную number
                if (!number.isIntegral()) {
                    throw new BadRequestException(
                            "Поле " + name + " должно быть целым числом"
                    );
                }
                return number.intValueExact();
            }

            //преобразование из строки в цлое число
            if (value instanceof JsonString string) {
                return Integer.valueOf(string.getString());
            }
        } catch (NumberFormatException | ArithmeticException e) {
            throw new BadRequestException(
                    "Поле " + name + " должно быть целым числом"
            );
        }

        throw new BadRequestException(
                "Поле " + name + " должно быть целым числом"
        );
    }

    // Чтение строкового параметра из JSON для QUERY
    private String readStringFilter(JsonObject filters, String name) {
        if (!filters.containsKey(name) || filters.isNull(name)) {
            return null;
        }

        JsonValue value = filters.get(name);

        if (value instanceof JsonString string) {
            return string.getString();
        }

        throw new BadRequestException(
                "Поле " + name + " должно быть строкой"
        );
    }

    // Получение списка студентов с поиском, фильтрацией, сортировкой и пагинацией
    @Operation(
            summary = "Получить список студентов",
            description = "Возвращает студентов с поддержкой поиска по ФИО, фильтрации, сортировки и пагинации"
    )
    @APIResponse(
            responseCode = "200",
            description = "Список студентов успешно получен"
    )
    @APIResponse(
            responseCode = "400",
            description = "Некорректные параметры запроса"
    )
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<StudentDTO> getAllStudents(
            @QueryParam("id") Long id, //аннотация Jakarta REST для получения параметров из URL
            @QueryParam("fullName") String fullName,
            @QueryParam("group") String group,
            @QueryParam("isuId") Integer isuId,
            @QueryParam("dormNumber") Integer dormNumber,
            @QueryParam("roomNumber") Integer roomNumber,
            @QueryParam("settlementDate") String settlementDate,
            @QueryParam("isForeigner") Boolean isForeigner,
            @QueryParam("notes") String notes,
            @QueryParam("search") String search,
            @QueryParam("sortBy") String sortBy,
            @QueryParam("sortOrder") String sortOrder,
            @QueryParam("page") Integer page,
            @QueryParam("size") Integer size) {

        validateParameters(sortBy, sortOrder, page, size);

        StudentFilterCriteria filter = new StudentFilterCriteria(
                id,
                fullName,
                group,
                isuId,
                dormNumber,
                roomNumber,
                settlementDate,
                isForeigner,
                notes,
                search,
                sortBy,
                sortOrder,
                page,
                size
        );

        return studentService.getStudentsByFilters(filter);
    }

    // Создание нового студента
    @Operation(
            summary = "Создать студента",
            description = "Создаёт нового студента после проверки данных и уникальности номера ИСУ"
    )
    @APIResponse(
            responseCode = "201",
            description = "Студент успешно создан"
    )
    @APIResponse(
            responseCode = "400",
            description = "Ошибка валидации данных студента"
    )
    @APIResponse(
            responseCode = "409",
            description = "Студент с таким номером ИСУ уже существует"
    )
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createStudent(@Valid StudentDTO dto) {

        StudentDTO createdStudent = studentService.createStudent(dto);

        //Формирование HTTP-ответа
        return Response.status(Response.Status.CREATED)
                .entity(createdStudent)
                .build();
    }

    // Получение информации о студенте по ID
    @Operation(
            summary = "Получить студента по ID",
            description = "Возвращает информацию о студенте по его уникальному идентификатору"
    )
    @APIResponse(
            responseCode = "200",
            description = "Студент найден"
    )
    @APIResponse(
            responseCode = "404",
            description = "Студент с указанным ID не найден"
    )
    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getStudentById(@PathParam("id") Long id) {

        StudentDTO student = studentService.getStudentById(id);

        if (student == null) {
            throw new NotFoundException("Студент не найден");
        }

        return Response.ok(student).build();
    }

    // Удаление студента по ID
    @Operation(
            summary = "Удалить студента",
            description = "Удаляет студента из базы данных по его уникальному идентификатору"
    )
    @APIResponse(
            responseCode = "204",
            description = "Студент успешно удалён"
    )
    @APIResponse(
            responseCode = "404",
            description = "Студент с указанным ID не найден"
    )
    @DELETE
    @Path("/{id}")
    public Response deleteStudent(@PathParam("id") Long id) {

        boolean deleted = studentService.deleteStudent(id);

        if (!deleted) {
            throw new NotFoundException("Студент не найден");
        }

        return Response.noContent().build();
    }

    // Частичное обновление
    @Operation(
            summary = "Частично обновить студента",
            description = "Обновляет только переданные поля студента без изменения остальных данных"
    )
    @APIResponse(
            responseCode = "200",
            description = "Данные студента успешно обновлены"
    )
    @APIResponse(
            responseCode = "400",
            description = "Некорректные данные для обновления"
    )
    @APIResponse(
            responseCode = "404",
            description = "Студент с указанным ID не найден"
    )
    @APIResponse(
            responseCode = "409",
            description = "Номер ИСУ уже занят другим студентом"
    )
    @PATCH
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateStudent(
            @PathParam("id") Long id,
            JsonObject updates) {

        StudentDTO updatedStudent = studentService.updateStudent(id, updates);

        if (updatedStudent == null) {
            throw new NotFoundException("Студент не найден");
        }

        return Response.ok(updatedStudent).build();
    }

    // Полное обновление
    @Operation(
            summary = "Полностью обновить студента",
            description = "Заменяет данные существующего студента новыми значениями после проверки всех обязательных полей"
    )
    @APIResponse(
            responseCode = "200",
            description = "Данные студента успешно обновлены"
    )
    @APIResponse(
            responseCode = "400",
            description = "Ошибка валидации данных студента"
    )
    @APIResponse(
            responseCode = "404",
            description = "Студент с указанным ID не найден"
    )
    @APIResponse(
            responseCode = "409",
            description = "Номер ИСУ уже занят другим студентом"
    )
    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateStudentFully(
            @PathParam("id") Long id,
            @Valid StudentDTO dto) {

        StudentDTO updatedStudent = studentService.updateStudentFully(id, dto);

        if (updatedStudent == null) {
            throw new NotFoundException("Студент не найден");
        }

        return Response.ok(updatedStudent).build();
    }

    // Получение студентов через QUERY
    @Operation(
            summary = "Фильтрация студентов через QUERY",
            description = "Получает студентов по фильтрам, переданным в JSON-теле запроса"
    )
    @APIResponse(
            responseCode = "200",
            description = "Список студентов успешно получен"
    )
    @APIResponse(
            responseCode = "400",
            description = "Некорректные параметры фильтрации"
    )
    @QUERY
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public List<StudentDTO> queryStudents(JsonObject filters) {

        if (filters == null) {
            throw new BadRequestException(
                    "Тело запроса обязательно"
            );
        }

        Integer page = readIntegerFilter(filters, "page");
        Integer size = readIntegerFilter(filters, "size");
        String sortBy = readStringFilter(filters, "sortBy");
        String sortOrder = readStringFilter(filters, "sortOrder");

        validateParameters(sortBy, sortOrder, page, size);

        return studentService.queryStudents(filters);
    }
}
