package ru.itmo.studentmanagement.service.parser;

import jakarta.json.JsonNumber;
import jakarta.json.JsonObject;
import jakarta.json.JsonValue;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public class StudentJsonParser {

    // Получение строки из JSON
    public static String getStringField(JsonObject json, String field) {
        if (json == null || !json.containsKey(field)
                || json.isNull(field)) {
            throw new IllegalArgumentException(
                    "Поле " + field + " отсутствует"
            );
        }

        if (json.get(field).getValueType()
                != JsonValue.ValueType.STRING) {
            throw new IllegalArgumentException(
                    "Поле " + field + " должно быть строкой"
            );
        }

        return json.getString(field);
    }

    // Получение целого числа
    public static int getIntegerField(JsonObject json, String field) {
        if (json == null || !json.containsKey(field)
                || json.isNull(field)) {
            throw new IllegalArgumentException(
                    "Поле " + field + " отсутствует"
            );
        }

        if (json.get(field).getValueType()
                != JsonValue.ValueType.NUMBER) {
            throw new IllegalArgumentException(
                    "Поле " + field + " должно быть числом"
            );
        }

        JsonNumber number = json.getJsonNumber(field);

        if (!number.isIntegral()) {
            throw new IllegalArgumentException(
                    "Поле " + field + " должно быть целым числом"
            );
        }

        try {
            return number.intValueExact();
        } catch (ArithmeticException e) {
            throw new IllegalArgumentException(
                    "Поле " + field + " содержит слишком большое число"
            );
        }
    }

    // Получение Boolean
    public static boolean getBooleanField(JsonObject json, String field) {
        if (json == null || !json.containsKey(field)
                || json.isNull(field)) {
            throw new IllegalArgumentException(
                    "Поле " + field + " отсутствует"
            );
        }

        JsonValue.ValueType type = json.get(field).getValueType();

        if (type != JsonValue.ValueType.TRUE
                && type != JsonValue.ValueType.FALSE) {
            throw new IllegalArgumentException(
                    "Поле " + field + " должно быть логическим значением"
            );
        }

        return json.getBoolean(field);
    }

    // Получение даты
    public static LocalDate getDateField(JsonObject json, String field) {
        String value = getStringField(json, field);

        try {
            return LocalDate.parse(value);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(
                    "Поле " + field
                            + " должно содержать дату в формате ГГГГ-ММ-ДД"
            );
        }
    }
}
