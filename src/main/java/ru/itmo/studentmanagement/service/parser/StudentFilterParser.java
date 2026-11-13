package ru.itmo.studentmanagement.service.parser;

import jakarta.json.JsonObject;
import jakarta.json.JsonValue;
import jakarta.json.JsonNumber;

import java.util.Set;
//Проверяет допустимые названия фильтров
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
            throw new IllegalArgumentException(
                    "Необходимо передать JSON с фильтрами"
            );
        }

        for (String field : filters.keySet()) {
            if (!ALLOWED_FIELDS.contains(field)) {
                throw new IllegalArgumentException(
                        "Неизвестный параметр: " + field
                );
            }
        }

        this.filters = filters;
    }

    public String getString(String field) {
        JsonValue value = getValue(field);
        if (value == null) return null;

        if (value.getValueType() != JsonValue.ValueType.STRING) {
            throw new IllegalArgumentException(
                    "Поле " + field + " должно быть строкой"
            );
        }

        return filters.getString(field);
    }

    public Integer getInteger(String field) {
        JsonValue value = getValue(field);
        if (value == null) return null;

        if (value.getValueType() == JsonValue.ValueType.NUMBER) {
            JsonNumber number = filters.getJsonNumber(field);

            if (!number.isIntegral()) {
                throw new IllegalArgumentException(
                        "Поле " + field + " должно быть целым числом"
                );
            }

            try {
                return number.intValueExact();
            } catch (ArithmeticException e) {
                throw new IllegalArgumentException(
                        "Слишком большое число в поле " + field
                );
            }
        }

        String text = getString(field);
        if (text.isBlank()) return null;

        try {
            return Integer.valueOf(text);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "Поле " + field + " должно быть целым числом"
            );
        }
    }

    public Long getLong(String field) {
        JsonValue value = getValue(field);
        if (value == null) return null;

        if (value.getValueType() == JsonValue.ValueType.NUMBER) {
            JsonNumber number = filters.getJsonNumber(field);

            if (!number.isIntegral()) {
                throw new IllegalArgumentException(
                        "Поле " + field + " должно быть целым числом"
                );
            }

            try {
                return number.longValueExact();
            } catch (ArithmeticException e) {
                throw new IllegalArgumentException(
                        "Слишком большое число в поле " + field
                );
            }
        }

        String text = getString(field);
        if (text.isBlank()) return null;

        try {
            return Long.valueOf(text);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "Поле " + field + " должно быть целым числом"
            );
        }
    }

    public Boolean getBoolean(String field) {
        JsonValue value = getValue(field);
        if (value == null) return null;

        if (value.getValueType() == JsonValue.ValueType.TRUE) {
            return true;
        }

        if (value.getValueType() == JsonValue.ValueType.FALSE) {
            return false;
        }

        String text = getString(field);
        if (text.isBlank()) return null;

        if (text.equalsIgnoreCase("true")) return true;
        if (text.equalsIgnoreCase("false")) return false;

        throw new IllegalArgumentException(
                "Поле " + field + " должно быть true или false"
        );
    }

    private JsonValue getValue(String field) {
        if (!filters.containsKey(field) || filters.isNull(field)) {
            return null;
        }

        return filters.get(field);
    }
}

