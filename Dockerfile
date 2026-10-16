# Этап 1: сборка приложения с помощью Maven и JDK 17
FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /app

COPY pom.xml .
COPY src ./src
COPY test ./test

RUN mvn clean package

# Этап 2: запуск приложения на JRE 17
FROM eclipse-temurin:17-jre

WORKDIR /app

COPY --from=build /app/target/helpdesk-1.0.0.jar app.jar
COPY web ./web

ENV PORT=8080
ENV WEB_DIR=/app/web
ENV DATA_FILE=/app/tickets.txt

EXPOSE 8080

CMD ["java", "-jar", "app.jar"]
