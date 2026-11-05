package ru.itmo.studentmanagement.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.time.LocalDate;

@Entity
@Table(name = "students")
public class Student {


    // ID: генерируется базой данных
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ФИО: от 2 до 100 символов,обязательно хотя бы одна буква
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
    @Column(name = "full_name", nullable = false)
    private String fullName;

    // Группа: одна буква и четыре цифры
    @NotBlank(message = "Группа обязательна")
    @Pattern(
            regexp = "^\\p{L}[0-9]{4}$",
            message = "Группа должна состоять из буквы и четырёх цифр"
    )
    @Column(name = "student_group", nullable = false)
    private String group;

    // Номер ИСУ: 6 цифр
    @NotNull(message = "Номер ИСУ обязателен")
    @Min(
            value = 100000,
            message = "ИСУ должен состоять из шести цифр"
    )
    @Max(
            value = 999999,
            message = "ИСУ должен состоять из шести цифр"
    )
    @Column(name = "isu_id", nullable = false, unique = true)
    private Integer isuId;

    // Номер общежития: от 1 до 10
    @NotNull(message = "Номер общежития обязателен")
    @Min(value = 1, message = "Номер общежития должен быть от 1 до 10")
    @Max(value = 10, message = "Номер общежития должен быть от 1 до 10")
    @Column(name = "dorm_number", nullable = false)
    private Integer dormNumber;

    // Номер комнаты: от 100 до 999
    @NotNull(message = "Номер комнаты обязателен")
    @Min(value = 100, message = "Номер комнаты должен быть от 100 до 999")
    @Max(value = 999, message = "Номер комнаты должен быть от 100 до 999")
    @Column(name = "room_number", nullable = false)
    private Integer roomNumber;

    // Дата заселения
    @NotNull(message = "Дата заселения обязательна")
    @PastOrPresent(
            message = "Дата заселения не может быть в будущем"
    )
    @Column(name = "settlement_date", nullable = false)
    private LocalDate settlementDate;

    // Иностранный?
    @NotNull(
            message = "Поле isForeigner должно быть булевым"
    )
    @Column(name = "is_foreigner", nullable = false)
    private Boolean isForeigner;


    // Заметки: максимум 50 слов, запрещённые символы.
    @NotNull(message = "Заметки должны быть строкой")
    @Size(
            max = 500,
            message = "Заметки не могут превышать 500 символов"
    )
    @Pattern(
            regexp = "^[^/\\\\*\"<>]*$",
            message = "Заметки содержат запрещённые символы"
    )
    @Column(name = "notes")
    private String notes = "";

    // Конструктор для JPA.
    public Student() {
    }

    // Проверка допустимого года заселения
    @AssertTrue(
            message = "Год заселения должен быть от 2018 до 2035"
    )
    public boolean isSettlementYearValid() {
        if (settlementDate == null) {
            return true;
        }

        int year = settlementDate.getYear();

        return year >= 2018 && year <= 2035;
    }

    // Дата заселения не старше 10 календарных лет
    @AssertTrue(
            message = "Дата заселения не может быть старше 10 лет"
    )
    public boolean isSettlementNotTooOld() {
        if (settlementDate == null) {
            return true;
        }

        int currentYear = LocalDate.now().getYear();

        return settlementDate.getYear() >= currentYear - 10;
    }

    // Проверка количества слов в заметках
    @AssertTrue(
            message = "Заметки не могут содержать больше 50 слов"
    )
    public boolean isNotesWordCountValid() {
        if (notes == null) {
            return true;
        }

        String trimmed = notes.trim();

        if (trimmed.isEmpty()) {
            return true;
        }

        String[] words = trimmed.split("\\s+");

        return words.length <= 50;
    }

    // Геттеры и сеттеры.

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
