# Multi-stage build
FROM maven:3.9.6-eclipse-temurin-21 AS builder

WORKDIR /app

# Copy Maven files for dependency caching
COPY pom.xml .
COPY src ./src

# Build the application
RUN mvn clean package -DskipTests

# Runtime stage - use distroless for minimal attack surface
FROM gcr.io/distroless/java21-debian12:nonroot

WORKDIR /app

# Copy the built JAR
COPY --from=builder /app/target/*.jar app.jar

# Distroless images run as non-root by default
# Port 8080 is exposed by Spring Boot automatically

ENTRYPOINT ["java", "-jar", "app.jar"]