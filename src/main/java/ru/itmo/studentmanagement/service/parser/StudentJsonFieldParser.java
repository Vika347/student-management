//Извлекать данные из JSON, проверять их типы и преобразовывать в Java-значения
//используется для PATCH, когда нужно получить конкретное поле и изменить его значение в существующем студенте.
package ru.itmo.studentmanagement.service.parser;

import jakarta.json.JsonNumber;
import jakarta.json.JsonObject;
import jakarta.json.JsonValue;
import jakarta.ws.rs.BadRequestException;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public class StudentJsonFieldParser {

    private static JsonValue getRequiredField(JsonObject json, String field) {
        if (json == null || !json.containsKey(field)
                || json.isNull(field)) {
            throw new BadRequestException(
                    "Поле " + field + " отсутствует или равно null"
            );
        }

        return json.get(field);
    }

    // Получение строки из JSON
    public static String getStringField(JsonObject json, String field) {
        JsonValue value = getRequiredField(json, field);

        if (value.getValueType() != JsonValue.ValueType.STRING) {
            throw new BadRequestException(
                    "Поле " + field + " должно быть строкой"
            );
        }

        return json.getString(field);
    }

    // Получение целого числа
    public static int getIntegerField(JsonObject json, String field) {
        JsonValue value = getRequiredField(json, field);

        if (value.getValueType() != JsonValue.ValueType.NUMBER) {
            throw new BadRequestException(
                    "Поле " + field + " должно быть числом"
            );
        }

        JsonNumber number = json.getJsonNumber(field);

        if (!number.isIntegral()) {
            throw new BadRequestException(
                    "Поле " + field + " должно быть целым числом"
            );
        }

        try {
            return number.intValueExact();
        } catch (ArithmeticException e) {
            throw new BadRequestException(
                    "Поле " + field + " содержит слишком большое число"
            );
        }
    }

    // Получение Boolean
    public static boolean getBooleanField(JsonObject json, String field) {
        JsonValue value = getRequiredField(json, field);

        JsonValue.ValueType type = value.getValueType();

        if (type != JsonValue.ValueType.TRUE
                && type != JsonValue.ValueType.FALSE) {
            throw new BadRequestException(
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
            throw new BadRequestException(
                    "Поле " + field
                            + " должно содержать дату в формате ГГГГ-ММ-ДД"
            );
        }
    }
}
