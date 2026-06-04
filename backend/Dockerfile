FROM gradle:8.10.2-jdk21-alpine AS build

WORKDIR /app

COPY gradlew .
COPY gradle ./gradle
COPY build.gradle settings.gradle ./
COPY src ./src

RUN chmod +x gradlew && ./gradlew clean bootJar -x test

FROM eclipse-temurin:21-jre-alpine AS runtime

WORKDIR /app

RUN addgroup -S chronos && adduser -S chronos -G chronos

COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8081

ENV SPRING_PROFILES_ACTIVE=prod

USER chronos

ENTRYPOINT ["java", "-jar", "/app/app.jar"]