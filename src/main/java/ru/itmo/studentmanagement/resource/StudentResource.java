package ru.itmo.studentmanagement.resource;

import jakarta.ejb.EJB;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import ru.itmo.studentmanagement.dto.StudentDTO;
import ru.itmo.studentmanagement.service.StudentService;

import java.util.List;

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
            @QueryParam("group") String group,
            @QueryParam("dormNumber") Integer dormNumber,
            @QueryParam("sortBy") String sortBy,
            @QueryParam("sortOrder") String sortOrder,
            @QueryParam("page") Integer page,
            @QueryParam("size") Integer size,
            @QueryParam("search") String search) {

        return studentService.getStudentsByFilters(
                group,
                dormNumber,
                sortBy,
                sortOrder,
                page,
                size,
                search
        );
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

        return Response.status(Response.Status.CREATED)
                .entity(createdStudent)
                .build();
    }


    // Получение информации о студенте по  ID
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
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Json.createObjectBuilder()
                            .add("error", Json.createObjectBuilder()
                                    .add("message", "Студент не найден"))
                            .build())
                    .build();
        }

        return Response.ok(student).build();
    }


    // Удаление студента из бд по ID
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
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Json.createObjectBuilder()
                            .add("error", Json.createObjectBuilder()
                                    .add("message", "Студент не найден"))
                            .build())
                    .type(MediaType.APPLICATION_JSON)
                    .build();
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
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Json.createObjectBuilder()
                            .add("error", Json.createObjectBuilder()
                                    .add("message", "Студент не найден"))
                            .build())
                    .build();
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
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Json.createObjectBuilder()
                            .add("error", Json.createObjectBuilder()
                                    .add("message", "Студент не найден"))
                            .build())
                    .build();
        }

        return Response.ok(updatedStudent).build();
    }
}
