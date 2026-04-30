# Stage 1: Build
FROM eclipse-temurin:25-jdk-alpine AS build
WORKDIR /app

# Переменные для доступа к GitHub Packages (common)
ARG GHP_USER
ARG GHP_TOKEN
ENV GHP_USER=$GHP_USER
ENV GHP_TOKEN=$GHP_TOKEN

COPY gradlew .
RUN chmod +x gradlew
COPY gradle gradle
COPY build.gradle settings.gradle ./
COPY src src

RUN ./gradlew bootJar -x test

# Stage 2: Run
FROM eclipse-temurin:25-jre-alpine
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8182
ENTRYPOINT ["java", "-jar", "app.jar"]