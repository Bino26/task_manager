

# ======== Stage : Build ========
FROM maven:3.9.6-eclipse-temurin-17 AS builder

# Set working directory inside container
WORKDIR /app

# Copy the Maven project files
COPY pom.xml .
COPY src ./src

# Build the application (skip tests if you want faster builds)
RUN mvn clean package -DskipTests

# ======== Stage : Run ========
FROM openjdk:17

# Set working directory for runtime container
WORKDIR /app

# Copy the JAR from the previous build stage
COPY --from=builder /app/target/*.jar app.jar

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/api/actuator/health || exit 1


# Run the app
ENTRYPOINT ["java", "-jar", "app.jar"]
