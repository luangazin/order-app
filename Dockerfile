FROM openjdk:21-jdk-slim AS builder

WORKDIR /app

COPY . .

RUN ./gradlew clean build -x test

FROM openjdk:21-jdk-slim AS runtime

WORKDIR /app

COPY --from=builder /app/build/libs/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]