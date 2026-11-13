package ru.itmo.studentmanagement.exception;

import java.util.Map;

//Единая структура ответа с ошибкой
public class ApiErrorResponse {

    private ErrorInfo error;

    public ApiErrorResponse() {
    }

    public ApiErrorResponse(String message) {
        this.error = new ErrorInfo(message, null);
    }

    public ApiErrorResponse(
            String message,
            Map<String, String> details
    ) {
        this.error = new ErrorInfo(message, details);
    }

    public ErrorInfo getError() {
        return error;
    }

    public static class ErrorInfo {

        private String message;
        private Map<String, String> details;

        public ErrorInfo() {
        }

        public ErrorInfo(
                String message,
                Map<String, String> details
        ) {
            this.message = message;
            this.details = details;
        }

        public String getMessage() {
            return message;
        }

        public Map<String, String> getDetails() {
            return details;
        }
    }
}
