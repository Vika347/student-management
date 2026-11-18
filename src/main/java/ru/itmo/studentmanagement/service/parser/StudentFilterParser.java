//используется для QUERY, когда нужно прочитать необязательные параметры поиска и создать StudentFilterCriteria
package ru.itmo.studentmanagement.service.parser;

import jakarta.json.JsonObject;
import jakarta.json.JsonValue;
import jakarta.json.JsonNumber;
import jakarta.ws.rs.BadRequestException;
import ru.itmo.studentmanagement.filtr.StudentFilterCriteria;

import java.util.Set;

// Проверяет допустимые названия фильтров и типы значений
public class StudentFilterParser {

    private static final Set<String> ALLOWED_FIELDS = Set.of(
            "id", "fullName", "group", "isuId",
            "dormNumber", "roomNumber", "settlementDate",
            "isForeigner", "notes", "search",
            "sortBy", "sortOrder", "page", "size"
    );

    private final JsonObject filters;

    public StudentFilterParser(JsonObject filters) {
        if (filters == null) {
            throw new BadRequestException(
                    "Необходимо передать JSON с фильтрами"
            );
        }

        for (String field : filters.keySet()) {
            if (!ALLOWED_FIELDS.contains(field)) {
                throw new BadRequestException(
                        "Неизвестный параметр: " + field
                );
            }
        }

        this.filters = filters;
    }

    // Получение строкового значения
    public String getString(String field) {
        JsonValue value = getValue(field);

        if (value == null) {
            return null;
        }

        if (value.getValueType() != JsonValue.ValueType.STRING) {
            throw new BadRequestException(
                    "Поле " + field + " должно быть строкой"
            );
        }

        return filters.getString(field);
    }

    // Получение целого числа Integer
    public Integer getInteger(String field) {
        JsonValue value = getValue(field);

        if (value == null) {
            return null;
        }

        if (value.getValueType() == JsonValue.ValueType.NUMBER) {
            JsonNumber number = filters.getJsonNumber(field);

            if (!number.isIntegral()) {
                throw new BadRequestException(
                        "Поле " + field + " должно быть целым числом"
                );
            }

            try {
                return number.intValueExact();
            } catch (ArithmeticException e) {
                throw new BadRequestException(
                        "Слишком большое число в поле " + field
                );
            }
        }

        String text = getString(field);

        if (text.isBlank()) {
            return null;
        }

        try {
            return Integer.valueOf(text);
        } catch (NumberFormatException e) {
            throw new BadRequestException(
                    "Поле " + field + " должно быть целым числом"
            );
        }
    }

    // Получение целого числа Long
    public Long getLong(String field) {
        JsonValue value = getValue(field);

        if (value == null) {
            return null;
        }

        if (value.getValueType() == JsonValue.ValueType.NUMBER) {
            JsonNumber number = filters.getJsonNumber(field);

            if (!number.isIntegral()) {
                throw new BadRequestException(
                        "Поле " + field + " должно быть целым числом"
                );
            }

            try {
                return number.longValueExact();
            } catch (ArithmeticException e) {
                throw new BadRequestException(
                        "Слишком большое число в поле " + field
                );
            }
        }

        String text = getString(field);

        if (text.isBlank()) {
            return null;
        }

        try {
            return Long.valueOf(text);
        } catch (NumberFormatException e) {
            throw new BadRequestException(
                    "Поле " + field + " должно быть целым числом"
            );
        }
    }

    // Получение логического значения Boolean
    public Boolean getBoolean(String field) {
        JsonValue value = getValue(field);

        if (value == null) {
            return null;
        }

        if (value.getValueType() == JsonValue.ValueType.TRUE) {
            return true;
        }

        if (value.getValueType() == JsonValue.ValueType.FALSE) {
            return false;
        }

        String text = getString(field);

        if (text.isBlank()) {
            return null;
        }

        if (text.equalsIgnoreCase("true")) {
            return true;
        }

        if (text.equalsIgnoreCase("false")) {
            return false;
        }

        throw new BadRequestException(
                "Поле " + field + " должно быть true или false"
        );
    }

    // Получение значения из JSON
    private JsonValue getValue(String field) {
        if (!filters.containsKey(field) || filters.isNull(field)) {
            return null;
        }

        return filters.get(field);
    }


    // Преобразование JSON-фильтров в StudentFilterCriteria
    public StudentFilterCriteria parse() {

        return new StudentFilterCriteria(
                getLong("id"),
                getString("fullName"),
                getString("group"),
                getInteger("isuId"),
                getInteger("dormNumber"),
                getInteger("roomNumber"),
                getString("settlementDate"),
                getBoolean("isForeigner"),
                getString("notes"),
                getString("search"),
                getString("sortBy"),
                getString("sortOrder"),
                getInteger("page"),
                getInteger("size")
        );
    }

}
