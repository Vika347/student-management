package ru.itmo.studentmanagement.dto;

import jakarta.validation.constraints.*; //аннотации для проверки данных
import java.time.LocalDate;

// Класс для передачи данных студента через REST API
public class StudentDTO {

    // ID студента генерируется базой данных
    private Long id;

    // ФИО: от 2 до 100 символов
    @NotBlank(message = "ФИО не может быть пустым")
    @Size(
            min = 2,
            max = 100,
            message = "ФИО должно содержать от 2 до 100 символов"
    )
    @Pattern(
            regexp = "^(?=.*\\p{L})[\\p{L} -]+$",
            message = "ФИО может содержать только буквы, пробелы и дефисы"
    )
    private String fullName;

    // Группа
    @NotBlank(message = "Группа обязательна")
    @Pattern(
            regexp = "^\\p{L}[0-9]{4}$",
            message = "Группа должна состоять из буквы и четырёх цифр"
    )
    private String group;

    // ИСУ: 6 цифр
    @NotNull(message = "Номер ИСУ обязателен")
    @Min(
            value = 100000,
            message = "ИСУ должен состоять из шести цифр"
    )
    @Max(
            value = 999999,
            message = "ИСУ должен состоять из шести цифр"
    )
    private Integer isuId;

    // Номер общежития: от 1 до 10
    @NotNull(message = "Номер общежития обязателен")
    @Min(value = 1, message = "Номер общежития должен быть от 1 до 10")
    @Max(value = 10, message = "Номер общежития должен быть от 1 до 10")
    private Integer dormNumber;

    // Номер комнаты: от 100 до 999
    @NotNull(message = "Номер комнаты обязателен")
    @Min(value = 100, message = "Номер комнаты должен быть от 100 до 999")
    @Max(value = 999, message = "Номер комнаты должен быть от 100 до 999")
    private Integer roomNumber;

    // Дата заселения
    @NotNull(message = "Дата заселения обязательна")
    @PastOrPresent(message = "Дата заселения не может быть в будущем")
    private LocalDate settlementDate;

    // Иностранный?
    @NotNull(message = "Поле isForeigner должно быть булевым")
    private Boolean isForeigner;

    // Заметки
    @NotNull(message = "Заметки должны быть строкой")
    @Size(
            max = 500,
            message = "Заметки не могут превышать 500 символов"
    )
    @Pattern(
            regexp = "^[^/\\\\*\"<>]*$",
            message = "Заметки содержат запрещённые символы"
    )
    private String notes = "";

    // Конструктор
    public StudentDTO() {
    }


    // Геттеры и сеттеры

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public Integer getIsuId() {
        return isuId;
    }

    public void setIsuId(Integer isuId) {
        this.isuId = isuId;
    }

    public Integer getDormNumber() {
        return dormNumber;
    }

    public void setDormNumber(Integer dormNumber) {
        this.dormNumber = dormNumber;
    }

    public Integer getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(Integer roomNumber) {
        this.roomNumber = roomNumber;
    }

    public LocalDate getSettlementDate() {
        return settlementDate;
    }

    public void setSettlementDate(LocalDate settlementDate) {
        this.settlementDate = settlementDate;
    }

    public Boolean getIsForeigner() {
        return isForeigner;
    }

    public void setIsForeigner(Boolean isForeigner) {
        this.isForeigner = isForeigner;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
