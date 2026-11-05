package ru.itmo.studentmanagement.config;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

// Настройка базового пути для всех REST-запросов
@ApplicationPath("/api")
public class RestApplication extends Application {
}
