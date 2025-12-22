# Multi-stage build for Spring Boot application
# Stage 1: Build the application
FROM gradle:8.5-jdk17 AS build

WORKDIR /app

# Copy Gradle wrapper and build files
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .

# Copy source code
COPY src src

# Build the application (skip tests for faster builds)
RUN chmod +x ./gradlew
RUN ./gradlew clean build -x test --no-daemon

# Stage 2: Run the application
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Create uploads directory
RUN mkdir -p /app/uploads/reports

# Copy the built JAR from build stage
COPY --from=build /app/build/libs/*.jar app.jar

# Expose the application port
EXPOSE 3000

# Set environment variables (can be overridden in docker-compose)
ENV SPRING_PROFILES_ACTIVE=prod

# Run the application
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
