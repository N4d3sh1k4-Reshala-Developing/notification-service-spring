FROM eclipse-temurin:21-jdk-alpine AS build

WORKDIR /app
COPY common common
WORKDIR /app/notification-service
COPY notification-service/ .

RUN chmod +x gradlew
RUN ./gradlew build -x test --no-daemon

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/notification-service/build/libs/*.jar app.jar

ENTRYPOINT ["java", "-Dspring.profiles.active=prod", "-jar", "app.jar"]