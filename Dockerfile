# Stage 1: Сборка
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app

# Копируем gradle файлы
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .

# Копируем исходники common и самого сервиса
COPY common common
COPY notification-service notification-service

# Собираем сервис (замени :user-service на имя своего модуля)
RUN chmod +x gradlew
RUN ./gradlew :notification-service:build -x test --no-daemon

# Stage 2: Запуск
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
# Копируем только собранный jar
COPY --from=build /app/notification-service/build/libs/*.jar app.jar

# Ограничиваем память внутри JVM
ENTRYPOINT ["java", "-Dspring.profiles.active=prod", "-jar", "app.jar"]