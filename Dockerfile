#Описывает сборку Java-приложения и образа Payara

# Этап 1. Сборка Java-приложения
FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /app

COPY pom.xml .
COPY src ./src

# Собираем WAR-файл
RUN mvn -B clean package -DskipTests

# Отдельно получаем JDBC-драйвер для Payara
RUN mvn -B dependency:copy \
    -Dartifact=org.postgresql:postgresql:42.7.7 \
    -DoutputDirectory=/tmp/jdbc

# Этап 2. Сервер приложений
FROM payara/server-full:6.2025.11-jdk17

# JDBC-драйвер доступен самому серверу Payara
COPY --from=build --chown=payara:payara \
    /tmp/jdbc/postgresql-42.7.7.jar \
    /opt/payara/appserver/glassfish/domains/domain1/lib/

# Команды настройки JDBC
COPY --chown=payara:payara \
    docker/payara/post-boot-commands.asadmin \
    /opt/payara/config/post-boot-commands.asadmin

# Java-приложение
COPY --from=build --chown=payara:payara \
    /app/target/student-management.war \
    /opt/payara/deployments/student-management.war

EXPOSE 8080